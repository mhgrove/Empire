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

import java.net.URI;

/**
 * <p>Interface for anything that can support having an rdf:ID</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @since 0.6.2
 */
public interface SupportsRdfId {
	/**
	 * Return the rdf:ID of this instance
	 * @return the rdf:ID
	 */
	public RdfKey getRdfId();

	/**
	 * Set the rdf:ID for this object
	 * @param theId the new rdf:ID
	 */
	void setRdfId(RdfKey theId);

	public interface RdfKey<T> {
		public T value();
	}

	public class URIKey implements RdfKey<java.net.URI> {
		private final java.net.URI mURI;

		public URIKey(final URI theURI) {
			mURI = theURI;
		}

		public java.net.URI value() { return mURI; }

		@Override
		public int hashCode() {
			return mURI.hashCode();
		}
		@Override
		public String toString() {
			return mURI.toASCIIString();
		}

		@Override
		public boolean equals(Object theObj) {
			if (theObj == null) {
				return false;
			}
			else if (this == theObj) {
				return true;
			}
			else {
				return theObj instanceof URIKey && mURI.equals(((URIKey) theObj).value());
			}
		}
	}

	public class BNodeKey implements RdfKey<String> {
		private final String mId;

		public BNodeKey(final String theId) {
			mId = theId;
		}

		public String value() { return mId; }

		@Override
		public int hashCode() {
			return mId.hashCode();
		}
		@Override
		public String toString() {
			return mId;
		}

		@Override
		public boolean equals(Object theObj) {
			if (theObj == null) {
				return false;
			}
			else if (this == theObj) {
				return true;
			}
			else {
				return theObj instanceof BNodeKey && mId.equals(((BNodeKey) theObj).value());
			}
		}	
	}
}
