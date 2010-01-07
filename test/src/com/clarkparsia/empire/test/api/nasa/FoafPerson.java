package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;

import javax.persistence.Entity;

import java.util.List;
import java.util.ArrayList;

import com.clarkparsia.utils.BasicUtils;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 30, 2009 11:11:07 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Namespaces({"", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@RdfsClass("foaf:Person")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class FoafPerson extends BaseTestClass {
	@RdfId
	@RdfProperty("foaf:name")
	private String name;

	@RdfProperty("performed")
	private List<MissionRole> performed = new ArrayList<MissionRole>();

	public String getName() {
		return name;
	}

	public void setName(final String theName) {
		name = theName;
	}

	public List<MissionRole> getPerformed() {
		return performed;
	}

	public void setPerformed(final List<MissionRole> thePerformed) {
		performed = thePerformed;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final FoafPerson that = (FoafPerson) o;

		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}
		if (performed != null ? !performed.equals(that.performed) : that.performed != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), that.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int aresult = name != null ? name.hashCode() : 0;
		aresult = 31 * aresult + (performed != null ? performed.hashCode() : 0);
		return aresult;
	}
}
