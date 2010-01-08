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

package com.clarkparsia.empire;

import org.openrdf.model.Graph;

import java.net.ConnectException;

import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.QueryFactory;

/**
 * <p>Simple interface to an RDF datasource providing a set of methods to query the underyling data and a factory
 * for generating queries in the query language supported by the data source.  Roughly analogous to
 * {@link javax.sql.DataSource}.</p>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 * @since 0.1
 */
public interface DataSource {

	/**
	 * Returns whether or not there is an open connection to this data source
	 * @return true if there is a connection, false otherwise.
	 */
	public boolean isConnected();

	/**
	 * Connect to the DataSource.  Operations on this datasource will throw IllegalStateExceptions until a connection
	 * is established.
	 * @throws ConnectException thrown if a connection could not be established.
	 */
	public void connect() throws ConnectException;

	/**
	 * Disconnect this DataSource.  All subsequent operations on a disconnected data source will throw IllegalStateExceptions.
	 */
	public void disconnect();

	/**
	 * Perform the select query and return a set of results.
	 * @param theQuery the query to execute
	 * @return the results of the query
	 * @throws QueryException thrown if there was an error performing the query or there was an error parsing the query
	 * @throws IllegalStateException thrown if the connection to the data source is not open.
	 */
	public ResultSet selectQuery(String theQuery) throws QueryException;

	/**
	 * Execute a construct query on the data source.
	 * @param theQuery the construct query to execute
	 * @return the graph resulting from the execution of the construct query
	 * @throws QueryException thrown if there was an error performing the query or there was an error parsing the query
	 * @throws IllegalStateException thrown if the connection to the data source is not open.
	 */
	public Graph graphQuery(String theQuery) throws QueryException;

	/**
	 * Perform a simple describe query on the given URI.  This is slightly different than the describe as detailed
	 * in the SPARQL specs.
	 * @param theURI the URI to describe.
	 * @return the RDF graph describing the given instance
	 * @throws DataSourceException thrown if there was an error performing the query
	 * @throws IllegalStateException thrown if the connection to the data source is not open.
	 */
	public Graph describe(java.net.URI theURI) throws DataSourceException;

	/**
	 * Return a {@link QueryFactory} for creating executable {@link javax.persistence.Query} instances against this data source
	 * @return the factory for this data source
	 */
	QueryFactory getQueryFactory();
}
