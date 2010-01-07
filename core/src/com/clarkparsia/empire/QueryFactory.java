package com.clarkparsia.empire;

import javax.persistence.Query;

/**
 * Title: QueryFactory<br/>
 * Description: Factory interface for generating queries against a data source in a specific query language.  These
 * match up to the query factory methods in the {@link javax.persistence.EntityManager} interface.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 14, 2009 4:03:56 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
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
