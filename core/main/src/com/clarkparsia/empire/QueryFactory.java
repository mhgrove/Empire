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

import javax.persistence.Query;

/**
 * <p>Factory interface for generating queries against a data source in a specific query language.  These
 * match up to the query factory methods in the {@link javax.persistence.EntityManager} interface.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @see javax.persistence.EntityManager
 */
public interface QueryFactory {
	/**
	 * Return the query dialect produced by this query factory
	 * @return the query dialect
	 */
	public Dialect getDialect();

	/**
	 * Create a query instance.  This can be a query fragment.
	 * @param theQueryString the query
	 * @return a query representing the given query string
	 * @throws IllegalArgumentException if the query string is not valid
	 */
	public Query createQuery(final String theQueryString);

	/**
	 * Retrieve a named query
	 * @param theName the name of the query
	 * @return the query with the given name
	 * @throws IllegalArgumentException if a query with the given name has not been defined
	 */
	public Query createNamedQuery(final String theName);

	/**
	 * Create a query instance from a fully defined query.
	 * @param theQueryString the query string
	 * @return a query representing the given query string
	 * @throws IllegalArgumentException if the query string is not valid
	 */
	public Query createNativeQuery(final String theQueryString);

	/**
	 * Create a query instance.
	 * @param theQueryString the query string
	 * @param theResultClass the class of the java beans to be returned as the results of this query
	 * @return a query representing the query string
	 * @throws IllegalArgumentException if the query string is not valid
	 */
	public Query createNativeQuery(final String theQueryString, final Class theResultClass);

	/**
	 * Create a query instance
	 * @param theQueryString the query string
	 * @param theResultSetMapping the result set mapping
	 * @return a query representing the query string
	 * @throws IllegalArgumentException if the query string is not valid
	 */
	public Query createNativeQuery(final String theQueryString, final String theResultSetMapping);
}
