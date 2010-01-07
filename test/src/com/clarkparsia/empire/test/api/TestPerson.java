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
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 29, 2009 3:21:45 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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

		if (!BasicUtils.equalsOrNull(likesVideoGames, aPerson.likesVideoGames)) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(aPerson.weight, weight)) {
			return false;
		}
		if (birthday != null ? !birthday.equals(aPerson.birthday) : aPerson.birthday != null) {
			return false;
		}
		if (firstName != null ? !firstName.equals(aPerson.firstName) : aPerson.firstName != null) {
			return false;
		}
		if (knows != null ? !knows.equals(aPerson.knows) : aPerson.knows != null) {
			return false;
		}
		if (lastName != null ? !lastName.equals(aPerson.lastName) : aPerson.lastName != null) {
			return false;
		}
		if (mbox != null ? !mbox.equals(aPerson.mbox) : aPerson.mbox != null) {
			return false;
		}
		if (name != null ? !name.equals(aPerson.name) : aPerson.name != null) {
			return false;
		}
		if (title != null ? !title.equals(aPerson.title) : aPerson.title != null) {
			return false;
		}
		if (weblogURI != null ? !weblogURI.equals(aPerson.weblogURI) : aPerson.weblogURI != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int aResult = name != null ? name.hashCode() : 0;
		aResult = 31 * aResult + (birthday != null ? birthday.hashCode() : 0);
		aResult = 31 * aResult + (title != null ? title.hashCode() : 0);
		aResult = 31 * aResult + (mbox != null ? mbox.hashCode() : 0);
		aResult = 31 * aResult + (lastName != null ? lastName.hashCode() : 0);
		aResult = 31 * aResult + (firstName != null ? firstName.hashCode() : 0);
		aResult = 31 * aResult + (weblogURI != null ? weblogURI.hashCode() : 0);
		aResult = 31 * aResult + (knows != null ? knows.hashCode() : 0);
		aResult = 31 * aResult + (weight != null && weight != +0.0f ? Float.floatToIntBits(weight) : 0);
		aResult = 31 * aResult + (likesVideoGames != null && likesVideoGames ? 1 : 0);
		return aResult;
	}
}
