package com.clarkparsia.empire.impl.serql;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.Dialect;
import com.clarkparsia.empire.impl.AbstractQueryFactory;
import com.clarkparsia.empire.impl.serql.SerqlDialect;

/**
 * Title: SerqlQueryFactory<br/>
 * Description: Implementation of a {@link com.clarkparsia.empire.QueryFactory} representing the SERQL query language.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 15, 2009 3:36:02 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class SerqlQueryFactory extends AbstractQueryFactory<SerqlQuery> {
	/**
	 * Create a new SerqlQueryFactory
	 *
	 * @param theSource the data source the queries will be generated against
	 */
	public SerqlQueryFactory(final DataSource theSource) {
		super(theSource);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected SerqlQuery newQuery(final String theQuery) {
		return new SerqlQuery(getSource(), theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public Dialect getDialect() {
		return SerqlDialect.instance();
	}
}
