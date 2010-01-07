package com.clarkparsia.empire.impl.sparql;

import com.clarkparsia.sesame.utils.query.SPARQLQueryRenderer;
import org.openrdf.model.Value;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.impl.RdfQuery;

/**
 * Title: SPARQLQuery<br/>
 * Description: Extends the {@link com.clarkparsia.empire.impl.RdfQuery} class to provide support for queries in the SPARQL language.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 16, 2009 9:53:16 AM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class SPARQLQuery extends RdfQuery {
	/**
	 * Create a new SPARQL query
	 * @param theSource the source to evaluate the query against
	 * @param theQueryString the query string
	 */
	public SPARQLQuery(final DataSource theSource, String theQueryString) {
		super(theSource, theQueryString);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String asQueryString(final Value theValue) {
		return SPARQLQueryRenderer.getSPARQLQueryString(theValue);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void validateQueryFormat() {
		// TODO: actually validate the partial format
		// We don't have a query parser like we do w/ serql, so there's no easy way to do this for now.
		// this means query exceptions that should be caught when the query is created will instead be caught when
		// its executed.  this violates the semantics of the JPA stuff, but it will do for now since you at least
		// "correctly" get a failure with an invalid query.
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String asProjectionVar(final String theVar) {
		return "?" + theVar;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String patternKeyword() {
		return "where";
	}
}
