package com.clarkparsia.empire.impl.serql;

import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.empire.DataSource;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.MalformedQueryException;
import com.clarkparsia.empire.impl.RdfQuery;

/**
 * Title: SerqlQuery<br/>
 * Description: Extends the {@link com.clarkparsia.empire.impl.RdfQuery} class to provide support for queries in the SERQL language.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 15, 2009 2:16:15 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class SerqlQuery extends RdfQuery {

	/**
	 * Create a new SERQL query
	 * @param theSource the source to evaluate a query against
	 * @param theQueryString the query string
	 */
	public SerqlQuery(final DataSource theSource, String theQueryString) {
		super(theSource, theQueryString);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String asQueryString(final Value theValue) {
		return SesameQueryUtils.getQueryString(theValue);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void validateQueryFormat() {
		String aQuery = getQueryString().toLowerCase().trim();
		aQuery = aQuery.replaceAll(VT_RE, "x");

		if (!aQuery.startsWith("select") && !aQuery.startsWith("construct")) {
            if (!aQuery.contains("from")) {
                aQuery = " from " + aQuery;
            }

			aQuery = "select " + MAGIC_PROJECTION_VAR + " " + aQuery;
		}

		try {
			if (aQuery.startsWith("select")) {
				SesameQueryUtils.tableQuery(aQuery);
			}
			else {
				SesameQueryUtils.graphQuery(aQuery);
			}
		}
		catch (MalformedQueryException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String asProjectionVar(String theName) {
		return theName;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String patternKeyword() {
		return "from";
	}
}
