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
import javax.persistence.NamedQuery;

import java.net.URI;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@NamedQuery(name="sovietSpacecraftSPARQL",
			query="where { ?result <http://purl.org/net/schemas/space/agency> \"U.S.S.R\" }")

@Namespaces({"space", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@RdfsClass("space:MissionRole")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class MissionRole extends BaseTestClass {
	@RdfProperty("space:role")
	private URI role;

	@RdfProperty("space:mission")
	private Mission mission;

	@RdfId
	@RdfProperty("rdfs:label")
	private String label;

	@RdfProperty("space:actor")
	private FoafPerson actor;

	public URI getRole() {
		return role;
	}

	public void setRole(final URI theRole) {
		role = theRole;
	}

	public Mission getMission() {
		return mission;
	}

	public void setMission(final Mission theIssion) {
		mission = theIssion;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String theLabel) {
		label = theLabel;
	}

	public FoafPerson getActor() {
		return actor;
	}

	public void setActor(final FoafPerson theActor) {
		actor = theActor;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final MissionRole that = (MissionRole) o;

		if (getActor() != null ? !getActor().equals(that.getActor()) : that.getActor() != null) {
			return false;
		}
		if (getLabel() != null ? !getLabel().equals(that.getLabel()) : that.getLabel() != null) {
			return false;
		}
		if (getMission() != null ? !getMission().equals(that.getMission()) : that.getMission() != null) {
			return false;
		}
		if (getRole() != null ? !getRole().equals(that.getRole()) : that.getRole() != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), that.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int aresult = getRole() != null ? getRole().hashCode() : 0;
		aresult = 31 * aresult + (getMission() != null ? getMission().hashCode() : 0);
		aresult = 31 * aresult + (getLabel() != null ? getLabel().hashCode() : 0);
		aresult = 31 * aresult + (getActor() != null ? getActor().hashCode() : 0);
		return aresult;
	}
}
