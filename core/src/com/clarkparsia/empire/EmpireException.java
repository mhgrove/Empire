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

package com.clarkparsia.empire;

/**
 * <p>The base exception for all Empire-related faults.</p>
 *
 * @author Michael Grove
 * @version 0.6.2
 * @since 0.6.2
 */
public class EmpireException extends Exception {

	/**
	 * Create a new EmpireException
	 */
	public EmpireException() {
	}

	/**
	 * Create a new EmpireException
	 * @param theMessage the error message
	 */
	public EmpireException(final String theMessage) {
		super(theMessage);
	}

	/**
	 * Create a new EmpireException
	 * @param theMessage the error message
	 * @param theCause Create a new EmpireException
	 */
	public EmpireException(final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
	}

	/**
	 * Create a new EmpireException
	 * @param theCause the root cause
	 */
	public EmpireException(final Throwable theCause) {
		super(theCause);
	}
}
