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
import com.complexible.common.openrdf.Graphs;
import com.complexible.common.openrdf.util.AdunaIterations;
import org.openrdf.model.Graph;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;

import com.clarkparsia.empire.ds.DataSource;

import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.impl.AbstractDataSource;

import com.clarkparsia.empire.impl.RdfQueryFactory;

import com.clarkparsia.empire.impl.serql.SerqlDialect;

import com.complexible.common.openrdf.ExtRepository;
import com.complexible.common.openrdf.OpenRdfUtil;
import org.openrdf.query.TupleQueryResult;

import java.net.ConnectException;

/**
 * <p>Implementation of the data source interface backed by an in-memory Sesame instance for testing purposes.</p>
 *
 * @author Michael Grove
 */
public class TestDataSource extends AbstractDataSource implements DataSource {
	private ExtRepository mRepo;

	public TestDataSource() {
		this(Graphs.newGraph());
	}

	public TestDataSource(ExtRepository theRepository) {
		mRepo = theRepository;
		setQueryFactory(new RdfQueryFactory(this, SerqlDialect.instance()));
	}

	public TestDataSource(Graph theGraph) {
		mRepo = OpenRdfUtil.createInMemoryRepo();

		try {
			mRepo.add(theGraph);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

        setQueryFactory(new RdfQueryFactory(this, SerqlDialect.instance()));
	}

	protected ExtRepository getRepository() {
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
            final TupleQueryResult aTupleQueryResult = mRepo.selectQuery(QueryLanguage.SERQL, theQuery);
            return new AbstractResultSet(AdunaIterations.iterator(aTupleQueryResult)) {
                @Override
                public void close() {
                    // no-op
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
	public Graph graphQuery(final String theQuery) throws QueryException {
		try {
			return mRepo.constructQuery(QueryLanguage.SERQL, theQuery);
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
	public Graph describe(final String theQuery) throws QueryException {
		throw new UnsupportedOperationException("SeRQL does not support DESCRIBE queries");
	}
}
