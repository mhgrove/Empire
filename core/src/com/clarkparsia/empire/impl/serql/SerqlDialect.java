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

import com.clarkparsia.empire.Dialect;
import com.clarkparsia.empire.impl.RdfQuery;
import com.clarkparsia.openrdf.query.SesameQueryUtils;

import com.clarkparsia.common.util.PrefixMapping;

import org.openrdf.model.Value;
import org.openrdf.query.parser.serql.SeRQLParserFactory;
import org.openrdf.query.MalformedQueryException;

/**
 * <p>Represents the SERQL query language</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7.1
 */
public final class SerqlDialect implements Dialect {
	/**
	 * The singleton instance
	 */
	private static SerqlDialect INSTANCE;

	/**
	 * Create a new SerqlDialect, private to protect access.
	 */
	private SerqlDialect() {
	}

	/**
	 * Return the instance of SerqlDialect
	 * @return the instance
	 */
	public static SerqlDialect instance() {
		if (INSTANCE == null) {
			INSTANCE = new SerqlDialect();
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
		return SesameQueryUtils.getSerqlQueryString(theValue);
	}

	private boolean startsWithKeyword(String theQuery) {
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
			new SeRQLParserFactory().getParser().parseQuery(aBuffer.toString(), "");
		}
		catch (MalformedQueryException e) {
			throw new IllegalArgumentException("Invalid query: " + aBuffer.toString(), e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public String asProjectionVar(String theName) {
		return theName;
	}

	/**
	 * @inheritDoc
	 */
	public String patternKeyword() {
		return "from";
	}

	/**
	 * @inheritDoc
	 */
	public void insertNamespaces(final StringBuffer theBuffer) {
		StringBuffer aNS = new StringBuffer();
		boolean aFirst = true;
		for (String aPrefix : PrefixMapping.GLOBAL.getPrefixes()) {
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

			aNS.append(aPrefix).append(" = <").append(PrefixMapping.GLOBAL.getNamespace(aPrefix)).append(">");
		}

		theBuffer.append("\n").append(aNS);
	}


	/**
	 * @inheritDoc
	 */
	public String asVar(String theVar) {
		if (theVar == null) {
			return "";
		}
		else {
			return theVar.replaceAll("\\?", "");
		}
	}
}
