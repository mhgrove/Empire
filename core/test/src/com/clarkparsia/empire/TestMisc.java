/*
 * Copyright (c) 2009-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.clarkparsia.empire.impl.EntityManagerFactoryImpl;
import com.clarkparsia.empire.api.TestDataSourceFactory;
import com.clarkparsia.empire.util.TestModule;
import com.clarkparsia.empire.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.openrdf.model.Resource;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.clarkparsia.empire.api.BaseTestClass;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.complexible.common.util.PrefixMapping;

import javax.persistence.Persistence;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import java.io.File;
import java.net.URL;
import java.util.Collections;

/**
 * <p>Various miscellaneous tests for non-JPA parts of the Empire API.</p>
 *
 * @author	Michael Grove
 * @since	0.6.4
 * @version 0.7
 */
public class TestMisc {
	// TODO: could use some more SPI tests

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("empire.configuration.file", new File(TestUtil.getProjectHome(), "core/test/test.empire.config.properties").getAbsolutePath());
        Empire.init(new TestModule());
    }

	@Test
	public void testInvalidFactory() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("no a factory",
																						 Collections.singletonMap("factory",
																												  "not a factory class name")));
	}

	@Test
	public void testNoFactory() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("no a factory",
																						 Collections.emptyMap()));
	}

	@Test
	public void testNotMutableDataSource() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("not mutable",
																						 Collections.singletonMap("factory",
																												  TestDataSourceFactory.class.getName())));
	}

	@Test
	public void testPersistenceHook() {
		assertEquals(Persistence.createEntityManagerFactory("test-data-source2").getClass(), EntityManagerFactoryImpl.class);
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
	public void testTimesTwo() throws InvalidRdfException {
		TestDoubleImpl obj = new TestDoubleImpl();

		Graph g = RdfGenerator.asRdf(obj);

		int aResult = GraphUtil.getObjects(g, EmpireUtil.asResource(obj), ValueFactoryImpl.getInstance().createURI(PrefixMapping.GLOBAL.uri("test:foo"))).size();

		assertEquals(1, aResult);
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
