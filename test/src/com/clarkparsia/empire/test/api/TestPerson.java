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

package com.clarkparsia.empire.test.api;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.NamedGraph;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;
import javax.persistence.PostRemove;
import javax.persistence.PreRemove;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.net.URI;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p>Class used for testing</p>
 *
 * @author Michael Grove
 */
@Namespaces({"", "http://xmlns.com/foaf/0.1/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/",
			 "test", "http://clarkparsia.com/empire/test/"})
@RdfsClass("foaf:Person")
@Entity
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class TestPerson extends BaseTestClass {
	@RdfProperty("foaf:name")
	private String name;

	@RdfProperty("foaf:birthday")
	private Date birthday;

	private String title;

	@RdfId
	@RdfProperty("mbox")
	private String mbox;

	@RdfProperty("foaf:surname")
	private String lastName;

	@RdfProperty("foaf:firstName")
	private String firstName;

	@RdfProperty("dc:publisher")
	private URI weblogURI;

	@RdfProperty("foaf:knows")
	private List<TestPerson> knows = new ArrayList<TestPerson>();

	@RdfProperty("test:weight")
	private Float weight;

	private Boolean likesVideoGames;

	@RdfProperty("test:spouse")
	private TestPerson spouse;

	@RdfProperty("dc:title")
	public void setTitle(String theTitle) {
		title = theTitle;
	}

	public String getTitle() {
		return title;
	}

	@RdfProperty("test:likesVideoGames")
	public Boolean isLikesVideoGames() {
		return likesVideoGames;
	}

	@RdfProperty("rdfs:label")
	public String getLabel() {
		if (lastName == null || firstName == null) {
			return null;
		}
		else {
			return lastName + ", " + firstName;
		}
	}

	public TestPerson getSpouse() {
		return spouse;
	}

	public void setSpouse(final TestPerson theSpouse) {
		spouse = theSpouse;
	}

	public void setLikesVideoGames(final Boolean theLikesVideoGames) {
		likesVideoGames = theLikesVideoGames;
	}

	public String getName() {
		return name;
	}

	public void setName(final String theName) {
		name = theName;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(final Date theBirthday) {
		birthday = theBirthday;
	}

	public String getMBox() {
		return mbox;
	}

	public void setMBox(final String theMBox) {
		mbox = theMBox;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String theLastName) {
		lastName = theLastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String theFirstName) {
		firstName = theFirstName;
	}

	public URI getWeblogURI() {
		return weblogURI;
	}

	public void setWeblogURI(final URI theWeblogURI) {
		weblogURI = theWeblogURI;
	}

	public List<TestPerson> getKnows() {
		return knows;
	}

	public void setKnows(final List<TestPerson> theKnows) {
		knows = theKnows;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(final Float theWeight) {
		weight = theWeight;
	}

	@Override
	public boolean equals(final Object theObj) {
		if (this == theObj) {
			return true;
		}
		
		if (!(theObj instanceof TestPerson)) {
			return false;
		}

		final TestPerson aPerson = (TestPerson) theObj;

		if (!BasicUtils.equalsOrNull(isLikesVideoGames(), aPerson.isLikesVideoGames())) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(aPerson.getWeight(), getWeight())) {
			return false;
		}
		if (getBirthday() != null ? !getBirthday().equals(aPerson.getBirthday()) : aPerson.getBirthday() != null) {
			return false;
		}
		if (getFirstName() != null ? !getFirstName().equals(aPerson.getFirstName()) : aPerson.getFirstName() != null) {
			return false;
		}
		if (getKnows() != null ? !getKnows().equals(aPerson.getKnows()) : aPerson.getKnows() != null) {
			return false;
		}
		if (getLastName() != null ? !getLastName().equals(aPerson.getLastName()) : aPerson.getLastName() != null) {
			return false;
		}
		if (getMBox() != null ? !getMBox().equals(aPerson.getMBox()) : aPerson.getMBox() != null) {
			return false;
		}
		if (getName() != null ? !getName().equals(aPerson.getName()) : aPerson.getName() != null) {
			return false;
		}
		if (getTitle() != null ? !getTitle().equals(aPerson.getTitle()) : aPerson.getTitle() != null) {
			return false;
		}
		if (getWeblogURI() != null ? !getWeblogURI().equals(aPerson.getWeblogURI()) : aPerson.getWeblogURI() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int aResult = getName() != null ? getName().hashCode() : 0;
		aResult = 31 * aResult + (getBirthday() != null ? getBirthday().hashCode() : 0);
		aResult = 31 * aResult + (getTitle() != null ? getTitle().hashCode() : 0);
		aResult = 31 * aResult + (getMBox() != null ? getMBox().hashCode() : 0);
		aResult = 31 * aResult + (getLastName() != null ? getLastName().hashCode() : 0);
		aResult = 31 * aResult + (getFirstName() != null ? getFirstName().hashCode() : 0);
		aResult = 31 * aResult + (getWeblogURI() != null ? getWeblogURI().hashCode() : 0);
		aResult = 31 * aResult + (getKnows() != null ? getKnows().hashCode() : 0);
		aResult = 31 * aResult + (getWeight() != null && getWeight() != +0.0f ? Float.floatToIntBits(getWeight()) : 0);
		aResult = 31 * aResult + (isLikesVideoGames() != null && isLikesVideoGames() ? 1 : 0);
		return aResult;
	}

	public String toString() {
		StringBuffer aBuffer = new StringBuffer();

		aBuffer.append("First name: ").append(getFirstName()).append("\n");
		aBuffer.append("Last name: ").append(getLastName()).append("\n");
		aBuffer.append("mbox: ").append(mbox).append("\n");
		aBuffer.append("title: ").append(getTitle()).append("\n");
		aBuffer.append("blog: ").append(getWeblogURI()).append("\n");
		aBuffer.append("weight: ").append(getWeight()).append("\n");
		aBuffer.append("birthday: ").append(getBirthday()).append("\n");
		aBuffer.append("likes video games: ").append(isLikesVideoGames()).append("\n");
		aBuffer.append("knows: ");
		for (TestPerson aPerson : getKnows()) {
			aBuffer.append(aPerson.getLabel()).append(", ");
		}
		aBuffer.append("\n");

		return aBuffer.toString();
	}
}
