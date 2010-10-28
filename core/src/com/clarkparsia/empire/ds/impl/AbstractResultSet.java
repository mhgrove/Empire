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

package com.clarkparsia.empire.ds.impl;

import com.clarkparsia.empire.ds.ResultSet;

import java.util.Iterator;
import java.util.Collection;

import org.openrdf.query.BindingSet;

/**
 * <p>Abstract implementation of the {@link ResultSet} interface that is an adapter for the underlying Iterator.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 */
public abstract class AbstractResultSet implements ResultSet {

	/**
	 * The underlying iterator
	 */
	private Iterator<BindingSet> mIter;

	/**
	 * Create a new AbstractResultSet
	 * @param theIter the iterator to back this result set.
	 */
	public AbstractResultSet(final Iterator<BindingSet> theIter) {
		mIter = theIter;
	}

	/**
	 * Create a new AbstractResultSet
	 * @param theCollection the collection to back this result set.
	 */
	public AbstractResultSet(final Collection<BindingSet> theCollection) {
		this(theCollection.iterator());
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasNext() {
		return mIter.hasNext();
	}

	/**
	 * @inheritDoc
	 */
	public BindingSet next() {
		return mIter.next();
	}

	/**
	 * @inheritDoc
	 */
	public void remove() {
		mIter.remove();
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<BindingSet> iterator() {
		return this;
	}
}
