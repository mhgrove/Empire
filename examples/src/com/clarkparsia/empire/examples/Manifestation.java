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
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.SupportsRdfId;

import javax.persistence.Entity;

import java.util.Date;
import java.net.URI;

/**
 * <p></p>
*
* @author Michael Grove
*/
@Namespaces({"frbr", "http://vocab.org/frbr/core#",
			 "dc",   "http://purl.org/dc/terms/",
			 "foaf", "http://xmlns.com/foaf/0.1/"})
@RdfsClass("frbr:Manifestation")
@Entity
public class Manifestation implements SupportsRdfId {
	/**
	 * Default support for the ID of an RDF concept
	 */
	private SupportsRdfId mIdSupport = new SupportsRdfIdImpl();

	@RdfProperty("dc:issued")
	private Date mIssued;

	@RdfProperty("dc:type")
	private URI mType;

	@RdfProperty("dc:identifier")
	private URI mDcId;

	@RdfProperty("dc:extent")
	private String mExtent;

	public Date getIssued() {
		return mIssued;
	}

	public void setIssued(final Date theIssued) {
		mIssued = theIssued;
	}

	public URI getType() {
		return mType;
	}

	public void setType(final URI theType) {
		mType = theType;
	}

	public URI getDcId() {
		return mDcId;
	}

	public void setDcId(final URI theDcId) {
		mDcId = theDcId;
	}

	public String getExtent() {
		return mExtent;
	}

	@Override
	public String toString() {
		return mType != null ? mType.toString() : getRdfId().toString(); 
	}

	public void setExtent(final String theExtent) {
		mExtent = theExtent;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Manifestation aObj = (Manifestation) o;

		if (getRdfId() != null) {
			return getRdfId().equals( aObj.getRdfId() );
		}
		else {
			if (mDcId != null ? !mDcId.equals(aObj.mDcId) : aObj.mDcId != null) {
				return false;
			}
			if (mExtent != null ? !mExtent.equals(aObj.mExtent) : aObj.mExtent != null) {
				return false;
			}
			if (mIssued != null ? !mIssued.equals(aObj.mIssued) : aObj.mIssued != null) {
				return false;
			}
			if (mType != null ? !mType.equals(aObj.mType) : aObj.mType != null) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId() == null ? 0 : getRdfId().value().hashCode();
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
}
