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

import com.clarkparsia.utils.BasicUtils;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 30, 2009 11:00:41 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Namespaces({"foaf", "http://xmlns.com/foaf/0.1/"})
@Entity
@RdfsClass("foaf:Image")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Image extends BaseTestClass {
	@RdfId
	private URI mURI;

	@RdfProperty("foaf:depicts")
	private Object depicts;

	@RdfProperty("foaf:thumbnail")
	private URI thumbnail;

	public URI getURI() {
		return mURI;
	}

	public void setURI(final URI theURI) {
		mURI = theURI;
	}

	public Object getDepicts() {
		return depicts;
	}

	public void setDepicts(final Object theDepicts) {
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
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Image aImage = (Image) o;

		if (depicts != null ? !depicts.equals(aImage.depicts) : aImage.depicts != null) {
			return false;
		}
		if (mURI != null ? !mURI.equals(aImage.mURI) : aImage.mURI != null) {
			return false;
		}
		if (thumbnail != null ? !thumbnail.equals(aImage.thumbnail) : aImage.thumbnail != null) {
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
