package com.clarkparsia.empire.impl;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.QueryFactory;

/**
 * Title: AbstractDataSource<br/>
 * Description: Abstract implementation of some parts of the {@link DataSource} interface.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 17, 2009 10:11:36 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public abstract class AbstractDataSource implements DataSource {

	/**
	 * Whether or not there is an open connection to the DataSource
	 */
	private boolean mIsConnected;

	/**
	 * The QueryFactory to use for this DataSource
	 */
	private QueryFactory mQueryFactory;

	/**
	 * Set the {@link QueryFactory} for this {@link DataSource}
	 * @param theQueryFactory the query factory
	 */
	protected void setQueryFactory(final QueryFactory theQueryFactory) {
		mQueryFactory = theQueryFactory;
	}

	/**
	 * Set whether or not a connection has been established for this DataSource
	 * @param theConnected true if connected, false otherwise
	 */
	protected void setConnected(boolean theConnected) {
		mIsConnected = theConnected;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isConnected() {
		return mIsConnected;
	}

	/**
	 * @inheritDoc
	 */
	public QueryFactory getQueryFactory() {
		return mQueryFactory;
	}

	/**
	 * Enforce that a connection must be open.
	 * @throws IllegalStateException thrown if a connection is not open.
	 */
	protected void assertConnected() throws IllegalStateException {
		if (!isConnected()) {
			throw new IllegalStateException("A connection must be open to perform this operation");
		}
	}
}
