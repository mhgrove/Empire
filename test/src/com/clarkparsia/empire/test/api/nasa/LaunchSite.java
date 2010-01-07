package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;

import javax.persistence.Entity;

import com.clarkparsia.utils.BasicUtils;

import java.util.List;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 30, 2009 11:17:48 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Namespaces({"", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@RdfsClass("LaunchSite")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class LaunchSite extends BaseTestClass {
	@RdfProperty("country")
	private List<String> country;

	@RdfProperty("rdfs:label")
	private List<String> label;

	@RdfProperty("place")
	private List<String> place;

	public List<String> getCountry() {
		return country;
	}

	public void setCountry(final List<String> theCountry) {
		country = theCountry;
	}

	public List<String> getLabel() {
		return label;
	}

	public void setLabel(final List<String> theLabel) {
		label = theLabel;
	}

	public List<String> getPlace() {
		return place;
	}

	public void setPlace(final List<String> thePlace) {
		place = thePlace;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final LaunchSite that = (LaunchSite) o;

		if (country != null ? !country.equals(that.country) : that.country != null) {
			return false;
		}
		if (label != null ? !label.equals(that.label) : that.label != null) {
			return false;
		}
		if (place != null ? !place.equals(that.place) : that.place != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), that.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int aresult = country != null ? country.hashCode() : 0;
		aresult = 31 * aresult + (label != null ? label.hashCode() : 0);
		aresult = 31 * aresult + (place != null ? place.hashCode() : 0);
		return aresult;
	}
}
