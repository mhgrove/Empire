/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.complexible.common.openrdf.model.Models2;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.MutableDataSource;
import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.TripleSource;
import com.clarkparsia.empire.QueryFactory;
import com.clarkparsia.empire.ds.SupportsTransactions;

import java.net.ConnectException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * <p><b>Very</b> simple transactional support to put on top of a database that does not already support it.
 * We do the operations live on the database, but keep a track of what was added or deleted so on rollback we can
 * try and undo the edits.  If the rollback fails, it very well could have failed for part of the rollback
 * and you are left with an inconsistent database.  For real transactional support, use a database that supports it.</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version 1.0
 */
public class TransactionalDataSource implements DataSource, MutableDataSource, SupportsTransactions {

	/**
	 * The DataSource the operations will be applied to
	 */
	private MutableDataSource mDataSource;
	
	/**
	 * If the underlying DataSource (mDataSource) is a TripleSource, this is
	 * identical to mDataSource. Otherwise, this is a TripleSourceAdapter to mDataSource
	 */
	private TripleSource mTripleSource;


	private List<TransactionOp> mTransactionOps;
	
	/**
	 * Whether or not a transaction is currently active
	 */
	private boolean mIsInTransaction;

	/**
	 * @inheritDoc
	 */
	public TransactionalDataSource(final MutableDataSource theDataSource) {
		mDataSource = theDataSource;
		
		if (mDataSource instanceof TripleSource) {
			mTripleSource = (TripleSource) mDataSource;
		}
		else {
			mTripleSource = new TripleSourceAdapter(mDataSource);
		}

		mTransactionOps = new LinkedList<TransactionOp>();
	}

	/**
	 * @inheritDoc
	 */
	public void begin() throws DataSourceException {
		assertNotInTransaction();

		mIsInTransaction = true;

		mTransactionOps.clear();
	}

	/**
	 * @inheritDoc
	 */
	public void commit() throws DataSourceException {
		assertInTransaction();

		mIsInTransaction = false;

		mTransactionOps.clear();
	}

	/**
	 * @inheritDoc
	 */
	public void rollback() throws DataSourceException {
		assertInTransaction();

		try {			
			// revert all operations starting from the last one and go backwards until the first one
			for (ListIterator<TransactionOp> it = mTransactionOps.listIterator(mTransactionOps.size()); 
				it.hasPrevious(); ) {
				TransactionOp op = it.previous();
				
				if (op.isAdded()) {
					mDataSource.remove(op.getData());
				} 
				else {
					mDataSource.add(op.getData());
				}
			}
		}
		catch (DataSourceException e) {
			throw new DataSourceException("Rollback failed, database is likely to be in an inconsistent state.", e);
		}
		finally {
			mIsInTransaction = false;

			mTransactionOps.clear();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Model theGraph) throws DataSourceException {
		if (isInTransaction()) {
			mTransactionOps.add(new TransactionOp(nonExistingTriples(theGraph), true));
		}
		
		mDataSource.add(theGraph);
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Model theGraph) throws DataSourceException {
		if (isInTransaction()) {
			mTransactionOps.add(new TransactionOp(existingTriples(theGraph), false));
		}
		
		mDataSource.remove(theGraph);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isConnected() {
		return mDataSource.isConnected();
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		mDataSource.connect();
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		mDataSource.disconnect();
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		return mDataSource.selectQuery(theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public Model graphQuery(final String theQuery) throws QueryException {
		return mDataSource.graphQuery(theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public Model describe(final String theQuery) throws QueryException {
		return mDataSource.describe(theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public boolean ask(final String theQuery) throws QueryException {
		return mDataSource.ask(theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public QueryFactory getQueryFactory() {
		return mDataSource.getQueryFactory();
	}

	/**
	 * Return whether or not this data source is in a transaction
	 * @return true if it is in a transaction, false otherwise
	 */
	public boolean isInTransaction() {
		return mIsInTransaction;
	}

	/**
	 * Asserts that this DataSource should not be in a transaction
	 * @throws com.clarkparsia.empire.ds.DataSourceException thrown if the data source is in a transaction
	 */
	private void assertNotInTransaction() throws DataSourceException {
		if (isInTransaction()) {
			throw new DataSourceException("Cannot complete action, currently in a transaction");
		}
	}

	/**
	 * Asserts that this DataSource should be in a transaction
	 * @throws DataSourceException thrown if the data source is not in a transaction
	 */
	private void assertInTransaction() throws DataSourceException {
		if (!isInTransaction()) {
			throw new DataSourceException("Cannot complete action, not in a transaction");
		}
	}
	
	/**
	 * Filters all triples from the specified graph that already exist in the underlying data source
	 * 
	 * @param theData the data to be filtered
	 * @return a graph that contains only triples that do not exist in the data source
	 * @throws DataSourceException if querying the data source causes an error
	 */
	private Model nonExistingTriples(Model theData) throws DataSourceException {
		Model aResult = Models2.newModel();
		
		// TODO: is there a more efficient way to check that than triple-by-triple? 
		// (for remote data sources this will cause one request for triple ...)
		for (Iterator<Statement> it = theData.iterator(); it.hasNext(); ) {
			Statement statement = it.next();
			
			if (!existsInDataSource(statement)) {
				aResult.add(statement);
			}
		}
		
		return aResult;
	}
	
	/**
	 * Filters all triples from the specified graph that do not exist in the underlying data source
	 * 
	 * @param theData the data to be filtered
	 * @return a graph that contains only triples that already exist in the data source
	 * @throws DataSourceException if querying the data source causes an error
	 */
	private Model existingTriples(Model theData) throws DataSourceException {
		Model aResult = Models2.newModel();

		// TODO: is there a more efficient way to check that than triple-by-triple? 
		// (for remote data sources this will cause one request for triple ...)
		for (Iterator<Statement> it = theData.iterator(); it.hasNext(); ) {
			Statement statement = it.next();
			
			if (existsInDataSource(statement)) {
				aResult.add(statement);
			}
		}
		
		return aResult;
	}
	
	/**
	 * Checks whether the given statement exists in the data source.
	 * 
	 * @param s the statement to be checked
	 * @return true, if the statement exists, false otherwise
	 * @throws DataSourceException
	 */
	private boolean existsInDataSource(Statement s) throws DataSourceException {	
		return mTripleSource.getStatements(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext()).iterator().hasNext();
	}

	/**
	 * Holds information about an add/remove operation within transaction
	 * 
	 * @author Blazej Bulka <blazej@clarkparsia.com>
	 */
	private static class TransactionOp {
		/**
		 * The data that was actually added/removed.
		 * 
		 * By "actually added" means triples that did not exist in the triple store before and 
		 * were added.
		 * 
		 * By "actually removed" means triples that existed in the triple store and were removed
		 * 
		 * The terms above are introduced because it is possible that the user attempts to add triples that were
		 * already there before -- a rollback must not remove such triples. Similarly, a user can request removal
		 * of triples that did not exist in the triple store -- a rollback must not add such triples.
		 */
		private Model mData;
		
		/**
		 * Information whether triples were added (true) or removed (false).
		 */
		private boolean mAdded;
		
		TransactionOp(Model theData, boolean theAdded) {
			this.mData = theData;
			this.mAdded = theAdded;
		}
		
		/**
		 * Gets the data involved in the operation
		 * 
		 * @return graph containing the data that was actually added/deleted
		 */
		public Model getData() {
			return mData;
		}
		
		/**
		 * Gets the flag whether the data was added/deleted
		 * 
		 * @return true if the data was added, false if it was deleted
		 */
		public boolean isAdded() {
			return mAdded;
		}		
	}
}
