package com.clarkparsia.empire.util;

import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.SupportsNamedGraphs;
import com.clarkparsia.empire.impl.serql.SerqlDialect;

import javax.persistence.Entity;

import org.openrdf.model.Graph;

import java.net.URI;

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

		if (theSource instanceof SupportsNamedGraphs && hasNamedGraphSpecified(theObj)) {
			aNG = getNamedGraph(theObj).toString();
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
}
