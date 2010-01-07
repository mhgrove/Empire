package com.clarkparsia.empire.impl.serql;

import com.clarkparsia.empire.Dialect;

/**
 * Title: SerqlDialect<br/>
 * Description: Represents the SERQL query language<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 16, 2009 9:47:25 AM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class SerqlDialect implements Dialect {
	/**
	 * The singleton instance
	 */
	private static SerqlDialect INSTANCE;

	/**
	 * Create a new SerqlDialect, private to protect access.
	 */
	private SerqlDialect() {
	}

	/**
	 * Return the instance of SerqlDialect
	 * @return the instance
	 */
	public static SerqlDialect instance() {
		if (INSTANCE == null) {
			INSTANCE = new SerqlDialect();
		}

		return INSTANCE;
	}
}
