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

package com.clarkparsia.empire.impl;

import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.SupportsTransactions;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

/**
 * <p>Implementation of the JPA EntityTransaction interface for an RDF data source.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.1
 * @see EntityManagerImpl
 */
public class DataSourceEntityTransaction implements EntityTransaction {
	/**
	 * Whether or not the transaction is currently active
	 */
	private boolean mIsActive;

	/**
	 * Sets whether or not the transaction can only be rolled back
	 */
	private boolean mRollbackOnly = false;

	/**
	 * The data source the transaction is being performed on
	 */
	private SupportsTransactions mDataSource;

	/**
	 * Create (but not open) a transaction for the specified data source
	 * @param theDataSource the data source that will have the transaction 
	 */
	public DataSourceEntityTransaction(final SupportsTransactions theDataSource) {
		mDataSource = theDataSource;
	}

	/**
	 * @inheritDoc
	 */
	public void begin() {
		assertInactive();

		mIsActive = true;

		try {
			mDataSource.begin();
		}
		catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void commit() {
		assertActive();

		if (getRollbackOnly()) {
			throw new RollbackException("Transaction cannot be committed, it is marked as rollback only.");
		}

		try {
			mDataSource.commit();
			mIsActive = false;
		}
		catch (DataSourceException e) {
			throw new RollbackException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void rollback() {
		assertActive();

		try {
			mDataSource.rollback();
		}
		catch (DataSourceException e) {
			throw new PersistenceException(e);
		}
		finally {
			mIsActive = false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public void setRollbackOnly() {
		assertActive();

		mRollbackOnly = true;
	}

	/**
	 * @inheritDoc
	 */
	public boolean getRollbackOnly() {
		assertActive();

		return mRollbackOnly;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isActive() {
		return mIsActive;
	}

	/**
	 * Force there to be no active transaction.  If one is active, an IllegalStateException is thrown
	 * @throws IllegalStateException if there is an active transaction
	 */
	private void assertInactive() {
		if (isActive()) {
			throw new IllegalStateException("Transaction must be inactive in order to perform this operation.");
		}
	}

	/**
	 * Force there to be an active transaction.  If one is not active, an IllegalStateException is thrown
	 * @throws IllegalStateException if there is not an active transaction
	 */
	private void assertActive() {
		if (!isActive()) {
			throw new IllegalStateException("Transaction must be active in order to perform this operation.");
		}
	}
}
