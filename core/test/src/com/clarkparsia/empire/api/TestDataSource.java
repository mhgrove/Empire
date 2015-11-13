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

package com.clarkparsia.empire.api;

import com.clarkparsia.empire.ds.impl.AbstractResultSet;
import com.clarkparsia.empire.util.Repositories2;
import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.openrdf.repository.Repositories;
import info.aduna.iteration.Iterations;
import org.openrdf.model.Graph;

import org.openrdf.model.Model;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;

import com.clarkparsia.empire.ds.DataSource;

import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.impl.AbstractDataSource;

import com.clarkparsia.empire.impl.RdfQueryFactory;

import com.clarkparsia.empire.impl.serql.SerqlDialect;

import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;

import java.net.ConnectException;

/**
 * <p>Implementation of the data source interface backed by an in-memory Sesame instance for testing purposes.</p>
 *
 * @author Michael Grove
 */
public class TestDataSource extends AbstractDataSource implements DataSource {
	private final Repository mRepo;

	public TestDataSource() {
		this(Models2.newModel());
	}

	public TestDataSource(final Repository theRepository) {
		mRepo = theRepository;
		setQueryFactory(new RdfQueryFactory(this, SerqlDialect.instance()));
	}

	public TestDataSource(Graph theGraph) {
		mRepo = Repositories2.createInMemoryRepo();

		try {
			Repositories.add(mRepo, theGraph);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

        setQueryFactory(new RdfQueryFactory(this, SerqlDialect.instance()));
	}

	protected Repository getRepository() {
		return mRepo;
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		try {
            final TupleQueryResult aTupleQueryResult = Repositories.selectQuery(mRepo, QueryLanguage.SERQL, theQuery);
            return new AbstractResultSet(Iterations.stream(aTupleQueryResult).iterator()) {
                @Override
                public void close() {
                    aTupleQueryResult.close();
                }
            };
		}
		catch (MalformedQueryException e) {
			throw new QueryException("Unsupported or invalid SeRQL query.", e);
		}
		catch (QueryEvaluationException e) {
			throw new QueryException("Error during query evaluation.", e);
		}
		catch (Exception e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Model graphQuery(final String theQuery) throws QueryException {
		try {
			return Models2.newModel(Repositories.constructQuery(mRepo, QueryLanguage.SERQL, theQuery));
		}
		catch (MalformedQueryException e) {
			throw new QueryException("Unsupported or invalid SeRQL query.", e);
		}
		catch (QueryEvaluationException e) {
			throw new QueryException("Error during query evaluation.", e);
		}
		catch (Exception e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean ask(final String theQuery) throws QueryException {
		throw new UnsupportedOperationException("SeRQL does not support ASK queries");
	}

	/**
	 * @inheritDoc
	 */
	public Model describe(final String theQuery) throws QueryException {
		throw new UnsupportedOperationException("SeRQL does not support DESCRIBE queries");
	}
}
