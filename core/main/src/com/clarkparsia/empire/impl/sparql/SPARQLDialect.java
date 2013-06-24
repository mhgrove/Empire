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

import com.clarkparsia.empire.Dialect;

import com.clarkparsia.empire.impl.RdfQuery;

import com.complexible.common.openrdf.query.SesameQueryUtils;
import com.complexible.common.util.PrefixMapping;

import org.openrdf.query.MalformedQueryException;

import org.openrdf.query.parser.sparql.SPARQLParserFactory;

import org.openrdf.model.Value;

/**
 * <p>Represents the SPARQL query language.</p>
 *
 * @author Michael Grove
 *
 * @since   0.1
 * @version 0.7.1
 */
public class SPARQLDialect implements Dialect {
	/**
	 * the singleton instance
	 */
	private static SPARQLDialect INSTANCE;

	/**
	 * Create a new SPARQLDialect
	 */
	protected SPARQLDialect() {
	}

	/**
	 * Return the single instance of SPARQLDialect
	 * @return the SPARQLDialect
	 */
	public static SPARQLDialect instance() {
		if (INSTANCE == null) {
			INSTANCE = new SPARQLDialect();
		}

		return INSTANCE;
	}

	/**
	 * @inheritDoc
	 */
	public boolean supportsStableBnodeIds() {
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String asQueryString(final Value theValue) {
		return SesameQueryUtils.getSPARQLQueryString(theValue);
	}

	private boolean startsWithKeyword(String theQuery) {
		// TODO: this will not work if there are already query prefixes, then the startsWith will fail, which is not good.
		// should probably use regex to jump past them and check that way, so some good regex sniffing would be good.
		
		String q = theQuery.toLowerCase().trim();
		return q.startsWith("select") || q.startsWith("construct") || q.startsWith("ask") || q.startsWith("describe");
	}

	/**
	 * @inheritDoc
	 */
	public void validateQueryFormat(final String theQuery, final String theProjectionVarName) {
		String aQuery = theQuery.toLowerCase().trim();
		aQuery = aQuery.replaceAll(RdfQuery.VT_RE, asProjectionVar("x"));

		if (!startsWithKeyword(aQuery)) {
            if (!aQuery.contains(patternKeyword())) {
                aQuery = " " + patternKeyword() + " " + aQuery;
            }

			aQuery = "select " + asProjectionVar(theProjectionVarName) + " " + aQuery;
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
	public String asProjectionVar(final String theVar) {
		return "?" + theVar;
	}

	/**
	 * @inheritDoc
	 */
	public String patternKeyword() {
		return "where";
	}

	/**
	 * @inheritDoc
	 */
	public void insertNamespaces(final StringBuffer theBuffer) {
		StringBuffer aNS = new StringBuffer();

		for (String aPrefix : PrefixMapping.GLOBAL.getPrefixes()) {
			if (aPrefix.trim().equals("")) {
				continue;
			}

			aNS.append("PREFIX ").append(aPrefix).append(": <").append(PrefixMapping.GLOBAL.getNamespace(aPrefix)).append(">\n");
		}

		if (aNS.length() > 0) {
			aNS.append("\n");
		}

		theBuffer.insert(0, aNS);
	}

	/**
	 * @inheritDoc
	 */
	public String asVar(String theVar) {
		if (theVar == null) {
			return "[]";
		}
		else {
			return "?" + theVar.replaceAll("\\?", "");
		}
	}
}
