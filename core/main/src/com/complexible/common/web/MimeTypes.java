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
 * <p>Enumeration of common mime-types.</p>
 *
 * @author Michael Grove
 */
@Deprecated
public enum MimeTypes {
	FormUrlEncoded("application/x-www-form-urlencoded"),
	TextPlain("text/plain");

	/**
	 * The mime-type string
	 */
	private String mType;

	/**
	 * Create the new MimeType
	 * @param theType the mime type
	 */
	MimeTypes(final String theType) {
		mType = theType;
	}

	/**
	 * Return the mime-type
	 * @return the mime-type string
	 */
	public String getMimeType() {
		return mType;
	}
}
