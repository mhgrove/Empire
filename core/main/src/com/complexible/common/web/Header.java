/*
 * Copyright (c) 2005-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.complexible.common.util.Tuple;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 * <p>Represents an HTTP header, either from a request or a response.</p>
 *
 * @author Michael Grove
 * @see Request
 * @see Response
 * @since 1.0
 * @version 2.0
 */
@Deprecated
public final class Header {

	/**
	 * The name of the header. {@link HttpHeaders} is an enumeration of common header names.
	 */
	private String mName;

	/**
	 * The list of values for the header
	 */
	private Map<String, String> mValues = new HashMap<String, String>();
	
	/**
	 * The list of raw values for the header (i.e., before parsing/splitting)
	 */
	private List<String> mRawValues = new LinkedList<String>();

	/**
	 * Create a new HTTP header
	 * @param theName the name of the header attribute
	 * @param theValue the singleton value of the header
	 */
	public Header(final String theName, String theValue) {
		mName = theName;
		addValue(theValue);
	}

	/**
	 * Create a new HTTP header
	 * @param theName the name of the header attribute
	 * @param theValues the values of the HTTP header
	 */
	public Header(final String theName, final List<String> theValues) {
		mName = theName;

		for (String aValue : theValues) {
			addValue(aValue);
		}
	}

	/**
	 * Create a new HTTP header
	 * @param theName the name of the header attribute
	 * @param theValues the values of the HTTP header
	 */
	public Header(final String theName, final Map<String, String> theValues) {
		mName = theName;
		mValues = theValues;
				
		// at this point there is no information about "raw" values 
		// (need to "recreate" the "raw" version)
		mRawValues.add(mapToValue(theValues));
	}

	/**
	 * Add a key-value pair to this header
	 * @param theName the name of the header element
	 * @param theValue the value of the element
	 * @return this Header object
	 */
	public Header addValue(String theName, String theValue) {
		addValues(Collections.singletonMap(theName, theValue));

		return this;
	}

	/**
	 * Add a value to the header
	 * @param theValue the value to add
	 */
	void addValue(String theValue) {
		if (theValue == null) {
			return;
		}

		if (theValue.indexOf(";") != -1) {
			for (String aKeyValuePair : Splitter.on(";").trimResults().omitEmptyStrings().split(theValue)) {
				Tuple aTuple = split(aKeyValuePair);
				mValues.put(aTuple.<String>get(0), (aTuple.length() < 2 ? null : aTuple.<String>get(1)));
			}
		}
		else if (theValue.indexOf("=") != -1) {
			Tuple aTuple = split(theValue);
			mValues.put(aTuple.<String>get(0), (aTuple.length() < 2 ? null : aTuple.<String>get(1)));
		}
		else {
			mValues.put(null, theValue);
		}
		
		mRawValues.add(theValue);
	}

	/**
	 * Add all the values to the header
	 * @param theValues the values to add
	 */
	void addValues(Map<String, String> theValues) {
		mValues.putAll(theValues);
		
		// at this point there is no information about "raw" values 
		// (need to "recreate" the "raw" version)
		mRawValues.add(mapToValue(theValues));
	}

	/**
	 * The name of the HTTP header.  Common HTTP header names can be found in {@link HttpHeaders}
	 * @return the header name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Returns the values of the HTTP header
	 * @return the header values
	 */
	public Map<String, String> getValues() {
		return mValues;
	}
	
	/**
	 * Return the raw values of the HTTP header (i.e., before any processing/splitting has
	 * occurred).
	 * 
	 * @return the list of raw header values (one for each header occurrence)
	 */
	public Collection<String> getRawValues() {
		return mRawValues;
	}
	
	/**
	 * Return the value of the header element
	 * @param theKey the name of the header element, or null to get the value of the header
	 * @return the value, or null if one is not specified
	 */
	public String getValue(String theKey) {
		return getValues().get(theKey);
	}

	/**
	 * Splits the key-value string on the = sign.
	 * @param theKeyValue the key/value string 
	 * @return a 2-tuple of strings with the key and value.
	 */
	private Tuple split(String theKeyValue) {
		List<String> aStrings = Lists.newArrayList(Splitter
													   .on("=")
													   .trimResults()
													   .omitEmptyStrings()
													   .split(theKeyValue));

		return new Tuple((Object[])aStrings.toArray(new String[aStrings.size()]));
	}

	/**
	 * Return the value(s) of the header as a semi-colon separated string.  For example, if your values are "foo", "bar"
	 * and "baz" this will return the string "foo; bar; baz"
	 * @return the string encoded reprsentation of the header values suitable for insertion into a HTTP request
	 */
	public String getHeaderValue() {
		return mapToValue(getValues());
	}
	
	/**
	 * Converts a map of key-value pairs to a semi-colon separated string.  For example, if your values are "foo", "bar"
	 * and "baz" this will return the string "foo; bar; baz"
	 * @param theValues a map to be converted
	 * @return the string encoded reprsentation of the header values suitable for insertion into a HTTP request
	 */
	private static String mapToValue(Map<String,String> theValues) {
		StringBuffer aBuffer = new StringBuffer();

		boolean aFirst = true;
		for (Map.Entry<String, String> aEntry : theValues.entrySet()) {

			if (!aFirst) {
				aBuffer.append("; ");
			}

			aFirst = false;

			if (aEntry.getKey() != null) {
				aBuffer.append(aEntry.getKey()).append("=");
			}
			
			aBuffer.append(aEntry.getValue());
		}

		return aBuffer.toString();		
	}
	
	/**
	 * Return a raw header value (i.e., before any processing/splitting). If the header was mentioned
	 * multiple times, this method returns all the header values concatenated together and separated by a comma
	 * (RFC 2616, Section 4.2 Message Headers: "It MUST be possible to combine the multiple header fields into one 
	 * (..) without changing the semantics of the message, by appending each subsequent field-value to the first, 
	 * each separated by a comma").
	 * 
	 * @return a single string that contain the raw header values concatenated together, separated by a comma
	 */
	public String getRawHeaderValue() {
		StringBuffer aBuffer = new StringBuffer();
		
		boolean aFirst = true;
		for (String aRawValue : getRawValues()) {
			if (!aFirst) {
				aBuffer.append(", ");
			}

			aFirst = false;

			aBuffer.append(aRawValue);
		}
		
		return aBuffer.toString();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return getName() + " [" + getHeaderValue()  + "]";
	}
}
