package com.clarkparsia.empire.test;

import com.sun.xml.internal.bind.v2.TODO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openrdf.model.impl.URIImpl;
import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfProperty;

import com.clarkparsia.empire.test.api.BaseTestClass;
import com.clarkparsia.empire.test.api.TestPerson;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.empire.test.api.TestDataSource;
import com.clarkparsia.empire.test.api.TestVocab;

import java.net.URI;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestSuite;

import com.clarkparsia.utils.NamespaceUtils;
import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

import com.clarkparsia.sesame.utils.ExtendedGraph;
import com.clarkparsia.sesame.vocabulary.FOAF;
import com.clarkparsia.sesame.vocabulary.DC;

import javax.persistence.Entity;

/**
 * Title: TestRdfConvert<br/>
 * Description: Test cases for classes in the com.clarkparsia.empire.annotation package.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 29, 2009 3:20:27 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class TestRdfConvert {
	// TODO: tests for properties whose values are serialized as rdf:List's
	// TODO: tests for stuff where an @RdfsClass extends another @RdfsClass
	// TODO: tests for construct queries
	// TODO: tests for named graph stuff

    @BeforeClass
    public static void beforeClass() {
        EmpireOptions.STRONG_TYPING = true;
    }
    
	@Test(expected=InvalidRdfException.class)
	public void testNoEntity() throws InvalidRdfException {
		RdfGenerator.asRdf(new NoEntity());
	}

	@Test(expected=InvalidRdfException.class)
	public void testNoRdfClass() throws InvalidRdfException {
		RdfGenerator.asRdf(new NoRdfsClass());
	}

	@Test(expected=InvalidRdfException.class)
	public void testNoSupports() throws InvalidRdfException {
		RdfGenerator.asRdf(new NoSupports());
	}

	@Test(expected=InvalidRdfException.class)
	public void testInvalidId() throws InvalidRdfException {
		// this should not succeed, null values for the RdfId annotation are not allowed
		RdfGenerator.asRdf(new TestPerson());
	}

	@Test(expected=InvalidRdfException.class)
	public void testNoDefaultConstructor() throws InvalidRdfException, DataSourceException {
		RdfGenerator.fromRdf(NoDefaultConstructor.class, URI.create("urn:foo"), new TestDataSource());
	}

	@Test(expected=InvalidRdfException.class)
	public void testUnreachableConstructor() throws InvalidRdfException, DataSourceException {
		RdfGenerator.fromRdf(UnreachableConstructor.class, URI.create("urn:foo"), new TestDataSource());
	}

	@Test(expected=InvalidRdfException.class)
	public void testMultiInvalidId() throws InvalidRdfException {
		RdfGenerator.asRdf(new MultipleRdfIds());
	}

	@Test
	public void testNoStatements() throws InvalidRdfException, DataSourceException {
		assertTrue(RdfGenerator.fromRdf(TestPerson.class, URI.create("urn:foo"), new TestDataSource()) == null);
	}

	@Test
	public void testUnbalancedNamespaces() {
		try {
			RdfGenerator.asRdf(new UnbalancedNamespaces());

			// the qname should not get expanded because the prefix was never asserted
			assertEquals(NamespaceUtils.uri("notvalid:test"), "notvalid:test");
		}
		catch (InvalidRdfException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testConvert() {
		TestPerson aJoe = new TestPerson();
		aJoe.setMBox("mailto:joe@example.org");
		aJoe.setFirstName("Joe");

		TestPerson aJane = new TestPerson();
		aJane.setMBox("mailto:jane@example.org");
		aJane.setFirstName("Jane");

		TestPerson aPerson = new TestPerson();

		aPerson.setMBox("mailto:bob@example.org");

		try {
			ExtendedGraph aGraph = RdfGenerator.asRdf(aPerson);

			org.openrdf.model.URI aPersonURI = aGraph.getValueFactory().createURI(aPerson.getRdfId().toString());

			assertEquals(aGraph.numStatements(), 2);

			// the statements should assert that the person is of type foaf:TestPerson
			assertEquals(aGraph.getValue(aPersonURI,
										 URIImpl.RDF_TYPE),
						 FOAF.Person);

			// and that the mbox is correct
			assertEquals(aGraph.getLiteral(aPersonURI, FOAF.mbox).getLabel(), aPerson.getMBox());

			// now lets try with some more properties

			aPerson.setWeight(123.45f);
			aPerson.setBirthday(new Date());
			aPerson.setFirstName("John");
			aPerson.setLastName("Doe");
			aPerson.setLikesVideoGames(true);
			aPerson.setTitle("Sir");

			aPerson.getKnows().add(aJoe);
			aPerson.getKnows().add(aJane);

			aPerson.setWeblogURI(URI.create("http://example.org"));

			aGraph = RdfGenerator.asRdf(aPerson);

			assertEquals(Float.parseFloat(aGraph.getLiteral(aPersonURI, TestVocab.ontology().weight).getLabel()),
						 aPerson.getWeight());

			// this tests if "inferring" from an annotated getter works
			assertEquals(Boolean.valueOf(aGraph.getLiteral(aPersonURI, TestVocab.ontology().likesVideoGames).getLabel()),
						 aPerson.isLikesVideoGames());

			// and this tests if 'inferring" from an annotated setter works
			// also checking that it properly used the other namespace
			assertEquals(aGraph.getLiteral(aPersonURI, DC.ontology().title).getLabel(),
						 aPerson.getTitle());

			assertEquals(URI.create(aGraph.getValue(aPersonURI, DC.ontology().publisher).toString()),
						 aPerson.getWeblogURI());

			assertEquals(aGraph.getLiteral(aPersonURI, FOAF.firstName).getLabel(),
						 aPerson.getFirstName());

			assertEquals(aGraph.getLiteral(aPersonURI, FOAF.surname).getLabel(),
						 aPerson.getLastName());

			assertEquals(aGraph.getLiteral(aPersonURI, URIImpl.RDFS_LABEL).getLabel(),
						 aPerson.getLabel());

			List aKnows = CollectionUtil.list(aGraph.getValues(aPersonURI, FOAF.knows));

			assertEquals(aKnows.size(), 2);

			assertTrue(aKnows.contains(aGraph.getSesameValueFactory().createURI(aJane.getRdfId())));
			assertTrue(aKnows.contains(aGraph.getSesameValueFactory().createURI(aJoe.getRdfId())));
		}
		catch (InvalidRdfException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testRoundTrip() {
		TestPerson aJoe = new TestPerson();
		aJoe.setMBox("mailto:joe@example.org");
		aJoe.setFirstName("Joe");

		TestPerson aJane = new TestPerson();
		aJane.setMBox("mailto:jane@example.org");
		aJane.setFirstName("Jane");

		TestPerson aBob = new TestPerson();

		aBob.setBirthday(BasicUtils.asDate("1980-01-01"));
		aBob.setFirstName("Bob");
		aBob.setLastName("Smith");
		aBob.setLikesVideoGames(false);
		aBob.setWeight(200.1f);
		aBob.setWeblogURI(URI.create("http://someblog.example.org"));
		aBob.setTitle("Mr");
		aBob.setMBox("mailto:bob@example.org");

		aBob.getKnows().add(aJoe);
		aBob.getKnows().add(aJane);

		try {
			ExtendedGraph aGraph = RdfGenerator.asRdf(aBob);

			// this is the set of data that would normally be in the database
			ExtendedGraph aSourceGraph = new ExtendedGraph();
			aSourceGraph.add(aGraph);
			aSourceGraph.add(RdfGenerator.asRdf(aJoe));
			aSourceGraph.add(RdfGenerator.asRdf(aJane));

			TestPerson aPerson = RdfGenerator.fromRdf(TestPerson.class, aBob.getRdfId(), new TestDataSource(aSourceGraph));

			assertEquals(aBob, aPerson);

			// now lets test the round trip w/ the added trick of a circular dependency
			aBob.setSpouse(aJane);

			aGraph = RdfGenerator.asRdf(aBob);

			// this is the set of data that would normally be in the database
			aSourceGraph = new ExtendedGraph();
			aSourceGraph.add(aGraph);
			aSourceGraph.add(RdfGenerator.asRdf(aJoe));
			aSourceGraph.add(RdfGenerator.asRdf(aJane));

			aPerson = RdfGenerator.fromRdf(TestPerson.class, aBob.getRdfId(), new TestDataSource(aSourceGraph));

			// should still be equal, should have re-used Jane
			assertEquals(aBob, aPerson);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testSupportsRdfIdImpl() {
		SupportsRdfId aImpl = new SupportsRdfIdImpl();

		URI aTestURI = URI.create("urn:some:identifier");

		assertNull(aImpl.getRdfId());

		aImpl.setRdfId(aTestURI);

		assertEquals(aImpl.getRdfId(), aTestURI);

		try {
			aImpl.setRdfId(URI.create("urn:new:id"));
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e) {
			// this is expected
		}

		assertEquals(aImpl, aImpl);

		assertFalse(aImpl.equals(null));
		
		assertFalse(aImpl.equals(""));

		assertEquals(aImpl, new SupportsRdfIdImpl(aTestURI));

		assertEquals(aImpl.hashCode(), new SupportsRdfIdImpl(aTestURI).hashCode());

		assertFalse(aImpl.equals(new SupportsRdfIdImpl()));

		assertFalse(aImpl.equals(new SupportsRdfIdImpl(URI.create("urn:new:id"))));
	}

	@RdfsClass("urn:TestClass")
	@Entity
	private class NoDefaultConstructor extends BaseTestClass {
		NoDefaultConstructor(String foo) {
			setRdfId(URI.create("urn:test:no:default"));
		}
	}

	@RdfsClass("urn:TestClass")
	@Entity
	private class UnreachableConstructor extends BaseTestClass {
		private UnreachableConstructor() {
			setRdfId(URI.create("urn:test:unreachable"));
		}
	}


	@Namespaces({"", "http://xmlns.com/foaf/0.1/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/",
			 "notvalid"})
	@RdfsClass("foaf:Person")
	@Entity
	private class UnbalancedNamespaces extends BaseTestClass {
		UnbalancedNamespaces() {
			setRdfId(URI.create("urn:test:unbalanced"));
		}
	}

	@RdfsClass("urn:TestClass")
	@Entity
	private class MultipleRdfIds {
		@RdfId
		private String one = "one";

		@RdfId
		private String two = "two";
	}

	@RdfsClass("urn:NoEntity")
	public class NoEntity extends BaseTestClass {
	}

	@Entity
	@RdfsClass("urn:NoSupports")
	public class NoSupports {
		@RdfId
		private String one = "one";
	}

	@Namespaces({"foaf", "http://xmlns.com/foaf/0.1/"})
	@Entity
	public class NoRdfsClass extends BaseTestClass {
		@RdfId
		@RdfProperty("foaf:name")
		public String name;
	}
}
