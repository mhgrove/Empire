package com.clarkparsia.empire;

/**
 * Title: SupportsTransactions<br/>
 * Description: Interface for something that supports Transactions.<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
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
