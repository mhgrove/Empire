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
import static com.clarkparsia.empire.util.EmpireUtil.asPrimaryKey;

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
			query="from {result} space:agency {\"U.S.S.R\"}")

@Namespaces({"space", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@EntityListeners({TestEntityListener.class})
@RdfsClass("space:Spacecraft")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Spacecraft extends BaseTestClass {
	@RdfProperty("space:agency")
	private String agency;

	@RdfProperty("space:alternateName")
	private List<String> alternateName;
	
	@RdfProperty("foaf:depiction")
	private Image depiction;

	@RdfProperty("dc:description")
	private String description;

	@RdfProperty("foaf:homepage")
	private URI homepage;

	@RdfProperty("foaf:name")
	private String name;

	@RdfProperty("space:mission")
	private Mission mission;

	@RdfProperty("space:mass")
	private String mass;

	@RdfProperty("space:internationalDesignator")
	private String internationalDesignator;

	@RdfProperty("space:launch")
	private Launch launch;

	@RdfProperty("space:discipline")
	private List<Discipline> disciplines = new ArrayList<Discipline>();

	public Spacecraft() {
	}

	public Spacecraft(URI theURI) {
		setRdfId(asPrimaryKey(theURI));
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
		if (!(o instanceof Spacecraft)) {
			return false;
		}

		final Spacecraft that = (Spacecraft) o;

		if (getAgency() != null ? !getAgency().equals(that.getAgency()) : that.getAgency() != null) {
			return false;
		}
		if (!((getAlternateName() == null && that.getAlternateName() == null) ||
			(getAlternateName() != null && that.getAlternateName() != null && CollectionUtil.contentsEqual(getAlternateName(),that.getAlternateName())))) {
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null) {
			return false;
		}
		if (getDisciplines() != null ? !getDisciplines().equals(that.getDisciplines()) : that.getDisciplines() != null) {
			return false;
		}
		if (getHomepage() != null ? !getHomepage().equals(that.getHomepage()) : that.getHomepage() != null) {
			return false;
		}
		if (getInternationalDesignator() != null ? !getInternationalDesignator().equals(that.getInternationalDesignator()) : that.getInternationalDesignator() != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getLaunch() == null ? null : getLaunch().getRdfId(),
									that.getLaunch() == null ? null : that.getLaunch().getRdfId())) {
			return false;
		}
		if (getMass() != null ? !getMass().equals(that.getMass()) : that.getMass() != null) {
			return false;
		}
		if (getMission() != null ? !getMission().equals(that.getMission()) : that.getMission() != null) {
			return false;
		}
		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
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
