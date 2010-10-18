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

package com.clarkparsia.empire.ds;

import com.clarkparsia.empire.ds.impl.TripleSourceAdapter;
import com.clarkparsia.empire.Dialect;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.impl.serql.SerqlDialect;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;
import com.clarkparsia.openrdf.query.serql.SeRQLQueryRenderer;
import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;
import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.collections.CollectionUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.Value;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.BindingSet;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.util.Collection;

/**
 * <p>Collection of utility methods for working with Empire DataSources</p>
 *
 * @author Michael Grove
 * @see DataSource
 * @see TripleSource
 *
 * @version 0.7
 * @since 0.7
 */
public class DataSourceUtil {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(Empire.class.getName());

	/**
	 * <p>Returns the given {@link DataSource} as a {@link TripleSource}.  If the DataSource does not natively support
	 * the interface, a wrapper is provided that delegates the triple level calls to SPARQL queries.</p>
	 * @param theSource the source
	 * @return the DataSource as a TripleSource.
	 * @see TripleSourceAdapter
	 * @throws DataSourceException if the TripleSource cannot be created.
	 */
	public static TripleSource asTripleSource(DataSource theSource) throws DataSourceException {
		if (theSource == null) {
			throw new DataSourceException("Cannot create triple source from null data source");
		}

		if (theSource instanceof TripleSource) {
			return (TripleSource) theSource;
		}
		else {
			return new TripleSourceAdapter(theSource);
		}
	}

	/**
	 * Do a poor-man's describe on the given resource, querying its context if that is supported, or otherwise
	 * querying the graph in general.
	 * @param theSource the {@link com.clarkparsia.empire.ds.DataSource} to query
	 * @param theObj the object to do the "describe" operation on
	 * @return all the statements about the given object
	 * @throws QueryException if there is an error while querying for the graph
	 */
	public static ExtGraph describe(DataSource theSource, Object theObj) throws QueryException {
		String aNG = null;

		if (EmpireUtil.asSupportsRdfId(theObj).getRdfId() == null) {
			return new ExtGraph();
		}

		if (theSource instanceof SupportsNamedGraphs && EmpireUtil.hasNamedGraphSpecified(theObj)) {
			java.net.URI aURI = EmpireUtil.getNamedGraph(theObj);

			if (aURI != null) {
				aNG = aURI.toString();
			}
		}

		Dialect aDialect = theSource.getQueryFactory().getDialect();

		Resource aResource = EmpireUtil.asResource(EmpireUtil.asSupportsRdfId(theObj));

		// TODO: if source supports describe queries, use that.

		String aSPARQL = "construct {?s ?p ?o}\n" +
						 (aNG == null ? "" : "from <" + aNG + ">\n") +
						 "where {?s ?p ?o. filter(?s = " + aDialect.asQueryString(aResource) + ") }";


		String aSeRQL = "construct {s} p {o}\n" +
						 (aNG == null ? "from\n" : "from context <" + aNG + ">\n") +
						 "{s} p {o} where s = " + aDialect.asQueryString(aResource) + "";

		Graph aGraph;

		if (theSource.getQueryFactory().getDialect() instanceof SerqlDialect) {
			aGraph = theSource.graphQuery(aSeRQL);
		}
		else {
			// fall back on sparql
			aGraph = theSource.graphQuery(aSPARQL);
		}

		return new ExtGraph(aGraph);
	}

	/**
	 * Do a poor-man's ask on the given resource to see if any triples using the resource (as the subject) exist,
	 * querying its context if that is supported, or otherwise querying the graph in general.
	 * @param theSource the {@link com.clarkparsia.empire.ds.DataSource} to query
	 * @param theObj the object to do the "ask" operation on
	 * @return true if there are statements about the object, false otherwise
	 * @throws QueryException if there is an error while querying for the graph
	 */
	public static boolean exists(DataSource theSource, Object theObj) throws QueryException {
		String aNG = null;

		if (EmpireUtil.asSupportsRdfId(theObj).getRdfId() == null) {
			return false;
		}

		if (theSource instanceof SupportsNamedGraphs && EmpireUtil.hasNamedGraphSpecified(theObj)) {
			java.net.URI aURI = EmpireUtil.getNamedGraph(theObj);

			if (aURI != null) {
				aNG = aURI.toString();
			}
		}

		Dialect aDialect = theSource.getQueryFactory().getDialect();

		String aSPARQL = "select distinct ?s\n" +
						 (aNG == null ? "" : "from <" + aNG + ">\n") +
						 "where {?s ?p ?o. filter(?s = " + aDialect.asQueryString(EmpireUtil.asResource(EmpireUtil.asSupportsRdfId(theObj))) + ") } limit 1";

		String aSeRQL = "select distinct s\n" +
						 (aNG == null ? "from\n" : "from context <" + aNG + ">\n") +
						 "{s} p {o} where s = " + aDialect.asQueryString(EmpireUtil.asResource(EmpireUtil.asSupportsRdfId(theObj))) + " limit 1";

		ResultSet aResults;

		if (theSource.getQueryFactory().getDialect() instanceof SerqlDialect) {
			aResults = theSource.selectQuery(aSeRQL);
		}
		else {
			// fall back on sparql
			aResults = theSource.selectQuery(aSPARQL);
		}

		try {
			return aResults.hasNext();
		}
		finally {
			aResults.close();
		}
	}

	/**
	 * Return the type of the resource in the data source.
	 * @param theSource the data source
	 * @param theConcept the concept whose type to lookup
	 * @return the rdf:type of the concept, or null if there is an error or one cannot be found.
	 */
	public static org.openrdf.model.URI getType(DataSource theSource, Resource theConcept) {
		if (theSource == null) {
			return null;
		}

		try {
			return new ExtGraph(describe(theSource, theConcept.toString())).getType(theConcept);
		}
		catch (DataSourceException e) {
			LOGGER.error("There was an error while getting the type of a resource", e);

			return null;
		}
	}

	/**
	 * Return the values for the property on the given resource.
	 * @param theSource the data source to query for values
	 * @param theSubject the subject to get property values for
	 * @param thePredicate the property to get values for
	 * @return a collection of all the values of the property on the given resource
	 * @throws com.clarkparsia.empire.ds.DataSourceException if there is an error while querying the data source.
	 */
	public static Collection<Value> getValues(final DataSource theSource, final Resource theSubject, final org.openrdf.model.URI thePredicate) throws DataSourceException {
		ParsedTupleQuery aQuery = QueryBuilderFactory.select("obj")
				.group()
				.atom(theSubject, thePredicate, "obj").closeGroup().query();

		ResultSet aResults;

		try {
			if (theSource.getQueryFactory().getDialect().equals(SerqlDialect.instance())) {
				aResults = theSource.selectQuery(new SeRQLQueryRenderer().render(aQuery));
			}
			else {
				aResults = theSource.selectQuery(new SPARQLQueryRenderer().render(aQuery));
			}

			return CollectionUtil.transform(CollectionUtil.set(aResults), new Function<BindingSet, Value>() {
					public Value apply(final BindingSet theIn) {
						return theIn.getValue("obj");
					}
			});
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * Return the values for the property on the given resource.
	 * @param theSource the data source to query for values
	 * @param theSubject the subject to get property values for
	 * @param thePredicate the property to get values for
	 * @return the first value of the resource
	 * @throws com.clarkparsia.empire.ds.DataSourceException if there is an error while querying the data source.
	 */
	public static Value getValue(final DataSource theSource, final Resource theSubject, final org.openrdf.model.URI thePredicate) throws DataSourceException {
		Collection<Value> aValues = getValues(theSource, theSubject, thePredicate);
		if (aValues.isEmpty()) {
			return null;
		}
		else {
			return aValues.iterator().next();
		}
	}
}
