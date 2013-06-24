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

package com.clarkparsia.empire.ds;

import com.clarkparsia.empire.EmpireException;

/**
 * <p>Parent Exception for operations on a {@link DataSource}</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 * @see DataSource
 */
public class DataSourceException extends EmpireException {

	/**
	 * Create a new DataSourceException
	 */
	public DataSourceException() {
	}

	/**
	 * Create a new DataSourceException
	 * @param theMessage the error message
	 */
	public DataSourceException(final String theMessage) {
		super(theMessage);
	}

	/**
	 * Create a new DataSourceException
	 * @param theMessage the error message
	 * @param theCause the error cause
	 */
	public DataSourceException(final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
	}

	/**
	 * Create a new DataSourceException
	 * @param theCause the error cause
	 */
	public DataSourceException(final Throwable theCause) {
		super(theCause);
	}
}
