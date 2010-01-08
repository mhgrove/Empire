/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.empire.impl.serql;

import com.clarkparsia.sesame.utils.query.SesameQueryUtils;
import com.clarkparsia.empire.DataSource;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.MalformedQueryException;
import com.clarkparsia.empire.impl.RdfQuery;

/**
 * <p>Extends the {@link com.clarkparsia.empire.impl.RdfQuery} class to provide support for queries in the
 * SeRQL language.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @see SerqlQueryFactory
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
