/*
 * Copyright (c) 2009-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.empire.impl;

import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.MutableDataSource;
import com.clarkparsia.empire.ds.SupportsNamedGraphs;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.ds.SupportsTransactions;
import com.clarkparsia.empire.ds.DataSourceUtil;
import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.impl.TransactionalDataSource;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.EmpireException;
import com.clarkparsia.empire.EmpireGenerated;
import com.clarkparsia.empire.EmpireOptions;

import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.AnnotationChecker;

import org.openrdf.model.Graph;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.EntityListeners;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PostLoad;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import java.lang.annotation.Annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;

import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.Set;

import java.net.URI;

import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedFields;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedGetters;
import static com.clarkparsia.empire.util.BeanReflectUtil.asSetter;
import static com.clarkparsia.empire.util.BeanReflectUtil.safeGet;
import static com.clarkparsia.empire.util.BeanReflectUtil.safeSet;
import static com.clarkparsia.empire.util.BeanReflectUtil.hasAnnotation;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedMethods;

import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.util.BeanReflectUtil;

import com.clarkparsia.openrdf.ExtGraph;
import com.google.common.base.Predicate;

/**
 * <p>Implementation of the JPA {@link EntityManager} interface to support the persistence model over
 * an RDF database.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 * @see EntityManager
 * @see com.clarkparsia.empire.ds.DataSource
 */
public final class EntityManagerImpl implements EntityManager {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EntityManagerImpl.class.getName());

	/**
	 * Whether or not this EntityManagerImpl is open
	 */
	private boolean mIsOpen = false;

	/**
	 * The underlying data source
	 */
	private MutableDataSource mDataSource;

	/**
	 * The current transaction
	 */
	private EntityTransaction mTransaction;

	/**
	 * The Entity Listeners for our managed entities.
	 */
	private Map<Object, Collection<Object>> mManagedEntityListeners = new WeakHashMap<Object, Collection<Object>>();

	/**
	 * The current collapsed view of a DataSourceOperation which is a merged set of adds & removes to the DataSource.
	 * Used during the canonical EntityManager operations such as merge, persist, remove
	 */
	private DataSourceOperation mOp;

	/**
	 * The list of things which are ready to be cascaded.  They are tracked in this list to help prevent infinite loops
	 */
	private Collection<Object> mCascadePending = new HashSet<Object>();

	/**
	 * Create a new EntityManagerImpl
	 * @param theSource the underlying RDF datasource used for persistence operations
	 */
	EntityManagerImpl(MutableDataSource theSource) {

		// TODO: sparql for everything, just convert serql into sparql
		// TODO: work like JPA/hibernate -- if something does not have a @Transient on it, convert it.  we'll just need to coin a URI in those cases
		// TODO: add an @RdfsLabel annotation that will use the value of a property as the label during annotation
		// TODO: support for owl/rdfs annotations not mappable to JPA annotations such as min/max cardinality and others.

		mIsOpen = true;

		mDataSource = theSource;
	}

	/**
	 * @inheritDoc
	 */
	public void flush() {
		assertOpen();
		
		// we'll do nothing here since our default implementation doesn't queue up changes, they're made
		// as soon as remove/persist are called
	}

	/**
	 * @inheritDoc
	 */
	public void setFlushMode(final FlushModeType theFlushModeType) {
		assertOpen();

		if (theFlushModeType != FlushModeType.AUTO) {
			throw new IllegalArgumentException("Commit style flush mode not supported");
		}
	}

	/**
	 * @inheritDoc
	 */
	public FlushModeType getFlushMode() {
		assertOpen();
		
		return FlushModeType.AUTO;
	}

	/**
	 * @inheritDoc
	 */
	public void lock(final Object theObj, final LockModeType theLockModeType) {
		throw new PersistenceException("Lock is not supported.");
	}
	
	/**
	 * @inheritDoc
	 */
	public void refresh(Object theObj) {
		assertStateOk(theObj);

		assertContains(theObj);

		Object aDbObj = find(theObj.getClass(), EmpireUtil.asSupportsRdfId(theObj).getRdfId());

        Collection<AccessibleObject> aAccessors = new HashSet<AccessibleObject>();

        aAccessors.addAll(getAnnotatedFields(aDbObj.getClass()));
        aAccessors.addAll(getAnnotatedGetters(aDbObj.getClass(), true));

        try {
            for (AccessibleObject aAccess : aAccessors) {
                Object aValue = safeGet(aAccess, aDbObj);
                
                AccessibleObject aSetter = asSetter(aDbObj.getClass(), aAccess);
                
                safeSet(aSetter, theObj, aValue);
            }
        }
        catch (InvocationTargetException e) {
            throw new PersistenceException(e);
        }
    }

	/**
	 * @inheritDoc
	 */
	public void clear() {
		assertOpen();

		cleanState();
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(final Object theObj) {
		assertStateOk(theObj);

		try {
			return DataSourceUtil.exists(getDataSource(), theObj);
		}
		catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Query createQuery(final String theQueryString) {
		return getDataSource().getQueryFactory().createQuery(theQueryString);
	}

	/**
	 * @inheritDoc
	 */
	public Query createNamedQuery(final String theName) {
		return getDataSource().getQueryFactory().createNamedQuery(theName);
	}

	/**
	 * @inheritDoc
	 */
	public Query createNativeQuery(final String theQueryString) {
		return getDataSource().getQueryFactory().createNativeQuery(theQueryString);
	}

	/**
	 * @inheritDoc
	 */
	public Query createNativeQuery(final String theQueryString, final Class theResultClass) {
		return getDataSource().getQueryFactory().createNativeQuery(theQueryString, theResultClass);
	}

	/**
	 * @inheritDoc
	 */
	public Query createNativeQuery(final String theQueryString, final String theResultSetMapping) {
		return getDataSource().getQueryFactory().createNativeQuery(theQueryString, theResultSetMapping);
	}

	/**
	 * @inheritDoc
	 */
	public void joinTransaction() {
		assertOpen();

		// TODO: maybe do something?  I don't really understand what this method is supposed to do.  from the javadoc
		// the intent is not clear.  i need to do a little more reading on this.  for now, lets make it fail
		// like a user would expect, but not do anything.  that's not ideal, but we'll eventually sort this out.
	}

	/**
	 * @inheritDoc
	 */
	public Object getDelegate() {
		return mDataSource;
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		if (!isOpen()) {
			throw new IllegalStateException("EntityManager is already closed.");
		}

		getDataSource().disconnect();

		mIsOpen = false;

		cleanState();
	}

	/**
	 * Clean up the current state of the EntityManager, release attached entities and the like.
	 */
	private void cleanState() {
		mManagedEntityListeners.clear();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isOpen() {
		return mIsOpen;
	}

	/**
	 * @inheritDoc
	 */
	public EntityTransaction getTransaction() {
		if (mTransaction == null) {
			mTransaction = new DataSourceEntityTransaction(asSupportsTransactions());
		}

		return mTransaction;
	}

	/**
	 * @inheritDoc
	 */
	public void persist(final Object theObj) {
		assertStateOk(theObj);

		try {
			assertNotContains(theObj);
		}
		catch (Throwable e) {
			throw new EntityExistsException(e);
		}

		try {
			prePersist(theObj);

			boolean isTopOperation = (mOp == null);

			DataSourceOperation aOp = new DataSourceOperation();

			Graph aData = RdfGenerator.asRdf(theObj);

			if (doesSupportNamedGraphs() && EmpireUtil.hasNamedGraphSpecified(theObj)) {
				aOp.add(EmpireUtil.getNamedGraph(theObj), aData);
			}
			else {
				aOp.add(aData);
			}

			joinCurrentDataSourceOperation(aOp);

			cascadeOperation(theObj, new IsPersistCascade(), new MergeCascade());

			finishCurrentDataSourceOperation(isTopOperation);

			postPersist(theObj);
		}
		catch (InvalidRdfException ex) {
			throw new IllegalStateException(ex);
		}
		catch (DataSourceException ex) {
			throw new PersistenceException(ex);
		}
	}

	private MutableDataSource getDataSource() {
		return (MutableDataSource) getDelegate();
	}

	private void finishCurrentDataSourceOperation(boolean theIsTop) throws DataSourceException {
		if (theIsTop) {
			mCascadePending.clear();
			mOp.execute();
			mOp = null;
		}
	}

	/**
	 * @inheritDoc
	 */
	@SuppressWarnings("unchecked")
	public <T> T merge(final T theT) {
		assertStateOk(theT);

		Graph aExistingData = null;
		
		if (theT instanceof EmpireGenerated) {
			aExistingData = ((EmpireGenerated) theT).getInstanceTriples();
			
			if (aExistingData == null) {
				aExistingData = new ExtGraph();
			}
		}
		else {
			aExistingData = assertContainsAndDescribe(theT);
		}

		try {
			preUpdate(theT);

			Graph aData = RdfGenerator.asRdf(theT);

			boolean isTopOperation = (mOp == null);

			DataSourceOperation aOp = new DataSourceOperation();

			if (doesSupportNamedGraphs() && EmpireUtil.hasNamedGraphSpecified(theT)) {
				java.net.URI aGraphURI = EmpireUtil.getNamedGraph(theT);

				aOp.remove(aGraphURI, aExistingData);
				aOp.add(aGraphURI, aData);
			}
			else {
				aOp.remove(aExistingData);
				aOp.add(aData);
			}

			joinCurrentDataSourceOperation(aOp);

			// cascade the merge
			cascadeOperation(theT, new IsMergeCascade(), new MergeCascade());

			finishCurrentDataSourceOperation(isTopOperation);

			postUpdate(theT);

            return theT;
		}
		catch (DataSourceException ex) {
			throw new PersistenceException(ex);
		}
		catch (InvalidRdfException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void joinCurrentDataSourceOperation(final DataSourceOperation theOp) {
		if (mOp == null) {
			mOp = theOp;
		}
		else {
			mOp.merge(theOp);
		}
	}

	private <T> void cascadeOperation(T theT, CascadeTest theCascadeTest, CascadeAction theAction) {
		// if we've already cascaded this, move on to the next thing, we don't want infinite loops
		if (mCascadePending.contains(theT)) {
			return;
		}
		else {
			mCascadePending.add(theT);
		}

		Collection<AccessibleObject> aAccessors = new HashSet<AccessibleObject>();
		
		aAccessors.addAll(getAnnotatedFields(theT.getClass()));
		aAccessors.addAll(getAnnotatedGetters(theT.getClass(), true));

		for (AccessibleObject aObj : aAccessors) {
			if (theCascadeTest.apply(aObj)) {
				try {
					Object aAccessorValue = BeanReflectUtil.safeGet(aObj, theT);

					if (aAccessorValue == null) {
						continue;
					}

					theAction.apply(aAccessorValue);
				}
				catch (Exception e) {
					throw new PersistenceException(e);
				}
			}
		}
	}

	private class MergeCascade extends CascadeAction {
		public void cascade(Object theValue) {
			// is it the correct JPA behavior to persist a value when it does not exist during a cascaded
			// merge?  or should that be a PersistenceException just like any normal merge for an un-managed
			// object?
			if (AnnotationChecker.isValid(theValue.getClass())) {
				if (contains(theValue)) {
					merge(theValue);
				}
				else {
					persist(theValue);
				}
			}
		}
	}

	private class RemoveCascade extends CascadeAction {
		public void cascade(Object theValue) {
			if (AnnotationChecker.isValid(theValue.getClass())) {
				if (contains(theValue)) {
					remove(theValue);
				}
			}
		}
	}

	private class IsMergeCascade extends CascadeTest {
		public boolean apply(final AccessibleObject theValue) {
			return BeanReflectUtil.isMergeCascade(theValue);
		}
	}

	private class IsRemoveCascade extends CascadeTest {
		public boolean apply(final AccessibleObject theValue) {
			return BeanReflectUtil.isRemoveCascade(theValue);
		}
	}

	private class IsPersistCascade extends CascadeTest {
		public boolean apply(final AccessibleObject theValue) {
			return BeanReflectUtil.isPersistCascade(theValue);
		}
	}

	private abstract class CascadeTest implements Predicate<AccessibleObject> {
	}

	private abstract class CascadeAction implements Predicate<Object> {
		public abstract void cascade(Object theObj);

		public final boolean apply(Object theObj) {
			// is it an error if you specify a cascade type for something that cannot be
			// cascaded?  such as strings, or a non Entity instance?
			if (Collection.class.isAssignableFrom(theObj.getClass())) {
				for (Object aValue : (Collection) theObj) {
					cascade(aValue);
				}
			}
			else {
				cascade(theObj);
			}

			return true;
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Object theObj) {
		assertStateOk(theObj);

		Graph aData = assertContainsAndDescribe(theObj);

		try {
			preRemove(theObj);

			boolean isTopOperation = (mOp == null);

			DataSourceOperation aOp = new DataSourceOperation();

			// we were transforming the current object to RDF and deleting that, but i dont think that's the intended
			// behavior.  you want to delete everything about the object in the database, not the properties specifically
			// on the thing being deleted -- there's an obvious case where there could be a delta between them and you
			// don't delete everything.  so we'll do a describe on the object and delete everything we know about it
			// i.e. everything where its in the subject position.

			//Graph aData = RdfGenerator.asRdf(theObj);
			//Graph aData = DataSourceUtil.describe(getDataSource(), theObj);

			if (doesSupportNamedGraphs() && EmpireUtil.hasNamedGraphSpecified(theObj)) {
				aOp.remove(EmpireUtil.getNamedGraph(theObj), aData);
			}
			else {
				aOp.remove(aData);
			}

			joinCurrentDataSourceOperation(aOp);

			cascadeOperation(theObj, new IsRemoveCascade(), new RemoveCascade());

			finishCurrentDataSourceOperation(isTopOperation);

			postRemove(theObj);
		}
		catch (DataSourceException ex) {
			throw new PersistenceException(ex);
		}
	}

	/**
	 * @inheritDoc
	 */
	public <T> T find(final Class<T> theClass, final Object theObj) {
		assertOpen();

		try {
			AnnotationChecker.assertValid(theClass);
		}
		catch (EmpireException e) {
			throw new IllegalArgumentException(e);
		}

		try {
			if (DataSourceUtil.exists(getDataSource(), EmpireUtil.asPrimaryKey(theObj))) {
				T aT = RdfGenerator.fromRdf(theClass, EmpireUtil.asPrimaryKey(theObj), getDataSource());

				postLoad(aT);

				return aT;
			}
			else {
				return null;
			}
		}
		catch (InvalidRdfException e) {
			throw new IllegalArgumentException("Type is not valid, or object with key is not a valid Rdf Entity.", e);
		}
		catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public <T> T getReference(final Class<T> theClass, final Object theObj) {
		assertOpen();

		T aObj = find(theClass, theObj);

		if (aObj == null) {
			throw new EntityNotFoundException("Cannot find Entity with primary key: " + theObj);
		}

		return aObj;
	}

	/**
	 * Enforce that the object exists in the database
	 * @param theObj the object that should exist
	 * @throws IllegalArgumentException thrown if the object does not exist in the database
	 */
	private void assertContains(Object theObj) {
		if (!contains(theObj)) {
			throw new IllegalArgumentException("Entity does not exist: " + theObj);
		}
	}

	/**
	 * Performs the same checks as assertContains, but returns the Graph describing the resource as a result so you
	 * do not need to perform a subsequent call to the database to get the data that is checked during containment
	 * checks in the first place, thus saving a query to the database.
	 * @param theObj the object that should exist.
	 * @return The graph describing the resource
	 * @throws IllegalArgumentException thrown if the object does not exist in the database
	 */
	private ExtGraph assertContainsAndDescribe(Object theObj) {
		assertStateOk(theObj);

		try {
			ExtGraph aGraph = DataSourceUtil.describe(getDataSource(), theObj);

			if (aGraph.isEmpty()) {
				throw new IllegalArgumentException("Entity does not exist: " + theObj);
			}

			return aGraph;
		}
		catch (QueryException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Enforce that the object does not exist in the database
	 * @param theObj the object that should not exist
	 * @throws IllegalArgumentException thrown if the object already exists in the database
	 */
	private void assertNotContains(Object theObj) {
		if (contains(theObj)) {
			throw new IllegalArgumentException("Entity already exists: " + theObj);
		}
	}

	/**
	 * Assert that the state of the EntityManager is ok; that it is open, and the specified object is a valid Rdf entity.
	 * @param theObj the object to check
	 * @throws IllegalStateException if the EntityManager is closed
	 * @throws IllegalArgumentException thrown if the value is not a valid Rdf Entity
	 */
	private void assertStateOk(Object theObj) {
		assertOpen();
		assertSupported(theObj);
	}

	/**
	 * Enforce that the EntityManager is open
	 * @throws IllegalStateException thrown if the EntityManager is closed or not yet open
	 */
	private void assertOpen() {
		if (!isOpen()) {
			throw new IllegalStateException("Cannot perform operation, EntityManager is not open");
		}
	}

	/**
	 * Assert that the object can be supported by this EntityManager, that is it a valid Rdf entity
	 * @param theObj the object to validate
	 * @throws IllegalArgumentException thrown if the object is not a valid Rdf entity.
	 */
	private void assertSupported(Object theObj) {
		if (theObj == null) {
			throw new IllegalArgumentException("null objects are not supported");
		}

		assertEntity(theObj);
		assertRdfClass(theObj);

		if (!(theObj instanceof SupportsRdfId)) {
			throw new IllegalArgumentException("Persistent RDF objects must implement the SupportsRdfId interface.");
		}

		try {
			AnnotationChecker.assertValid(theObj.getClass());
		}
		catch (EmpireException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Enforce that the object has the {@link Entity} annotation
	 * @param theObj the instance
	 * @throws IllegalArgumentException if the instances does not have the Entity Annotation
	 * @see Entity
	 */
	private void assertEntity(Object theObj) {
		if (EmpireOptions.ENFORCE_ENTITY_ANNOTATION) {
			assertHasAnnotation(theObj, Entity.class);
		}
	}

	/**
	 * Enforce that the object has the {@link com.clarkparsia.empire.annotation.RdfsClass} annotation
	 * @param theObj the instance
	 * @throws IllegalArgumentException if the instances does not have the RdfClass annotation
	 * @see com.clarkparsia.empire.annotation.RdfsClass
	 */
	private void assertRdfClass(Object theObj) {
		assertHasAnnotation(theObj, RdfsClass.class);
	}

	/**
	 * Verify that the instance has the specified annotation
	 * @param theObj the instance
	 * @param theAnnotation the annotation the instance is required to have
	 * @throws IllegalArgumentException thrown if the instance does not have the required annotation
	 */
	private void assertHasAnnotation(Object theObj, Class<? extends Annotation> theAnnotation) {
		if (!hasAnnotation(theObj.getClass(), theAnnotation)) {
			throw new IllegalArgumentException("Object (" + theObj.getClass() + ") is not an " + theAnnotation.getSimpleName());
		}
	}

	/**
	 * Returns whether or not the data source supports operations on named sub-graphs
	 * @return true if it does, false otherwise.  Returning true indicates calls to {@link #asSupportsNamedGraphs()}
	 * will return successfully without a ClassCastException
	 */
	private boolean doesSupportNamedGraphs() {
		return getDataSource() instanceof SupportsNamedGraphs;
	}

	/**
	 * Returns a reference to an object (the data source) which can perform operations on named sub-graphs
	 * @return the data source as a {@link com.clarkparsia.empire.ds.SupportsNamedGraphs}
	 * @throws ClassCastException thrown if the data source does not implements SupportsNamedGraphs
	 */
	private SupportsNamedGraphs asSupportsNamedGraphs() {
		return (SupportsNamedGraphs) getDataSource();
	}

	/**
	 * Returns a reference to an object (the DataSource) which supports Transactions.  Transaction support is
	 * provided by either the DataSource's native transaction support, or via our naive
	 * {@link TransactionalDataSource transactional wrapper}.
	 * @return a source which supports transactions
	 */
	private SupportsTransactions asSupportsTransactions() {
		if (mDataSource instanceof SupportsTransactions) {
			return (SupportsTransactions) mDataSource;
		}
		else {
			// it doesnt support transactions natively, so we'll wrap it in our naive transaction support.
			return new TransactionalDataSource(mDataSource);
		}
	}

	/**
	 * Fire the PostPersist lifecycle event for the Entity
	 * @param theObj the Entity to fire the event for
	 */
	private void postPersist(Object theObj) {
		handleLifecycleCallback(theObj, PostPersist.class);
	}

	/**
	 * Fire the PostRemove lifecycle event for the Entity
	 * @param theObj the Entity to fire the event for
	 */
	private void postRemove(Object theObj) {
		handleLifecycleCallback(theObj, PostRemove.class);
	}

	/**
	 * Fire the PostLoad lifecycle event for the Entity
	 * @param theObj the Entity to fire the event for
	 */
	private void postLoad(Object theObj) {
		handleLifecycleCallback(theObj, PostLoad.class);
	}

	/**
	 * Fire the PreRemove lifecycle event for the Entity
	 * @param theObj the Entity to fire the event for
	 */
	private void preRemove(Object theObj) {
		handleLifecycleCallback(theObj, PreRemove.class);
	}

	/**
	 * Fire the PreUpdate lifecycle event for the Entity
	 * @param theObj the Entity to fire the event for
	 */
	private void preUpdate(Object theObj) {
		handleLifecycleCallback(theObj, PreUpdate.class);
	}

	/**
	 * Fire the PostUpdate lifecycle event for the Entity
	 * @param theObj the Entity to fire the event for
	 */
	private void postUpdate(Object theObj) {
		handleLifecycleCallback(theObj, PostUpdate.class);
	}

	/**
	 * Fire the PrePersist lifecycle event for the Entity
	 * @param theObj the entity to fire the event for
	 */
	private void prePersist(Object theObj) {
		handleLifecycleCallback(theObj, PrePersist.class);
	}

	/**
	 * Handle the dispatching of the specified lifecycle event
	 * @param theObj the object involved in the event
	 * @param theLifecycleAnnotation the annotation denoting the event, such as {@link PrePersist}, {@link PostLoad}, etc.
	 */
	private void handleLifecycleCallback(Object theObj, Class<? extends Annotation> theLifecycleAnnotation) {
		if (theObj == null) {
			return;
		}

		Collection<Method> aMethods = getAnnotatedMethods(theObj.getClass(), theLifecycleAnnotation);

		// Entity methods take no arguments...
		try {
			for (Method aMethod : aMethods) {
				aMethod.invoke(theObj);
			}
		}
		catch (Exception e) {
			LOGGER.error("There was an error during entity lifecycle notification for annotation: " +
						 theLifecycleAnnotation + " on object: " + theObj +".", e);
		}

		for (Object aListener : getEntityListeners(theObj)) {
			Collection<Method> aListenerMethods = getAnnotatedMethods(aListener.getClass(), theLifecycleAnnotation);

			// EntityListeners methods take a single arguement, the entity
			try {
				for (Method aListenerMethod : aListenerMethods) {
					aListenerMethod.invoke(aListener, theObj);
				}
			}
			catch (Exception e) {
				LOGGER.error("There was an error during lifecycle notification for annotation: " +
							 theLifecycleAnnotation + " on object: " + theObj + ".", e);
			}
		}
	}

	/**
	 * Get or create the list of EntityListeners for an object.  If a list is created, it will be kept around and
	 * re-used for later persistence operations.
	 * @param theObj the object to get EntityLIsteners for
	 * @return the list of EntityListeners for the object, or null if they do not exist
	 */
	private Collection<Object> getEntityListeners(Object theObj) {
		Collection<Object> aListeners = mManagedEntityListeners.get(theObj);

		if (aListeners == null) {
			EntityListeners aEntityListeners = BeanReflectUtil.getAnnotation(theObj.getClass(), EntityListeners.class);
			
			if (aEntityListeners != null) {
				// if there are entity listeners, lets create them
				aListeners = new HashSet<Object>();
				for (Class<?> aClass : aEntityListeners.value()) {
					try {
						aListeners.add(Empire.get().instance(aClass));
					}
					catch (Exception e) {
						LOGGER.error("There was an error instantiating an EntityListener. ", e);
					}
				}

				mManagedEntityListeners.put(theObj, aListeners);
			}
			else {
				aListeners = Collections.emptyList();
			}
		}

		return aListeners;
	}

	/**
	 * Class which encapsulates a set of adds & removes to a DataSource.  Used to process a set of changes in a single
	 * operation, well, two operations.  Remove and then Add.  Also will verify that all objects that should have been
	 * added/removed from the KB have been added or removed.
	 * @author Michael Grove
	 * @since 0.7
	 * @version 0.7
	 */
	protected class DataSourceOperation {
		// HashMap's used here rather than the more generic Map interface because we allow null keys (no specified
		// named graph) which HashMap allows, while generically Map makes no guarantees about this, so we're explicit here.

		private final Map<java.net.URI, Graph> mAdd;
		private final Map<java.net.URI, Graph> mRemove;

		private final Set<Object> mVerifyAdd = new HashSet<Object>();
		private final Set<Object> mVerifyRemove = new HashSet<Object>();

		/**
		 * Create a new DataSourceOperation
		 */
		DataSourceOperation() {
			mAdd = new HashMap<java.net.URI, Graph>();
			mRemove = new HashMap<java.net.URI, Graph>();
		}

		/**
		 * Execute this operation.  Removes & adds all the specified data in as few database calls as possible.  Then
		 * verifies the results of the operations
		 * @throws DataSourceException if there is an error while performing the add/remove operations
		 * @throws PersistenceException if any objects were failed to be added or removed from the database
		 */
		public void execute() throws DataSourceException {
			// TODO: should this be in its own transaction?  or join the current one?

			for (URI aGraphURI : mRemove.keySet()) {
				if (doesSupportNamedGraphs() && aGraphURI != null) {
					asSupportsNamedGraphs().remove(aGraphURI, mRemove.get(aGraphURI));
				}
				else {
					getDataSource().remove(mRemove.get(aGraphURI));
				}
			}

			for (URI aGraphURI : mAdd.keySet()) {
				if (doesSupportNamedGraphs() && aGraphURI != null) {
					asSupportsNamedGraphs().add(aGraphURI, mAdd.get(aGraphURI));
				}
				else {
					getDataSource().add(mAdd.get(aGraphURI));
				}
			}

			verify();
		}

		/**
		 * Add the specified object to the list of objects that should be removed from the database when this operation
		 * is executed.
		 * @param theObj the object that should be revmoed from the database when the operation is executed
		 */
		public void verifyRemove(Object theObj) {
			mVerifyRemove.add(theObj);
		}

		/**
		 * Add the specified object to the list of objects that should be added to the database when this operation
		 * is executed.
		 * @param theObj the object that should be added to the database when the operation is executed
		 */
		public void verifyAdd(Object theObj) {
			mVerifyAdd.add(theObj);
		}

		/**
		 * Verify that all the objects to be added/removed were completed successfully.
		 * @throws PersistenceException if an add or remove failed for any reason
		 */
		private void verify() {
			for (Object aObj : mVerifyRemove) {
				if (contains(aObj)) {
					throw new PersistenceException("Remove failed for object: " + aObj.getClass() + " -> " + EmpireUtil.asSupportsRdfId(aObj).getRdfId());
				}
			}

			for (Object aObj : mVerifyAdd) {
				if (!contains(aObj)) {
					throw new PersistenceException("Addition failed for object: " + aObj.getClass() + " -> " + EmpireUtil.asSupportsRdfId(aObj).getRdfId());
				}
			}
		}

		/**
		 * Add this graph to the set of data to be added when this operation is executed
		 * @param theGraph the graph to be added
		 */
		public void add(Graph theGraph) {
			add(null, theGraph);
		}

		/**
		 * Add this graph to the set of data to be added, to the specified named graph, when this operation is executed
		 * @param theGraphURI the named graph the data should be added to
		 * @param theGraph the data to add
		 */
		public void add(java.net.URI theGraphURI, Graph theGraph) {
			Graph aGraph = mAdd.get(theGraphURI);

			if (aGraph == null) {
				aGraph = new ExtGraph();
			}

			aGraph.addAll(theGraph);

			mAdd.put(theGraphURI, aGraph);
		}
		
		/**
		 * Add this graph to the set of data to be removed when this operation is executed
		 * @param theGraph the graph to be removed
		 */
		public void remove(Graph theGraph) {
			remove(null, theGraph);
		}

		/**
		 * Add this graph to the set of data to be removed, from the specified named graph, when this operation is executed
		 * @param theGraphURI the named graph the data should be removed from
		 * @param theGraph the data to remove
		 */
		public void remove(java.net.URI theGraphURI, Graph theGraph) {
			Graph aGraph = mRemove.get(theGraphURI);

			if (aGraph == null) {
				aGraph = new ExtGraph();
			}

			aGraph.addAll(theGraph);

			mRemove.put(theGraphURI, aGraph);
		}

		/**
		 * Merge the operation with this one.  This will merge all the changes being tracked into a single operation.
		 * @param theOp the operation to merge
		 */
		public void merge(final DataSourceOperation theOp) {
			for (Map.Entry<URI, Graph> aEntry : theOp.mRemove.entrySet()) {
				remove(aEntry.getKey(), aEntry.getValue());
			}

			for (Map.Entry<URI, Graph> aEntry : theOp.mAdd.entrySet()) {
				add(aEntry.getKey(), aEntry.getValue());
			}

			mVerifyAdd.addAll(theOp.mVerifyAdd);
			mVerifyRemove.addAll(theOp.mVerifyRemove);
		}
	}
}
