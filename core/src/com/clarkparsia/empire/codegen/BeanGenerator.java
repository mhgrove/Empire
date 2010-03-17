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

package com.clarkparsia.empire.codegen;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.rio.RDFFormat;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static com.clarkparsia.utils.collections.CollectionUtil.filter;
import static com.clarkparsia.utils.collections.CollectionUtil.set;
import static com.clarkparsia.utils.collections.CollectionUtil.transform;

import com.clarkparsia.utils.collections.MultiIterator;

import com.clarkparsia.utils.Predicate;
import com.clarkparsia.utils.NamespaceUtils;
import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.FunctionUtil;
import com.clarkparsia.utils.BasicUtils;

import com.clarkparsia.utils.io.IOUtil;

import static com.clarkparsia.utils.FunctionUtil.compose;
import com.clarkparsia.openrdf.ExtRepository;
import com.clarkparsia.openrdf.OpenRdfUtil;
import com.clarkparsia.openrdf.SesameQuery;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import java.net.URL;

/**
 * <p>Generate a set of Java beans which are compatible with Empire from a given RDF schema, OWL ontology, or blob
 * of RDF data.  The generated source code will map to the domain represented in the RDF.</p>
 *
 * @author Michael Grove
 * @since 0.5.1
 * @version 0.6.2
 */
public class BeanGenerator {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(BeanGenerator.class);

	/**
	 * String URI constant for the owl:Thing conccept
	 */
	private static final URI OWL_THING = ValueFactoryImpl.getInstance().createURI(OWL.NAMESPACE + "Thing");

	/**
	 * The list of xsd datatypes which map to Integer
	 */
	private static final List<URI> integerTypes = Arrays.asList(XMLSchema.INT, XMLSchema.INTEGER, XMLSchema.POSITIVE_INTEGER,
														   XMLSchema.NEGATIVE_INTEGER, XMLSchema.NON_NEGATIVE_INTEGER,
														   XMLSchema.NON_POSITIVE_INTEGER, XMLSchema.UNSIGNED_INT);

	/**
	 * The list of xsd datatypes which map to Long
	 */
	private static final List<URI> longTypes = Arrays.asList(XMLSchema.LONG, XMLSchema.UNSIGNED_LONG);

	/**
	 * The list of xsd datatypes which map to Float
	 */
	private static final List<URI> floatTypes = Arrays.asList(XMLSchema.FLOAT, XMLSchema.DECIMAL);

	/**
	 * The list of xsd datatypes which map to Short
	 */
	private static final List<URI> shortTypes = Arrays.asList(XMLSchema.SHORT, XMLSchema.UNSIGNED_SHORT);

	/**
	 * The list of xsd datatypes which map to Byte
	 */
	private static final List<URI> byteTypes = Arrays.asList(XMLSchema.BYTE, XMLSchema.UNSIGNED_BYTE);

	private static final Map<Resource, String> NAMES = new HashMap<Resource, String>();
	private static final Map<String, Integer> NAMES_TO_COUNT = new HashMap<String, Integer>();

	/**
	 * Return the Java bean source code that represents the given RDF class
	 * @param thePackageName the name of the package the source will be in
	 * @param theGraph the repository containing information about the class
	 * @param theClass the class that is to be turned into Java source
	 * @param theMap the map of classes to the properties in their domain
	 * @return a string of the source code of the equivalent Java bean
	 * @throws Exception if there is an error while converting
	 */
	private static String toSource(String thePackageName, ExtRepository theGraph, Resource theClass, Map<Resource, Collection<URI>> theMap) throws Exception {
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
	private static String functionType(final ExtRepository theGraph, final URI theProp) throws Exception {
		String aType;

		URI aRange = (URI) theGraph.getValue(theProp, RDFS.RANGE);

		if (aRange == null) {
			// no explicit range, try to infer it...
			try {
				TupleQueryResult aResults = theGraph.selectQuery(SesameQuery.serql("select distinct r from {s} <"+theProp+"> {o}, {o} rdf:type {r}"));

				if (aResults.hasNext()) {
					URI aTempRange = (URI) aResults.next().getValue("r");
					if (!aResults.hasNext()) {
						aRange = aTempRange;
					}
					else {
						// TODO: leave range as null, the property is used for things of multiple different values.  so here
						// we should try and find the superclass of all the values and use that as the range.
					}
				}

				aResults.close();

				if (aRange == null) {
					// could not get it from type usage, so maybe its a literal and we can guess it from datatype

					aResults = theGraph.selectQuery(SesameQuery.serql("select distinct datatype(o) as dt from {s} <"+theProp+"> {o} where isLiteral(o)"));

					if (aResults.hasNext()) {
						URI aTempRange = null;
						while (aTempRange == null && aResults.hasNext()) {
							Literal aLit = (Literal) aResults.next().getValue("o");
							if (aLit != null){
								aTempRange = aLit.getDatatype();
							}
						}
						
						if (!aResults.hasNext()) {
							aRange = aTempRange;
						}
						else {
							// TODO: do something here, literals of multiple types used
						}
					}

					aResults.close();
				}
			}
			catch (Exception e) {
				// don't worry about it
				e.printStackTrace();
			}
		}

		if (XMLSchema.STRING.equals(aRange) || RDFS.LITERAL.equals(aRange)) {
			aType = "String";
		}
		else if (XMLSchema.BOOLEAN.equals(aRange)) {
			aType = "Boolean";
		}
		else if (integerTypes.contains(aRange)) {
			aType = "Integer";
		}
		else if (longTypes.contains(aRange)) {
			aType = "Long";
		}
		else if (XMLSchema.DOUBLE.equals(aRange)) {
			aType = "Double";
		}
		else if (floatTypes.contains(aRange)) {
			aType = "Float";
		}
		else if (shortTypes.contains(aRange)) {
			aType = "Short";
		}
		else if (byteTypes.contains(aRange)) {
			aType = "Byte";
		}
		else if (XMLSchema.ANYURI.equals(aRange)) {
			aType = "java.net.URI";
		}
		else if (XMLSchema.DATE.equals(aRange) || XMLSchema.DATETIME.equals(aRange)) {
			aType = "Date";
		}
		else if (XMLSchema.TIME.equals(aRange)) {
			aType = "Date";
		}
		else if (aRange == null || aRange.equals(OWL_THING)) {
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
	private static boolean isCollection(final ExtRepository theGraph, final URI theProp) throws Exception {
		// TODO: this is not fool proof.

		String aCardQuery = "select distinct card from " +
					   "{s} rdf:type {owl:Restriction}, " +
					   "{s} owl:onProperty {<"+theProp+">}, " +
					   "{s} cardProp {card} " +
					   "where cardProp = owl:cardinality or cardProp = owl:minCardinality or cardProp = owl:maxCardinality";

		TupleQueryResult aResults = theGraph.selectQuery(SesameQuery.serql(aCardQuery));
		if (aResults.hasNext()) {
			Literal aCard = (Literal) aResults.next().getValue("card") ;

			try {
				return Integer.parseInt(aCard.getLabel()) > 1;
			}
			catch (NumberFormatException e) {
				LOGGER.error("Unparseable cardinality value for '" + theProp + "' of '" + aCard + "'", e);
			}
		}

		aResults.close();

		try {
			aResults = theGraph.selectQuery(SesameQuery.serql("select distinct s from {s} <"+theProp+"> {o}"));
			for (BindingSet aBinding : OpenRdfUtil.iterable(aResults)) {
				Collection aCollection = set(theGraph.getValues( (Resource) aBinding.getValue("s"), theProp));
				if (aCollection.size() > 1) {
					return true;
				}
			}

			return false;
		}
		finally {
			aResults.close();
		}
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

		return aProps;
	}

	/**
	 * Given a Resource, return the Java class name for that resource
	 * @param theClass the resource
	 * @return the name of the Java class
	 */
	private static String className(Resource theClass) {
		if (NAMES.containsKey(theClass)) {
			return NAMES.get(theClass);
		}

		String aLabel;

		if (theClass instanceof URI) {
			aLabel = NamespaceUtils.getLocalName(theClass.toString());
		}
		else {
			aLabel = theClass.stringValue();
		}

		aLabel = String.valueOf(aLabel.charAt(0)).toUpperCase() + aLabel.substring(1);

		aLabel = aLabel.replaceAll(" ", "");

		if (NAMES_TO_COUNT.containsKey(aLabel)) {
			String aNewLabel = aLabel + NAMES_TO_COUNT.get(aLabel);

			NAMES_TO_COUNT.put(aLabel, NAMES_TO_COUNT.get(aLabel)+1);

			aLabel = aNewLabel;
		}
		else {
			NAMES_TO_COUNT.put(aLabel, 0);
		}

		NAMES.put(theClass, aLabel);

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
		NAMES_TO_COUNT.clear();

		ExtRepository aRepository = new ExtRepository();
		
		aRepository.initialize();

		aRepository.read(theOntology.openStream(), theFormat);

		Collection<Resource> aClasses = transform(new MultiIterator<Statement>(OpenRdfUtil.toIterator(aRepository.getStatements(null, RDF.TYPE, RDFS.CLASS)),
																			   OpenRdfUtil.toIterator(aRepository.getStatements(null, RDF.TYPE, OWL.CLASS))),
												  new StatementToSubject());

		Collection<Resource> aIndClasses = transform(OpenRdfUtil.toIterator(aRepository.getStatements(null, RDF.TYPE, null)),
													 compose(new StatementToObject(),
															 new FunctionUtil.Cast<Value, Resource>(Resource.class)));

		aClasses.addAll(aIndClasses);

		aClasses = filter(aClasses, new Predicate<Resource>() {
			public boolean accept(final Resource theValue) {
				return !theValue.stringValue().startsWith(RDFS.NAMESPACE)
					   && !theValue.stringValue().startsWith(RDF.NAMESPACE)
					   && !theValue.stringValue().startsWith(OWL.NAMESPACE);
			}
		});

		Map<Resource, Collection<URI>> aMap = new HashMap<Resource, Collection<URI>>();

		for (Resource aClass : aClasses) {
			Collection<URI> aProps = new HashSet<URI>(transform(OpenRdfUtil.toIterator(aRepository.getStatements(null, RDFS.DOMAIN, aClass)),
																compose(new StatementToSubject(),
																		new FunctionUtil.Cast<Resource, URI>(URI.class))));

			// infer properties based on usage in actual instance data
			for (BindingSet aBinding : OpenRdfUtil.iterable(aRepository.selectQuery(SesameQuery.serql("select distinct p from {s} rdf:type {<" + aClass + ">}, {s} p {o}")))) {
				aProps.add( (URI) aBinding.getValue("p"));
			}

			// don't include rdf:type as a property
			aProps = filter(aProps, new Predicate<URI>() {
				public boolean accept(final URI theValue) {
					return !RDF.TYPE.equals(theValue);
				}
			});

			aMap.put(aClass, aProps);
		}

		if (!theDirToSave.exists()) {
			if (!theDirToSave.mkdirs()) {
				throw new IOException("Could not create output directory");
			}
		}

		for (Resource aClass :  aMap.keySet()) {
			String aSrc = toSource(thePackageName, aRepository, aClass, aMap);

			if (aSrc == null) {
				continue;
			}

			File aFile = new File(theDirToSave, className(aClass) + ".java");

			System.out.println("Writing source to file: " + aFile.getName());

			IOUtil.writeStringToFile(aSrc, aFile);
		}
	}

	public static void main(String[] args) throws Exception {
		//aGraph.read(new URL("http://xmlns.com/foaf/spec/index.rdf").openStream());
//		File aOut = new File("/Users/mhgrove/work/GitHub/empire/core/src/com/clarkparsia/empire/codegen/test/");
//
//		generateSourceFiles("com.clarkparsia.empire.codegen.test", new File("test/data/nasa.nt").toURI().toURL(), RDFFormat.NTRIPLES, aOut);

		if (args.length < 4) {
			System.err.println("Must provide four arguments to the program, the package name, ontology URL, rdf format of the ontology (rdf/xml|turtle|ntriples), and the output directory for the source code.\n");
			System.err.println("For example:\n");
			System.err.println("\tBeanGenerator my.package.domain /usr/local/files/myontology.ttl turtle /usr/local/code/src/my/package/domain");

			return;
		}

		URL aURL;

		if (BasicUtils.isURL(args[1])) {
			aURL = new URL(args[1]);
		}
		else {
			aURL = new File(args[1]).toURI().toURL();
		}

		generateSourceFiles(args[0], aURL, RDFFormat.valueOf(args[2]), new File(args[3]));
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
