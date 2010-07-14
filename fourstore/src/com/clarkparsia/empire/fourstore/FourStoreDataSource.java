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

package com.clarkparsia.empire.fourstore;

import com.clarkparsia.empire.ds.MutableDataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.SupportsNamedGraphs;
import com.clarkparsia.empire.ds.impl.AbstractDataSource;

import com.clarkparsia.empire.ds.impl.AbstractResultSet;

import com.clarkparsia.empire.impl.RdfQueryFactory;

import com.clarkparsia.empire.impl.sparql.SPARQLDialect;

import org.openrdf.model.Graph;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.rio.RDFFormat;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.net.ConnectException;
import java.net.URI;

import java.io.StringWriter;
import java.io.IOException;

import com.clarkparsia.fourstore.api.Store;
import com.clarkparsia.fourstore.api.StoreException;

import com.clarkparsia.openrdf.OpenRdfIO;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.openrdf.query.util.DescribeVisitor;
import com.clarkparsia.openrdf.query.util.AskVisitor;
import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;

import static com.clarkparsia.openrdf.OpenRdfUtil.toIterator;

/**
 * <p>Implementation of a DataSource which is backed by a 4Store instance.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 */
public class FourStoreDataSource extends AbstractDataSource implements MutableDataSource, SupportsNamedGraphs {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FourStoreDataSource.class.getName());

	/**
	 * The 4Store database
	 */
	private Store mStore;

	/**
	 * Create a new FourStoreDataSource
	 * @param theStore the 4Store db to use
	 */
	FourStoreDataSource(Store theStore) {
		mStore = theStore;

		// i'm told by one of the Jena guys that 4store also supports the arq-style encoding for bnodes.
		// need to confirm with steve.
		setQueryFactory(new RdfQueryFactory(this, SPARQLDialect.instance()));
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		try {
			mStore.add(theGraph, null);
		}
		catch (StoreException e) {
			throw new DataSourceException(e);
		}
	}
	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		try {
			mStore.delete(theGraph, null);
		}
		catch (StoreException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		mStore.connect();
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		try {
			mStore.disconnect();
		}
		catch (ConnectException e) {
			LOGGER.warn("Disconnecting from 4store db failed");
		}
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		try {
			final TupleQueryResult aResult = mStore.query(theQuery);
            return new AbstractResultSet(toIterator(aResult)) {
                public void close() {
                    try {
                        aResult.close();
                    }
                    catch (QueryEvaluationException e) {
                        e.printStackTrace();
                    }
                }
            };
		}
		catch (com.clarkparsia.fourstore.api.QueryException e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		try {
			return mStore.constructQuery(theQuery);
		}
		catch (com.clarkparsia.fourstore.api.QueryException e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean ask(final String theQuery) throws QueryException {
		String aQueryStr = theQuery;

		try {
			ParsedQuery aQuery = new SPARQLParser().parseQuery(theQuery, "http://example.org");

			if (new AskVisitor().checkQuery(aQuery).isAsk()) {
				// 4Store does not support ask queries, so lets render the partial query, make it a select w/ limit 1
				// and evalue that to simulate having support for ASK queries.
				aQueryStr = new SPARQLQueryRenderer().render(new SPARQLParser().parseQuery(theQuery, "http://example.org"));

				aQueryStr = "select distinct * where { " + aQueryStr + " } limit 1";
			}
		}
		catch (Exception e) {
			throw new QueryException(e);
		}

		ResultSet aResults = selectQuery(aQueryStr);

		try {
			return aResults.hasNext();
		}
		finally {
			aResults.close();
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final String theQuery) throws QueryException {
		try {
			// rasqal doesn't support describe, and thus, neither does 4store.  we'll do a poor man's implementation
			// of it with a construct query
			//return mStore.constructQuery("construct { ?s ?p ?o }  where { ?s ?p ?o. filter(?s = <" + theURI + ">) } ");

			ParsedQuery aQuery = new SPARQLParser().parseQuery(theQuery, "http://example.org");
			DescribeVisitor aVisitor = new DescribeVisitor();
			aVisitor.checkQuery(aQuery);

			if (!aVisitor.isDescribe()) {
				// not a describe, try a construct?
				return graphQuery(theQuery);
			}
			else {
				ExtGraph aGraph = new ExtGraph();

				for (Value aValue : aVisitor.getValues()) {
					if (aValue instanceof org.openrdf.model.URI) {
						aGraph.addAll(mStore.constructQuery("construct { ?s ?p ?o }  where { ?s ?p ?o. filter(?s = <" + aValue.stringValue() + ">) } "));
					}
					else {
						throw new QueryException("Cannot describe non-URI values");
					}
				}

				return aGraph;
			}
		}
		catch (Exception e) {
			if (e instanceof QueryException) {
				throw (QueryException) e;
			}

			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void add(final URI theGraphURI, final Graph theGraph) throws DataSourceException {
		StringWriter aWriter = new StringWriter();

		try {
			OpenRdfIO.writeGraph(theGraph, aWriter, RDFFormat.TURTLE);
		}
		catch (IOException e) {
			throw new DataSourceException(e);
		}

		try {
			mStore.append(aWriter.toString(), RDFFormat.TURTLE, ValueFactoryImpl.getInstance().createURI(theGraphURI.toString()));
		}
		catch (StoreException e) {
			// TODO: need a better way to detect this
			if (e.getMessage().contains("<h1>Not found")) {
				// kind of hacky, but if we're trying to append here and we get a not found message, this graph context
				// does not yet exist, so lets try adding it
				try {
					mStore.add(aWriter.toString(), RDFFormat.TURTLE, ValueFactoryImpl.getInstance().createURI(theGraphURI.toString()));
				}
				catch (StoreException ex) {
					// now we definitely failed, but re-throw the original exception, that might be more relevant
					throw new DataSourceException(e);
				}
			}
			else {
				throw new DataSourceException(e);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final URI theGraphURI) throws DataSourceException {
		remove(theGraphURI, null);
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final URI theGraphURI, final Graph theGraph) throws DataSourceException {
		try {
			mStore.delete(ValueFactoryImpl.getInstance().createURI(theGraphURI.toString()));
		}
		catch (StoreException e) {
			throw new DataSourceException(e);
		}
	}
}
