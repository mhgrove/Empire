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

package com.clarkparsia.empire.annotation;

/**
 * <p>Exception for when {@link RdfGenerator} operations cannot be performed</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class InvalidRdfException extends Exception {

	/**
	 * Create a new InvalidRdfException
	 * @param theMessage the error message
	 */
	public InvalidRdfException(final String theMessage) {
		super(theMessage);
	}

	/**
	 * Create a new InvalidRdfException
	 * @param theCause the error cause
	 */
	public InvalidRdfException(final Throwable theCause) {
		super(theCause);
	}

	/**
	 * Create a new InvalidRdfException
	 * @param theMessage the error message
	 * @param theCause the error cause
	 */
	public InvalidRdfException(final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
	}
}
