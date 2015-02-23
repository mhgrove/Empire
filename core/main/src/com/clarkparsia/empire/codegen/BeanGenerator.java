/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.clarkparsia.empire.util.Repositories2;
import com.complexible.common.collect.Iterables2;
import com.complexible.common.collect.Iterators2;
import com.complexible.common.openrdf.model.Statements;
import com.complexible.common.openrdf.repository.Repositories;
import com.google.common.collect.Iterables;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.BNode;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import com.complexible.common.openrdf.util.AdunaIterations;

import com.complexible.common.collect.MultiIterator;

import com.complexible.common.net.NetUtils;
import com.complexible.common.base.Functions2;
import com.google.common.base.Predicate;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author	Michael Grove
 * @since	0.5.1
 * @version	0.7.3
 */
public final class BeanGenerator {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BeanGenerator.class);

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
	 * NO instances
	 */
	private BeanGenerator() {
	}

	/**
	 * Return the Java bean source code that represents the given RDF class
	 * @param thePackageName the name of the package the source will be in
	 * @param theGraph the repository containing information about the class
	 * @param theClass the class that is to be turned into Java source
	 * @param theMap the map of classes to the properties in their domain
	 * @return a string of the source code of the equivalent Java bean
	 * @throws Exception if there is an error while converting
	 */
	private static String toSource(final String thePackageName, final Repository theGraph, final Resource theClass, final Map<Resource, Collection<URI>> theMap) throws Exception {
		StringBuffer aSrc = new StringBuffer();

		aSrc.append("package ").append(thePackageName).append(";\n\n");

		aSrc.append("import java.util.*;\n");
		aSrc.append("import javax.persistence.Entity;\n");
		aSrc.append("import com.clarkparsia.empire.SupportsRdfId;\n");
		aSrc.append("import com.clarkparsia.empire.annotation.*;\n\n");

		// TODO: more imports? less?

		Iterable<Resource> aSupers = Iterables2.present(Iterables.transform(AdunaIterations.iterable(Repositories.getStatements(theGraph, theClass, RDFS.SUBCLASSOF, null)),
		                                                                    Statements.objectAsResource()));

		aSrc.append("@Entity\n");
		aSrc.append("@RdfsClass(\"").append(theClass).append("\")\n");
		aSrc.append("public interface ").append(className(theClass));

		aSupers = Collections2.filter(Sets.newHashSet(aSupers), new Predicate<Resource>() {
			public boolean apply(final Resource theValue) {
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
	 * @param theRepo the graph of the ontology/data
	 * @param theProp the property
	 * @return the String representation of the property type
	 * @throws Exception if there is an error querying the data
	 */
	private static String functionType(final Repository theRepo, final URI theProp) throws Exception {
		String aType;

		Resource aRangeRes = Statements.objectAsResource().apply(AdunaIterations.singleResult(Repositories.getStatements(theRepo, theProp, RDFS.RANGE, null)).orNull()).orNull();

		if (aRangeRes instanceof BNode) {
			// we can't handle bnodes very well, so we're just going to assume Object
			return "Object";
		}

		URI aRange = (URI) aRangeRes;

		if (aRange == null) {
			// no explicit range, try to infer it...
			try {
				TupleQueryResult aResults = Repositories.selectQuery(theRepo, QueryLanguage.SERQL, "select distinct r from {s} <"+theProp+"> {o}, {o} rdf:type {r}");

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

					aResults = Repositories.selectQuery(theRepo, QueryLanguage.SERQL, "select distinct datatype(o) as dt from {s} <"+theProp+"> {o} where isLiteral(o)");

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

		if (isCollection(theRepo, theProp)) {
			aType = "Collection<? extends " + aType + ">";
		}

		return aType;
	}

	/**
	 * Determine whether or not the property's range is a collection.  This will inspect both the ontology, for cardinality
	 * restrictions, and when that is not available, it will use the actual structure of the data.
	 * @param theRepo the graph of the ontology/data
	 * @param theProp the property
	 * @return true if the property has a collection as it's value, false if it's just a single valued property
	 * @throws Exception if there is an error querying the data
	 */
	private static boolean isCollection(final Repository theRepo, final URI theProp) throws Exception {
		// TODO: this is not fool proof.

		String aCardQuery = "select distinct ?card where {\n" +
					   "?s rdf:type owl:Restriction.\n" +
					   "?s owl:onProperty <"+theProp+">.\n" +
					   "?s ?cardProp ?card.\n" +
					   "FILTER (?cardProp = owl:cardinality || ?cardProp = owl:minCardinality || ?cardProp = owl:maxCardinality)\n" +
					   "}";
			TupleQueryResult aResults = Repositories.selectQuery(theRepo, QueryLanguage.SPARQL ,aCardQuery);
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
			aResults = Repositories.selectQuery(theRepo, QueryLanguage.SPARQL, "select distinct ?s where  { ?s <"+theProp+"> ?o}");
			for (BindingSet aBinding : AdunaIterations.iterable(aResults)) {

				Collection aCollection = Sets.newHashSet(Iterators2.present(Iterators.transform(AdunaIterations.iterator(Repositories.getStatements(theRepo, (Resource) aBinding.getValue("s"), theProp, null)),
				                                                                                Statements.objectOptional())));
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
			aLabel = ((URI) theClass).getLocalName();
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

		Repository aRepository = Repositories2.createInMemoryRepo();

		Repositories.add(aRepository, theOntology.openStream(), theFormat);

		Collection<Resource> aClasses = Sets.newHashSet(Iterators.transform(new MultiIterator<Statement>(AdunaIterations.iterator(Repositories.getStatements(aRepository, null, RDF.TYPE, RDFS.CLASS)),
																										 AdunaIterations.iterator(Repositories.getStatements(aRepository, null, RDF.TYPE, OWL.CLASS))),
																			new StatementToSubject()));

		aClasses = Collections2.filter(aClasses, new Predicate<Resource>() { public boolean apply(Resource theRes) { return theRes instanceof URI; } });

		Collection<Resource> aIndClasses = Sets.newHashSet(Iterators.transform(AdunaIterations.iterator(Repositories.getStatements(aRepository, null, RDF.TYPE, null)),
		                                                                       Functions.compose(Functions2.<Value, Resource>cast(Resource.class),
		                                                                                         new StatementToObject())));

		aClasses.addAll(aIndClasses);

		aClasses = Collections2.filter(aClasses, new Predicate<Resource>() {
			public boolean apply(final Resource theValue) {
				return !theValue.stringValue().startsWith(RDFS.NAMESPACE)
					   && !theValue.stringValue().startsWith(RDF.NAMESPACE)
					   && !theValue.stringValue().startsWith(OWL.NAMESPACE);
			}
		});

		Map<Resource, Collection<URI>> aMap = new HashMap<Resource, Collection<URI>>();

		for (Resource aClass : aClasses) {
			if (aClass instanceof BNode) { continue; }
			Collection<URI> aProps = Sets.newHashSet(Iterators.transform(AdunaIterations.iterator(Repositories.getStatements(aRepository, null, RDFS.DOMAIN, aClass)),
			                                                             Functions.compose(Functions2.<Resource, URI>cast(URI.class),
			                                                                               new StatementToSubject())));

			// infer properties based on usage in actual instance data
			for (BindingSet aBinding : AdunaIterations.iterable(Repositories.selectQuery(aRepository, QueryLanguage.SPARQL, "select distinct ?p where { ?s rdf:type <" + aClass + ">. ?s ?p ?o }"))) {
				aProps.add( (URI) aBinding.getValue("p"));
			}

			// don't include rdf:type as a property
			aProps = Collections2.filter(aProps, new Predicate<URI>() {
				public boolean apply(final URI theValue) {
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

			Files.write(aSrc, aFile, Charsets.UTF_8);
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

		if (NetUtils.isURL(args[1])) {
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
