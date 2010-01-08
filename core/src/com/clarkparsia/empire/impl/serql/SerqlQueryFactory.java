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
import com.clarkparsia.empire.Dialect;
import com.clarkparsia.empire.impl.AbstractQueryFactory;
import com.clarkparsia.empire.impl.serql.SerqlDialect;

/**
 * <p>Implementation of a {@link com.clarkparsia.empire.QueryFactory} representing the SeRQL query language.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @see SerqlQuery
 */
public class SerqlQueryFactory extends AbstractQueryFactory<SerqlQuery> {
	/**
	 * Create a new SerqlQueryFactory
	 *
	 * @param theSource the data source the queries will be generated against
	 */
	public SerqlQueryFactory(final DataSource theSource) {
		super(theSource);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected SerqlQuery newQuery(final String theQuery) {
		return new SerqlQuery(getSource(), theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public Dialect getDialect() {
		return SerqlDialect.instance();
	}
}
