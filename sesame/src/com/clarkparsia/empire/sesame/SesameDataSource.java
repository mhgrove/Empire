package com.clarkparsia.empire.sesame;

import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSourceException;

import com.clarkparsia.empire.impl.AbstractDataSource;

import org.openrdf.sesame.repository.SesameRepository;

import org.openrdf.model.Graph;

import java.net.ConnectException;
import java.net.URI;

import com.clarkparsia.sesame.repository.ExtendedSesameRepository;
import com.clarkparsia.sesame.utils.SesameValueFactory;
import com.clarkparsia.sesame.utils.query.SesameQuery;

/**
 * Title: SesameDataSource<br/>
 * Description: Implemention of the DataSource interface backed by a Sesame Repository.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 17, 2009 10:11:00 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SesameDataSource extends AbstractDataSource implements MutableDataSource {

	/**
	 * The Sesame Repository that backs this DataSource
	 */
	private ExtendedSesameRepository mRepo;

	/**
	 * Create a new SesameDataSource
	 * @param theRepo the underlying repository
	 */
	public SesameDataSource(final SesameRepository theRepo) {
		this(new ExtendedSesameRepository(theRepo));
	}

	/**
	 * Create a new SesameDataSource
	 * @param theRepo the underyling Sesame repository
	 */
	public SesameDataSource(final ExtendedSesameRepository theRepo) {
		mRepo = theRepo;
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
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		try {
			return new SesameResultSet(mRepo.performSelectQuery(SesameQuery.serql(theQuery)));
		}
		catch (Exception ex) {
			throw new QueryException(ex);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		try {
			return mRepo.performConstructQuery(SesameQuery.serql(theQuery));
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

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		try {
			mRepo.addGraph(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		try {
			// remember that sesame is fantastically, ungodly slow for remove operations.  This will be an expensive
			// call for all but the most trivially small KBs.
			mRepo.removeGraph(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}
}
