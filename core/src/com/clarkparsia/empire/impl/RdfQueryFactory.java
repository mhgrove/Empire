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

package com.clarkparsia.empire.impl;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.QueryFactory;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.Dialect;

import javax.persistence.Query;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.NamedQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedNativeQueries;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Arrays;

/**
 * <p>Implements the common operations of a {@link QueryFactory} and defers query language specific operations
 * to concrete implementations of this class.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.5
 */
public class RdfQueryFactory implements QueryFactory {
	/**
	 * the data source the queries will be executed against
	 */
	private DataSource mSource;

	/**
	 * The query language dialect supported by this factory
	 */
	private Dialect mDialect;

	/**
	 * User-defined NamedQueries.  The actual queries are evaluated on-demand, we'll just keep the annotations which
	 * contain the information needed to create them here.
	 */
	private Map<String, NamedQueryInfo> mNamedQueries = new HashMap<String, NamedQueryInfo>();

	/**
	 * Create a new AbstractQueryFactory
	 * @param theSource the data source the queries will be executed against
	 * @param theDialect the Query dialect supporte by this query factory
	 */
	public RdfQueryFactory(final DataSource theSource, Dialect theDialect) {
		mSource = theSource;
		mDialect = theDialect;

		Collection<Class<?>> aClasses = Empire.get().getAnnotationProvider().getClassesWithAnnotation(NamedQuery.class);
		for (Class<?> aClass :  aClasses) {
			addNamedQuery(new NamedQueryInfo(aClass.getAnnotation(NamedQuery.class)));
		}

		aClasses = Empire.get().getAnnotationProvider().getClassesWithAnnotation(NamedQueries.class);
		for (Class<?> aClass : aClasses) {
			NamedQueries aNamedQueries = aClass.getAnnotation(NamedQueries.class);

			for (NamedQuery aQuery : aNamedQueries.value()) {
				addNamedQuery(new NamedQueryInfo(aQuery));
			}
		}

		aClasses = Empire.get().getAnnotationProvider().getClassesWithAnnotation(NamedNativeQuery.class);
		for (Class<?> aClass : aClasses) {
			addNamedQuery(new NamedQueryInfo(aClass.getAnnotation(NamedNativeQuery.class)));
		}

		aClasses = Empire.get().getAnnotationProvider().getClassesWithAnnotation(NamedNativeQueries.class);
		for (Class<?> aClass : aClasses) {
			NamedNativeQueries aQueries = aClass.getAnnotation(NamedNativeQueries.class);
			for (NamedNativeQuery aQuery : aQueries.value()) {
				addNamedQuery(new NamedQueryInfo(aQuery));
			}
		}
	}

	/**
	 * Create a new Query against the current data source with the given query string
	 * @param theQuery the query string
	 * @return a new query
	 */
	protected RdfQuery newQuery(String theQuery) {
		return new RdfQuery(mSource, theQuery);
	}

	/**
	 * Return the data source the queries will be executed against
	 * @return the data source
	 */
	protected DataSource getSource() {
		return mSource;
	}

	/**
	 * Add a named query to this factory
	 * @param theInfo the information about the query
	 */
	private void addNamedQuery(NamedQueryInfo theInfo) {
		mNamedQueries.put(theInfo.getName(), theInfo);
	}

	/**
	 * @inheritDoc
	 */
	public Dialect getDialect() {
		return mDialect;
	}

	/**
	 * @inheritDoc
	 */
	public Query createQuery(final String theQueryString) {
		return newQuery(theQueryString);
	}

	/**
	 * @inheritDoc
	 */
	public Query createNamedQuery(final String theName) {
		if (mNamedQueries.containsKey(theName)) {
			NamedQueryInfo aNamedQuery = mNamedQueries.get(theName);

			RdfQuery aQuery = newQuery(aNamedQuery.getQuery());
			for (QueryHint aHint : aNamedQuery.getHints()) {
				aQuery.setHint(aHint.name(), aHint.value());
			}

			aQuery.setSource(getSource());

			return aQuery;
		}
		else {
			throw new IllegalArgumentException("Query named '" + theName + "' does not exist.");
		}
	}

	/**
	 * @inheritDoc
	 */
	public Query createNativeQuery(final String theQueryString) {
		return newQuery(theQueryString);
	}

	/**
	 * @inheritDoc
	 */
	public Query createNativeQuery(final String theQueryString, final Class theResultClass) {
		RdfQuery aQuery = newQuery(theQueryString);

		aQuery.setBeanClass(theResultClass);

		return aQuery;
	}

	/**
	 * @inheritDoc
	 */
	public Query createNativeQuery(final String theQueryString, final String theResultSetMapping) {
		throw new UnsupportedOperationException();
	}

	private class NamedQueryInfo {
		private String mName;
		private String mQuery;
		private Class mResultClass;
		private Collection<QueryHint> mHints;
		private String mResultMapping;

		private NamedQueryInfo(final String theName, final String theQuery) {
			mName = theName;
			mQuery = theQuery;
		}

		private NamedQueryInfo(NamedQuery theQuery) {
			mName = theQuery.name();
			mQuery = theQuery.query();
			mHints = Arrays.asList(theQuery.hints());
		}

		private NamedQueryInfo(NamedNativeQuery theQuery) {
			mName = theQuery.name();
			mQuery = theQuery.query();
			mResultMapping = theQuery.resultSetMapping();
			mResultClass = theQuery.resultClass();
			mHints = Arrays.asList(theQuery.hints());
		}

		public String getName() {
			return mName;
		}

		public void setName(final String theName) {
			mName = theName;
		}

		public String getQuery() {
			return mQuery;
		}

		public void setQuery(final String theQuery) {
			mQuery = theQuery;
		}

		public Class getResultClass() {
			return mResultClass;
		}

		public void setResultClass(final Class theResultClass) {
			mResultClass = theResultClass;
		}

		public Collection<QueryHint> getHints() {
			return mHints;
		}

		public void setHints(final Collection<QueryHint> theHints) {
			mHints = theHints;
		}

		public String getResultMapping() {
			return mResultMapping;
		}

		public void setResultMapping(final String theResultMapping) {
			mResultMapping = theResultMapping;
		}
	}
}
