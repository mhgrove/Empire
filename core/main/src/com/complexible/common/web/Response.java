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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * <p>A response to an HTTP invocation.  Responses must be closed when they are no longer used to close the
 * connection to the server and release the content stream.</p>
 *
 * @author Michael Grove
 * @since 1.0
 * @version 1.1
 */
@Deprecated
public class Response implements Closeable {

	private InputStream mContent = null;
	private InputStream mErrorStream = null;

	private final Map<String, Header> mHeaders;
	private String mMessage;
    private final HttpURLConnection mConnection;
    private int mResponseCode;

    public Response(final HttpURLConnection theConn, final Collection<Header> theHeaders) {

        mHeaders = new HashMap<String, Header>();

        for (Header aHeader : theHeaders) {
            mHeaders.put(aHeader.getName(), aHeader);
        }

        mConnection = theConn;

		try {
			mContent = theConn.getInputStream();
			
			// if this is GZIP encoded, then wrap the input stream
			String contentEncoding = theConn.getContentEncoding();
			if ("gzip".equals(contentEncoding)) {
				mContent = new GZIPInputStream(mContent);
			}
		}
		catch (IOException e) {
			// there was an error in the connection, so probably the error stream will be populated, this is safe to ignore
		}

		try {
			mErrorStream = theConn.getErrorStream();
		}
		catch (Exception e) {
			// there was an error in the connection, probably just no error stream.
		}

		try {
			mMessage = theConn.getResponseMessage();
		}
		catch (IOException e) {
			// ugh?
		}

		try {
			mResponseCode = theConn.getResponseCode();
		}
		catch (IOException e) {
			// ugh?
			mResponseCode = -1;
		}
	}

	/**
	 * Return the error stream from the connection
	 * @return the error stream
	 */
	public InputStream getErrorStream() {
		return mErrorStream;
	}

	/**
     * Return the response message from the server
     * @return the message
     */
	public String getMessage() {
		return mMessage;
	}

    /**
     * Return all headers returned by the server
     * @return the headers
     */
	public Collection<Header> getHeaders() {
		return mHeaders.values();
	}

    /**
     * Get the header with the specified name
     * @param theName the header name
     * @return the value of the header, or null if the header is not present
     */
	public Header getHeader(String theName) {
		return mHeaders.get(theName);
	}

    /**
     * Return the response code
     * @return the response code
     */
	public int getResponseCode() {
		return mResponseCode;
	}

    /**
     * Return the response content from the server
     * @return the response content
     */
	public InputStream getContent() {
		return mContent;
	}

    /**
     * Return whether or not this has an error result
     * @return true if there is an error result, false otherwise
     */
	public boolean hasErrorCode() {
		// TODO: right?
		return (getResponseCode() >= 400) || (getResponseCode() < 0);
	}

    /**
     * Close this response
     * @throws IOException if there is an error while closing
     */
    public void close() throws IOException {
		if (mContent != null) {
        	mContent.close();
		}

		if (mErrorStream != null) {
			mErrorStream.close();
		}

		mConnection.disconnect();
    }
}
