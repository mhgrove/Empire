package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.SupportsTransactions;
import com.clarkparsia.empire.impl.AbstractDataSource;
import com.clarkparsia.empire.impl.sparql.SPARQLQueryFactory;

import java.net.ConnectException;
import java.net.URI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.clarkparsia.empire.jena.util.JenaSesameUtils;
import org.openrdf.model.Graph;

/**
 * Title: JenaDataSource<br/>
 * Description: Implementation of the Empire DataSource API backed by a Jena Model<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 6, 2010 1:42:21 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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

		setQueryFactory(new SPARQLQueryFactory(this));
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		// no-op
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		mModel.close();
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		QueryExecution aQueryExec = query(theQuery);

		return new JenaResultSet(aQueryExec, aQueryExec.execSelect());
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		QueryExecution aQueryExec = query(theQuery);

		try {
			return JenaSesameUtils.asSesameGraph(aQueryExec.execConstruct());
		}
		finally {
			aQueryExec.close();
		}
	}

	private QueryExecution query(final String theQuery) {
		return QueryExecutionFactory.create(QueryFactory.create(theQuery), mModel);
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final URI theURI) throws DataSourceException {
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
		mModel.add(JenaSesameUtils.asJenaModel(theGraph));
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
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
