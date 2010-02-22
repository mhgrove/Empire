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

import com.clarkparsia.openrdf.query.SesameQueryUtils;
import org.openrdf.model.Value;

import org.openrdf.query.parser.sparql.SPARQLParserFactory;

import org.openrdf.query.MalformedQueryException;

import com.clarkparsia.empire.DataSource;

import com.clarkparsia.empire.impl.RdfQuery;
import com.clarkparsia.utils.NamespaceUtils;

/**
 * <p>Extends the {@link com.clarkparsia.empire.impl.RdfQuery} class to provide support for queries in the SPARQL language.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.1
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
		return SesameQueryUtils.getQueryString(theValue);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void validateQueryFormat() {
		String aQuery = getQueryString().toLowerCase().trim();
		aQuery = aQuery.replaceAll(VT_RE, asProjectionVar("x"));

		if (!aQuery.startsWith("select") && !aQuery.startsWith("construct")) {
            if (!aQuery.contains(patternKeyword())) {
                aQuery = " " + patternKeyword() + " " + aQuery;
            }

			aQuery = "select " + asProjectionVar(getProjectionVarName()) + " " + aQuery;
		}

		StringBuffer aBuffer = new StringBuffer(aQuery);

		insertNamespaces(aBuffer);

		try {
			new SPARQLParserFactory().getParser().parseQuery(aBuffer.toString(), "http://example.org");
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid query: " + aBuffer.toString(), e);
		}
		catch (MalformedQueryException e) {
			throw new IllegalArgumentException("Invalid query: " + aBuffer.toString(), e);
		}
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

	/**
	 * @inheritDoc
	 */
	protected void insertNamespaces(final StringBuffer theBuffer) {
		StringBuffer aNS = new StringBuffer();

		for (String aPrefix : NamespaceUtils.prefixes()) {
			if (aPrefix.trim().equals("")) {
				continue;
			}

			aNS.append("PREFIX ").append(aPrefix).append(": <").append(NamespaceUtils.namespace(aPrefix)).append(">\n");
		}

		if (aNS.length() > 0) {
			aNS.append("\n");
		}

		theBuffer.insert(0, aNS);
	}
}
