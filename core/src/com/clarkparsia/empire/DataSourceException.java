package com.clarkparsia.empire;

/**
 * Title: DataSourceException<br/>
 * Description: Parent Exception for operations on a {@link com.clarkparsia.empire.DataSource}<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 14, 2009 11:17:47 AM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class DataSourceException extends Exception {

	/**
	 * Create a new DataSourceException
	 */
	public DataSourceException() {
	}

	/**
	 * Create a new DataSourceException
	 * @param theMessage the error message
	 */
	public DataSourceException(final String theMessage) {
		super(theMessage);
	}

	/**
	 * Create a new DataSourceException
	 * @param theMessage the error message
	 * @param theCause the error cause
	 */
	public DataSourceException(final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
	}

	/**
	 * Create a new DataSourceException
	 * @param theCause the error cause
	 */
	public DataSourceException(final Throwable theCause) {
		super(theCause);
	}
}
