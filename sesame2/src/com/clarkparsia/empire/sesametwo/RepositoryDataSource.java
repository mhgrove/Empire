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

package com.clarkparsia.empire.sesametwo;

import com.clarkparsia.empire.ds.SupportsNamedGraphs;
import com.clarkparsia.empire.ds.MutableDataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.TripleSource;
import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.impl.AbstractDataSource;

import com.clarkparsia.empire.impl.RdfQueryFactory;
import com.clarkparsia.empire.impl.sparql.SPARQLDialect;

import com.clarkparsia.empire.impl.serql.SerqlDialect;
import com.clarkparsia.openrdf.OpenRdfUtil;
import com.clarkparsia.openrdf.util.GraphBuildingRDFHandler;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.ConnectException;
import java.net.URI;


/**
 * <p>Implementation of the DataSource interface(s) backed by a Sesame 2 repository.  This can be used as a base class
 * for any back-end which supports the Sesame 2 SAIL api, such as BigData, OWLIM, Neo4j, and others.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.7
 */
public class RepositoryDataSource extends AbstractDataSource implements MutableDataSource, TripleSource, SupportsNamedGraphs {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(RepositoryDataSource.class);

	/**
	 * The underlying Sesame repository
	 */
	private Repository mRepository;

	/**
	 * The connection to the repository
	 */
	private RepositoryConnection mConnection;

	/**
	 * The query languge to use when sending queries to the repository
	 */
	private QueryLanguage mQueryLang;

	/**
	 * Create a new RepositoryDataSource which uses the SPARQL query dialect for its Query API
	 * @param theRepository the sesame repository to back this data source
	 */
	RepositoryDataSource(final Repository theRepository) {
		this(theRepository, false);
	}

	/**
	 * Create a new RepositoryDataSource
	 * @param theRepository the sesame repository to back this data source
	 * @param theUseSerql true to use the serql query dialect with this data source, false to default to sparql
	 */
	RepositoryDataSource(final Repository theRepository, boolean theUseSerql) {
		mRepository = theRepository;

		// TODO: add the SupportsTransactions interface to this class so Empire notices it natively supports
		// transactions.  right now, changes within a transaction are not "live", even within the same
		// connection, so it looks like adds & deletes fail.  not good.  need a different isolation level
		// for the transactions for it to work right.  or i go back to the drawing board on some parts of entitymanager
		// sesame 3 will have different transaction isolation levels, settable for each repo, which is what we need
		// i think.  really, the catch is that transactions behave differently (different isolation levels) depending
		// on the repository implementation.  so its' not easy to do *one* solution that will work for all of them
		// so we have to rely on our naive poor-man's transaction implementation.

		if (theUseSerql) {
			mQueryLang = QueryLanguage.SERQL;
			setQueryFactory(new RdfQueryFactory(this, SerqlDialect.instance()));
		}
		else {
			mQueryLang = QueryLanguage.SPARQL;
			setQueryFactory(new RdfQueryFactory(this, SPARQLDialect.instance()));
		}
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		assertConnected();

		try {
			mConnection.add(theGraph);
		}
		catch (RepositoryException e) {
			rollback();

			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		assertConnected();

		try {
			mConnection.remove(theGraph);
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean isConnected() {
		try {
			return mConnection != null && mConnection.isOpen() && super.isConnected();
		}
		catch (RepositoryException e) {
			LOGGER.error(e);

			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		if (!isConnected()) {
			setConnected(true);
			try {
				mConnection = mRepository.getConnection();

				// TODO: disable this when enabling sesame's native transaction support
				mConnection.setAutoCommit(true);
			}
			catch (RepositoryException e) {
				throw (ConnectException) new ConnectException("There was an error establishing the connection").initCause(e);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		assertConnected();

		try {
			mConnection.close();

			setConnected(false);
		}
		catch (RepositoryException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		assertConnected();

		try {
			TupleQueryResult aResult = mConnection.prepareTupleQuery(mQueryLang, theQuery).evaluate();

			return new TupleQueryResultSet(aResult);
		}
		catch (Exception e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		assertConnected();

		GraphBuildingRDFHandler aHandler = new GraphBuildingRDFHandler();

		try {
			GraphQuery aQuery = mConnection.prepareGraphQuery(mQueryLang, theQuery);
			aQuery.evaluate(aHandler);
			return aHandler.getGraph();
		}
		catch (Exception e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean ask(final String theQuery) throws QueryException {
		try {
			return mConnection.prepareBooleanQuery(mQueryLang, theQuery).evaluate();
		}
		catch (Exception e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final String theQuery) throws QueryException {
		return graphQuery(theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public void add(final URI theGraphURI, final Graph theGraph) throws DataSourceException {
		assertConnected();

		try {
			mConnection.add(theGraph, mConnection.getValueFactory().createURI(theGraphURI.toString()));
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final URI theGraphURI) throws DataSourceException {
		assertConnected();

		try {
			Resource aContext = mConnection.getValueFactory().createURI(theGraphURI.toString());

			mConnection.remove(mConnection.getStatements(null, null, null, true, aContext), aContext);
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final URI theGraphURI, final Graph theGraph) throws DataSourceException {
		assertConnected();

		try {
			mConnection.remove(theGraph, mConnection.getValueFactory().createURI(theGraphURI.toString()));
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void begin() throws DataSourceException {
		assertConnected();

		// i dont think there is anything to do here, sesame's connection being open means a transaction is open as
		// i understand it.
	}

	/**
	 * @inheritDoc
	 */
	public void commit() throws DataSourceException {
		assertConnected();

		try {
			mConnection.commit();
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void rollback() throws DataSourceException {
		assertConnected();

		try {
			mConnection.rollback();
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
    public Iterable<Statement> getStatements(Resource theSubject, org.openrdf.model.URI thePredicate, Value theObject) 
    		throws DataSourceException {
    	try {
			return OpenRdfUtil.iterable(mConnection.getStatements(theSubject, thePredicate, theObject, true));
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
    }
}
