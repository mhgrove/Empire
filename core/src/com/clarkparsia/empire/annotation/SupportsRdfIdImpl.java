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

import com.clarkparsia.empire.SupportsRdfId;

import java.net.URI;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p>Utility implementation of the {@link SupportsRdfId} interface.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public final class SupportsRdfIdImpl implements SupportsRdfId {
	/**
	 * the rdf:id
	 */
	private java.net.URI mId;

	/**
	 * Create a new SupportsRdfIdImpl
	 */
	public SupportsRdfIdImpl() {
		// TODO: I think canonically, this should put the @Id JPA annotation on mId, but that would badly
		// mix with Play! since they already assign their own @Id.  This is something to remember for the future though.

		mId = null;
	}

	/**
	 * Create a new SupportsRdfIdImpl
	 * @param theId the rdf:ID of the object
	 */
	public SupportsRdfIdImpl(final URI theId) {
		mId = theId;
	}

	/**
	 * @inheritDoc
	 */
	public URI getRdfId() {
		return mId;
	}

	/**
	 * @inheritDoc
	 */
	public void setRdfId(final URI theId) {
		if (mId != null) {
			throw new IllegalStateException("Cannot set the rdf id of an object once it is set");
		}

		mId = theId;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(final Object theObj) {
		if (this == theObj) {
			return true;
		}

		if (theObj == null || getClass() != theObj.getClass()) {
			return false;
		}

		final SupportsRdfIdImpl that = (SupportsRdfIdImpl) theObj;

		return BasicUtils.equalsOrNull(mId, that.mId);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return mId == null ? 0 : mId.hashCode();
	}
}
