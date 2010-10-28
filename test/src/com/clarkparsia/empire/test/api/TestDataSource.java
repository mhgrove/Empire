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

package com.clarkparsia.empire.test.api;

import org.openrdf.model.Graph;
import org.openrdf.model.Value;

import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.serql.SeRQLParser;
import org.openrdf.query.parser.sparql.SPARQLParser;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.impl.AbstractDataSource;
import com.clarkparsia.empire.sesametwo.TupleQueryResultSet;

import com.clarkparsia.empire.impl.RdfQueryFactory;

import com.clarkparsia.empire.impl.serql.SerqlDialect;

import com.clarkparsia.openrdf.ExtRepository;
import com.clarkparsia.openrdf.OpenRdfUtil;
import com.clarkparsia.openrdf.SesameQuery;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.openrdf.query.util.AskVisitor;
import com.clarkparsia.openrdf.query.util.DescribeVisitor;
import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;
import com.clarkparsia.openrdf.query.serql.SeRQLQueryRenderer;

import java.net.URI;
import java.net.ConnectException;

/**
 * <p>mplementation of the data source interface backed by an in-memory Sesame instance for testing purposes.</p>
 *
 * @author Michael Grove
 */
public class TestDataSource extends AbstractDataSource implements DataSource {
	private ExtRepository mRepo;

	public TestDataSource() {
		this(new GraphImpl());
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
			return new TupleQueryResultSet(mRepo.selectQuery(SesameQuery.serql(theQuery)));
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
			return mRepo.constructQuery(SesameQuery.serql(theQuery));
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
