package com.clarkparsia.empire.util;

import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.AnnotationChecker;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.Empire;

import com.clarkparsia.empire.impl.serql.SerqlDialect;
import com.clarkparsia.empire.impl.sparql.SPARQLDialect;

import com.clarkparsia.openrdf.query.builder.QueryBuilder;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;

import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;

import com.clarkparsia.openrdf.query.serql.SeRQLQueryRenderer;

import com.clarkparsia.utils.NamespaceUtils;
import com.clarkparsia.utils.BasicUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.openrdf.model.Resource;
import org.openrdf.model.BNode;

import org.openrdf.model.vocabulary.RDF;

import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.query.parser.ParsedTupleQuery;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import java.util.List;
import java.util.ArrayList;

/**
 * <p>A collection of utility functions for Empire.</p>
 *
 * @author Michael Grove
 * 
 * @since 0.6.1
 * @version 0.7
 */
public class EmpireUtil {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(Empire.class.getName());

	/**
	 * Return the RDF-izable support object's identifier.  This will be either a URI or a BNode.  All java URI/URL objects
	 * and anything whose toString() value is a valid URI will be returned as such.  All other strings are assumed
	 * to be bnode identifiers.
	 * @param theSupport the support object
	 * @return it's identifier as a Sesame Resource
	 */
	public static Resource asResource(SupportsRdfId theSupport) {
		if (theSupport.getRdfId() == null) {
			return null;
		}
		else if (theSupport.getRdfId() instanceof SupportsRdfId.URIKey) {
			return ValueFactoryImpl.getInstance().createURI( ((SupportsRdfId.URIKey) theSupport.getRdfId()).value().toASCIIString() );
		}
		else if (theSupport.getRdfId() instanceof SupportsRdfId.BNodeKey) {
			return ValueFactoryImpl.getInstance().createBNode( ((SupportsRdfId.BNodeKey) theSupport.getRdfId()).value() );
		}
		else {
			String aValue = theSupport.getRdfId().toString();

			if (BasicUtils.isURI(aValue)) {
				return ValueFactoryImpl.getInstance().createURI(aValue);
			}
			else {
				return ValueFactoryImpl.getInstance().createBNode(aValue);
			}
		}
	}

	/**
	 * Return the object as an instanceof {@link com.clarkparsia.empire.SupportsRdfId}
	 * @param theObj the object
	 * @return the object as SupportsRdfId
	 * @throws ClassCastException if the object is not a valid SupportsRdfId
	 * @see com.clarkparsia.empire.SupportsRdfId
	 */
	public static SupportsRdfId asSupportsRdfId(Object theObj) {
		if (theObj instanceof SupportsRdfId.RdfKey) {
			return new SupportsRdfIdImpl( (SupportsRdfId.RdfKey) theObj);
		}
		else if (theObj instanceof java.net.URI) {
			return new SupportsRdfIdImpl(new SupportsRdfId.URIKey( (java.net.URI) theObj));
		}
		else if (theObj instanceof SupportsRdfId) {
			return (SupportsRdfId) theObj;
		}
		else {
			return new SupportsRdfIdImpl(asPrimaryKey(theObj));
		}
	}

	/**
	 * Returns whether or not a NamedGraph context has been specified for the type of the specified instance.
	 * When a named graph is specified, all operations which mutate the data source will attempt to operate
	 * on the specified named graph.
	 * @param theObj the object to check
	 * @return true if it has a named graph specified, false otherwise
	 */
	public static boolean hasNamedGraphSpecified(Object theObj) {
		NamedGraph aAnnotation = theObj.getClass().getAnnotation(NamedGraph.class);

		return aAnnotation != null &&
			   (aAnnotation.type() == NamedGraph.NamedGraphType.Instance || (aAnnotation.type() == NamedGraph.NamedGraphType.Static
																			 && !aAnnotation.value().equals("")));
	}

	/**
	 * Returns the URI of the named graph that operations involving instances should be performed.  If null is returned
	 * operations will be performed on the data source without a specified context.
	 * @param theObj the instance
	 * @return the URI of the instance's named graph, or null if there isn't one
	 * @throws java.net.URISyntaxException if the named graph specified (when the type is {@link com.clarkparsia.empire.annotation.NamedGraph.NamedGraphType#Static}) is not a valid URI
	 */
	public static java.net.URI getNamedGraph(Object theObj) {
		if (!hasNamedGraphSpecified(theObj)) {
			return null;
		}

		NamedGraph aAnnotation = theObj.getClass().getAnnotation(NamedGraph.class);

		if (aAnnotation.type() == NamedGraph.NamedGraphType.Instance) {
			SupportsRdfId aId = asSupportsRdfId(theObj);

			try {
				return asURI(aId);
			}
			catch (URISyntaxException e) {
				LOGGER.warn("There was an error trying to get the instance-level named graph URI from an object.  Its key is not a URI.", e);
				return null;
			}
		}
		else {
			return URI.create(aAnnotation.value());
		}
	}

	/**
	 * Return the SupportsRdfId key as a java.net.URI.
	 * @param theSupport the RDF-izable support class
	 * @return the id key as a java URI, or null if it cannot be converted to a URI.
	 * @throws URISyntaxException thrown if the value is not a valid URI.
	 */
	private static java.net.URI asURI(SupportsRdfId theSupport) throws URISyntaxException {
		if (theSupport.getRdfId() == null) {
			return null;
		}
		else if (theSupport.getRdfId() instanceof SupportsRdfId.URIKey) {
			return ((SupportsRdfId.URIKey) theSupport.getRdfId()).value();
		}
		else {
			String aValue = theSupport.getRdfId().toString();

			if (BasicUtils.isURI(aValue)) {
				return URI.create(aValue);
			}
		}

		return null;
	}

	/**
	 * Return all instances of the specified class in the EntityManager
	 * @param theManager the manager to query
	 * @param theClass the type of objects to query for
	 * @param <T> the type of objects returned
	 * @return the list of all objects of the given type in the EntityManager
	 */
	public static <T> List<T> all(EntityManager theManager, Class<T> theClass) {
		List<T> aList = new ArrayList<T>();

		LOGGER.debug("Starting read all");
		long start = System.currentTimeMillis();
		
		if (!AnnotationChecker.isValid(theClass) || !(theManager.getDelegate() instanceof DataSource)) {
			return aList;
		}

		LOGGER.debug("Class valid check : " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		
		RdfsClass aClass = theClass.getAnnotation(RdfsClass.class);

		LOGGER.debug("Got annotation : " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		
		// this init should be handled by the static block in RdfGenerator, but if there is no annotation index,
		// or RdfGenerator has not been referenced, the namespace stuff will not have been initialized.  so we'll
		// call this here as a backup.
		RdfGenerator.addNamespaces(theClass);

		QueryBuilder<ParsedTupleQuery> aQuery = QueryBuilderFactory.select("result").distinct()
				.group().atom("result", RDF.TYPE, ValueFactoryImpl.getInstance().createURI(NamespaceUtils.uri(aClass.value()))).closeGroup();
		
		LOGGER.debug("Created query : " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		
		String aQueryStr = null;

		try {
			DataSource aSource = (DataSource) theManager.getDelegate();

			LOGGER.debug("Got source : " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			
			if (aSource.getQueryFactory().getDialect() instanceof SPARQLDialect) {
				aQueryStr = new SPARQLQueryRenderer().render(aQuery.query());
			}
			else if (aSource.getQueryFactory().getDialect() instanceof SerqlDialect) {
				aQueryStr = new SeRQLQueryRenderer().render(aQuery.query());
			}

			LOGGER.debug("Got query string : " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
		catch (Exception e) {
			throw new PersistenceException(e);
		}
		
		if (LOGGER.isDebugEnabled()) LOGGER.debug("EntityManager open : " + theManager.isOpen());
		
		List aResults = theManager.createNativeQuery(aQueryStr, theClass).getResultList();

		LOGGER.debug("Created native query : " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		
		for (Object aObj : aResults) {
			try {
				aList.add( theClass.cast(aObj));
			}
			catch (ClassCastException e) {
				throw new PersistenceException(e);
			}

			LOGGER.debug("Added result item : " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}

		return aList;
	}

	/**
	 * Returns the object as a primary key value.  Generally, for an rdf database, the identifying key for a resource
	 * will be it's rdf:ID or rdf:nodeID.  So the value must be a URI, for named resources, or a non-uri string, for
	 * nodeID.
	 * @param theObj the possible primary key
	 * @return the primary key as a RdfKey object
	 * @throws IllegalArgumentException thrown if the value is null, or if it is not a valid URI, or it cannot be turned into one
	 */
	public static SupportsRdfId.RdfKey asPrimaryKey(Object theObj) {
		if (theObj == null) {
			throw new IllegalArgumentException("null is not a valid primary key for an Entity");
		}

		try {
			if (theObj instanceof URI) {
				return new SupportsRdfId.URIKey((URI) theObj);
			}
			else if (theObj instanceof URL) {
				return new SupportsRdfId.URIKey(((URL) theObj).toURI());
			}
			else if (theObj instanceof org.openrdf.model.URI) {
				return new SupportsRdfId.URIKey( URI.create( ((org.openrdf.model.URI) theObj).stringValue()) );
			}
			else if (theObj instanceof org.openrdf.model.BNode) {
				return new SupportsRdfId.BNodeKey( ((BNode) theObj).getID() );
			}
			else {
				if (BasicUtils.isURI(theObj.toString())) {
					return new SupportsRdfId.URIKey(new URI(theObj.toString()));
				}
				else {
					if (theObj.toString().matches("[a-zA-Z_0-9]{1}[a-zA-Z_\\-0-9]*")) {
						return new SupportsRdfId.BNodeKey(theObj.toString());
					}
					else if (theObj.toString().matches("_:[a-zA-Z_0-9]{1}[a-zA-Z_\\-0-9]*")) {
						return new SupportsRdfId.BNodeKey(theObj.toString().substring(2));
					}
					throw new IllegalArgumentException("'" + theObj + "' is not a valid primary key, it is not a URI or a valid BNode identifer");
				}
			}
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException(theObj + " is not a valid primary key, it is not a URI.", e);
		}
	}

}
