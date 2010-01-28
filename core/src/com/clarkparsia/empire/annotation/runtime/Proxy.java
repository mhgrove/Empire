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

import com.clarkparsia.empire.DataSource;

import com.clarkparsia.empire.annotation.RdfGenerator;

import java.net.URI;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class Proxy<T> {
	private T mValue;
	private Class<T> mClass;
	private URI mURI;
	private DataSource mDataSource;

	public Proxy(Class<T> theClass, URI theURI, DataSource theSource) {
		mClass = theClass;
		mURI = theURI;
		mDataSource = theSource;
	}

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
}
