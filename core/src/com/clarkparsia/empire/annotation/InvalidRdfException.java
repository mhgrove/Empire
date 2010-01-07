package com.clarkparsia.empire.annotation;

/**
 * Title: InvalidRdfException<br/>
 * Description: Exception for when {@link RdfGenerator} operations cannot be performed<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 4:23:53 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class InvalidRdfException extends Exception {

	/**
	 * Create a new InvalidRdfException
	 * @param theMessage the error message
	 */
	public InvalidRdfException(final String theMessage) {
		super(theMessage);
	}

	/**
	 * Create a new InvalidRdfException
	 * @param theCause the error cause
	 */
	public InvalidRdfException(final Throwable theCause) {
		super(theCause);
	}

	/**
	 * Create a new InvalidRdfException
	 * @param theMessage the error message
	 * @param theCause the error cause
	 */
	public InvalidRdfException(final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
	}
}
