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

package com.clarkparsia.empire.ds.impl;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.QueryFactory;

/**
 * <p>Abstract implementation of some parts of the {@link com.clarkparsia.empire.ds.DataSource} interface.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 * @see com.clarkparsia.empire.impl.EntityManagerImpl
 * @see com.clarkparsia.empire.ds.DataSourceFactory
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
