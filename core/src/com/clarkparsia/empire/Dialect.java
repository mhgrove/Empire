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

package com.clarkparsia.empire;

import org.openrdf.model.Value;

/**
 * <p>Interface for specifying the query language dialect supported by a data source and providing language specific
 * opterations such as validation, and serialization.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.5
 *
 * @see com.clarkparsia.empire.impl.RdfQuery
 * @see QueryFactory
 * @see DataSource
 */
public interface Dialect {

	/**
	 * Return the Value object in a properly encoded query string for the supported query language
	 * @param theValue the value to encode
	 * @return the value as a valid query string element.
	 */
	public String asQueryString(Value theValue);

	/**
	 * Validate that the query fragment is valid for the query language supported
	 * by this query dialect.
	 * @param theQuery the query fragment
	 * @param theProjectionVarName the name of the var used in the projection
	 */
	public void validateQueryFormat(final String theQuery, final String theProjectionVarName);

	/**
	 * Return the variable name in a suitable format for insertion into the projection of the query
	 * @param theVar the projection variable name
	 * @return the projection variable in the correct syntax to be inserted into the project clause of the query
	 */
	public String asProjectionVar(String theVar);

	/**
	 * Return the keyword that denotes the start of graph patterns in the query.
	 * @return the pattern keyword for the language represented by this query
	 */
	public String patternKeyword();

	/**
	 * Insert all of the global namespaces into the query string so that declared namespace prefixes are available
	 * in all queries.
	 * @param theBuffer the buffer containing the current, complete query without namespaces.
	 */
	public void insertNamespaces(StringBuffer theBuffer);

	/**
	 * Return the variable name as a syntactically correct variable for use in an query atom.
	 * @param theVar the variable name, or null for an unnamed variable
	 * @return the var name in the correct syntax for the dialect
	 */
	public String asVar(String theVar);
}
