package com.clarkparsia.empire.test.api;

import com.clarkparsia.sesame.repository.ExtendedSesameRepository;

import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.sesame.utils.SesameValueFactory;
import com.clarkparsia.sesame.utils.query.SesameQuery;

import org.openrdf.model.Graph;

import org.openrdf.model.impl.GraphImpl;

import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.ResultSet;

import com.clarkparsia.empire.sesame.SesameResultSet;

import com.clarkparsia.empire.impl.AbstractDataSource;
import com.clarkparsia.empire.impl.serql.SerqlQueryFactory;

import java.net.URI;
import java.net.ConnectException;

/**
 * Title: TestDataSource<br/>
 * Description: Implementation of the data source interface backed by an in-memory Sesame instance for testing purposes.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 15, 2009 8:47:52 AM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class TestDataSource extends AbstractDataSource implements DataSource {
	private ExtendedSesameRepository mRepo;

	public TestDataSource() {
		this(new GraphImpl());
	}

	public TestDataSource(Graph theGraph) {
		mRepo = new ExtendedSesameRepository(SesameUtils.createInMemSource());

		try {
			mRepo.addGraph(theGraph);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

        setQueryFactory(new SerqlQueryFactory(this));
	}

	protected ExtendedSesameRepository getRepository() {
		return mRepo;
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		try {
			return new SesameResultSet(mRepo.performSelectQuery(SesameQuery.serql(theQuery)));
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
			return mRepo.performConstructQuery(SesameQuery.serql(theQuery));
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
	public Graph describe(final URI theURI) throws DataSourceException {
		return mRepo.describe(SesameValueFactory.instance().createURI(theURI));
	}
}
