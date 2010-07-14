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

package com.clarkparsia.empire.sesametwo;

import com.clarkparsia.empire.ds.impl.AbstractResultSet;

import static com.clarkparsia.openrdf.OpenRdfUtil.toIterator;

import org.openrdf.query.TupleQueryResult;

import org.openrdf.query.QueryEvaluationException;

/**
 * <p>Simple extension of the {@link com.clarkparsia.empire.ds.impl.AbstractResultSet} to provide iteration over a Sesame 2.x result set and to
 * close the results when completed.</p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class TupleQueryResultSet extends AbstractResultSet {
	private TupleQueryResult mResults;

	public TupleQueryResultSet(final TupleQueryResult theResults) {
		super(toIterator(theResults));

		mResults = theResults;
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		try {
			mResults.close();
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
	}
}
