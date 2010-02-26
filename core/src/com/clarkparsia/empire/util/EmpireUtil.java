package com.clarkparsia.empire.util;

import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.SupportsNamedGraphs;
import com.clarkparsia.empire.impl.serql.SerqlDialect;
import com.clarkparsia.empire.impl.sparql.SPARQLDialect;
import com.clarkparsia.openrdf.query.builder.QueryBuilder;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;
import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;
import com.clarkparsia.openrdf.query.serql.SeRQLQueryRenderer;
import com.clarkparsia.utils.NamespaceUtils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.openrdf.model.Graph;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.parser.ParsedTupleQuery;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>A collection of utility functions for Empire.k</p>
 *
 * @author Michael Grove
 * @since 0.6.1
 * @version 0.6.1
 */
public class EmpireUtil {

	/**
	 * Return whether or not the given class instances is compatible with Empire, in that it has all the required
	 * annotations, and can have an rdf:ID.
	 * @param theClass the class to check
	 * @return true if its Empire compatible, false otherwise.
	 */
	public static boolean isEmpireCompatible(final Class theClass) {
		return theClass.isAnnotationPresent(Entity.class) &&
			   theClass.isAnnotationPresent(RdfsClass.class) &&
			   SupportsRdfId.class.isAssignableFrom(theClass);
	}


	/**
	 * Do a poor-man's describe on the given resource, querying its context if that is supported, or otherwise
	 * querying the graph in general.
	 * @param theSource the {@link DataSource} to query
	 * @param theObj the object to do the "describe" operation on
	 * @return all the statements about the given object
	 * @throws com.clarkparsia.empire.QueryException if there is an error while querying for the graph
	 */
	public static Graph describe(DataSource theSource, Object theObj) throws QueryException {
//		ParsedGraphQuery aQuery =
//				QueryBuilderFactory.construct().addProjectionStatement("s", "p", "o")
//				.group()
//				.setContext(ValueFactoryImpl.getInstance())
//				.atom("s", "p", "o")
//				.filter().eq("s", ValueFactoryImpl.getInstance().createURI(asSupportsRdfId(theObj).getRdfId().toString())).closeGroup().query();

		String aNG = null;

		if (asSupportsRdfId(theObj).getRdfId() == null) {
			return new GraphImpl();
		}

		if (theSource instanceof SupportsNamedGraphs && hasNamedGraphSpecified(theObj)) {
			java.net.URI aURI = getNamedGraph(theObj);

			if (aURI != null) {
				aNG = aURI.toString();
			}
		}

		String aSPARQL = "construct {?s ?p ?o}\n" +
						 (aNG == null ? "" : "from <" + aNG + ">\n") +
						 "where {?s ?p ?o. filter(?s = <" + asSupportsRdfId(theObj).getRdfId() + ">) }";

		String aSeRQL = "construct {s} p {o}\n" +
						 (aNG == null ? "from\n" : "from context <" + aNG + ">\n") +
						 "{s} p {o} where s = <" + asSupportsRdfId(theObj).getRdfId() + ">";

		Graph aGraph;

		if (theSource.getQueryFactory().getDialect().equals(SerqlDialect.instance())) {
			aGraph = theSource.graphQuery(aSeRQL);
		}
		else {
			// fall back on sparql
			aGraph = theSource.graphQuery(aSPARQL);
		}

		return aGraph;
	}

	/**
	 * Return the object as an instanceof {@link com.clarkparsia.empire.SupportsRdfId}
	 * @param theObj the object
	 * @return the object as SupportsRdfId
	 * @throws ClassCastException if the object is not a valid SupportsRdfId
	 * @see com.clarkparsia.empire.SupportsRdfId
	 */
	public static SupportsRdfId asSupportsRdfId(Object theObj) {
		return SupportsRdfId.class.cast(theObj);
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
			return asSupportsRdfId(theObj).getRdfId();
		}
		else {
			return URI.create(aAnnotation.value());
		}
	}

	public static <T> List<T> all(EntityManager theManager, Class<T> theClass) {
		List<T> aList = new ArrayList<T>();

		if (!isEmpireCompatible(theClass) || !(theManager.getDelegate() instanceof DataSource)) {
			return aList;
		}

		RdfsClass aClass = theClass.getAnnotation(RdfsClass.class);

		QueryBuilder<ParsedTupleQuery> aQuery = QueryBuilderFactory.select("result").distinct()
				.group().atom("result", RDF.TYPE, ValueFactoryImpl.getInstance().createURI(NamespaceUtils.uri(aClass.value()))).closeGroup();

		String aQueryStr = null;

		try {
			DataSource aSource = (DataSource) theManager.getDelegate();
			if (aSource.getQueryFactory().getDialect().equals(SPARQLDialect.instance())) {
				aQueryStr = new SPARQLQueryRenderer().render(aQuery.query());
			}
			else if (aSource.getQueryFactory().getDialect().equals(SerqlDialect.instance())) {
				aQueryStr = new SeRQLQueryRenderer().render(aQuery.query());
			}
		}
		catch (Exception e) {
			throw new PersistenceException(e);
		}

		List aResults = theManager.createNativeQuery(aQueryStr, theClass).getResultList();
		for (Object aObj : aResults) {
			try {
				aList.add( theClass.cast(aObj));
			}
			catch (ClassCastException e) {
				throw new PersistenceException(e);
			}
		}

		return aList;
	}
}
