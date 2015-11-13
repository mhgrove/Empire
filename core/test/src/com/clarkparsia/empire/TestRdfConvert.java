/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire;

import com.complexible.common.openrdf.model.Models2;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.openrdf.model.Model;
import org.openrdf.model.impl.SimpleValueFactory;

import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDFS;

import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfProperty;

import com.clarkparsia.empire.api.BaseTestClass;
import com.clarkparsia.empire.api.TestPerson;
import com.clarkparsia.empire.ds.DataSourceException;
import static com.clarkparsia.empire.util.EmpireUtil.asPrimaryKey;
import com.clarkparsia.empire.api.TestDataSource;
import com.clarkparsia.empire.api.TestVocab;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.complexible.common.openrdf.vocabulary.FOAF;
import com.complexible.common.openrdf.vocabulary.DC;
import com.complexible.common.base.Dates;
import com.complexible.common.util.PrefixMapping;
import com.google.common.collect.Lists;

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
			assertEquals(PrefixMapping.GLOBAL.uri("notvalid:test"), "notvalid:test");
		}
		catch (InvalidRdfException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testConvert() throws Exception {
		TestPerson aJoe = new TestPerson();
		aJoe.setMBox("mailto:joe@example.org");
		aJoe.setFirstName("Joe");

		TestPerson aJane = new TestPerson();
		aJane.setMBox("mailto:jane@example.org");
		aJane.setFirstName("Jane");

		TestPerson aPerson = new TestPerson();

		aPerson.setMBox("mailto:bob@example.org");

		try {
			Model aGraph = RdfGenerator.asRdf(aPerson);

			org.openrdf.model.IRI aPersonURI = SimpleValueFactory.getInstance().createIRI(aPerson.getRdfId().toString());

			assertEquals(aGraph.size(), 2);

			// the statements should assert that the person is of type foaf:TestPerson
			assertTrue(Sets.newHashSet(Models2.getTypes(aGraph, aPersonURI)).contains(FOAF.ontology().Person));

			// and that the mbox is correct
			assertEquals(Models2.getLiteral(aGraph, aPersonURI, FOAF.ontology().mbox).get().getLabel(), aPerson.getMBox());

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

			assertEquals((Float) Float.parseFloat(Models2.getLiteral(aGraph, aPersonURI, TestVocab.ontology().weight).get().getLabel()),
						 aPerson.getWeight());

			// this tests if "inferring" from an annotated getter works
			assertEquals(Boolean.valueOf(Models2.getLiteral(aGraph, aPersonURI, TestVocab.ontology().likesVideoGames).get().getLabel()),
						 aPerson.isLikesVideoGames());

			// and this tests if 'inferring" from an annotated setter works
			// also checking that it properly used the other namespace
			assertEquals(Models2.getLiteral(aGraph, aPersonURI, DC.ontology().title).get().getLabel(),
						 aPerson.getTitle());

			assertEquals(URI.create(GraphUtil.getUniqueObject(aGraph, aPersonURI, DC.ontology().publisher).stringValue()),
						 aPerson.getWeblogURI());

			assertEquals(Models2.getLiteral(aGraph, aPersonURI, FOAF.ontology().firstName).get().getLabel(),
						 aPerson.getFirstName());

			assertEquals(Models2.getLiteral(aGraph, aPersonURI, FOAF.ontology().surname).get().getLabel(),
						 aPerson.getLastName());

			assertEquals(Models2.getLiteral(aGraph, aPersonURI, RDFS.LABEL).get().getLabel(),
						 aPerson.getLabel());

			List aKnows = Lists.newArrayList(GraphUtil.getObjects(aGraph, aPersonURI, FOAF.ontology().knows));

			assertEquals(aKnows.size(), 2);

			assertTrue(aKnows.contains(SimpleValueFactory.getInstance().createIRI(aJane.getRdfId().toString())));
			assertTrue(aKnows.contains(SimpleValueFactory.getInstance().createIRI(aJoe.getRdfId().toString())));
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

		aBob.setBirthday(Dates.asDate("1980-01-01"));
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
			Model aGraph = RdfGenerator.asRdf(aBob);

			// this is the set of data that would normally be in the database
			Model aSourceGraph = Models2.newModel();
			aSourceGraph.addAll(aGraph);
			aSourceGraph.addAll(RdfGenerator.asRdf(aJoe));
			aSourceGraph.addAll(RdfGenerator.asRdf(aJane));

			TestPerson aPerson = RdfGenerator.fromRdf(TestPerson.class, aBob.getRdfId(), new TestDataSource(aSourceGraph));

			assertEquals(aBob, aPerson);

			// now lets test the round trip w/ the added trick of a circular dependency
			aBob.setSpouse(aJane);

			aGraph = RdfGenerator.asRdf(aBob);

			// this is the set of data that would normally be in the database
			aSourceGraph = Models2.newModel();
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
    public void testThawByInterfacesAndCustomClasses() {
        MyInterfaceImpl aImpl = new MyInterfaceImpl();
        MyRootClass root = new MyRootClass();
        root.addFoo( aImpl );

        try {
            Collection<Class<?>> klasses = new ArrayList<Class<?>>(  );
            klasses.add( MyInterfaceImpl.class );
            klasses.add( MyRootClass.class );
            RdfGenerator.init( klasses );

	        Model aGraph = RdfGenerator.asRdf( root );

	        Model aSourceGraph = Models2.newModel();
            aSourceGraph.addAll(aGraph);

            MyRootClass aRoot = RdfGenerator.fromRdf(MyRootClass.class, "urn:id:00", new TestDataSource(aSourceGraph));
            assertSame( aRoot.getFoo().get( 0 ).getClass(), aImpl.getClass() );
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }

	@Test
	public void testTransience() {
		TransientTest aObj = new TransientTest();

		aObj.foo = "foo";
		aObj.bar = "bar";
		aObj.baz = "baz";

		try {
			Model aGraph = RdfGenerator.asRdf(aObj);

			// we should have the normal field
			assertTrue(aGraph.contains(null, SimpleValueFactory.getInstance().createIRI("urn:foo"), null));

			// but neither of the transient ones
			assertFalse(aGraph.contains(null, SimpleValueFactory.getInstance().createIRI("urn:bar"), null));
			assertFalse(aGraph.contains(null, SimpleValueFactory.getInstance().createIRI("urn:baz"), null));
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



    public abstract static class EmpireImpl implements SupportsRdfId, EmpireGenerated {
        private RdfKey key;
        private Model allTriples;
        private Model instanceTriples;

        public EmpireImpl( URIKey uriKey ) {
            this.key = uriKey;
        }

        @Override
        public RdfKey getRdfId() {
            return key;
        }

        @Override
        public void setRdfId( RdfKey theId ) {
            this.key = theId;
        }

        @Override
        public Model getAllTriples() {
            return allTriples;
        }

        @Override
        public void setAllTriples( Model aGraph ) {
            this.allTriples = aGraph;
        }

        @Override
        public Model getInstanceTriples() {
            return instanceTriples;
        }

        @Override
        public void setInstanceTriples( Model aGraph ) {
            this.instanceTriples = aGraph;
        }

    }

    @RdfsClass("urn:MyInterface")
    @Entity
    public static interface MyInterface extends SupportsRdfId {
    }

    @RdfsClass("urn:MyInterface")
    @Entity
    public static class MyInterfaceImpl extends EmpireImpl implements MyInterface, EmpireGenerated {

        public MyInterfaceImpl() {
            super( new URIKey( URI.create( "urn:sub:01" ) ) );
        }

        @Override
        public Class getInterfaceClass() {
            return MyInterface.class;
        }
    }

    @RdfsClass("urn:MyClassOfSort")
    @Entity
    public static class MyRootClass extends EmpireImpl implements SupportsRdfId, EmpireGenerated {

        List<MyInterface> foo;

        public MyRootClass() {
            super( new URIKey( URI.create( "urn:id:00" ) ) );
            foo = new ArrayList<MyInterface>(  );
        }

        @RdfProperty("urn:foo")
        public List<MyInterface> getFoo() {
            return foo;
        }

        public void setFoo( List<MyInterface> foo ) {
            this.foo = foo;
        }

        @Override
        public Class getInterfaceClass() {
            return EmpireGenerated.class;
        }

        public void addFoo( MyInterface aImpl ) {
            this.foo.add( aImpl );
        }
    }
}