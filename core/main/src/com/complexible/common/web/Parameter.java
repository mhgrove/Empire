/*
 * Copyright (c) 2005-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.common.web;

/**
 * <p>A representation of a parameter, simply a key-value pair.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
@Deprecated
public class Parameter {
	/**
	 * The parameter name
	 */
	private final String mName;

	/**
	 * The parameter value
	 */
	private final String mValue;

	/**
	 * Create a new Parameter
	 * @param theName the name of the parameter
	 * @param theValue the value of the parameter
	 */
	public Parameter(final String theName, final String theValue) {
		mName = theName;
		mValue = theValue;
	}

	/**
	 * Return the name of the parameter
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Return the value of the parameter
	 * @return the value
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return getName() + " = " + getValue();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return getName().hashCode() + getValue().hashCode();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object theObject) {
		return theObject instanceof Parameter
			   && ((Parameter)theObject).getName().equals(getName())
			   && ((Parameter)theObject).getValue().equals(getValue());
	}
}
