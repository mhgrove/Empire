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
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;
import com.clarkparsia.empire.test.api.TestEntityListener;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.EntityListeners;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@NamedQuery(name="sovietSpacecraft",
			query="from {result} <http://purl.org/net/schemas/space/agency> {\"U.S.S.R\"}")

@Namespaces({"", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@EntityListeners({TestEntityListener.class})
@RdfsClass("Spacecraft")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Spacecraft extends BaseTestClass {
	@RdfProperty("agency")
	private String agency;

	@RdfProperty("alternateName")
	private List<String> alternateName;
	
	@RdfProperty("foaf:depiction")
	private Image depiction;

	@RdfProperty("dc:description")
	private String description;

	@RdfProperty("foaf:homepage")
	private URI homepage;

	@RdfProperty("foaf:name")
	private String name;

	@RdfProperty("mission")
	private Mission mission;

	@RdfProperty("mass")
	private String mass;

	@RdfProperty("internationalDesignator")
	private String internationalDesignator;

	@RdfProperty("launch")
	private Launch launch;

	@RdfProperty("discipline")
	private List<Discipline> disciplines = new ArrayList<Discipline>();

	public Spacecraft() {
	}

	public Spacecraft(URI theURI) {
		setRdfId(theURI);
	}

	public List<Discipline> getDisciplines() {
		return disciplines;
	}

	public void setDisciplines(final List<Discipline> theDisciplines) {
		disciplines = theDisciplines;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(final String theAgency) {
		agency = theAgency;
	}

	public List<String> getAlternateName() {
		return alternateName;
	}

	public void setAlternateName(final List<String> theAlternateName) {
		alternateName = theAlternateName;
	}

	public Image getDepiction() {
		return depiction;
	}

	public void setDepiction(final Image theDepiction) {
		depiction = theDepiction;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String theDescription) {
		description = theDescription;
	}

	public URI getHomepage() {
		return homepage;
	}

	public void setHomepage(final URI theHomepage) {
		homepage = theHomepage;
	}

	public String getName() {
		return name;
	}

	public void setName(final String theName) {
		name = theName;
	}

	public Mission getMission() {
		return mission;
	}

	public void setMission(final Mission theMission) {
		mission = theMission;
	}

	public String getMass() {
		return mass;
	}

	public void setMass(final String theMass) {
		mass = theMass;
	}

	public String getInternationalDesignator() {
		return internationalDesignator;
	}

	public void setInternationalDesignator(final String theInternationalDesignator) {
		internationalDesignator = theInternationalDesignator;
	}

	public Launch getLaunch() {
		return launch;
	}

	public void setLaunch(final Launch theLaunch) {
		launch = theLaunch;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Spacecraft that = (Spacecraft) o;

		if (agency != null ? !agency.equals(that.agency) : that.agency != null) {
			return false;
		}
		if (!((alternateName == null && that.alternateName == null) ||
			(alternateName != null && that.alternateName != null && CollectionUtil.contentsEqual(alternateName,that.alternateName)))) {
			return false;
		}
		if (description != null ? !description.equals(that.description) : that.description != null) {
			return false;
		}
		if (disciplines != null ? !disciplines.equals(that.disciplines) : that.disciplines != null) {
			return false;
		}
		if (homepage != null ? !homepage.equals(that.homepage) : that.homepage != null) {
			return false;
		}
		if (internationalDesignator != null ? !internationalDesignator.equals(that.internationalDesignator) : that.internationalDesignator != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(launch == null ? null : launch.getRdfId(),
									that.launch == null ? null : that.launch.getRdfId())) {
			return false;
		}
		if (mass != null ? !mass.equals(that.mass) : that.mass != null) {
			return false;
		}
		if (mission != null ? !mission.equals(that.mission) : that.mission != null) {
			return false;
		}
		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), that.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId() == null ? 31 : getRdfId().hashCode();
	}
}
