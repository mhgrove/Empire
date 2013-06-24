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

package com.clarkparsia.empire.ds;

/**
 * <p>Interface for something that supports transactions.  Usually used in conjunction with a
 * {@link MutableDataSource} to provide a transactional database.</p>
 *
 * @author Michael Grove
 * @see MutableDataSource
 *
 * @since 0.1
 * @version 0.7
 */
public interface SupportsTransactions {
	
	/**
	 * Begin a transaction
	 * @throws DataSourceException thrown if the transaction could not be started
	 */
	public void begin() throws DataSourceException;

	/**
	 * Commit the transaction
	 * @throws DataSourceException thrown if there is an error while commiting
	 */
	public void commit() throws DataSourceException;

	/**
	 * Rollback the current transaction
	 * @throws DataSourceException thrown if there is an error while rolling back the transaction
	 */
	public void rollback() throws DataSourceException;
}
