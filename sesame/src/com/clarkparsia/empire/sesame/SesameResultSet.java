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

package com.clarkparsia.empire.sesame;

import com.clarkparsia.empire.impl.AbstractResultSet;
import com.clarkparsia.sesame.utils.query.IterableQueryResultsTable;

import org.openrdf.sesame.query.QueryResultsTable;

/**
 * <p>Implementation of the {@link com.clarkparsia.empire.ResultSet} interface backed by a Sesame
 * {@link org.openrdf.sesame.query.QueryResultsTable}</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class SesameResultSet extends AbstractResultSet {
	/**
	 * Create a SesameResultSet
	 * @param theResults the results
	 */
	public SesameResultSet(final QueryResultsTable theResults) {
		this(IterableQueryResultsTable.iterable(theResults));
	}

	/**
	 * Create a SesameResultSet
	 * @param theResults the results
	 */
	public SesameResultSet(final IterableQueryResultsTable theResults) {
		super(theResults.iterator());
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		// no-op
	}
}