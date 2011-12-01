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
import org.openrdf.model.impl.ValueFactoryImpl;
import com.clarkparsia.empire.codegen.InstanceGenerator;
import com.clarkparsia.empire.test.api.TestInterface;
import com.clarkparsia.empire.test.api.BaseTestClass;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.openrdf.ExtGraph;
import com.clarkparsia.common.util.PrefixMapping;

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

/**
 * <p>Various miscellaneous tests for non-JPA parts of the Empire API.</p>
 *
 * @author Michael Grove
 * @version 0.6.4
 * @since 0.7
 */
public class TestMisc {

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

		One one = new One();
		Two two = new Two();

		one.two = two;
		two.one = one;

		aMgr.persist(one);

		// i'm not testing correctness of the persistence here, the other test cases should catch that
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/one")
	private class One extends BaseTestClass {
		@OneToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
		@RdfProperty("http://empire.clarkparsia.com/hasTwo")
		Two two;
	}

	@Entity
	@RdfsClass("http://empire.clarkparsia.com/two")
	private class Two extends BaseTestClass {
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
