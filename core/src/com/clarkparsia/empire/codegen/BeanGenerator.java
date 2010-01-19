package com.clarkparsia.empire.codegen;

import org.openrdf.vocabulary.OWL;
import org.openrdf.vocabulary.RDFS;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.XmlSchema;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;

import org.openrdf.model.impl.URIImpl;

import org.openrdf.sesame.constants.RDFFormat;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.clarkparsia.sesame.repository.ExtendedSesameRepository;

import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;
import com.clarkparsia.sesame.utils.query.SesameQuery;
import com.clarkparsia.sesame.utils.query.Binding;

import com.clarkparsia.sesame.utils.SesameValueFactory;

import static com.clarkparsia.utils.collections.CollectionUtil.filter;
import static com.clarkparsia.utils.collections.CollectionUtil.set;
import static com.clarkparsia.utils.collections.CollectionUtil.transform;

import com.clarkparsia.utils.collections.MultiIterator;

import com.clarkparsia.utils.Predicate;
import com.clarkparsia.utils.NamespaceUtils;
import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.FunctionUtil;

import com.clarkparsia.utils.io.IOUtil;

import static com.clarkparsia.utils.FunctionUtil.compose;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;

import java.io.File;

import java.net.URL;

/**
 * <p>Generate a set of Java beans which are compatible with Empire from a given RDF schema, OWL ontology, or blob
 * of RDF data.  The generated source code will map to the domain represented in the RDF.</p>
 *
 * @author Michael Grove
 */
public class BeanGenerator {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(BeanGenerator.class);

	/**
	 * String URI constant for the owl:Thing conccept
	 */
	private static final String OWL_THING = OWL.NAMESPACE + "Thing";

	/**
	 * The list of xsd datatypes which map to Integer
	 */
	static final List<String> integerTypes = Arrays.asList(XmlSchema.INT, XmlSchema.INTEGER, XmlSchema.POSITIVE_INTEGER,
														   XmlSchema.NEGATIVE_INTEGER, XmlSchema.NON_NEGATIVE_INTEGER,
														   XmlSchema.NON_POSITIVE_INTEGER, XmlSchema.UNSIGNED_INT);

	/**
	 * The list of xsd datatypes which map to Long
	 */
	static final List<String> longTypes = Arrays.asList(XmlSchema.LONG, XmlSchema.UNSIGNED_LONG);

	/**
	 * The list of xsd datatypes which map to Float
	 */
	static final List<String> floatTypes = Arrays.asList(XmlSchema.FLOAT, XmlSchema.DECIMAL);

	/**
	 * The list of xsd datatypes which map to Short
	 */
	static final List<String> shortTypes = Arrays.asList(XmlSchema.SHORT, XmlSchema.UNSIGNED_SHORT);

	/**
	 * The list of xsd datatypes which map to Byte
	 */
	static final List<String> byteTypes = Arrays.asList(XmlSchema.BYTE, XmlSchema.UNSIGNED_BYTE);

	/**
	 * Return the Java bean source code that represents the given RDF class
	 * @param thePackageName the name of the package the source will be in
	 * @param theGraph the repository containing information about the class
	 * @param theClass the class that is to be turned into Java source
	 * @param theMap the map of classes to the properties in their domain
	 * @return a string of the source code of the equivalent Java bean
	 * @throws Exception if there is an error while converting
	 */
	private static String toSource(String thePackageName, ExtendedSesameRepository theGraph, Resource theClass, Map<Resource, Collection<URI>> theMap) throws Exception {
		StringBuffer aSrc = new StringBuffer();

		aSrc.append("package ").append(thePackageName).append(";\n\n");

		aSrc.append("import java.util.*;\n");
		aSrc.append("import javax.persistence.Entity;\n");
		aSrc.append("import com.clarkparsia.empire.SupportsRdfId;\n");
		aSrc.append("import com.clarkparsia.empire.annotation.*;\n\n");

		// TODO: more imports? less?

		Iterable<Resource> aSupers = theGraph.getSuperclasses(theClass);

		aSrc.append("@Entity\n");
		aSrc.append("@RdfsClass(\"").append(theClass).append("\")\n");
		aSrc.append("public interface ").append(className(theClass));

		aSupers = filter(set(aSupers), new Predicate<Resource>() {
			public boolean accept(final Resource theValue) {
				return theValue != null &&
					   !theValue.toString().startsWith(OWL.NAMESPACE)
					   && !theValue.toString().startsWith(RDFS.NAMESPACE)
					   && !theValue.toString().startsWith(RDF.NAMESPACE);
			}
		});

		boolean aNeedsComma = false;
		aSrc.append(" extends");

		if (aSupers.iterator().hasNext()) {
			for (Resource aSuper : aSupers) {
				if (aNeedsComma) {
					aSrc.append(",");
				}
				else {
					aNeedsComma = true;
				}

				aSrc.append(" ").append(className(aSuper));
			}
		}

		if (aNeedsComma) {
			aSrc.append(",");
		}

		aSrc.append(" SupportsRdfId");

		aSrc.append(" { \n\n");

		Collection<URI> aProps = props(theClass, theMap);

		for (URI aProp : aProps) {
			aSrc.append("@RdfProperty(\"").append(aProp).append("\")\n");
			aSrc.append("public ").append(functionType(theGraph, aProp)).append(" get").append(functionName(aProp)).append("();\n");
			aSrc.append("public void set").append(functionName(aProp)).append("(").append(functionType(theGraph, aProp)).append(" theValue);\n\n");
		}

		aSrc.append("}");

		return aSrc.toString();
	}

	/**
	 * Return the type of the function (getter & setter), i.e. the bean property type, for the given rdf:Property
	 * @param theGraph the graph of the ontology/data
	 * @param theProp the property
	 * @return the String representation of the property type
	 * @throws Exception if there is an error querying the data
	 */
	private static String functionType(final ExtendedSesameRepository theGraph, final URI theProp) throws Exception {
		String aType;

		URI aRange = (URI) theGraph.getValue(theProp, URIImpl.RDFS_RANGE);

		if (aRange == null) {
			// no explicit range, try to infer it...
			try {
				IterableQueryResultsTable aResults = theGraph.performSelectQuery(SesameQuery.serql("select distinct r from {s} <"+theProp+"> {o}, {o} rdf:type {r}"));

				Iterator<Binding> aIter = aResults.iterator();
				if (aIter.hasNext()) {
					URI aTempRange = aIter.next().getURI("r");
					if (!aIter.hasNext()) {
						aRange = aTempRange;
					}
					else {
						// TODO: leave range as null, the property is used for things of multiple different values.  so here
						// we should try and find the superclass of all the values and use that as the range.
					}
				}

				if (aRange == null) {
					// could not get it from type usage, so maybe its a literal and we can guess it from datatype

					aResults = theGraph.performSelectQuery(SesameQuery.serql("select distinct datatype(o) from {s} <"+theProp+"> {o} where isLiteral(o) and datatype(o) != null"));

					aIter = aResults.iterator();
					if (aIter.hasNext()) {
						URI aTempRange = aIter.next().getURI("datatype(o)");
						if (!aIter.hasNext()) {
							aRange = aTempRange;
						}
						else {
							// TODO: do something here, literals of multiple types used
						}
					}
				}
			}
			catch (Exception e) {
				// don't worry about it
				e.printStackTrace();
			}
		}

		String aDatatype = aRange != null ? aRange.getURI() : null;

		if (XmlSchema.STRING.equals(aDatatype) || RDFS.LITERAL.equals(aDatatype)) {
			aType = "String";
		}
		else if (XmlSchema.BOOLEAN.equals(aDatatype)) {
			aType = "Boolean";
		}
		else if (integerTypes.contains(aDatatype)) {
			aType = "Integer";
		}
		else if (longTypes.contains(aDatatype)) {
			aType = "Long";
		}
		else if (XmlSchema.DOUBLE.equals(aDatatype)) {
			aType = "Double";
		}
		else if (floatTypes.contains(aDatatype)) {
			aType = "Float";
		}
		else if (shortTypes.contains(aDatatype)) {
			aType = "Short";
		}
		else if (byteTypes.contains(aDatatype)) {
			aType = "Byte";
		}
		else if (XmlSchema.ANYURI.equals(aDatatype)) {
			aType = "java.net.URI";
		}
		else if (XmlSchema.DATE.equals(aDatatype) || XmlSchema.DATETIME.equals(aDatatype)) {
			aType = "Date";
		}
		else if (XmlSchema.TIME.equals(aDatatype)) {
			aType = "Date";
		}
		else if (aDatatype == null || aDatatype.equals(OWL_THING)) {
			aType = "Object";
		}
		else {
			aType = className(aRange);
		}

		if (isCollection(theGraph, theProp)) {
			aType = "Collection<? extends " + aType + ">";
		}

		return aType;
	}

	/**
	 * Determine whether or not the property's range is a collection.  This will inspect both the ontology, for cardinality
	 * restrictions, and when that is not available, it will use the actual structure of the data.
	 * @param theGraph the graph of the ontology/data
	 * @param theProp the property
	 * @return true if the property has a collection as it's value, false if it's just a single valued property
	 * @throws Exception if there is an error querying the data
	 */
	private static boolean isCollection(final ExtendedSesameRepository theGraph, final URI theProp) throws Exception {
		// TODO: this is not fool proof.

		String aCardQuery = "select distinct card from " +
					   "{s} rdf:type {owl:Restriction}, " +
					   "{s} owl:onProperty {<"+theProp+">}, " +
					   "{s} cardProp {card} " +
					   "where cardProp = owl:cardinality or cardProp = owl:minCardinality or cardProp = owl:maxCardinality";

		IterableQueryResultsTable aResults = theGraph.performSelectQuery(SesameQuery.serql(aCardQuery));
		if (aResults.iterator().hasNext()) {
			Literal aCard = aResults.iterator().next().getLiteral("card");

			try {
				return Integer.parseInt(aCard.getLabel()) > 1;
			}
			catch (NumberFormatException e) {
				LOGGER.error("Unparseable cardinality value for '" + theProp + "' of '" + aCard + "'", e);
			}
		}

		for (Binding aBinding : theGraph.performSelectQuery(SesameQuery.serql("select distinct s from {s} <"+theProp+"> {o}"))) {
			Collection c = set(theGraph.getValues(aBinding.getResource("s"), theProp));
			if (c.size() > 1) {
				return true;
			}
		}


		return false;
	}

	/**
	 * Return the name of the function (the bean property) for this rdf:Property
	 * @param theProp the rdf:Property
	 * @return the name of the Java property/function name
	 */
	private static String functionName(final URI theProp) {
		return className(theProp);
	}

	/**
	 * Return all the properties for the given resource.  This will return only the properties which are directly
	 * associated with the class, not any properties from its parent, or otherwise inferred from the data.
	 * @param theRes the resource
	 * @param theMap the map of resources to properties
	 * @return a collection of the proeprties associated with the class
	 */
	private static Collection<URI> props(final Resource theRes, final Map<Resource, Collection<URI>> theMap) {
		Collection<URI> aProps = new HashSet<URI>();

		if (theMap.containsKey(theRes)) {
			aProps.addAll(theMap.get(theRes));
		}

//		for (Resource aSuper : theGraph.getSuperclasses(theRes)) {
//			aProps.addAll(props(theGraph, aSuper, theMap));
//		}

		return aProps;
	}

	/**
	 * Given a Resource, return the Java class name for that resource
	 * @param theClass the resource
	 * @return the name of the Java class
	 */
	private static String className(Resource theClass) {
		String aLabel;

		if (theClass instanceof URI) {
			aLabel = NamespaceUtils.getLocalName(theClass.toString());
		}
		else {
			aLabel = theClass.toString();
		}

		aLabel = String.valueOf(aLabel.charAt(0)).toUpperCase() + aLabel.substring(1);

		aLabel = aLabel.replaceAll(" ", "");

		return aLabel;
	}

	/**
	 * Given an ontology/schema, generate Empire compatible Java beans for each class in the ontology.
	 * @param thePackageName the name of the packages the source should belong to
	 * @param theOntology the location of the ontology to load
	 * @param theFormat the RDF format the ontology is in
	 * @param theDirToSave where to save the generated source code
	 * @throws Exception if there is an error while generating the source
	 */
	public static void generateSourceFiles(String thePackageName, URL theOntology, RDFFormat theFormat, File theDirToSave) throws Exception {
		ExtendedSesameRepository aRepository = new ExtendedSesameRepository();

		aRepository.read(theOntology.openStream(), theFormat);

		Collection<Resource> aClasses = transform(new MultiIterator<Statement>(aRepository.getStatements(null, URIImpl.RDF_TYPE, URIImpl.RDFS_CLASS).iterator(),
																			   aRepository.getStatements(null, URIImpl.RDF_TYPE, SesameValueFactory.instance().createURI(OWL.CLASS))),
												  new StatementToSubject());

		Collection<Resource> aIndClasses = transform(aRepository.getStatements(null, URIImpl.RDF_TYPE, null).iterator(),
													 compose(new StatementToObject(),
															 new FunctionUtil.Cast<Value, Resource>(Resource.class)));

		aClasses.addAll(filter(aIndClasses, new Predicate<Resource>() {
			public boolean accept(final Resource theValue) {
				return !theValue.toString().startsWith(RDFS.NAMESPACE)
					   && !theValue.toString().startsWith(RDF.NAMESPACE)
					   && !theValue.toString().startsWith(OWL.NAMESPACE);
			}
		}));

		Map<Resource, Collection<URI>> aMap = new HashMap<Resource, Collection<URI>>();

		for (Resource aClass : set(aClasses)) {
			Collection<URI> aProps = new HashSet<URI>(transform(aRepository.getStatements(null, URIImpl.RDFS_DOMAIN, aClass),
																compose(new StatementToSubject(),
																		new FunctionUtil.Cast<Resource, URI>(URI.class))));

			// infer properties based on usage in actual instance data
			for (Binding aBinding : aRepository.performSelectQuery(SesameQuery.serql("select distinct p from {s} rdf:type {<" + aClass + ">}, {s} p {o}"))) {
				aProps.add(aBinding.getURI("p"));
			}

			// don't include rdf:type as a property
			aProps = filter(aProps, new Predicate<URI>() {
				public boolean accept(final URI theValue) {
					return !URIImpl.RDF_TYPE.equals(theValue);
				}
			});

			aMap.put(aClass, aProps);
		}

		for (Resource aClass :  aMap.keySet()) {
			String aSrc = toSource(thePackageName, aRepository, aClass, aMap);

			File aFile = new File(theDirToSave, className(aClass) + ".java");

			System.out.println("Writing source to file: " + aFile.getName());

			IOUtil.writeStringToFile(aSrc, aFile);
		}
	}

	public static void main(String[] args) throws Exception {
		//aGraph.read(new URL("http://xmlns.com/foaf/spec/index.rdf").openStream());
		File aOut = new File("/Users/mhgrove/work/GitHub/empire/core/src/com/clarkparsia/empire/codegen/test/");

		generateSourceFiles("com.clarkparsia.empire.codegen.test", new File("test/data/nasa.nt").toURI().toURL(), RDFFormat.NTRIPLES, aOut);
	}

	private static class StatementToObject implements Function<Statement, Value> {
		public Value apply(final Statement theIn) {
			return theIn.getObject();
		}
	}

	private static class StatementToSubject implements Function<Statement, Resource> {
		public Resource apply(final Statement theIn) {
			return theIn.getSubject();
		}
	}
}
