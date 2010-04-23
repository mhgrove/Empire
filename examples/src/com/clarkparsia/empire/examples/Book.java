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

package com.clarkparsia.empire.examples;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.SupportsRdfId;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;

import java.util.Date;
import java.util.Collection;
import java.util.HashSet;
import java.net.URI;

/**
 * <p>Example class from the Empire Semantic Universe Article</p>
 *
 * @author Michael Grove
 */
@Namespaces({"frbr", "http://vocab.org/frbr/core#",
			 "dc",   "http://purl.org/dc/terms/",
			 "foaf", "http://xmlns.com/foaf/0.1/"})
@RdfsClass("frbr:Expression")
@Entity
public class Book implements SupportsRdfId {
	/**
	 * Default support for the ID of an RDF concept
	 */
	private SupportsRdfId mIdSupport = new SupportsRdfIdImpl();

	@RdfProperty("dc:title")
	private String mTitle;

	@RdfProperty("dc:publisher")
	private String mPublisher;

	@RdfProperty("dc:issued")
	private Date mIssued;

	@RdfProperty("foaf:primarySubjectOf")
	private URI mPrimarySubjectOf;

	@OneToMany(fetch = FetchType.LAZY,
			   cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@RdfProperty("frbr:embodiment")
	private Collection<Manifestation> mEmbodiments = new HashSet<Manifestation>();

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(final String theTitle) {
		mTitle = theTitle;
	}

	public String getPublisher() {
		return mPublisher;
	}

	public void setPublisher(final String thePublisher) {
		mPublisher = thePublisher;
	}

	public Date getIssued() {
		return mIssued;
	}

	public void setIssued(final Date theIssued) {
		mIssued = theIssued;
	}

	public URI getPrimarySubjectOf() {
		return mPrimarySubjectOf;
	}

	public void setPrimarySubjectOf(final URI thePrimarySubjectOf) {
		mPrimarySubjectOf = thePrimarySubjectOf;
	}

	public Collection<Manifestation> getEmbodiments() {
		return mEmbodiments;
	}

	public void setEmbodiments(final Collection<Manifestation> theEmbodiments) {
		mEmbodiments = theEmbodiments;
	}

	/**
	 * @inheritDoc
	 */
	public RdfKey getRdfId() {
		return mIdSupport.getRdfId();
	}

	/**
	 * @inheritDoc
	 */
	public void setRdfId(final RdfKey theId) {
		mIdSupport.setRdfId(theId);
	}

	@Override
	public boolean equals(final Object theObj) {
		if (this == theObj) {
			return true;
		}
		if (theObj == null || getClass() != theObj.getClass()) {
			return false;
		}

		final Book aBook = (Book) theObj;

		if (getRdfId() != null) {
			return getRdfId().equals( aBook.getRdfId() );
		}
		else {
			if (mEmbodiments != null ? !mEmbodiments.equals(aBook.mEmbodiments) : aBook.mEmbodiments != null) {
				return false;
			}
			if (mIssued != null ? !mIssued.equals(aBook.mIssued) : aBook.mIssued != null) {
				return false;
			}
			if (mPrimarySubjectOf != null ? !mPrimarySubjectOf.equals(aBook.mPrimarySubjectOf) : aBook.mPrimarySubjectOf != null) {
				return false;
			}
			if (mPublisher != null ? !mPublisher.equals(aBook.mPublisher) : aBook.mPublisher != null) {
				return false;
			}
			if (mTitle != null ? !mTitle.equals(aBook.mTitle) : aBook.mTitle != null) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId() == null ? 0 : getRdfId().value().hashCode();
	}
}
