package com.clarkparsia.empire.impl.sparql;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.Dialect;
import com.clarkparsia.empire.impl.AbstractQueryFactory;

/**
 * Title: SPARQLQueryFactory<br/>
 * Description: Implementation of a {@link com.clarkparsia.empire.QueryFactory} for the SPARQL query language.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 16, 2009 9:53:25 AM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class SPARQLQueryFactory extends AbstractQueryFactory<SPARQLQuery> {
	/**
	 * Create a new SPARQLQueryFactory
	 *
	 * @param theSource the data source the queries will be executed against
	 */
	public SPARQLQueryFactory(final DataSource theSource) {
		super(theSource);
	}

	/**
	 * @inheritDoc
	 */
	public Dialect getDialect() {
		return SPARQLDialect.instance();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected SPARQLQuery newQuery(final String theQuery) {
		return new SPARQLQuery(getSource(), theQuery);
	}
}
