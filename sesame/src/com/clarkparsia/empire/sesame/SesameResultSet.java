package com.clarkparsia.empire.sesame;

import com.clarkparsia.empire.impl.AbstractResultSet;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;

import org.openrdf.sesame.query.QueryResultsTable;

/**
 * Title: SesameResultSet<br/>
 * Description: Implementation of the {@link com.clarkparsia.empire.ResultSet} interface backed by a Sesame
 * {@link org.openrdf.sesame.query.QueryResultsTable}<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 17, 2009 9:46:01 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SesameResultSet extends AbstractResultSet {
	/**
	 * Create a SesameResultSet
	 * @param theResults the results
	 */
	public SesameResultSet(final QueryResultsTable theResults) {
		this(IterableQueryResultsTable.iterable(theResults));
	}

	/**
	 * Create a SesameResultSet
	 * @param theResults the results
	 */
	public SesameResultSet(final IterableQueryResultsTable theResults) {
		super(theResults.iterator());
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		// no-op
	}
}