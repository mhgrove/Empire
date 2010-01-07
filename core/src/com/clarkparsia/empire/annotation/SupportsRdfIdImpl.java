package com.clarkparsia.empire.annotation;

import com.clarkparsia.empire.SupportsRdfId;

import java.net.URI;

import com.clarkparsia.utils.BasicUtils;

/**
 * Title: SupportsRdfIdImpl<br/>
 * Description: Utility implementation of the SupportsRdfId interface.  I think canonically, this should put the @Id
 * JPA annotation on mId, but that would badly mix with Play since they already assign their own @Id.  This is
 * something to remember for the future though.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 11, 2009 5:00:21 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
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
