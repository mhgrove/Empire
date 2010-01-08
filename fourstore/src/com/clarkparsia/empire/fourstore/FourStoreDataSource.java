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

import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.SupportsNamedGraphs;

import com.clarkparsia.empire.impl.AbstractDataSource;
import com.clarkparsia.empire.fourstore.FourStoreResultSet;
import com.clarkparsia.empire.impl.sparql.SPARQLQueryFactory;

import org.openrdf.model.Graph;
import org.openrdf.sesame.constants.RDFFormat;

import java.net.ConnectException;
import java.net.URI;

import java.io.StringWriter;
import java.io.IOException;

import fourstore.api.Store;
import fourstore.api.StoreException;
import fourstore.api.Format;

import fourstore.impl.sesame.FourStoreToSesame;

import com.clarkparsia.sesame.utils.SesameIO;

/**
 * <p>Implementation of a DataSource which is backed by a 4Store instance.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class FourStoreDataSource extends AbstractDataSource implements MutableDataSource, SupportsNamedGraphs {

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

		setQueryFactory(new SPARQLQueryFactory(this));
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		// limitation of 4store: http://4store.org/trac/wiki/TODO
		throw new UnsupportedOperationException("Adding single triples, or graphs of triples is not supported, only adds to named graphs are supported");
	}
	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		// limitation of 4store: http://4store.org/trac/wiki/TODO
		throw new UnsupportedOperationException("Removing single triples, or graphs of triples is not supported, only named graph removal is supported");
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
			// TODO: log me
			System.err.println("Disconnecting from 4store db failed");
		}
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		try {
			return new FourStoreResultSet(mStore.query(theQuery));
		}
		catch (fourstore.api.QueryException e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		try {
			return FourStoreToSesame.toSesameGraph(mStore.constructQuery(theQuery));
		}
		catch (fourstore.api.QueryException e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final URI theURI) throws DataSourceException {
		try {
			//return FourStoreToSesame.toSesameGraph(mStore.describe(new FourStoreValueFactory().createURI(theURI.toString())));

			// rasqal doesn't support describe, and thus, neither does 4store.  we'll do a poor man's implementation
			// of it with a construct query
			return FourStoreToSesame.toSesameGraph(mStore.constructQuery("construct { ?s ?p ?o }  where { ?s ?p ?o. filter(?s = <" + theURI + ">) } "));
		}
		catch (fourstore.api.QueryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void add(final URI theGraphURI, final Graph theGraph) throws DataSourceException {
		StringWriter aWriter = new StringWriter();

		try {
			SesameIO.writeGraph(theGraph, aWriter, RDFFormat.TURTLE);
		}
		catch (IOException e) {
			throw new DataSourceException(e);
		}

		try {
			mStore.append(aWriter.toString(), Format.Turtle, theGraphURI);
		}
		catch (StoreException e) {
			// TODO: need a better way to detect this
			if (e.getMessage().contains("<h1>Not found")) {
				// kind of hacky, but if we're trying to append here and we get a not found message, this graph context
				// does not yet exist, so lets try adding it
				try {
					mStore.add(aWriter.toString(), Format.Turtle, theGraphURI);
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
			mStore.delete(theGraphURI);
		}
		catch (StoreException e) {
			throw new DataSourceException(e);
		}
	}
}
