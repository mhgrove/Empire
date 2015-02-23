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

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.base.Charsets;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 1.0
 * @version 2.0
 */
@Deprecated
public class Request {
	private URL mURL;
	private Method mMethod;
	private ParameterList mParameters = new ParameterList();
	private InputStream mBody;
	private Map<String, Header> mHeaders = new HashMap<String, Header>();

	private int mTimeout = -1;
	private boolean mFollowRedirects;

	public Request(String theURL) throws MalformedURLException {
		this(Method.GET, new URL(theURL));
	}

	public Request(URL theURL) {
		this(Method.GET, theURL);
	}

	public Request(Method theMethod, URL theURL) {
		mMethod = theMethod;
		mURL = theURL;
	}

	public static Request formPost(URL theURL, ParameterList theParams) {
		Request aRequest = new Request(Method.POST, theURL);
		aRequest.addHeader(new Header(HttpHeaders.ContentType.getName(), MimeTypes.FormUrlEncoded.getMimeType()));
		aRequest.setBody(theParams.toString());

		return aRequest;
	}

	/**
	 * Return the current timeout value
	 * @return the timeout value in milliseconds or -1 for no timeout
	 */
	public int getTimeout() {
		return mTimeout;
	}

	/**
	 * Set the timeout associated associated with this request
	 * @param theTimeout the timeout in milliseconds, or -1 for no timeout
	 * @return this request
	 */
	public Request setTimeout(final int theTimeout) {
		mTimeout = theTimeout;

		return this;
	}

	/**
	 * Return whether or not this request will follow redirects
	 * @return true to follow redirects, false otherwise
	 */
	public boolean isFollowRedirects() {
		return mFollowRedirects;
	}

	/**
	 * Set whether or not this request will follow redirects
	 * @param theFollowRedirects true to follow redirects, false otherwise
	 * @return this request
	 */
	public Request setFollowRedirects(final boolean theFollowRedirects) {
		mFollowRedirects = theFollowRedirects;

		return this;
	}

	/**
	 * Add a parameter to this web request
	 * @param theKey the parameter key
	 * @param theValue the parameter value
	 * @return this request
	 */
	public Request addParameter(String theKey, String theValue) {
		return addParameter(new Parameter(theKey, theValue));
	}

	/**
	 * Adds a parameter to this web request
	 * @param theParameter the parameter to add
	 * @return this request
	 */
	public Request addParameter(Parameter theParameter) {
		mParameters.add(theParameter);

		return this;
	}

	/**
	 * Sets the list of parameters for this web request
	 * @param theParameters the list of parameters
	 * @return this request
	 */
	public Request setParameters(final ParameterList theParameters) {
		mParameters = theParameters;

		return this;
	}

	/**
	 * Add a header to this request
	 * @param theHeader the header to add
	 * @return this request
	 */
	public Request addHeader(Header theHeader) {
		if (mHeaders.containsKey(theHeader.getName())) {
			theHeader.addValues(mHeaders.get(theHeader.getName()).getValues());
		}

		mHeaders.put(theHeader.getName(), theHeader);

		return this;
	}

	public Request addHeader(String theName, String... theValue) {
		addHeader(new Header(theName, Arrays.asList(theValue)));

		return this;
	}

	public Request setBody(String theString) {
		mBody = new ByteArrayInputStream(theString.getBytes(Charsets.UTF_8));

		return this;
	}

	public Request setBody(final InputStream theBody) {
		mBody = theBody;

		return this;
	}

	public URL getURL() {
		return mURL;
	}

	public Method getMethod() {
		return mMethod;
	}

	public ParameterList getParameters() {
		return mParameters;
	}

	public InputStream getBody() {
		return mBody;
	}

	public Collection<Header> getHeaders() {
		return Collections.unmodifiableCollection(mHeaders.values());
	}

	private URL getURLWithParams() throws IOException {
		if (!getParameters().isEmpty()) {
			try {
				return new URL(getURL().toString() + "?" + getParameters().getURLEncoded());
			}
			catch (MalformedURLException e) {
				throw new IOException(e.getMessage());
			}
		}
		else {
			return getURL();
		}
	}

	public Header getHeader(String theName) {
		return mHeaders.get(theName);
	}

	public Response execute() throws IOException {

		// TODO: use-caches?, if-modified-since, HTTPS security twiddling, HTTP Authentication, chunking, user interactions?
		InputStream aResponseStream = null;
		InputStream aInput = null;
		HttpURLConnection aConn = null;

		try {
			URLConnection aTempConn = getURLWithParams().openConnection();

			if (!(aTempConn instanceof HttpURLConnection)) {
				throw new IllegalArgumentException("Only HTTP or HTTPS are supported");
			}

			aConn = (HttpURLConnection) aTempConn;

			aConn.setDoInput(true);

			if (getTimeout() != -1) {
				aConn.setConnectTimeout(getTimeout());
				aConn.setReadTimeout(getTimeout());
			}

			aConn.setInstanceFollowRedirects(isFollowRedirects());
			aConn.setRequestMethod(getMethod().name());

			for (Header aHeader : getHeaders()) {
				aConn.setRequestProperty(aHeader.getName(), aHeader.getHeaderValue());
			}

			aConn.setInstanceFollowRedirects(isFollowRedirects());
			aConn.setRequestMethod(getMethod().name());

			aInput = getBody();

			if (aInput == null && getMethod() == Method.POST) {
				aInput = new ByteArrayInputStream(new byte[0]);
			}

			if (aInput != null && (getMethod() != Method.DELETE)) {
				aConn.setDoOutput(true);
				OutputStream aOut = aConn.getOutputStream();

				ByteStreams.copy(aInput, aOut);

				if (aOut != null) {
					aOut.flush();
					aOut.close();
				}

				aInput.close();
			}

			aConn.connect();

			Collection<Header> aResponseHeaders = new HashSet<Header>();

			Map<String, List<String>> aHeaderMap = aConn.getHeaderFields();

			for (Map.Entry<String, List<String>> aEntry : aHeaderMap.entrySet()) {
				aResponseHeaders.add(new Header(aEntry.getKey(), aEntry.getValue()));
			}

            return new Response(aConn, aResponseHeaders);
		}
		finally {
			Closeables.closeQuietly(aInput);
		}
	}
}
