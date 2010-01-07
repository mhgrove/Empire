package com.clarkparsia.empire;

/**
 * Title: QueryException<br/>
 * Description: Specific type of {@link DataSourceException} that occurs when there is an error while performing or
 * parsing a query against a data source.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 14, 2009 1:26:07 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class QueryException extends DataSourceException {
	/**
	 * Create a new QueryException
	 */
	public QueryException() {
	}

	/**
	 * Create a new QueryException
	 * @param theMessage the error message
	 */
	public QueryException(final String theMessage) {
		super(theMessage);
	}

	/**
	 * Create a new QueryException
	 * @param theMessage the error message
	 * @param theCause the error cause
	 */
	public QueryException(final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
	}

	/**
	 * Create a new QueryException
	 * @param theCause the error cause
	 */
	public QueryException(final Throwable theCause) {
		super(theCause);
	}
}
