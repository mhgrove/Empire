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

import com.clarkparsia.empire.DataSource;

import com.clarkparsia.openrdf.query.SesameQueryUtils;

import org.openrdf.model.Value;

import org.openrdf.query.parser.serql.SeRQLParserFactory;

import org.openrdf.query.MalformedQueryException;

import com.clarkparsia.empire.impl.RdfQuery;
import com.clarkparsia.utils.NamespaceUtils;

/**
 * <p>Extends the {@link com.clarkparsia.empire.impl.RdfQuery} class to provide support for queries in the
 * SeRQL language.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @since 0.6.1
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
		aQuery = aQuery.replaceAll(VT_RE, asProjectionVar("x"));

		if (!aQuery.startsWith("select") && !aQuery.startsWith("construct")) {
            if (!aQuery.contains(patternKeyword())) {
                aQuery = " " + patternKeyword() + " " + aQuery;
            }

			aQuery = "select " + asProjectionVar(MAGIC_PROJECTION_VAR) + " " + aQuery;
		}

		StringBuffer aBuffer = new StringBuffer(aQuery);

		insertNamespaces(aBuffer);

		try {
			new SeRQLParserFactory().getParser().parseQuery(aBuffer.toString(), "");
		}
		catch (MalformedQueryException e) {
			throw new IllegalArgumentException("Invalid query: " + aQuery, e);
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

	/**
	 * @inheritDoc
	 */
	protected void insertNamespaces(final StringBuffer theBuffer) {
		StringBuffer aNS = new StringBuffer();
		boolean aFirst = true;
		for (String aPrefix : NamespaceUtils.prefixes()) {
			if (aPrefix.trim().equals("")) {
				continue;
			}
			
			if (aFirst) {
				aNS.append("using namespace\n");
				aFirst = false;
			}
			else {
				aNS.append(",\n");
			}

			aNS.append(aPrefix).append(" = <").append(NamespaceUtils.namespace(aPrefix)).append(">");
		}

		theBuffer.append("\n").append(aNS);
	}
}
