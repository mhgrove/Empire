package com.clarkparsia.empire.impl.sparql;

import com.clarkparsia.empire.Dialect;

/**
 * Title: SPARQLDialect<br/>
 * Description: Represents the SPARQL query language.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 16, 2009 9:49:11 AM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class SPARQLDialect implements Dialect {
	/**
	 * the singleton instance
	 */
	private static SPARQLDialect INSTANCE;

	/**
	 * Create a new SPARQLDialect
	 */
	private SPARQLDialect() {
	}

	/**
	 * Return the single instance of SPARQLDialect
	 * @return the SPARQLDialect
	 */
	public static SPARQLDialect instance() {
		if (INSTANCE == null) {
			INSTANCE = new SPARQLDialect();
		}

		return INSTANCE;
	}
}
