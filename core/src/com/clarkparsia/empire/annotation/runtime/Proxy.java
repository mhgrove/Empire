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

package com.clarkparsia.empire.annotation.runtime;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.SupportsRdfId;

import com.clarkparsia.empire.annotation.RdfGenerator;

/**
 * <p>Wrapper class which serves as a proxy for an object to the database.</p>
 *
 * @author Michael Grove
 * @since 0.5
 * @version 0.7
 */
public class Proxy<T> {

	/**
	 * The value this is a proxy for
	 */
	private T mValue;

	/**
	 * The type of the value
	 */
	private Class<T> mClass;

	/**
	 * The key of the value
	 */
	private SupportsRdfId.RdfKey mURI;

	/**
	 * The datasource to retrieve the value from
	 */
	private DataSource mDataSource;

	/**
	 * Create a new Proxy object
	 * @param theClass the type of the object
	 * @param theKey the database key of the object
	 * @param theSource the database to grab the proxied object from
	 */
	public Proxy(Class<T> theClass, SupportsRdfId.RdfKey theKey, DataSource theSource) {
		mClass = theClass;
		mURI = theKey;
		mDataSource = theSource;
	}

	/**
	 * Return the value backing this proxy.  This will either be a cached copy of the already retrieved value, or
	 * it will actually go and grab the object from the database.
	 * @return the value this class proxies for
	 */
	public T value() {
		if (mValue == null) {
			try {
				mValue = RdfGenerator.fromRdf(mClass, mURI, mDataSource);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return mValue;
	}

	/**
	 * Return the type of the object this is proxying for
	 * @return the object type
	 * @see #value
	 */
	public Class<T> getProxyClass() {
		return mClass;
	}
}
