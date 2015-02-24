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
 * <p>Enumeration of common http headers.</p>
 *
 * @author Michael Grove
 * @since 1.1
 */
@Deprecated
public enum HttpHeaders {

	Accept("Accept"),
	Authentication("Authentication"),
	ContentDisposition("Content-Disposition"),
	ContentLength("Content-Length"),
	ContentType("Content-Type"),
	TransferEncoding("Transfer-Encoding");

	/**
	 * The name of the HTTP header
	 */
	private String mName;

	/**
	 * Create a new HTTP header key
	 * @param theName the name of the header
	 */
	HttpHeaders(final String theName) {
		mName = theName;
	}

	/**
	 * Return the name of the header
	 * @return the header name
	 */
	public String getName() {
		return mName;
	}
}
