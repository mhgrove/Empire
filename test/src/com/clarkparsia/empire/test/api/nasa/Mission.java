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
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;

import javax.persistence.Entity;

import java.util.List;
import java.util.ArrayList;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@Namespaces({"", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@RdfsClass("Mission")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Mission extends BaseTestClass {
	@RdfId
	@RdfProperty("dc:title")
	private String title;

	@RdfProperty("missionRole")
	private List<MissionRole> missionRoles = new ArrayList<MissionRole>();

	public String getTitle() {
		return title;
	}

	public void setTitle(final String theTitle) {
		title = theTitle;
	}

	public List<MissionRole> getIssionRoles() {
		return missionRoles;
	}

	public void setIssionRoles(final List<MissionRole> theIssionRoles) {
		missionRoles = theIssionRoles;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Mission aMission = (Mission) o;

		if (missionRoles != null ? !missionRoles.equals(aMission.missionRoles) : aMission.missionRoles != null) {
			return false;
		}
		if (title != null ? !title.equals(aMission.title) : aMission.title != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), aMission.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int aresult = title != null ? title.hashCode() : 0;
		aresult = 31 * aresult + (missionRoles != null ? missionRoles.hashCode() : 0);
		return aresult;
	}
}
