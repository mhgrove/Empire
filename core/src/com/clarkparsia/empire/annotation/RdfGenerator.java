package com.clarkparsia.empire.annotation;

import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.utils.AbstractDataCommand;
import com.clarkparsia.utils.NamespaceUtils;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.URI;

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
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.clarkparsia.sesame.utils.GraphBuilder;
import com.clarkparsia.sesame.utils.ResourceBuilder;
import com.clarkparsia.sesame.utils.SesameValueFactory;
import com.clarkparsia.sesame.utils.ExtendedGraph;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.Function;

import com.clarkparsia.utils.collections.CollectionUtil;

import org.openrdf.model.impl.URIImpl;

import org.openrdf.sesame.query.MalformedQueryException;

import org.openrdf.sesame.sail.StatementIterator;

import org.openrdf.vocabulary.XmlSchema;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.SupportsRdfId;

import com.clarkparsia.empire.impl.serql.SerqlDialect;

import javax.persistence.Entity;

/**
 * <p>Title: RdfGenerator</p>
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
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class RdfGenerator {
	/**
	 * Map from rdf:type URI's to the Java class which corresponds to that resource.
	 */
	private final static Map<URI, Class> TYPE_TO_CLASS = new HashMap<URI, Class>();

	/**
	 * Map to keep a record of what instances are currently being created in order to prevent cycles
	 */
	private final static Map<java.net.URI, Object> OBJECT_M = Collections.synchronizedMap(new HashMap<java.net.URI, Object>());

	static {
		// add default namespaces
		NamespaceUtils.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		NamespaceUtils.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		NamespaceUtils.addNamespace("owl", "http://www.w3.org/2002/07/owl#");

		Collection<Class> aClasses = EmpireOptions.ANNOTATION_PROVIDER.getClassesWithAnnotation(RdfsClass.class);
		for (Class aClass : aClasses) {
			RdfsClass aAnnotation = (RdfsClass) aClass.getAnnotation(RdfsClass.class);

			addNamespaces(aClass);

			TYPE_TO_CLASS.put(SesameValueFactory.instance().createURI(NamespaceUtils.uri(aAnnotation.value())), aClass);
		}
	}

	/**
	 * Create an instance of the specified class and instantiate it's data from the given data source using the RDF
	 * instance specified by the given URI
	 * @param theClass the class to create
	 * @param theURI the URI of the RDF individual containing the data for the new instance
	 * @param theSource the KB to get the RDF data from
	 * @param <T> the type of the instance to create
	 * @return a new instance
	 * @throws InvalidRdfException thrown if the class does not support RDF JPA operations, or does not provide sufficient access to its fields/data.
	 * @throws DataSourceException thrown if there is an error while retrieving data from the graph
	 */
	public static <T> T fromRdf(Class<T> theClass, java.net.URI theURI, DataSource theSource) throws InvalidRdfException, DataSourceException {
		T aObj = null;

		try {
			aObj = theClass.newInstance();
			asSupportsRdfId(aObj).setRdfId(theURI);
		}
		catch (InstantiationException e) {
			throw new InvalidRdfException("Cannot create instance of bean, should have a default constructor.", e);
		}
		catch (IllegalAccessException e) {
			throw new InvalidRdfException("Could not access default constructor for class: " + theClass, e);
		}

		return fromRdf(aObj, theURI, theSource);
	}

	/**
	 * Populate the fields of the current instance from the RDF indiviual with the given URI
	 * @param theObj the Java object to populate
	 * @param theURI the URI of the equivalent RDF individual
	 * @param theSource the KB to get the RDF data from
	 * @param <T> the type of the class being populated
	 * @return theObj, populated from the specified DataSource
	 * @throws InvalidRdfException thrown if the object does not support the RDF JPA API.
	 * @throws DataSourceException thrown if there is an error retrieving data from the database
	 */
	public static <T> T fromRdf(T theObj, java.net.URI theURI, DataSource theSource) throws InvalidRdfException, DataSourceException {
		if (OBJECT_M.containsKey(theURI)) {
			// TODO: this is probably a safe cast, i dont see how something w/ the same URI, which should be the same
			// object would change types
			return (T) OBJECT_M.get(theURI);
		}
		else {
			OBJECT_M.put(theURI, theObj);
		}

		ExtendedGraph aGraph = new ExtendedGraph(theSource.describe(theURI));

		if (aGraph.numStatements() == 0) {
			OBJECT_M.remove(theURI);

			return null;
		}

		URI aInd = aGraph.getSesameValueFactory().createURI(theURI);
		Set<URI> aProps = new HashSet<URI>();
		StatementIterator sIter = aGraph.getStatements(aInd, null, null);

		while (sIter.hasNext()) {
			aProps.add(sIter.next().getPredicate());
		}
		sIter.close();

		Collection<Field> aFields = getAnnotatedFields(theObj.getClass());
		Collection<Method> aMethods = getAnnotatedSetters(theObj.getClass(), true);

		addNamespaces(theObj.getClass());

		final Map<URI, Object> aAccessMap = new HashMap<URI, Object>();
		
		CollectionUtil.each(aFields, new AbstractDataCommand<Field>() {
			public void execute() {
				aAccessMap.put(SesameValueFactory.instance().createURI(NamespaceUtils.uri(getData().getAnnotation(RdfProperty.class).value())),
							  getData());
			}
		});

		CollectionUtil.each(aMethods, new AbstractDataCommand<Method>() {
			public void execute() {
				RdfProperty aAnnotation = getAnnotation(getData(), RdfProperty.class);
				if (aAnnotation != null) {
					aAccessMap.put(SesameValueFactory.instance().createURI(NamespaceUtils.uri(aAnnotation.value())),
								   getData());
				}
			}
		});

		for (URI aProp : aProps) {
			Object aAccess = aAccessMap.get(aProp);

			if (aAccess == null && URIImpl.RDF_TYPE.equals(aProp)) {
				// we can skip the rdf:type property.  it's basically assigned in the @RdfsClass annotation on the
				// java class, so we can figure it out later if need be. TODO: of course, if something has multiple types
				// that information is lost, which is not good.
				// TODO: we should however make sure this rdf:type matches the rdf:type on the Java class
				continue;
			}
			else if (aAccess == null) {
				// TODO: this is a lossy transformation, there's rdf data which is not represented by a field on the java class
				// so if we don't convert it into something on the java bean, they don't have a full representation of
				// what was in the database AND if they save that back to the database, they will lose this information
				// that is not good either.
				continue;
			}

			ToObjectFunction aFunc = new ToObjectFunction(theSource, aInd, aAccess, aProp);

			Object aValue = aFunc.apply(CollectionUtil.list(aGraph.getValues(aInd, aProp)));

			boolean aOldAccess = isAccessible(aAccess);

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

		OBJECT_M.remove(theURI);
		
		return theObj;
	}

	/**
	 * Return whether or not the given accessor, either a {@link Field} or a {@link Method} is accessible
	 * @param theAccess the Field or Method
	 * @return true if its accessible, false otherwise, or if it is not a Field or Method
	 */
	private static boolean isAccessible(Object theAccess) {
		if (theAccess instanceof Field) {
			return ((Field)theAccess).isAccessible();
		}
		else if (theAccess instanceof Method) {
			return ((Method)theAccess).isAccessible();
		}
		else {
			return false;
		}
	}

	/**
	 * Set the specified value on the the given object using the provided accessor, either a {@link Field} or a
	 * {@link Method}.  If the accessor is neither a field or a method, then no operation takes place.
	 * @param theAccessor the accessor, a Field or a Method
	 * @param theObj the object to set the value on
	 * @param theValue the value to set via the aceessor
	 * @throws InvocationTargetException thrown if there was an error setting the value on the field/method
	 * @throws IllegalAccessException thrown if you cannot access the field/method
	 */
	private static void set(Object theAccessor, Object theObj, Object theValue) throws InvocationTargetException, IllegalAccessException {
		if (theAccessor instanceof Field) {
			((Field) theAccessor).set(theObj, theValue);
		}
		else if (theAccessor instanceof Method) {
			((Method) theAccessor).invoke(theObj, theValue);
		}
	}

	/**
	 * Toggle the accessibility of the parameter, which should be an instance of {@link Field} or {@link Method}.  If
	 * not, then nothing will occur.
	 * @param theAccessor the object toggle accessibility of
	 * @param theAccess the new accessibility level, true to make it accessible, false otherwise
	 */
	private static void setAccessible(Object theAccessor, boolean theAccess) {
		if (theAccessor instanceof Field) {
			((Field)theAccessor).setAccessible(theAccess);
		}
		else if (theAccessor instanceof Method) {
			((Method)theAccessor).setAccessible(theAccess);
		}
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
			throw new InvalidRdfException("Object does not implements SupportsRdfId, anonymous instances are not supported.");
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
	private static URI id(Object theObj) throws InvalidRdfException {
		SupportsRdfId aSupport = asSupportsRdfId(theObj);

		if (aSupport.getRdfId() != null) {
			return SesameValueFactory.instance().createURI(aSupport.getRdfId());
		}

		Field aIdField = null;

		for (Field aField : theObj.getClass().getDeclaredFields()) {
			if (aField.getAnnotation(RdfId.class) != null) {
				if (aIdField != null) {
					throw new InvalidRdfException("Cannot have multiple id properties");
				}
				else {
					aIdField = aField;
				}
			}
		}

		String aValue = hash(BasicUtils.getRandomString(10));
		String aNS = RdfId.DEFAULT;

		URI aURI = SesameValueFactory.instance().createURI(aNS + aValue);

		if (aIdField != null && !aIdField.getAnnotation(RdfId.class).namespace().equals("")) {
			aNS = aIdField.getAnnotation(RdfId.class).namespace();
		}

		if (aIdField != null) {
			RdfId aIdAnnotation = aIdField.getAnnotation(RdfId.class);

			boolean aOldAccess = aIdField.isAccessible();
			aIdField.setAccessible(true);

			try {
				if (aIdField.get(theObj) == null) {
					throw new InvalidRdfException("id field must have a value");
				}

				Object aValObj = aIdField.get(theObj);

				if (aValObj instanceof java.net.URI || BasicUtils.isURI(aValObj.toString())) {
					aURI = SesameValueFactory.instance().createURI(java.net.URI.create(aValObj.toString()));
				}
				else {
					aValue = hash(aValObj);

					aURI = SesameValueFactory.instance().createURI(aNS + aValue);
				}
			}
			catch (IllegalAccessException ex) {
				throw new InvalidRdfException(ex);
			}

			aIdField.setAccessible(aOldAccess);
		}

		aSupport.setRdfId(java.net.URI.create(aURI.getURI()));

		return aURI;
	}

	/**
	 * Scan the object for {@link Namespaces} annotations and add them to the current list of known namespaces
	 * @param theObj the object to scan.
	 */
	private static void addNamespaces(Class theObj) {
		Namespaces aNS = (Namespaces) theObj.getAnnotation(Namespaces.class);

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
	 * Return the list of annotated setter methods on the class.  Anything with an {@link RdfProperty} annotation
	 * that returns a (non-void) value will be found by this method.  When the infer flag is set to true, we will
	 * inspect the getter methods, if there is a annotated getter, but no annotated setter for a property, we'll infer
	 * the annotation for the property so you get the expected paired behavior of the annotation.
	 * @param theClass the class
	 * @param theInfer true to infer setters from annotated getters, false otherwise.
	 * @return the list of annotated setter methods
	 */
	private static Collection<Method> getAnnotatedSetters(Class theClass, boolean theInfer) {
		Collection<Method> aMethods = new HashSet<Method>();

		for (Method aMethod : theClass.getDeclaredMethods()) {
			if (aMethod.getAnnotation(RdfProperty.class) != null
				&& (aMethod.getGenericReturnType().equals(Void.class) || aMethod.getGenericReturnType().toString().equals("void"))
				&& aMethod.getParameterTypes().length == 1) {

				aMethods.add(aMethod);
			}
		}

		// now let's infer setters.  if you only applied the annotation to the getter, we'll carry it over to the
		// setter if one exists.

		if (theInfer) {
			Collection<Method> aGetters = getAnnotatedGetters(theClass, false);
			for (Method aGetterMethod : aGetters) {
				String aSetterName = aGetterMethod.getName().replaceFirst("get", "set");
				boolean tryIs = false;

				try {
					Method aSetter = theClass.getMethod(aSetterName, aGetterMethod.getReturnType());

					// so we have a setter for a annotated getter, so here we will add this to the list
					// of setters to infer the annotation on the setter even though its not explicit

					if (!aMethods.contains(aSetter)) {
						aMethods.add(aSetter);
					}
				}
				catch (NoSuchMethodException e) {
					// no biggie, setter doesn't exist, we'll just move on, try looking for isXXXX
					tryIs = true;
				}

				if (tryIs) {
					try {
						Method aSetter = theClass.getMethod(aGetterMethod.getName().replaceFirst("is", "set"), aGetterMethod.getReturnType());

						// so we have a setter for a annotated getter, so here we will add this to the list
						// of setters to infer the annotation on the setter even though its not explicit

						if (!aMethods.contains(aSetter)) {
							aMethods.add(aSetter);
						}
					}
					catch (NoSuchMethodException e) {
						// no biggie, setter doesn't exist, we'll just move on
					}
				}
			}
		}

		return aMethods;
	}

	/**
	 * Return the list of annotated get style methods from the class.  Anything w/ an {@link RdfProperty} annotation
	 * that does not return a value will be found by this method.  When the inter flag is set to true, we will inspect
	 * the setter methods, if there is an annotated setter, but no annotatted getter for a property, we'll infer
	 * the annotation for the property so you get the expected paired behavior of the annotation.
	 * @param theClass the class
	 * @param theInfer true to infer getters from annotated setters, false otherwise.
	 * @return the list of annotated get methods
	 */
	private static Collection<Method> getAnnotatedGetters(Class theClass, boolean theInfer) {
		Collection<Method> aMethods = new HashSet<Method>();

		for (Method aMethod : theClass.getDeclaredMethods()) {
			if (aMethod.getAnnotation(RdfProperty.class) != null
				&& !aMethod.getGenericReturnType().equals(Void.class)
				&& aMethod.getParameterTypes().length == 0) {

				aMethods.add(aMethod);
			}
		}

		if (theInfer) {
			Collection<Method> aSetters = getAnnotatedSetters(theClass, false);
			for (Method aSetterMethod : aSetters) {
				String aGetterName = aSetterMethod.getName().replaceFirst("set", "get");
				boolean tryIs = false;

				try {
					Method aGetter = theClass.getMethod(aGetterName);

					// so we have a setter for a annotated getter, so here we will add this to the list
					// of setters to infer the annotation on the setter even though its not explicit

					if (!aMethods.contains(aGetter)) {
						aMethods.add(aGetter);
					}
				}
				catch (NoSuchMethodException e) {
					// no biggie, setter doesn't exist, we'll just move on
					tryIs = true;
				}

				if (tryIs) {
					// didn't find a getter w/ the getXXX form, so lets try isXXXX which I think is normal (valid)
					// convention for boolean properties
					aGetterName = aSetterMethod.getName().replaceFirst("set", "is");

					try {
						Method aGetter = theClass.getMethod(aGetterName);

						// so we have a setter for a annotated getter, so here we will add this to the list
						// of setters to infer the annotation on the setter even though its not explicit

						if (!aMethods.contains(aGetter)) {
							aMethods.add(aGetter);
						}
					}
					catch (NoSuchMethodException e) {
						// no biggie, setter doesn't exist, we'll just move on
					}
				}
			}
		}

		return aMethods;
	}

	/**
	 * Return a list of all the fields on the given class which are annotated with the {@link RdfProperty} annotation.
	 * @param theClass the class to scan
	 * @return the list of annotated fields on the class
	 */
	private static Collection<Field> getAnnotatedFields(Class theClass) {
		Collection<Field> aProps = new HashSet<Field>();

		for (Field aField : theClass.getDeclaredFields()) {
			if (aField.getAnnotation(RdfProperty.class) != null) {
				aProps.add(aField);
			}
		}

		return aProps;
	}

	/**
	 * Return the given Java bean as a set of RDF triples
	 * @param theObj the object
	 * @return the object represented as RDF triples
	 * @throws InvalidRdfException thrown if the object cannot be transformed into RDF.
	 */
	public static ExtendedGraph asRdf(Object theObj) throws InvalidRdfException {
		RdfsClass aClass = asRdfClass(theObj);

		URI aURI = id(theObj);

		addNamespaces(theObj.getClass());

		GraphBuilder aBuilder = new GraphBuilder();

		Collection aAccessors = getAnnotatedFields(theObj.getClass());
		aAccessors.addAll(getAnnotatedGetters(theObj.getClass(), true));

		try {
			ResourceBuilder aRes = aBuilder.instance(aBuilder.getSesameValueFactory().createURI(NamespaceUtils.uri(aClass.value())),
													 aURI.getURI());

			AsValueFunction aFunc = new AsValueFunction();
			for (Object aAccess : aAccessors) {

				RdfProperty aPropertyAnnotation = getAnnotation(aAccess, RdfProperty.class);
				URI aProperty = aBuilder.getSesameValueFactory().createURI(NamespaceUtils.uri(aPropertyAnnotation.value()));

				boolean aOldAccess = isAccessible(aAccess);
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
	 * Get the value on the object from the specified accessor, either a {@link Field} or {@link Method}
	 * @param theAccess the accessor
	 * @param theObject the object to get a value from
	 * @return the value of the field or (getter) method on an object.
	 * @throws IllegalAccessException thrown if you cannot access the field or method
	 * @throws InvocationTargetException thrown if there is an error while invoking the getter method.  Usually because
	 * it's not a bean-style getter w/ no parameters.
	 */
	private static Object get(Object theAccess, Object theObject) throws IllegalAccessException, InvocationTargetException {
		if (theAccess instanceof Field) {
			return ((Field)theAccess).get(theObject);
		}
		else if (theAccess instanceof Method) {
			return ((Method)theAccess).invoke(theObject);
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

		public ToObjectFunction(final DataSource theSource, URI theURI, final Object theField, final URI theProp) {
			valueToObject = new ValueToObject(theSource, theURI, theField, theProp);

			mField = theField;
		}

		public Object apply(final Collection<Value> theIn) {
			if (theIn == null || theIn.isEmpty()) {
				return null;
			}
			else if (Collection.class.isAssignableFrom(classFrom(mField))) {
				try {
					Collection aValues = instantiateCollectionFromField(classFrom(mField));

					for (Value aValue : theIn) {
						aValues.add(valueToObject.apply(aValue));
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

	private static Collection instantiateCollectionFromField(Class theValueType) {
		try {
			// try creating a new instance.  this will work if they've specified a concrete type
			return (Collection) theValueType.newInstance();
		}
		catch (Throwable e) {
			// TODO: make this less brittle -- should we have some sort of facade collection or something in front
			// that we generate at runtime?  or is there a better way to handle this situation?

			// if the above failed, that means the type of the field is something like List, or Set, which is not
			// directly instantiable.  If it's a known type, we'll hand instantiate something here.
			if (List.class.isAssignableFrom(theValueType)) {
				return new ArrayList();
			}
			else if (Set.class.isAssignableFrom(theValueType)) {
				if (SortedSet.class.isAssignableFrom(theValueType)) {
					return new TreeSet();
				}
				else {
					return new LinkedHashSet();
				}
			}
			else {
				// last option is Map, but i dunno what the hell to do in that case, it doesn't map to our use here.
				throw new RuntimeException("Unknown or unsupported collection type for a field: " + theValueType);
			}
		}
	}

	public static class ValueToObject implements Function<Value, Object> {
		static final List<String> integerTypes = Arrays.asList(XmlSchema.INT, XmlSchema.INTEGER, XmlSchema.POSITIVE_INTEGER,
													  XmlSchema.NEGATIVE_INTEGER, XmlSchema.NON_NEGATIVE_INTEGER,
													  XmlSchema.NON_POSITIVE_INTEGER, XmlSchema.UNSIGNED_INT);
		static final List<String> longTypes = Arrays.asList(XmlSchema.LONG, XmlSchema.UNSIGNED_LONG);
		static final List<String> floatTypes = Arrays.asList(XmlSchema.FLOAT, XmlSchema.DECIMAL);
		static final List<String> shortTypes = Arrays.asList(XmlSchema.SHORT, XmlSchema.UNSIGNED_SHORT);
		static final List<String> byteTypes = Arrays.asList(XmlSchema.BYTE, XmlSchema.UNSIGNED_BYTE);

		private URI mProperty;
		private Object mAccessor;
		private DataSource mSource;
		private URI mURI;

		public ValueToObject(final DataSource theSource, URI theURI, final Object theAccessor, final URI theProp) {
			mURI = theURI;
			mSource = theSource;
			mAccessor = theAccessor;
			mProperty = theProp;
		}

		public Object apply(final Value theValue) {
			if (theValue instanceof Literal) {
				Literal aLit = (Literal) theValue;
				String aDatatype = aLit.getDatatype() != null ? aLit.getDatatype().getURI() : null;
				if (aDatatype == null || XmlSchema.STRING.equals(aDatatype)) {
					return aLit.getLabel();
				}
				else if (XmlSchema.BOOLEAN.equals(aDatatype)) {
					return Boolean.valueOf(aLit.getLabel());
				}
				else if (integerTypes.contains(aDatatype)) {
					return Integer.parseInt(aLit.getLabel());
				}
				else if (longTypes.contains(aDatatype)) {
					return Long.parseLong(aLit.getLabel());
				}
				else if (XmlSchema.DOUBLE.equals(aDatatype)) {
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
				else if (XmlSchema.ANYURI.equals(aDatatype)) {
					return java.net.URI.create(aLit.getLabel());
				}
				else if (XmlSchema.DATE.equals(aDatatype) || XmlSchema.DATETIME.equals(aDatatype)) {
					return BasicUtils.asDate(aLit.getLabel());
				}
				else if (XmlSchema.TIME.equals(aDatatype)) {
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
				// TODO: make this less fragile
				
				if (mAccessor != null && Collection.class.isAssignableFrom(classFrom(mAccessor))) {
					// the field takes a collection, lets create a new instance of said collection, and hopefully the
					// bnode is a list.  this approach will only work if the property is a singleton value, eg
					// :inst someProperty _:a where _:a is the head of a list.  if you have another value _:b for
					// some property on :inst, we don't have any way of figuring out which one you're talking about
					// since bnode id references are not guaranteed to be stable in SPARQL, ie just because its id "a"
					// in the result set, does not mean i can do another query for _:a and get the expected results.
					// and you can't do a describe for the same reason.


					try {
						String aQuery = getBNodeConstructQuery(mSource, mURI, mProperty);
						
						ExtendedGraph aGraph = new ExtendedGraph(mSource.graphQuery(aQuery));
						Resource aPossibleListHead = (Resource) aGraph.getValue(mURI, mProperty);
						if (aGraph.isList(aPossibleListHead)) {
							List aList = aGraph.asList(aPossibleListHead);

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
					Class aClass = classFrom(mAccessor);

					// TODO: this is brittle =(
					if (java.net.URI.class.isAssignableFrom(aClass)) {
						return java.net.URI.create(aURI.getURI());
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

					if (aClass.getAnnotation(RdfsClass.class) == null) {
						// k, so either the parameter of the collection or the declared type of the field does
						// not map to an instance/bean type.  this is most likely an error, but lets try and find
						// the rdf:type of the field, and see if we can map that to a class in the path and we'll
						// create an instance of that.  that will work, and pushes the likely failure back off to
						// the assignment of the created instance

						URI aType = getType(mSource, aURI);

						// k, so now we know the type, if we can match the type to a class then we're in business
						if (aType != null) {
							Class aTypeClass = TYPE_TO_CLASS.get(aType);
							if (aTypeClass != null && aTypeClass.isAnnotationPresent(RdfsClass.class)) {
								// lets try this one
								aClass = aTypeClass;
							}
						}
					}

					return fromRdf(aClass, java.net.URI.create(aURI.getURI()), mSource);
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

	private static URI getType(DataSource theSource, URI theConcept) {
		try {
			return new ExtendedGraph(theSource.describe(java.net.URI.create(theConcept.getURI()))).getType(theConcept);
		}
		catch (DataSourceException e) {
			// todo: log me
			return null;
		}
	}

	private static String getBNodeConstructQuery(DataSource theSource, URI theURI, URI theProperty) {
		String aQuery = "construct * from {<" + theURI.getURI() + ">} <"+theProperty.getURI()+"> {o}, {o} po {oo}";

		if (theSource.getQueryFactory().getDialect() == SerqlDialect.instance()) {
			return aQuery;
		}
		else {
			// TODO: little less hacky here, we're just assuming/hoping at this point that they support sparql.  which
			// will most likely be the case, but possibly not always.
			try {
				// TODO: actually write the sparql query here rather than converting from serql.  i just dont feel like
				// looking up the sparql syntax and i dont remember it off thetype of my head.
				return SesameQueryUtils.convertQuery(aQuery, "serql", "sparql");
			}
			catch (MalformedQueryException e) {
				// cannot happen, i hope =)
				throw new RuntimeException(e);
			}
		}
	}

    private static boolean isPrimitive(Object theObj) {
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
                return SesameValueFactory.instance().createLiteral(theIn.toString());
            }
			else if (Boolean.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Boolean.class.cast(theIn));
			}
			else if (Integer.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Integer.class.cast(theIn));
			}
			else if (Long.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Long.class.cast(theIn));
			}
			else if (Short.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Short.class.cast(theIn));
			}
			else if (Double.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Double.class.cast(theIn));
			}
			else if (Float.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Float.class.cast(theIn));
			}
			else if (Date.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createDatetimeTypedLiteral(Date.class.cast(theIn));
			}
			else if (String.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(String.class.cast(theIn));
			}
			else if (Character.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Character.class.cast(theIn));
			}
			else if (java.net.URI.class.isInstance(theIn)) {
				//return SesameValueFactory.instance().createLiteral(theIn.toString(), SesameValueFactory.instance().createURI(XmlSchema.ANYURI));
                return SesameValueFactory.instance().createURI(theIn.toString());
			}
			else if (Value.class.isAssignableFrom(theIn.getClass())) {
				return Value.class.cast(theIn);
			}
			else if (theIn.getClass().getAnnotation(RdfsClass.class) != null) {
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
