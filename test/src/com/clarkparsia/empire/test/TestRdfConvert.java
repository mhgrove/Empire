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

package com.clarkparsia.empire.test;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.model.Graph;

import org.openrdf.model.vocabulary.RDFS;

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
import com.clarkparsia.empire.ds.DataSourceException;
import static com.clarkparsia.empire.util.EmpireUtil.asPrimaryKey;
import com.clarkparsia.empire.test.api.TestDataSource;
import com.clarkparsia.empire.test.api.TestVocab;

import java.net.URI;
import java.util.Date;
import java.util.List;

import com.clarkparsia.utils.NamespaceUtils;
import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

import com.clarkparsia.openrdf.vocabulary.FOAF;
import com.clarkparsia.openrdf.vocabulary.DC;
import com.clarkparsia.openrdf.ExtGraph;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * <p>Test cases for classes in the com.clarkparsia.empire.annotation package.</p>
 *
 * @author Michael Grove
 * @since 0.1
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
		// we should at least return an object in these cases.
		assertFalse(RdfGenerator.fromRdf(TestPerson.class, URI.create("urn:foo"), new TestDataSource()) == null);
	}

	@Test
	public void testUnbalancedNamespaces() {
		try {
			RdfGenerator.asRdf(new UnbalancedNamespaces());

			// the qname should not get expanded because the prefix was never asserted
			assertEquals(NamespaceUtils.uri("notvalid:test"), "notvalid:test");
		}
		catch (InvalidRdfException e) {
			e.printStackTrace();
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
			ExtGraph aGraph = RdfGenerator.asRdf(aPerson);

			org.openrdf.model.URI aPersonURI = aGraph.getValueFactory().createURI(aPerson.getRdfId().toString());

			assertEquals(aGraph.size(), 2);

			// the statements should assert that the person is of type foaf:TestPerson
			assertEquals(aGraph.getType(aPersonURI), FOAF.ontology().Person);

			// and that the mbox is correct
			assertEquals(aGraph.getLiteral(aPersonURI, FOAF.ontology().mbox).getLabel(), aPerson.getMBox());

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

			assertEquals((Float) Float.parseFloat(aGraph.getLiteral(aPersonURI, TestVocab.ontology().weight).getLabel()),
						 aPerson.getWeight());

			// this tests if "inferring" from an annotated getter works
			assertEquals(Boolean.valueOf(aGraph.getLiteral(aPersonURI, TestVocab.ontology().likesVideoGames).getLabel()),
						 aPerson.isLikesVideoGames());

			// and this tests if 'inferring" from an annotated setter works
			// also checking that it properly used the other namespace
			assertEquals(aGraph.getLiteral(aPersonURI, DC.ontology().title).getLabel(),
						 aPerson.getTitle());

			assertEquals(URI.create(aGraph.getValue(aPersonURI, DC.ontology().publisher).stringValue()),
						 aPerson.getWeblogURI());

			assertEquals(aGraph.getLiteral(aPersonURI, FOAF.ontology().firstName).getLabel(),
						 aPerson.getFirstName());

			assertEquals(aGraph.getLiteral(aPersonURI, FOAF.ontology().surname).getLabel(),
						 aPerson.getLastName());

			assertEquals(aGraph.getLiteral(aPersonURI, RDFS.LABEL).getLabel(),
						 aPerson.getLabel());

			List aKnows = CollectionUtil.list(aGraph.getValues(aPersonURI, FOAF.ontology().knows));

			assertEquals(aKnows.size(), 2);

			assertTrue(aKnows.contains(aGraph.getValueFactory().createURI(aJane.getRdfId().toString())));
			assertTrue(aKnows.contains(aGraph.getValueFactory().createURI(aJoe.getRdfId().toString())));
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
			Graph aGraph = RdfGenerator.asRdf(aBob);

			// this is the set of data that would normally be in the database
			Graph aSourceGraph = new GraphImpl();
			aSourceGraph.addAll(aGraph);
			aSourceGraph.addAll(RdfGenerator.asRdf(aJoe));
			aSourceGraph.addAll(RdfGenerator.asRdf(aJane));

			TestPerson aPerson = RdfGenerator.fromRdf(TestPerson.class, aBob.getRdfId(), new TestDataSource(aSourceGraph));

			assertEquals(aBob, aPerson);

			// now lets test the round trip w/ the added trick of a circular dependency
			aBob.setSpouse(aJane);

			aGraph = RdfGenerator.asRdf(aBob);

			// this is the set of data that would normally be in the database
			aSourceGraph = new GraphImpl();
			aSourceGraph.addAll(aGraph);
			aSourceGraph.addAll(RdfGenerator.asRdf(aJoe));
			aSourceGraph.addAll(RdfGenerator.asRdf(aJane));

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

		aImpl.setRdfId(asPrimaryKey(aTestURI));

		assertEquals(aImpl.getRdfId(), asPrimaryKey(aTestURI));

		try {
			aImpl.setRdfId(asPrimaryKey(URI.create("urn:new:id")));
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

	@Test
	public void testTransience() {
		TransientTest aObj = new TransientTest();

		aObj.foo = "foo";
		aObj.bar = "bar";
		aObj.baz = "baz";

		try {
			ExtGraph aGraph = RdfGenerator.asRdf(aObj);

			// we should have the normal field
			assertTrue(aGraph.contains(null, ValueFactoryImpl.getInstance().createURI("urn:foo"), null));

			// but neither of the transient ones
			assertFalse(aGraph.contains(null, ValueFactoryImpl.getInstance().createURI("urn:bar"), null));
			assertFalse(aGraph.contains(null, ValueFactoryImpl.getInstance().createURI("urn:baz"), null));
		}
		catch (InvalidRdfException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@RdfsClass("urn:TestClass")
	@Entity
	private static class NoDefaultConstructor extends BaseTestClass {
		NoDefaultConstructor(String foo) {
			setRdfId(asPrimaryKey(URI.create("urn:test:no:default")));
		}
	}


	@Namespaces({"", "http://xmlns.com/foaf/0.1/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/",
			 "notvalid"})
	@RdfsClass("foaf:Person")
	@Entity
	private static class UnbalancedNamespaces extends BaseTestClass {
		UnbalancedNamespaces() {
			setRdfId(asPrimaryKey(URI.create("urn:test:unbalanced")));
		}
	}

	@RdfsClass("urn:TestClass")
	@Entity
	private static class MultipleRdfIds {
		@RdfId
		private String one = "one";

		@RdfId
		private String two = "two";
	}

	@RdfsClass("urn:NoEntity")
	public static class NoEntity extends BaseTestClass {
	}

	@Entity
	@RdfsClass("urn:NoSupports")
	public static class NoSupports {
		@RdfId
		private String one = "one";
	}

	@Namespaces({"foaf", "http://xmlns.com/foaf/0.1/"})
	@Entity
	public static class NoRdfsClass extends BaseTestClass {
		@RdfId
		@RdfProperty("foaf:name")
		public String name;
	}

	@RdfsClass("urn:TestClass")
	@Entity
	public static class TransientTest extends BaseTestClass {
		@RdfProperty("urn:foo")
		private String foo;

		@RdfProperty("urn:bar")
		private transient String bar;

		@Transient
		@RdfProperty("urn:baz")
		private String baz;
	}
}
