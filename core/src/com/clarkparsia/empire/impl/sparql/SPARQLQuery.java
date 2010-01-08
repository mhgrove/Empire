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

package com.clarkparsia.empire.impl.sparql;

import com.clarkparsia.sesame.utils.query.SPARQLQueryRenderer;
import org.openrdf.model.Value;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.impl.RdfQuery;

/**
 * <p>Extends the {@link com.clarkparsia.empire.impl.RdfQuery} class to provide support for queries in the SPARQL language.</p>
 *
 * @author Michael Grove
 * @since 0.1
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
