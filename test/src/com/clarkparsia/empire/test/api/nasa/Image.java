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

package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.NamedGraph;

import com.clarkparsia.empire.test.api.BaseTestClass;
import com.clarkparsia.empire.SupportsRdfId;

import javax.persistence.Entity;

import java.net.URI;
import java.util.List;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@Namespaces({"foaf", "http://xmlns.com/foaf/0.1/"})
@Entity
@RdfsClass("foaf:Image")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Image extends BaseTestClass {
	@RdfId
	private URI mURI;

	@RdfProperty("foaf:depicts")
	private List<Object> depicts;

	@RdfProperty("foaf:thumbnail")
	private URI thumbnail;

	public URI getURI() {
		return mURI;
	}

	public void setURI(final URI theURI) {
		mURI = theURI;
	}

	public List<Object> getDepicts() {
		return depicts;
	}

	public void setDepicts(final List<Object> theDepicts) {
		depicts = theDepicts;
	}

	public URI getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(final URI theThumbnail) {
		thumbnail = theThumbnail;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Image)) {
			return false;
		}

		final Image aImage = (Image) o;

		if (getDepicts() != null ? !getDepicts().equals(aImage.getDepicts()) : aImage.getDepicts() != null) {
			return false;
		}
		if (getURI() != null ? !getURI().equals(aImage.getURI()) : aImage.getURI() != null) {
			return false;
		}
		if (getThumbnail() != null ? !getThumbnail().equals(aImage.getThumbnail()) : aImage.getThumbnail() != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), aImage.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId().hashCode();
	}
}
