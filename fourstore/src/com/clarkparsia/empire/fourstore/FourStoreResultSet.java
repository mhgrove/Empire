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

package com.clarkparsia.empire.fourstore;

import com.clarkparsia.empire.impl.AbstractResultSet;
import com.clarkparsia.sesame.utils.query.Binding;
import com.clarkparsia.utils.collections.CollectionUtil;
import com.clarkparsia.utils.Function;

import java.util.Map;
import java.util.HashMap;

import org.openrdf.model.Value;

import com.clarkparsia.fourstore.impl.sesame.FourStoreToSesame;

/**
 * <p>Implementation of an Empire ResultSet interface backed by a 4Store result set.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class FourStoreResultSet extends AbstractResultSet {

	/**
	 * Create a new FourStoreResultSet
	 * @param theResults the FourStore results that will back this ResultSet
	 */
	FourStoreResultSet(final com.clarkparsia.fourstore.api.results.ResultSet theResults) {
		super(new CollectionUtil.TransformingIterator<com.clarkparsia.fourstore.api.results.Binding, Binding>(theResults.iterator(),
																							   new ToSesameBinding()));
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		// no-op
	}

	/**
	 * Function to transform a FourStore API binding to a Sesame binding
	 */
	private static class ToSesameBinding implements Function<com.clarkparsia.fourstore.api.results.Binding, Binding> {

		/**
		 * @inheritDoc
		 */
		public Binding apply(final com.clarkparsia.fourstore.api.results.Binding theIn) {
			Map<String, Value> aMap = new HashMap<String, Value>();

			for (String aVar : theIn.variables()) {
				aMap.put(aVar, FourStoreToSesame.toSesameValue(theIn.get(aVar)));
			}

			return new Binding(aMap);
		}
	}
}
