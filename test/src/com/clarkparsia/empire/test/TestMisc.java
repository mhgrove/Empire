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

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.openrdf.model.Resource;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.RepositoryResult;
import com.clarkparsia.empire.codegen.InstanceGenerator;
import com.clarkparsia.empire.test.api.TestInterface;
import com.clarkparsia.empire.test.api.BaseTestClass;
import com.clarkparsia.empire.test.util.TestModule;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.ds.TripleSource;
import com.clarkparsia.empire.ds.MutableDataSource;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.sesametwo.RepositoryDataSourceFactory;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.openrdf.Graphs;
import com.clarkparsia.openrdf.OpenRdfUtil;
import com.clarkparsia.openrdf.ExtRepository;
import com.clarkparsia.common.util.PrefixMapping;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;

import javax.persistence.OneToOne;
import javax.persistence.CascadeType;
import javax.persistence.Persistence;
import javax.persistence.EntityManager;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.Query;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

/**
 * <p>Various miscellaneous tests for non-JPA parts of the Empire API.</p>
 *
 * @author Michael Grove
 * @version 0.6.4
 * @since 0.7
 */
public class TestMisc {
	@BeforeClass
	public static void beforeClass () {
		System.setProperty("empire.configuration.file", "test.empire.config.properties");

		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(),
					new JenaEmpireModule(), new TestModule());
	}

	@Test
	public void testInstGen() throws Exception {
		Class<TestInterface> aIntClass = InstanceGenerator.generateInstanceClass(TestInterface.class);

		TestInterface aInt = aIntClass.newInstance();

		// this should successfully re-use the previously generated class file.  we want to make sure
		// this can happen without error.
		TestInterface aInt2 = InstanceGenerator.generateInstanceClass(TestInterface.class).newInstance();

		URI aURI = URI.create("urn:uri");
		Integer aNumber = 5;
		String aStr = "some string value";
		SupportsRdfId.RdfKey aKey = new SupportsRdfId.URIKey(URI.create("urn:id"));
		SupportsRdfId.RdfKey aKey2 = new SupportsRdfId.URIKey(URI.create("urn:id2"));

		aInt.setURI(aURI);
		aInt.setInt(aNumber);
		aInt.setString(aStr);
		aInt.setRdfId(aKey);

		aInt2.setRdfId(aKey2);

		aInt.setObject(aInt2);

		assertEquals(aInt, aInt);
		assertEquals(aURI, aInt.getURI());
		assertEquals(aStr, aInt.getString());
		assertEquals(aNumber, aInt.getInt());
		assertEquals(aKey, aInt.getRdfId());
		assertEquals(aInt2, aInt.getObject());
		assertEquals(aKey.toString(), aInt.toString());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBadInstGen() throws Exception {
		InstanceGenerator.generateInstanceClass(BadTestInterface.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNoSupportsInstGen() throws Exception {
		InstanceGenerator.generateInstanceClass(NoSupportsTestInterface.class);
	}

	@Test
	public void testEmpireUtil() throws Exception {
		SupportsRdfId aId = new SupportsRdfIdImpl();

		assertTrue(EmpireUtil.asResource(aId) == null);

		Resource aRes = EmpireUtil.asResource(new SupportsRdfIdImpl(new SupportsRdfId.BNodeKey("asdf")));
		assertTrue(aRes instanceof BNode);
		assertEquals(((BNode)aRes).getID(), "asdf");

		aId = EmpireUtil.asSupportsRdfId(java.net.URI.create("urn:foo"));
		assertTrue(aId.getRdfId() instanceof SupportsRdfId.URIKey);
		assertEquals(aId.getRdfId().value(), java.net.URI.create("urn:foo"));

		assertTrue(EmpireUtil.getNamedGraph("") == null);

		SupportsRdfId.RdfKey aKey = EmpireUtil.asPrimaryKey(new URL("http://example.org"));
		assertTrue(aKey instanceof SupportsRdfId.URIKey);
		assertEquals(aKey.value(), new URL("http://example.org").toURI());

		BNode aAnon = ValueFactoryImpl.getInstance().createBNode("foobar");
		aKey = EmpireUtil.asPrimaryKey(aAnon);
		assertTrue(aKey instanceof SupportsRdfId.BNodeKey);
		assertEquals(aKey.value(), "foobar");
	}
	
	@Test
	public void testGenerateSuperInterfaces() {
		try {
			BaseInterface aBase = InstanceGenerator.generateInstanceClass(BaseInterface.class).newInstance();

			aBase.setFoo("some value");
			assertEquals("some value", aBase.getFoo());

			ChildInterface aChild = InstanceGenerator.generateInstanceClass(ChildInterface.class).newInstance();

			aChild.setBar(23);
			assertEquals(23, aChild.getBar());

			aChild.setFoo("some value");
			assertEquals("some value", aChild.getFoo());
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public interface BaseInterface extends SupportsRdfId {
		public String getFoo();
		public void setFoo(String theString);
	}

	public interface ChildInterface extends BaseInterface {
		public long getBar();
		public void setBar(long theString);
	}

	public interface NoSupportsTestInterface {
		public String getBar();
		public void setBar(String theStr);
	}
	
	public interface BadTestInterface extends SupportsRdfId {
		public void foo();

		public String getBar();
		public void setBar(String theStr);
	}

	/**
	 * Test to verify that setting the parameter of a query w/ a java.net.URI works and does not result in an NPE
	 */
	@Test
	public void testSettingQueryParameterAsURI() {
		EntityManager aManager = Persistence.createEntityManagerFactory("test-data-source").createEntityManager();
		

		try {
			Query aQuery = aManager.createQuery("select ?s where { ?s ??p ?o }");

			aQuery.setParameter("p", URI.create("urn:p"));
		}
		finally {
			aManager.close();
		}
	}

	@Test
	public void infiniteLoopsAreBadMmmK() {
		EntityManager aMgr = Persistence.createEntityManagerFactory("test-data-source").createEntityManager();


		try {
			One one = new One();
			Two two = new Two();

			one.two = two;
			two.one = one;

			aMgr.persist(one);

			// i'm not testing correctness of the persistence here, the other test cases should catch that
		}
		finally {
			aMgr.close();
		}
	}

	/**
	 * As reported on the mailing list "Problem with blank nodes" if you have a bnode as the target of an rdf:type triple, you end
	 * up with a class cast exception.
	 *
	 * @throws Exception test error
	 */
	@Test
	public void testBNodeTypeInProperty() throws Exception {
		EntityManager aMgr = Persistence.createEntityManagerFactory("test-data-source").createEntityManager();

		try {
			SupportsRdfId.RdfKey aId = new SupportsRdfId.URIKey(URI.create("urn:one"));

			One one = new One();
			one.setRdfId(aId);

			aMgr.persist(one);

			MutableDataSource ts = (MutableDataSource) aMgr.getDelegate();
			ts.add(Graphs.newGraph(ValueFactoryImpl.getInstance().createStatement(ValueFactoryImpl.getInstance().createURI("urn:two"),
																				  RDF.TYPE,
																				  ValueFactoryImpl.getInstance().createBNode()),
								   ValueFactoryImpl.getInstance().createStatement(ValueFactoryImpl.getInstance().createURI("urn:one"),
																				  ValueFactoryImpl.getInstance().createURI("http://empire.clarkparsia.com/hasMoreTwos"),
																				  ValueFactoryImpl.getInstance().createURI("urn:two"))));

			assertTrue(aMgr.find(One.class, aId) != null);
		}
		finally {
			aMgr.close();
		}
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/one")
	public static class One extends BaseTestClass {
		@OneToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
		@RdfProperty("http://empire.clarkparsia.com/hasTwo")
		Two two;

		@OneToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
		@RdfProperty("http://empire.clarkparsia.com/hasMoreTwos")
		Collection<BaseTestClass> listOfTwos;
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/two")
	public static class Two extends BaseTestClass {
		@OneToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
		@RdfProperty("http://empire.clarkparsia.com/hasOne")
		One one;
	}

	@Test
	public void testNPEUsingIsList() {
		System.setProperty("empire.configuration.file", "test.empire.config.properties");

		EntityManager aMgr = Persistence.createEntityManagerFactory("jena-test-data-source").createEntityManager();

		OneWithList one = new OneWithList();

		one.list.add(new Elem("a"));
		one.list.add(new Elem("b"));
		one.list.add(new Elem("c"));
		one.list.add(new Elem("d"));

		aMgr.persist(one);

		System.err.println(aMgr.find(OneWithList.class, one.getRdfId()).list);
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/OneWithList")
	public static class OneWithList extends BaseTestClass {
		@RdfProperty(value="http://empire.clarkparsia.com/list", isList=true)
		@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST})
		Collection<Elem> list = new ArrayList<Elem>();

		public OneWithList() {
		}
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/Elem")
	public static class Elem extends BaseTestClass {

		public Elem() {
		}

		private Elem(final String theName) {
			name = theName;
		}

		@RdfProperty("rdfs:label")
		public String name;

		@Override
		public String toString() {
			return "Elem: " + name;
		}
	}

	@Test
	public void testFunkyInstGenWithBoolean() throws Exception {
		IFoo aFoo = InstanceGenerator.generateInstanceClass(FooImpl.class).newInstance();

		assertTrue(aFoo.isDereferenced());
	}

	/**
	 * Test case for using generated instances and avoiding duplicates.  If you use a generated classes and persist it originally to an EM, then make changes on
	 * the *same* object and merge those changes, EmpireGenerated is not correctly populated, so nothing is deleted and you end up with duplicated values.  So
	 * now merge will do a describe on the instance if there is nothing known about it in EmpireGenerated to try and alleviate this case.  That's probably not
	 * strictly correct as you might not want the results of the describe deleted as that could be out of date wrt to the original object, but there's not much
	 * you can do.  Correct usage would be to refresh the object before doing anything more with it.
	 *
	 * @throws Exception test error
	 */
	@Test
	public void testDuplicate() throws Exception {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		Map aMap = Maps.newHashMap();
		aMap.put(RepositoryDataSourceFactory.REPO_HANDLE, aRepo);
		aMap.put("factory", "sesame");

		EntityManager em = Persistence.createEntityManagerFactory("test", aMap).createEntityManager();

		EntityTest aObj = InstanceGenerator.generateInstanceClass(EntityTest.class).newInstance();
		aObj.setId("someid");
		aObj.setLabel("some label");

		em.persist(aObj);

		aObj.setLabel("foo");

		em.merge(aObj);

		RepositoryResult<Statement> stmts = aRepo.getStatements();
		ExtGraph aGraph = new ExtGraph();
		while (stmts.hasNext()) {
			aGraph.add(stmts.next());
		}

		assertEquals(3, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.getStatements(null, ValueFactoryImpl.getInstance().createURI("urn:label"), null)).size());

		em.remove(aObj);

		ParentEntity pe = InstanceGenerator.generateInstanceClass(ParentEntity.class).newInstance();
		pe.setEntity(aObj);

		em.persist(pe);

		stmts = aRepo.getStatements();
		aGraph = new ExtGraph();
		while (stmts.hasNext()) {
			aGraph.add(stmts.next());
		}

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.getStatements(null, ValueFactoryImpl.getInstance().createURI("urn:label"), null)).size());

		aObj.setLabel("foobarbaz");

		em.merge(pe);

		stmts = aRepo.getStatements();
		aGraph = new ExtGraph();
		while (stmts.hasNext()) {
			aGraph.add(stmts.next());
		}

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.getStatements(null, ValueFactoryImpl.getInstance().createURI("urn:label"), null)).size());
	}

	/**
	 * Tests the same basic thing as {@link #testDuplicate()} except we do refreshes after persists which should auto-update the information in the EmpireGenerated
	 * stub internally and result in the correct behavior
	 * @throws Exception test error
	 */
	@Test
	public void testDuplicate2() throws Exception {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		Map aMap = Maps.newHashMap();
		aMap.put(RepositoryDataSourceFactory.REPO_HANDLE, aRepo);
		aMap.put("factory", "sesame");

		EntityManager em = Persistence.createEntityManagerFactory("test", aMap).createEntityManager();

		EntityTest aObj = InstanceGenerator.generateInstanceClass(EntityTest.class).newInstance();
		aObj.setId("someid");
		aObj.setLabel("some label");

		em.persist(aObj);

		em.refresh(aObj);

		aObj.setLabel("foo");

		em.merge(aObj);

		RepositoryResult<Statement> stmts = aRepo.getStatements();
		ExtGraph aGraph = new ExtGraph();
		while (stmts.hasNext()) {
			aGraph.add(stmts.next());
		}

		assertEquals(3, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.getStatements(null, ValueFactoryImpl.getInstance().createURI("urn:label"), null)).size());

		em.remove(aObj);

		ParentEntity pe = InstanceGenerator.generateInstanceClass(ParentEntity.class).newInstance();
		pe.setEntity(aObj);

		em.persist(pe);

		em.refresh(pe);

		stmts = aRepo.getStatements();
		aGraph = new ExtGraph();
		while (stmts.hasNext()) {
			aGraph.add(stmts.next());
		}

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.getStatements(null, ValueFactoryImpl.getInstance().createURI("urn:label"), null)).size());

		aObj.setLabel("foobarbaz");

		em.merge(pe);

		stmts = aRepo.getStatements();
		aGraph = new ExtGraph();
		while (stmts.hasNext()) {
			aGraph.add(stmts.next());
		}

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.getStatements(null, ValueFactoryImpl.getInstance().createURI("urn:label"), null)).size());
	}

	@Entity
	@RdfsClass("urn:EntityTest")
	public interface EntityTest extends SupportsRdfId {
		@RdfProperty("urn:id")
		String getId();
		void setId(String theId);

		@RdfProperty("urn:label")
		String getLabel();
		void setLabel(String theLabel);
	}

	@Entity
	@RdfsClass("urn:ParentEntity")
	public interface ParentEntity extends SupportsRdfId {

		@RdfProperty("urn:entity")
		@OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
		EntityTest getEntity();
		void setEntity(EntityTest theEntity);
	}

	public static abstract class FooImpl extends AbstractFoo implements IFoo {
	}
	
	public static interface IFoo {
		public boolean isDereferenced();
	}

	@MappedSuperclass
	public static abstract class AbstractFoo implements SupportsRdfId {
		/**
		 * @inheritDoc
		 */
		public boolean isDereferenced() {
			return true;
		}
	}

	@Test
	public void testTimesTwo() throws InvalidRdfException {
		TestDoubleImpl obj = new TestDoubleImpl();

		Graph g = RdfGenerator.asRdf(obj);

		assertEquals(1, new ExtGraph(g).getValues(EmpireUtil.asResource(obj), ValueFactoryImpl.getInstance().createURI(PrefixMapping.GLOBAL.uri("test:foo"))).size());
	}

	@MappedSuperclass
	public interface TestDouble extends SupportsRdfId {
		@RdfProperty("test:foo")
		public String getFoo();
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/TestDouble")
	public class TestDoubleImpl extends BaseTestClass implements TestDouble {
		@RdfProperty("test:foo")
		public String getFoo() {
			return "foo";
		}
	}
}
