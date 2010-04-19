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

package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.SupportsTransactions;
import com.clarkparsia.empire.impl.AbstractDataSource;

import java.net.ConnectException;
import java.net.URI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.Syntax;

import com.clarkparsia.empire.jena.util.JenaSesameUtils;

import org.openrdf.model.Graph;

/**
 * <p>Implementation of the Empire DataSource API backed by a Jena Model</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.3
 */
public class JenaDataSource extends AbstractDataSource implements MutableDataSource, SupportsTransactions {

	/**
	 * The underlying Jena model
	 */
	private Model mModel;

	/**
	 * Create a new Jena-backed Data Source
	 * @param theModel the model
	 */
	JenaDataSource(final Model theModel) {
		mModel = theModel;

		setQueryFactory(new JenaSPARQLQueryFactory(this));
	}

	public Model getModel() {
		return mModel;
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		setConnected(true);
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		setConnected(false);

		// TODO: should we close the model here?
		//mModel.close();
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		assertConnected();

		QueryExecution aQueryExec = query(theQuery);

		return new JenaResultSet(aQueryExec, aQueryExec.execSelect());
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		assertConnected();

		QueryExecution aQueryExec = query(theQuery);

		try {
			return JenaSesameUtils.asSesameGraph(aQueryExec.execConstruct());
		}
		finally {
			aQueryExec.close();
		}
	}

	private QueryExecution query(final String theQuery) {
		assertConnected();

		return QueryExecutionFactory.create(QueryFactory.create(theQuery, Syntax.syntaxSPARQL), mModel);
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final URI theURI) throws DataSourceException {
		assertConnected();

		QueryExecution aQueryExec = query("describe <" + theURI + ">");

		try {
			return JenaSesameUtils.asSesameGraph(aQueryExec.execDescribe());
		}
		finally {
			aQueryExec.close();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		assertConnected();

		mModel.add(JenaSesameUtils.asJenaModel(theGraph));
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		assertConnected();

		mModel.remove(JenaSesameUtils.asJenaModel(theGraph));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void assertConnected() {
		super.assertConnected();

		if (mModel.isClosed()) {
			throw new IllegalStateException("Model is closed, cannot perform operation");
		}
	}

	/**
	 * @inheritDoc
	 */
	public void begin() throws DataSourceException {
		mModel.begin();
	}

	/**
	 * @inheritDoc
	 */
	public void commit() throws DataSourceException {
		mModel.commit();
	}

	/**
	 * @inheritDoc
	 */
	public void rollback() throws DataSourceException {
		mModel.abort();
	}
}
