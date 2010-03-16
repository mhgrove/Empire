/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire.annotation;

import com.clarkparsia.utils.AbstractDataCommand;
import com.clarkparsia.utils.NamespaceUtils;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.Statement;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.model.vocabulary.RDFS;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;

import java.lang.annotation.Annotation;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.io.Encoder;

import com.clarkparsia.utils.collections.CollectionUtil;

import org.openrdf.model.impl.ValueFactoryImpl;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.annotation.runtime.Proxy;

import com.clarkparsia.empire.impl.serql.SerqlDialect;

import static com.clarkparsia.empire.util.BeanReflectUtil.set;
import static com.clarkparsia.empire.util.BeanReflectUtil.setAccessible;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedFields;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedGetters;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedSetters;
import static com.clarkparsia.empire.util.BeanReflectUtil.get;

import com.clarkparsia.empire.util.BeanReflectUtil;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.openrdf.util.ResourceBuilder;
import com.clarkparsia.openrdf.util.GraphBuilder;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.openrdf.query.SesameQueryUtils;

import javax.persistence.Entity;
import javax.persistence.Transient;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.MethodHandler;

/**
 * <p>Description: Utility for creating RDF from a compliant Java Bean, and for turning RDF (the results of a describe
 * on a given rdf:ID into a KB) into a Java bean.</p>
 * <p>Usage:<br/>
 * <code><pre>
 *   MyClass aObj = new MyClass();
 *
 *  // set some data on the object
 *   KB.add(RdfGenerator.toRdf(aObj));
 *
 *  MyClass aObjCopy = RdfGenerator.fromRdf(MyClass.class, aObj.getRdfId(), KB);
 *
 *   // this will print true
 *   System.out.println(aObj.equals(aObjCopy));
 * </pre>
 * </code>
 * </p>
 * <p>
 * Compliant classes must be annotated with the {@link Entity} JPA annotation, the {@link RdfsClass} annotation,
 * and must implement the {@link SupportsRdfId} interface.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.2
 */
public class RdfGenerator {

	/**
	 * Global ValueFactory to use for converting Java values into sesame objects for serialization to RDF
	 */
	private static final ValueFactory FACTORY = new ValueFactoryImpl();

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(RdfGenerator.class.getName());

	/**
	 * Map from rdf:type URI's to the Java class which corresponds to that resource.
	 */
	private final static Map<URI, Class> TYPE_TO_CLASS = new HashMap<URI, Class>();

	/**
	 * Map to keep a record of what instances are currently being created in order to prevent cycles.  Keys are the
	 * identifiers of the instances and the values are the instances
	 */
	private final static Map<Object, Object> OBJECT_M = Collections.synchronizedMap(new HashMap<Object, Object>());

	static {
		// add default namespaces
		NamespaceUtils.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		NamespaceUtils.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		NamespaceUtils.addNamespace("owl", "http://www.w3.org/2002/07/owl#");

		Collection<Class<?>> aClasses = Empire.get().getAnnotationProvider().getClassesWithAnnotation(RdfsClass.class);
		for (Class<?> aClass : aClasses) {
			RdfsClass aAnnotation = aClass.getAnnotation(RdfsClass.class);

			addNamespaces(aClass);

			TYPE_TO_CLASS.put(FACTORY.createURI(NamespaceUtils.uri(aAnnotation.value())), aClass);
		}
	}

	/**
	 * Create an instance of the specified class and instantiate it's data from the given data source using the RDF
	 * instance specified by the given URI
	 * @param theClass the class to create
	 * @param theKey the id of the RDF individual containing the data for the new instance
	 * @param theSource the KB to get the RDF data from
	 * @param <T> the type of the instance to create
	 * @return a new instance
	 * @throws InvalidRdfException thrown if the class does not support RDF JPA operations, or does not provide sufficient access to its fields/data.
	 * @throws DataSourceException thrown if there is an error while retrieving data from the graph
	 */
	public static <T> T fromRdf(Class<T> theClass, String theKey, DataSource theSource) throws InvalidRdfException, DataSourceException {
		return fromRdf(theClass, EmpireUtil.asPrimaryKey(theKey), theSource);
	}

	/**
	 * Create an instance of the specified class and instantiate it's data from the given data source using the RDF
	 * instance specified by the given URI
	 * @param theClass the class to create
	 * @param theURI the id of the RDF individual containing the data for the new instance
	 * @param theSource the KB to get the RDF data from
	 * @param <T> the type of the instance to create
	 * @return a new instance
	 * @throws InvalidRdfException thrown if the class does not support RDF JPA operations, or does not provide sufficient access to its fields/data.
	 * @throws DataSourceException thrown if there is an error while retrieving data from the graph
	 */
	public static <T> T fromRdf(Class<T> theClass, java.net.URI theURI, DataSource theSource) throws InvalidRdfException, DataSourceException {
		return fromRdf(theClass, new SupportsRdfId.URIKey(theURI), theSource);
	}

	/**
	 * Create an instance of the specified class and instantiate it's data from the given data source using the RDF
	 * instance specified by the given URI
	 * @param theClass the class to create
	 * @param theId the id of the RDF individual containing the data for the new instance
	 * @param theSource the KB to get the RDF data from
	 * @param <T> the type of the instance to create
	 * @return a new instance
	 * @throws InvalidRdfException thrown if the class does not support RDF JPA operations, or does not provide sufficient access to its fields/data.
	 * @throws DataSourceException thrown if there is an error while retrieving data from the graph
	 */
	public static <T> T fromRdf(Class<T> theClass, SupportsRdfId.RdfKey theId, DataSource theSource) throws InvalidRdfException, DataSourceException {
		T aObj;

		try {
			if (theClass.isInterface()) {
				aObj = com.clarkparsia.empire.codegen.InstanceGenerator.generateInstanceClass(theClass).newInstance();
			}
			else {
				aObj = theClass.newInstance();
			}

			asSupportsRdfId(aObj).setId(theId);
		}
		catch (InstantiationException e) {
			throw new InvalidRdfException("Cannot create instance of bean, should have a default constructor.", e);
		}
		catch (IllegalAccessException e) {
			throw new InvalidRdfException("Could not access default constructor for class: " + theClass, e);
		}
		catch (Exception e) {
			throw new InvalidRdfException("Cannot create an instance of bean", e);
		}

		return fromRdf(aObj, theSource);
	}

	/**
	 * Populate the fields of the current instance from the RDF indiviual with the given URI
	 * @param theObj the Java object to populate
	 * @param theSource the KB to get the RDF data from
	 * @param <T> the type of the class being populated
	 * @return theObj, populated from the specified DataSource
	 * @throws InvalidRdfException thrown if the object does not support the RDF JPA API.
	 * @throws DataSourceException thrown if there is an error retrieving data from the database
	 */
	private static <T> T fromRdf(T theObj, DataSource theSource) throws InvalidRdfException, DataSourceException {
		SupportsRdfId.RdfKey theKeyObj = asSupportsRdfId(theObj).getId();

		if (OBJECT_M.containsKey(theKeyObj)) {
			// TODO: this is probably a safe cast, i dont see how something w/ the same URI, which should be the same
			// object would change types
			return (T) OBJECT_M.get(theKeyObj);
		}
		else {
			OBJECT_M.put(theKeyObj, theObj);
		}

		ExtGraph aGraph = new ExtGraph(EmpireUtil.describe(theSource, theObj));

		if (aGraph.size() == 0) {
			OBJECT_M.remove(theKeyObj);

			return theObj;
		}

		Resource aRes = EmpireUtil.asResource(asSupportsRdfId(theObj));
		Set<URI> aProps = new HashSet<URI>();
		Iterator<Statement> sIter = aGraph.match(aRes, null, null);

		while (sIter.hasNext()) {
			aProps.add(sIter.next().getPredicate());
		}

		Collection<Field> aFields = getAnnotatedFields(theObj.getClass());
		Collection<Method> aMethods = getAnnotatedSetters(theObj.getClass(), true);

		addNamespaces(theObj.getClass());

		final Map<URI, AccessibleObject> aAccessMap = new HashMap<URI, AccessibleObject>();
		
		CollectionUtil.each(aFields, new AbstractDataCommand<Field>() {
			public void execute() {
				aAccessMap.put(FACTORY.createURI(NamespaceUtils.uri(getData().getAnnotation(RdfProperty.class).value())),
							  getData());
			}
		});

		CollectionUtil.each(aMethods, new AbstractDataCommand<Method>() {
			public void execute() {
				RdfProperty aAnnotation = getAnnotation(getData(), RdfProperty.class);
				if (aAnnotation != null) {
					aAccessMap.put(FACTORY.createURI(NamespaceUtils.uri(aAnnotation.value())),
								   getData());
				}
			}
		});

		for (URI aProp : aProps) {
			AccessibleObject aAccess = aAccessMap.get(aProp);

			if (aAccess == null && RDF.TYPE.equals(aProp)) {
				// we can skip the rdf:type property.  it's basically assigned in the @RdfsClass annotation on the
				// java class, so we can figure it out later if need be. TODO: of course, if something has multiple types
				// that information is lost, which is not good.

				URI aType = (URI) aGraph.getValue(aRes, aProp);
				if (!TYPE_TO_CLASS.containsKey(aType) ||
					!TYPE_TO_CLASS.get(aType).isAssignableFrom(theObj.getClass())) {

					if (TYPE_TO_CLASS.containsKey(aType) && !TYPE_TO_CLASS.get(aType).getName().equals(theObj.getClass().getName())) {
						// TODO: this might just be an error
						LOGGER.warn("Asserted rdf:type of the individual does not match the rdf:type annotation on the object. " + aType + " " + TYPE_TO_CLASS.get(aType) + " " + theObj.getClass() + " " +TYPE_TO_CLASS.get(aType).isAssignableFrom(theObj.getClass())+ " " +TYPE_TO_CLASS.get(aType).equals(theObj.getClass()) + " " + TYPE_TO_CLASS.get(aType).getName().equals(theObj.getClass().getName()));
					}
					else {
						// if they're not equals() or isAssignableFrom, but have the same name, this is usually
						// means that the class loaders don't match.  so probably not an error, so no warning.
					}
				}

				continue;
			}
			else if (aAccess == null) {
				// TODO: this is a lossy transformation, there's rdf data which is not represented by a field on the java class
				// so if we don't convert it into something on the java bean, they don't have a full representation of
				// what was in the database AND if they save that back to the database, they will lose this information
				// that is not good either.
				continue;
			}

			ToObjectFunction aFunc = new ToObjectFunction(theSource, aRes, aAccess, aProp);

			Object aValue = aFunc.apply(aGraph.getValues(aRes, aProp));

			boolean aOldAccess = aAccess.isAccessible();

			try {
				setAccessible(aAccess, true);
				set(aAccess, theObj, aValue);
			}
			catch (InvocationTargetException e) {
				// oh crap
				throw new InvalidRdfException(e);
			}
			catch (IllegalAccessException e) {
				// this should not happen since we toggle the accessibility of the field, but we'll re-throw regardless
				throw new InvalidRdfException(e);
			}
			catch (IllegalArgumentException e) {
				// this is "likely" to happen.  we'll get this exception if the rdf does not match the java.  for example
				// if something is specified to be an int in the java class, but it typed as a float (though down conversion
				// in that case might work) the set call will fail.
			}
			catch (RuntimeException e) {
				// TODO: i dont like keying on a RuntimeException here to get the error condition, but since the
				// Function interface does not throw anything, this is the best we can do.  maybe consider a
				// version of the Function interface that has a throws clause, it would make this more clear.

				// this was probably an error converting from a Value to an Object
				throw new InvalidRdfException(e);
			}
			finally {
				setAccessible(aAccess, aOldAccess);
			}
		}

		OBJECT_M.remove(theKeyObj);
		
		return theObj;
	}


	/**
	 * Return the RdfClass annotation on the object.
	 * @param theObj the object to get that annotation from
	 * @return the objects' RdfClass annotation
	 * @throws InvalidRdfException thrown if the object does not have the required annotation, does not have an @Entity
	 * annotation, or does not {@link SupportsRdfId support Rdf Id's}
	 */
	private static RdfsClass asRdfClass(Object theObj) throws InvalidRdfException {
		if (theObj.getClass().getAnnotation(RdfsClass.class) == null) {
			throw new InvalidRdfException("Specified value is not an RdfsClass object");
		}

		if (theObj.getClass().getAnnotation(Entity.class)  == null) {
			throw new InvalidRdfException("Specified value is not a JPA Entity object");
		}

		// verify that it supports rdf id's
		asSupportsRdfId(theObj);

		return theObj.getClass().getAnnotation(RdfsClass.class);
	}

	/**
	 * Return the object casted to {@link SupportsRdfId}
	 * @param theObj the object to cast
	 * @return the object, casted to the interface
	 * @throws InvalidRdfException thrown if the object does not implement the interface
	 */
	private static SupportsRdfId asSupportsRdfId(Object theObj) throws InvalidRdfException {
		if (!(theObj instanceof SupportsRdfId)) {
			throw new InvalidRdfException("Object of type '" + (theObj.getClass().getName()) + "' does not implements SupportsRdfId, anonymous instances are not supported.");
		}
		else {
			return (SupportsRdfId) theObj;
		}
	}

	/**
	 * Given an object, return it's rdf:ID.  If it already has an id, that will be returned, otherwise the id
	 * will either be generated from the data, using the {@link RdfId} annotation as a guide, or it will auto-generate one.
	 * @param theObj the object
	 * @return the object's rdf:Id
	 * @throws InvalidRdfException thrown if the object does not support the minimum to create or retrieve an rdf:ID
	 * @see SupportsRdfId
	 */
	private static Resource id(Object theObj) throws InvalidRdfException {
		SupportsRdfId aSupport = asSupportsRdfId(theObj);

		if (aSupport.getId() != null) {
			//return FACTORY.createURI(aSupport.getRdfId().toString());
			return EmpireUtil.asResource(aSupport);
		}

		Field aIdField = BeanReflectUtil.getIdField(theObj.getClass());

		String aValue = hash(BasicUtils.getRandomString(10));
		String aNS = RdfId.DEFAULT;

		URI aURI = FACTORY.createURI(aNS + aValue);

		if (aIdField != null && !aIdField.getAnnotation(RdfId.class).namespace().equals("")) {
			aNS = aIdField.getAnnotation(RdfId.class).namespace();
		}

		if (aIdField != null) {
			boolean aOldAccess = aIdField.isAccessible();
			aIdField.setAccessible(true);

			try {
				if (aIdField.get(theObj) == null) {
					throw new InvalidRdfException("id field must have a value");
				}

				Object aValObj = aIdField.get(theObj);

				aValue = Encoder.urlEncode(aValObj.toString());

				if (aValObj instanceof java.net.URI || BasicUtils.isURI(aValObj.toString())) {
					try {
						aURI = FACTORY.createURI(aValObj.toString());
					}
					catch (IllegalArgumentException e) {
						// sometimes sesame disagrees w/ Java about what a valid URI is.  so we'll have to try
						// and construct a URI from the possible fragment
						aURI = FACTORY.createURI(aNS + aValue);
					}
				}
				else {
					//aValue = hash(aValObj);
					aURI = FACTORY.createURI(aNS + aValue);
				}
			}
			catch (IllegalAccessException ex) {
				throw new InvalidRdfException(ex);
			}

			aIdField.setAccessible(aOldAccess);
		}

		aSupport.setId(new SupportsRdfId.URIKey(java.net.URI.create(aURI.toString())));

		return aURI;
	}

	/**
	 * Scan the object for {@link Namespaces} annotations and add them to the current list of known namespaces
	 * @param theObj the object to scan.
	 */
	private static void addNamespaces(Class<?> theObj) {
		Namespaces aNS = theObj.getAnnotation(Namespaces.class);

		if (aNS == null) {
			return;
		}

		int aIndex = 0;
		while (aIndex+1 < aNS.value().length) {
			String aPrefix = aNS.value()[aIndex];
			String aURI = aNS.value()[aIndex+1];

			// TODO: maybe have a local version of this, this will add a global namespace, and could potentially
			// overwrite global things that use the same prefix but different uris, which would be bad
			NamespaceUtils.addNamespace(aPrefix, aURI);
			aIndex += 2;
		}
	}

	/**
	 * Return the given Java bean as a set of RDF triples
	 * @param theObj the object
	 * @return the object represented as RDF triples
	 * @throws InvalidRdfException thrown if the object cannot be transformed into RDF.
	 */
	public static ExtGraph asRdf(Object theObj) throws InvalidRdfException {
		RdfsClass aClass = asRdfClass(theObj);

		Resource aSubj = id(theObj);

		addNamespaces(theObj.getClass());

		GraphBuilder aBuilder = new GraphBuilder();

		Collection<AccessibleObject> aAccessors = new HashSet<AccessibleObject>();
		aAccessors.addAll(getAnnotatedFields(theObj.getClass()));
		aAccessors.addAll(getAnnotatedGetters(theObj.getClass(), true));

		try {
			ResourceBuilder aRes = aBuilder.instance(aBuilder.getValueFactory().createURI(NamespaceUtils.uri(aClass.value())),
													 aSubj);

			AsValueFunction aFunc = new AsValueFunction();
			for (AccessibleObject aAccess : aAccessors) {

				if (aAccess.isAnnotationPresent(Transient.class)
					|| (aAccess instanceof Field
						&& Modifier.isTransient( ((Field)aAccess).getModifiers() ))) {

					// transient fields or accessors with the Transient annotation do not get converted.
					continue;
				}

				RdfProperty aPropertyAnnotation = getAnnotation(aAccess, RdfProperty.class);
				URI aProperty = aBuilder.getValueFactory().createURI(NamespaceUtils.uri(aPropertyAnnotation.value()));

				boolean aOldAccess = aAccess.isAccessible();
				setAccessible(aAccess, true);

				Object aValue = get(aAccess, theObj);

				setAccessible(aAccess, aOldAccess);

				if (aValue == null || aValue.toString().equals("")) {
					continue;
				}
				else if (Collection.class.isAssignableFrom(aValue.getClass())) {
					@SuppressWarnings("unchecked")
					List<Value> aValueList = asList((Collection<Object>) Collection.class.cast(aValue));

					if (aValueList.isEmpty()) {
						continue;
					}

					if (aPropertyAnnotation.isList()) {
						aRes.addProperty(aProperty, aValueList);
					}
					else {
						for (Value aVal : aValueList) {
							aRes.addProperty(aProperty, aVal);
						}
					}
				}
				else {
					aRes.addProperty(aProperty, aFunc.apply(aValue));
				}
			}
		}
		catch (IllegalAccessException e) {
			throw new InvalidRdfException(e);
		}
		catch (RuntimeException e) {
			throw new InvalidRdfException(e);
		}
		catch (InvocationTargetException e) {
			throw new InvalidRdfException("Cannot invoke method", e);
		}

		return aBuilder.graph();
	}

	/**
	 * Return the Annotation of the specified type on the accessor, either a {@link Field} or a {@link Method}
	 * @param theAccess the accessor
	 * @param theAnnotation the annotation to get
	 * @param <T> the type of annotation to retrieve
	 * @return the value of the annotation on the accessor, or null if one is not found, or the accessor is not a Field or Method.
	 */
	private static <T extends Annotation> T getAnnotation(Object theAccess, Class<T> theAnnotation) {
		if (theAccess instanceof Field) {
			return ((Field)theAccess).getAnnotation(theAnnotation);
		}
		else if (theAccess instanceof Method) {
			Method aMethod = (Method) theAccess;
			T aAnnotation = aMethod.getAnnotation(theAnnotation);

			if (aAnnotation == null) {
				// if this is the case, it might be that this is from an "inferred" method.  so let's check it's twin,
				// either a getter or setter to see if it has the annotation.  If not, I don't know what the hell
				// happened

				try {
					Method aPairedMethod = null;
					if (aMethod.getName().startsWith("get")) {
						aPairedMethod = aMethod.getDeclaringClass().getMethod(aMethod.getName().replaceFirst("get", "set"),
																			  aMethod.getReturnType());
					}
					else if (aMethod.getName().startsWith("set")) {
						try {
							aPairedMethod = aMethod.getDeclaringClass().getMethod(aMethod.getName().replaceFirst("set", "get"));
						}
						catch (Exception e) {
							// no-op
						}

						if (aPairedMethod == null) {
							aPairedMethod = aMethod.getDeclaringClass().getMethod(aMethod.getName().replaceFirst("set", "is"));
						}
					}

					if (aPairedMethod != null) {
						aAnnotation = aPairedMethod.getAnnotation(theAnnotation);
					}
				}
				catch (NoSuchMethodException e) {
					// could not find a paired getter/setter.  don't know why.  probably the user put the annotations
					// on methods of non-bean compliant code, which is screwing things up.  we'll try to fail as
					// gracefully as possible
				}
			}

			return aAnnotation;
		}
		else {
			return null;
		}
	}

	/**
	 * Transform a list of Java Objects into the corresponding RDF values
	 * @param theCollection the collection to transform
	 * @return the collection as a list of RDF values
	 * @throws InvalidRdfException thrown if any of the values cannot be transformed
	 */
	private static List<Value> asList(Collection<Object> theCollection) throws InvalidRdfException {
		try {
			return CollectionUtil.list(CollectionUtil.transform(theCollection, new AsValueFunction()));
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			throw new InvalidRdfException(e.getMessage());
		}
	}

	/**
	 * Return a base64 encoded md5 hash of the given object
	 * @param theObj the object to hash
	 * @return the hashed version of the object.
	 */
	private static String hash(Object theObj) {
		return BasicUtils.hex(BasicUtils.md5(theObj.toString()));
//		return Encoder.base64Encode(BasicUtils.md5(theObj.toString()));
	}

	/**
	 * Implementation of the function interface to turn a Collection of RDF values into Java bean(s).
	 */
	private static class ToObjectFunction implements Function<Collection<Value>, Object> {
		/**
		 * Function to turn a single value into an object
		 */
		private ValueToObject valueToObject;

		/**
		 * Reference to the Type which the values will be assigned
		 */
		private Object mField;

		public ToObjectFunction(final DataSource theSource, Resource theResource, final Object theField, final URI theProp) {
			valueToObject = new ValueToObject(theSource, theResource, theField, theProp);

			mField = theField;
		}

		public Object apply(final Collection<Value> theIn) {
			if (theIn == null || theIn.isEmpty()) {
				return null;
			}
			else if (Collection.class.isAssignableFrom(classFrom(mField))) {
				try {
					Collection<Object> aValues = instantiateCollectionFromField(classFrom(mField));

					for (Value aValue : theIn) {
						Object aListValue = valueToObject.apply(aValue);

						if (aListValue == null) {
							throw new RuntimeException("Error converting a list value.");
						}

						aValues.add(aListValue);
					}

					return aValues;
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else if (theIn.size() == 1) {
				// collection of one element, just convert the single element and send that back
				return valueToObject.apply(theIn.iterator().next());
			}
			else {
				throw new RuntimeException("Cannot convert list of values to anything meaningful for the field. " + mField + " " + theIn);
			}
		}
	}

	/**
	 * Return the Class type of the accessor.  For a {@link Field} it's the declared type of the field, for a
	 * {@link Method}, which should be a bean-style setter, it's the type of the single parameter to the method.
	 * @param theObject the accessor
	 * @return the Class type of the Field/Method
	 * @throws RuntimeException thrown if you don't pass in a Field or Method, or if the Method is not of the expected
	 * bean-style setter variety.
	 */
	private static Class classFrom(Object theObject) {
		if (theObject instanceof Field) {
			return ((Field)theObject).getType();
		}
		else if (theObject instanceof Method) {
			// this should be a setter style bean method, taking one param which corresponds to the type of the property
			// it represents

			Method aMethod = (Method) theObject;
			if (aMethod.getParameterTypes().length == 1) {
				return aMethod.getParameterTypes()[0];
			}
			else {
				throw new RuntimeException("Unknown or unsupported accessor method type");
			}
		}
        else if (theObject instanceof Class) {
            return (Class) theObject;
        }
		else {
			throw new RuntimeException("Unknown or unsupported accessor type: " + theObject);
		}
	}

	private static Collection<Object> instantiateCollectionFromField(Class theValueType) {
		try {
			// try creating a new instance.  this will work if they've specified a concrete type
			return (Collection<Object>) theValueType.newInstance();
		}
		catch (Throwable e) {
			// TODO: make this less brittle -- should we have some sort of facade collection or something in front
			// that we generate at runtime?  or is there a better way to handle this situation?

			// if the above failed, that means the type of the field is something like List, or Set, which is not
			// directly instantiable.  If it's a known type, we'll hand instantiate something here.
			if (List.class.isAssignableFrom(theValueType)) {
				return new ArrayList<Object>();
			}
			else if (Set.class.isAssignableFrom(theValueType)) {
				if (SortedSet.class.isAssignableFrom(theValueType)) {
					return new TreeSet<Object>();
				}
				else {
					return new LinkedHashSet<Object>();
				}
			}
			else if (Collection.class.equals(theValueType)) {
				return new LinkedHashSet<Object>();
			}
			else {
				// last option is Map, but i dunno what the hell to do in that case, it doesn't map to our use here.
				throw new RuntimeException("Unknown or unsupported collection type for a field: " + theValueType);
			}
		}
	}

	public static class ValueToObject implements Function<Value, Object> {
		static final List<URI> integerTypes = Arrays.asList(XMLSchema.INT, XMLSchema.INTEGER, XMLSchema.POSITIVE_INTEGER,
													  XMLSchema.NEGATIVE_INTEGER, XMLSchema.NON_NEGATIVE_INTEGER,
													  XMLSchema.NON_POSITIVE_INTEGER, XMLSchema.UNSIGNED_INT);
		static final List<URI> longTypes = Arrays.asList(XMLSchema.LONG, XMLSchema.UNSIGNED_LONG);
		static final List<URI> floatTypes = Arrays.asList(XMLSchema.FLOAT, XMLSchema.DECIMAL);
		static final List<URI> shortTypes = Arrays.asList(XMLSchema.SHORT, XMLSchema.UNSIGNED_SHORT);
		static final List<URI> byteTypes = Arrays.asList(XMLSchema.BYTE, XMLSchema.UNSIGNED_BYTE);

		private URI mProperty;
		private Object mAccessor;
		private DataSource mSource;
		private Resource mResource;

		public ValueToObject(final DataSource theSource, Resource theResource, final Object theAccessor, final URI theProp) {
			mResource = theResource;
			mSource = theSource;
			mAccessor = theAccessor;
			mProperty = theProp;
		}

		public Object apply(final Value theValue) {
			if (theValue instanceof Literal) {
				Literal aLit = (Literal) theValue;
				URI aDatatype = aLit.getDatatype() != null ? aLit.getDatatype() : null;
				if (aDatatype == null || XMLSchema.STRING.equals(aDatatype) || RDFS.LITERAL.equals(aDatatype)) {
					return aLit.getLabel();
				}
				else if (XMLSchema.BOOLEAN.equals(aDatatype)) {
					return Boolean.valueOf(aLit.getLabel());
				}
				else if (integerTypes.contains(aDatatype)) {
					return Integer.parseInt(aLit.getLabel());
				}
				else if (longTypes.contains(aDatatype)) {
					return Long.parseLong(aLit.getLabel());
				}
				else if (XMLSchema.DOUBLE.equals(aDatatype)) {
					return Double.valueOf(aLit.getLabel());
				}
				else if (floatTypes.contains(aDatatype)) {
					return Float.valueOf(aLit.getLabel());
				}
				else if (shortTypes.contains(aDatatype)) {
					return Short.valueOf(aLit.getLabel());
				}
				else if (byteTypes.contains(aDatatype)) {
					return Byte.valueOf(aLit.getLabel());
				}
				else if (XMLSchema.ANYURI.equals(aDatatype)) {
					return java.net.URI.create(aLit.getLabel());
				}
				else if (XMLSchema.DATE.equals(aDatatype) || XMLSchema.DATETIME.equals(aDatatype)) {
					return BasicUtils.asDate(aLit.getLabel());
				}
				else if (XMLSchema.TIME.equals(aDatatype)) {
					return new Date(Long.parseLong(aLit.getLabel()));
				}
				else {
					// no idea what this value is from its data type.  if the field takes a string
					// we'll just assign the plain string, otherwise its an error
					if (classFrom(mAccessor).isAssignableFrom(String.class)) {
						return aLit.getLabel();
					}
					else {
						throw new RuntimeException("Unsupported or unknown literal datatype");
					}
				}
			}
			else if (theValue instanceof BNode) {
				// TODO: this is not bulletproof, clean this up

				if (mAccessor != null && Collection.class.isAssignableFrom(classFrom(mAccessor))) {
					// the field takes a collection, lets create a new instance of said collection, and hopefully the
					// bnode is a list.  this approach will only work if the property is a singleton value, eg
					// :inst someProperty _:a where _:a is the head of a list.  if you have another value _:b for
					// some property on :inst, we don't have any way of figuring out which one you're talking about
					// since bnode id references are not guaranteed to be stable in SPARQL, ie just because its id "a"
					// in the result set, does not mean i can do another query for _:a and get the expected results.
					// and you can't do a describe for the same reason.


					try {
						String aQuery = getBNodeConstructQuery(mSource, mResource, mProperty);
						
						ExtGraph aGraph = new ExtGraph(mSource.graphQuery(aQuery));
						Resource aPossibleListHead = (Resource) aGraph.getValue(mResource, mProperty);
						if (aGraph.isList(aPossibleListHead)) {
							List<Value> aList = aGraph.asList(aPossibleListHead);

							return new ToObjectFunction(mSource, null, null, null).apply(aList);
						}
						else {
							throw new RuntimeException("Arbitrary bnodes not supported");
						}
					}
					catch (QueryException e) {
						throw new RuntimeException(e);
					}
				}
				else {
					throw new RuntimeException("Arbitrary bnodes not supported");
				}
			}
			else if (theValue instanceof URI) {
				URI aURI = (URI) theValue;
				try {
					// we need to figure out what type of bean this instance maps to.
					Class<?> aClass = classFrom(mAccessor);

					// TODO: need a better method for this situation
					if (aClass.isAssignableFrom(java.net.URI.class)) {
						return java.net.URI.create(aURI.toString());
					}
					else if (Collection.class.isAssignableFrom(aClass)) {
						// if the field we're assigning from is a collection, try and figure out the type of the thing
						// we're creating from the collection

						Type[] aTypes = null;

						if (mAccessor instanceof Field && ((Field)mAccessor).getGenericType() instanceof ParameterizedType) {
							aTypes = ((ParameterizedType) ((Field)mAccessor).getGenericType()).getActualTypeArguments();
						}
						else if (mAccessor instanceof Method) {
							aTypes = ((Method) mAccessor).getGenericParameterTypes();
						}


						if (aTypes != null && aTypes.length >= 1 && aTypes[0] instanceof Class) {
							// first type argument to a collection is usually the one we care most about
							aClass = (Class) aTypes[0];
						}
					}

					if (!BeanReflectUtil.hasAnnotation(aClass, RdfsClass.class)) {
						// k, so either the parameter of the collection or the declared type of the field does
						// not map to an instance/bean type.  this is most likely an error, but lets try and find
						// the rdf:type of the field, and see if we can map that to a class in the path and we'll
						// create an instance of that.  that will work, and pushes the likely failure back off to
						// the assignment of the created instance

						URI aType = getType(mSource, aURI);

						// k, so now we know the type, if we can match the type to a class then we're in business
						if (aType != null) {
							Class aTypeClass = TYPE_TO_CLASS.get(aType);
							if (aTypeClass != null && BeanReflectUtil.hasAnnotation(aTypeClass, RdfsClass.class)) {
								// lets try this one
								aClass = aTypeClass;
							}
						}
					}

					return getProxyOrDbObject(aClass, java.net.URI.create(aURI.toString()), mSource);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				throw new RuntimeException("Unexpected Value type");
			}
		}
	}

	private static <T> T getProxyOrDbObject(Class<T> theClass, java.net.URI theURI, DataSource theSource) throws Exception {
		if (EmpireOptions.ENABLE_PROXY_OBJECTS) {
			Proxy<T> aProxy = new Proxy<T>(theClass, theURI, theSource);

			ProxyFactory aFactory = new ProxyFactory();
			aFactory.setSuperclass(theClass);
			aFactory.setHandler(new ProxyHandler<T>(aProxy));

			return (T) aFactory.createClass().newInstance();
		}
		else {
			return fromRdf(theClass, new SupportsRdfId.URIKey(theURI), theSource);
		}
	}

	/**
	 * Javassist {@link MethodHandler} implementation for method proxying.
	 * @param <T> the proxy class type
	 */
	private static class ProxyHandler<T> implements MethodHandler {
		Proxy<T> mProxy;

		private ProxyHandler(final Proxy<T> theProxy) {
			mProxy = theProxy;
		}

		public Object invoke(final Object theThis, final Method theMethod, final Method theProxyMethod, final Object[] theArgs) throws Throwable {
			return theMethod.invoke(mProxy.value(), theArgs);
		}
	}

	private static URI getType(DataSource theSource, URI theConcept) {
		try {
			return new ExtGraph(EmpireUtil.describe(theSource, java.net.URI.create(theConcept.toString()))).getType(theConcept);
		}
		catch (DataSourceException e) {
			LOGGER.error("There was an error while getting the type of a resource", e);

			return null;
		}
	}

	private static String getBNodeConstructQuery(DataSource theSource, Resource theRes, URI theProperty) {
		String aSerqlQuery = "construct * from {" + SesameQueryUtils.getQueryString(theRes) + "} <"+theProperty.toString()+"> {o}, {o} po {oo}";

		String aSparqlQuery = "CONSTRUCT  { " + SesameQueryUtils.getQueryString(theRes) + " <"+theProperty.toString()+"> ?o . ?o ?po ?oo  } \n" +
							  "WHERE\n" +
							  "{ " + SesameQueryUtils.getQueryString(theRes) + " <"+theProperty.toString()+"> ?o.\n" +
							  "?o ?po ?oo. }";

		if (theSource.getQueryFactory().getDialect() == SerqlDialect.instance()) {
			return aSerqlQuery;
		}
		else {
			// TODO: we're just assuming/hoping at this point that they support sparql.  which
			// will most likely be the case, but possibly not always.
			return aSparqlQuery;
		}
	}

	/**
	 * Return whether or not the given object is a Java primitive type (String is included as a primitive).
	 * @param theObj the object
	 * @return true if its a primitive, false otherwise.
	 */
    public static boolean isPrimitive(Object theObj) {
        return (Boolean.class.isInstance(theObj) || Integer.class.isInstance(theObj) || Long.class.isInstance(theObj)
                || Short.class.isInstance(theObj) || Double.class.isInstance(theObj) || Float.class.isInstance(theObj)
                || Date.class.isInstance(theObj) || String.class.isInstance(theObj) || Character.class.isInstance(theObj));
    }

	public static class AsValueFunction implements Function<Object, Value> {
		public Value apply(final Object theIn) {
			if (theIn == null) {
				return null;
			}
            else if (!EmpireOptions.STRONG_TYPING && isPrimitive(theIn)) {
                return FACTORY.createLiteral(theIn.toString());
            }
			else if (Boolean.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Boolean.class.cast(theIn));
			}
			else if (Integer.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Integer.class.cast(theIn));
			}
			else if (Long.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Long.class.cast(theIn));
			}
			else if (Short.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Short.class.cast(theIn));
			}
			else if (Double.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Double.class.cast(theIn));
			}
			else if (Float.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Float.class.cast(theIn));
			}
			else if (Date.class.isInstance(theIn)) {
				return FACTORY.createLiteral(BasicUtils.datetime(Date.class.cast(theIn)), XMLSchema.DATETIME);
			}
			else if (String.class.isInstance(theIn)) {
				return FACTORY.createLiteral(String.class.cast(theIn));
			}
			else if (Character.class.isInstance(theIn)) {
				return FACTORY.createLiteral(Character.class.cast(theIn));
			}
			else if (java.net.URI.class.isInstance(theIn)) {
				//return SesameValueFactory.instance().createLiteral(theIn.toString(), SesameValueFactory.instance().createURI(XmlSchema.ANYURI));
                return FACTORY.createURI(theIn.toString());
			}
			else if (Value.class.isAssignableFrom(theIn.getClass())) {
				return Value.class.cast(theIn);
			}
			else if (BeanReflectUtil.hasAnnotation(theIn.getClass(), RdfsClass.class)) {
				try {
					return id(theIn);
				}
				catch (InvalidRdfException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				throw new RuntimeException("Unknown type conversion: " + theIn.getClass() + " " + theIn);
			}
		}
	}
}
