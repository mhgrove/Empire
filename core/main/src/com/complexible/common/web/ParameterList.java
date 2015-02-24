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

import com.complexible.common.base.Strings2;

import java.util.ArrayList;

/**
 * <p>A list of {@link Parameter} objects.</p>
 *
 * @author Michael Grove
 * @since 1.0
 * @version 2.0
 */
@Deprecated
public class ParameterList extends ArrayList<Parameter> {

	/**
	 * Adds a new parameter to the list
	 * @param theName the name of the parameter
	 * @param theValue the value of the parameter
	 * @return the list itself
	 */
	public ParameterList add(String theName, String theValue) {
		add(new Parameter(theName, theValue));
		return this;
	}

	/**
	 * Create a string representation of the list of parameters
	 * @param theEncode true if the values of the parameters should be URL encoded, false otherwise
	 * @return the params as a string
	 */
	private String str(boolean theEncode) {

		// guesstimate the size needed to serialize to a string
		int size = 0;
		for (int i = 0; i < size(); i++) {
			Parameter aParam = get(i);
			size += aParam.getName().length() + aParam.getValue().length() + 16 /* padding for url encoding of value */;
		}

		StringBuilder aBuffer = new StringBuilder(size);

		boolean aFirst = true;
		for (Parameter aParam : this) {

			if (!aFirst) {
				aBuffer.append('&');
			}
			
			aBuffer.append(aParam.getName());
			aBuffer.append('=');
			aBuffer.append(theEncode
						   ? Strings2.urlEncode(aParam.getValue())
						   : aParam.getValue());

			aFirst = false;
		}

		return aBuffer.toString();
	}

	/**
	 * Return the Parameters in the list in URL (encoded) form.  They will be & delimited and the values of each parameter
	 * will be encoded.  If you have two parameters a:b and c:"d d", the result is a=b&c=d+d
	 * @return the URL encoded parameter list in key-value pairs
	 */
	public String getURLEncoded() {
		return str(true /* encode */);
	}


	/**
	 * Functionally similar to {@link #getURLEncoded} but the values of the parameters are not URL encoded.
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return str(false /* don't encode */);
	}
}
