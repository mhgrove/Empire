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

package com.clarkparsia.empire.jena;

import java.util.Iterator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

import com.clarkparsia.empire.jena.util.JenaSesameUtils;

import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.MapBindingSet;


import com.clarkparsia.empire.ds.impl.AbstractResultSet;
import com.google.common.collect.Iterators;
import com.google.common.base.Function;

/**
 *<p>Implementation of an Empire ResultSet backed by a Jena ResultSet</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 */
class JenaResultSet extends AbstractResultSet {
	/**
	 * The Jena Results
	 */
	private QueryExecution mQueryExec;

	/**
	 * Create a new JenaResultSet
	 * @param theQueryExec The query execution context, so it can be closed when this result set has been used.
	 * @param theResults the Jena result set to back this ResultSet instance
	 */
	public JenaResultSet(final QueryExecution theQueryExec, final ResultSet theResults) {
		super(Iterators.<QuerySolution, BindingSet>transform(theResults, new ToSesameBinding()));

		mQueryExec = theQueryExec;
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		mQueryExec.close();
	}

	/**
	 * Function to convert from Jena QuerySolutions to Sesame query Bindings
	 */
	private static class ToSesameBinding implements Function<QuerySolution, BindingSet> {

		/**
		 * @inheritDoc
		 */
		public BindingSet apply(QuerySolution theIn) {
			MapBindingSet aMap = new MapBindingSet();

			Iterator<String> aIter = theIn.varNames();

			while (aIter.hasNext()) {
				String aVar = aIter.next();
				aMap.addBinding(aVar, JenaSesameUtils.asSesameValue(theIn.get(aVar)));
			}

			return aMap;
		}
	}
}
