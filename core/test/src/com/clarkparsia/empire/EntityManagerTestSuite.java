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

import java.io.File;
import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceException;
import javax.persistence.PreRemove;
import javax.persistence.Query;

import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
import com.clarkparsia.empire.codegen.InstanceGenerator;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.ds.MutableDataSource;
import com.clarkparsia.empire.ds.SupportsTransactions;
import com.clarkparsia.empire.ds.TripleSource;

import com.clarkparsia.empire.impl.EntityManagerFactoryImpl;
import com.clarkparsia.empire.api.BaseTestClass;

import com.clarkparsia.empire.api.TestEntityListener;
import com.clarkparsia.empire.api.nasa.FoafPerson;
import com.clarkparsia.empire.api.nasa.Launch;
import com.clarkparsia.empire.api.nasa.LaunchSite;
import com.clarkparsia.empire.api.nasa.LaunchUsingProxy;
import com.clarkparsia.empire.api.nasa.SpaceVocab;
import com.clarkparsia.empire.api.nasa.Spacecraft;
import com.clarkparsia.empire.typing.A;
import com.clarkparsia.empire.typing.AnotherB;
import com.clarkparsia.empire.typing.B;

import com.clarkparsia.empire.util.TestUtil;
import com.complexible.common.openrdf.model.ModelIO;
import com.complexible.common.openrdf.model.Models2;

import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

import org.openrdf.rio.RDFParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 *
 * <p>Tests for the basic capabilities of an EntityManger backed by an RDF triple store.  Implementers should provide an appropriate {@link DataSourceFactory} for
 * their {@link DataSource} implementation to test it with Empire.</p>
 * 
 * @author Michael Grove
 * @since	0.7.1
 * @version 1.0
 */
public abstract class EntityManagerTestSuite {

	public static final String DATA_DIR = System.getProperty("empire.test.data") != null ? System.getProperty("empire.test.data") : TestUtil.getTestDataDirPath("data" );
	public static final String DATA_FILE = DATA_DIR + "/lite.nasa.nt";
	public static final String TYPING_FILE = DATA_DIR + "/typing.ttl";

	private static final String TEST_QUERY = "where { ?result <urn:prop> ?y }";
	private static final String TEST_NATIVE_QUERY = "select distinct ?result where { ?uri <" + SpaceVocab.ontology().mass + "> ?result }";
	private static final String TEST_NATIVE_FRAGMENT = "where { ?uri <" + SpaceVocab.ontology().mass + "> ?result }";
	private static final String TEST_AGENCY_QUERY = "where { ?result <" + SpaceVocab.ontology().agency + "> \"U.S.S.R\" }";
	private static final String TEST_AGENCY_WC_QUERY = "where { ?result <" + SpaceVocab.ontology().agency + "> ?? }";
	private static final String TEST_PARAM_QUERY = "where { ?result <" + SpaceVocab.ontology().agency + "> ??. ?result <" + SpaceVocab.ontology().alternateName + "> ??altName }";
	private static final String TEST_QUERY_NAME = "sovietSpacecraftSPARQL";

	@Before
	public void before() {
	}

	@After
	public void after() {
	}

	@BeforeClass
	public static void beforeClass() {
		RdfGenerator.init(Lists.<Class<?>>newArrayList(A.class, B.class));

		// our test data set doesn't type any literals, so we have to set to weak (no) typing
		// TODO: don't hard code this if we're doing tests w/ other datasets.
		EmpireOptions.STRONG_TYPING = false;
	}

	@AfterClass
	public static void afterClass() {
	}


	private EntityManagerFactory createEntityManagerFactory() {
		return new EntityManagerFactoryImpl(createDataSourceFactory());
	}

	protected abstract DataSourceFactory createDataSourceFactory();

	private EntityManager createEntityManager() {
		return createEntityManagerFactory().createEntityManager();
	}

	private EntityManager createClosedEntityManager() {
		EntityManager aManager = createEntityManager();
		aManager.close();

		return aManager;
	}

	/**
	 * Test to verify that setting the parameter of a query w/ a java.net.URI works and does not result in an NPE
	 */
	@Test
	public void testSettingQueryParameterAsURI() {
		EntityManager aManager = createEntityManager();

		try {
			Query aQuery = aManager.createQuery("select ?s where { ?s ??p ?o }");

			aQuery.setParameter("p", URI.create("urn:p"));
		}
		finally {
			aManager.close();
		}
	}

	/**
	 * Test to ensure we don't get into an infinite loop persisting two objects which both hold a reference to each other.
	 */
	@Test
	public void infiniteLoopsAreBadMmmK() {
		EntityManager aMgr = createEntityManager();

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
		EntityManager aMgr = createEntityManager();

		try {
			SupportsRdfId.RdfKey aId = new SupportsRdfId.URIKey(URI.create("urn:one"));

			One one = new One();
			one.setRdfId(aId);

			aMgr.persist(one);

			MutableDataSource ts = (MutableDataSource) aMgr.getDelegate();
            if (ts instanceof SupportsTransactions) {
                ((SupportsTransactions) ts).begin();
            }

			ts.add(Models2.newModel(SimpleValueFactory.getInstance().createStatement(SimpleValueFactory.getInstance().createIRI("urn:two"),
																				  RDF.TYPE,
																				  SimpleValueFactory.getInstance().createBNode()),
			                       SimpleValueFactory.getInstance().createStatement(SimpleValueFactory.getInstance().createIRI("urn:one"),
								                                                  SimpleValueFactory.getInstance().createIRI("http://empire.clarkparsia.com/hasMoreTwos"),
								                                                  SimpleValueFactory.getInstance().createIRI("urn:two"))));

            if (ts instanceof SupportsTransactions) {
                ((SupportsTransactions) ts).commit();
            }

			assertTrue(aMgr.find(One.class, aId) != null);
		}
		finally {
			aMgr.close();
		}
	}

	/**
	 * Test to verify we don't get an NPE when using the isList descriptor in the RdfProperty annotation
	 * and that we correctly get the list back
	 */
	@Test
	public void testNPEUsingIsList() {
		EntityManager aMgr = createEntityManager();

		assumeTrue(((DataSource) aMgr.getDelegate()).getQueryFactory().getDialect().supportsStableBnodeIds());

		OneWithList one = new OneWithList();

		one.list.add(new Elem("a"));
		one.list.add(new Elem("b"));
		one.list.add(new Elem("c"));
		one.list.add(new Elem("d"));

		aMgr.persist(one);

		List<Elem> c = Lists.newArrayList(aMgr.find(OneWithList.class, one.getRdfId()).list);

		assertEquals(c, one.list);
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
		EntityManager em = createEntityManager();

		assumeTrue(em.getDelegate() instanceof TripleSource);

		TripleSource aSource = (TripleSource) em.getDelegate();

		EntityTest aObj = InstanceGenerator.generateInstanceClass(EntityTest.class).newInstance();
		aObj.setId("someid");
		aObj.setLabel("some label");

		em.persist(aObj);

		aObj.setLabel("foo");

		em.merge(aObj);

		Graph aGraph = Models2.newModel(aSource.getStatements(null, null, null));

		assertEquals(3, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.match(null, SimpleValueFactory.getInstance().createIRI("urn:label"), null)).size());

		em.remove(aObj);

		ParentEntity pe = InstanceGenerator.generateInstanceClass(ParentEntity.class).newInstance();
		pe.setEntity(aObj);

		em.persist(pe);

		aGraph = Models2.newModel(aSource.getStatements(null, null, null));

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.match(null, SimpleValueFactory.getInstance().createIRI("urn:label"), null)).size());

		aObj.setLabel("foobarbaz");

		em.merge(pe);

		aGraph = Models2.newModel(aSource.getStatements(null, null, null));

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.match(null, SimpleValueFactory.getInstance().createIRI("urn:label"), null)).size());
	}


	/**
	 * Tests the same basic thing as {@link #testDuplicate()} except we do refreshes after persists which should auto-update the information in the EmpireGenerated
	 * stub internally and result in the correct behavior
	 * @throws Exception test error
	 */
	@Test
	public void testDuplicate2() throws Exception {
		EntityManager em = createEntityManager();

		assumeTrue(em.getDelegate() instanceof TripleSource);
		
		TripleSource aSource = (TripleSource) em.getDelegate();

		EntityTest aObj = InstanceGenerator.generateInstanceClass(EntityTest.class).newInstance();
		aObj.setId("someid");
		aObj.setLabel("some label");

		em.persist(aObj);

		em.refresh(aObj);

		aObj.setLabel("foo");

		em.merge(aObj);

		Graph aGraph = Models2.newModel(aSource.getStatements(null, null, null));

		assertEquals(3, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.match(null, SimpleValueFactory.getInstance().createIRI("urn:label"), null)).size());

		em.remove(aObj);

		ParentEntity pe = InstanceGenerator.generateInstanceClass(ParentEntity.class).newInstance();
		pe.setEntity(aObj);

		em.persist(pe);

		em.refresh(pe);

		aGraph = Models2.newModel(aSource.getStatements(null, null, null));

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.match(null, SimpleValueFactory.getInstance().createIRI("urn:label"), null)).size());

		aObj.setLabel("foobarbaz");

		em.merge(pe);

		aGraph = Models2.newModel(aSource.getStatements(null, null, null));

		assertEquals(5, aGraph.size());
		assertEquals(1, Lists.newArrayList(aGraph.match(null, SimpleValueFactory.getInstance().createIRI("urn:label"), null)).size());
	}

	/**
	 * Test for basic proxying using Javassist
	 * @throws Exception test error
	 */
	@Test
	public void testProxying() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		// this is not a bullet proof proxy detection, but it should prove whether or not proxying is correctly
		// inserted into the chain of events.

		String javaAssistMarker = "$$_javassist";

		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";

		Launch aLaunch = aManager.find(Launch.class, aLaunchURI);

		LaunchUsingProxy aProxySupportingLaunch = aManager.find(LaunchUsingProxy.class, aLaunchURI);

		// this object is proxied
		LaunchSite aOrigSite = aProxySupportingLaunch.getLaunchSite();

		// this should be a proxy class...
		// the easy way to check this is via the class name to see if its a javassist created class.
		// this means the proxy was correctly created in RdfGenerator
		// and the proxy *works* if the results of the gets and sets are what is expected.
		assertTrue(aOrigSite.getClass().getName().indexOf(javaAssistMarker) != -1);

		// and the non-proxying version should not return a proxy object
		assertTrue(aLaunch.getLaunchSite().getClass().getName().indexOf(javaAssistMarker) == -1);

		// and the proxied object should be equal to a non proxied version of the same thing
		assertEquals(aOrigSite, aLaunch.getLaunchSite());

		// we want to make sure that get/set operations work via a proxied object fine, that we don't get anything cached
		// or stale proxied objects returned accidentally

		LaunchSite aNewSite = new LaunchSite();
		aNewSite.setLabel(Arrays.asList("new launch site"));

		assertFalse(aOrigSite.equals(aNewSite));

		assertEquals(aProxySupportingLaunch.getLaunchSite(), aOrigSite);

		aProxySupportingLaunch.setLaunchSite(aNewSite);

		assertEquals(aProxySupportingLaunch.getLaunchSite(), aNewSite);
	}

	/**
	 * Test for typing of proxied objects
	 * 
	 * @throws Exception test error
	 */
	@Test
	public void testTargetEntity() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";
		
		Launch aLaunch = aManager.find(Launch.class, aLaunchURI);
		LaunchUsingProxy aProxySupportingLaunch = aManager.find(LaunchUsingProxy.class, aLaunchURI);		

		assertTrue(aProxySupportingLaunch.getSpacecraft().size() > 0);

		// the values for this are all space craft in the data
		// but the mapping just specifies an untyped collection (a List).
		// we're using the targetEntity property of the OneToMany annotation to
		// specify the type, so we want to make sure that everything returned by this
		// function is a space craft.  The fact that it returns at all pretty much
		// guarantees that, but we want to make sure.
		for (Object aObj : aProxySupportingLaunch.getSpacecraft()) {
			assertTrue(aObj instanceof Spacecraft);
		}

		// "normal" mapping should be equal to the proxied, targetEntity mapped version
		assertTrue(Lists.newArrayList(aLaunch.getSpacecraft()).equals(Lists.newArrayList(aProxySupportingLaunch.getSpacecraft())));
	}

	/**
	 * Test for basic cascading of remove operations
	 * @throws Exception test error
	 */
	@Test
	public void testRemoveCascading() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";
		String aOtherLaunchURI = "http://nasa.dataincubator.org/launch/PION2";

		// ================= test remove cascade
		LaunchUsingProxy aExistingLaunchWithProxy = aManager.find(LaunchUsingProxy.class, aLaunchURI);
		
		Launch aExistingLaunch = aManager.find(Launch.class, aOtherLaunchURI);
		
		assertTrue(aExistingLaunch != null);

		List<Spacecraft> aExistingSpacecraft = aExistingLaunch.getSpacecraft();

		assertTrue(aExistingSpacecraft.size() > 0);

		aExistingLaunch.getSpacecraft().clear();

		aManager.remove(aExistingLaunch);

		// delete shoudl have worked...
		assertFalse(aManager.contains(aExistingLaunch));

		// but not cascaded
		for (Spacecraft aCraft : aExistingSpacecraft) {
			assertTrue(aManager.contains(aCraft));
		}

		aExistingSpacecraft = aExistingLaunchWithProxy.getSpacecraft();
		assertTrue(aExistingSpacecraft.size() > 0);

		aManager.remove(aExistingLaunchWithProxy);

		// delete should have worked...
		assertFalse(aManager.contains(aExistingLaunchWithProxy));

		// and delete should have cascaded
		for (Spacecraft aCraft : aExistingSpacecraft) {
			assertFalse(aManager.contains(aCraft));
		}
	}

	/**
	 * Test for basic persist cascading
	 * @throws Exception test error
	 */
	@Test
	public void testPersistCascading() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));
		
		// ================= test persist cascade

		LaunchUsingProxy aNewLaunchWithProxy = new LaunchUsingProxy();
		Launch aNewLaunch = new Launch();

		Spacecraft aNewOtherSpacecraft = new Spacecraft();
		aNewOtherSpacecraft.setRdfId(new SupportsRdfId.URIKey(URI.create("http://empire.clarkparsia.com/test/persist/cascade/aNewOtherSpacecraft")));
		aNewOtherSpacecraft.setAgency("agency");

		aNewLaunchWithProxy.setOtherSpacecraft(aNewOtherSpacecraft);
		aNewLaunch.setOtherSpacecraft(aNewOtherSpacecraft);

		aManager.persist(aNewLaunch);

		assertTrue(aManager.contains(aNewLaunch));

		// it was persisted as a relation...
		assertTrue(aManager.find(Launch.class, aNewLaunch.getRdfId()).getOtherSpacecraft().getRdfId().equals(aNewOtherSpacecraft.getRdfId()));

		// but persist does not cascade
		assertFalse(aManager.contains(aNewOtherSpacecraft));

		aManager.persist(aNewLaunchWithProxy);

		assertTrue(aManager.contains(aNewLaunchWithProxy));

		// it was persisted as a relation...
		assertTrue(aManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getOtherSpacecraft().equals(aNewOtherSpacecraft));

		// and this should be true now because the persist was cascaded
		assertTrue(aManager.contains(aNewOtherSpacecraft));
	}

	/**
	 * Test basic merge cascading
	 * @throws Exception test error
	 */
	@Test
	public void testMergeCascading() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));
		
		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";
		String aOtherLaunchURI = "http://nasa.dataincubator.org/launch/PION2";

		// =============== test merge cascade
		LaunchUsingProxy aExistingLaunchWithProxy = aManager.find(LaunchUsingProxy.class, aLaunchURI);
		Launch aExistingLaunch = aManager.find(Launch.class, aOtherLaunchURI);
		
		assertTrue(aExistingLaunch != null);

		Spacecraft aNewSpacecraft = new Spacecraft();
		aNewSpacecraft.setRdfId(new SupportsRdfId.URIKey(URI.create("http://empire.clarkparsia.com/test/merge/cascade/aNewOtherSpacecraft")));
		aNewSpacecraft.setAgency("agency");

		aExistingLaunch.setOtherSpacecraft(aNewSpacecraft);
		aExistingLaunchWithProxy.setOtherSpacecraft(aNewSpacecraft);

		aManager.merge(aExistingLaunch);

		assertTrue(aManager.contains(aExistingLaunch));
		// it was merged as a relation...
		assertTrue(aManager.find(Launch.class, aOtherLaunchURI).getOtherSpacecraft().getRdfId().equals(aNewSpacecraft.getRdfId()));

		// but merge does not cascade
		assertFalse(aManager.contains(aNewSpacecraft));

		aManager.merge(aExistingLaunchWithProxy);

		assertTrue(aManager.contains(aExistingLaunchWithProxy));

		// this should be true now because the merge was cascaded
		assertTrue(aManager.contains(aNewSpacecraft));

	}

	/**
	 * Test all types of cascading
	 * @throws Exception test error
	 */
	@Test
	public void testAllCascade() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		// ============ test all cascade
		Launch aNewLaunch = new Launch();
		LaunchUsingProxy aNewLaunchWithProxy = new LaunchUsingProxy();

		LaunchSite aNewSiteOne = new LaunchSite();
		aNewSiteOne.setLabel(Arrays.asList("new launch site one"));

		LaunchSite aNewSiteTwo = new LaunchSite();
		aNewSiteTwo.setLabel(Arrays.asList("new launch site two"));

		aNewLaunch.setLaunchSite(aNewSiteOne);
		aNewLaunchWithProxy.setLaunchSite(aNewSiteOne);

		aManager.persist(aNewLaunch);

		assertTrue(aManager.contains(aNewLaunch));
		assertTrue(aManager.find(Launch.class, aNewLaunch.getRdfId()).getLaunchSite().getRdfId().equals(aNewSiteOne.getRdfId()));
		assertFalse(aManager.contains(aNewSiteOne));

		aManager.persist(aNewLaunchWithProxy);

		assertTrue(aManager.contains(aNewLaunchWithProxy));
		assertTrue(aManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getLaunchSite().equals(aNewSiteOne));
		assertTrue(aManager.contains(aNewSiteOne));

		aNewLaunch.setLaunchSite(aNewSiteTwo);
		aNewLaunchWithProxy.setLaunchSite(aNewSiteTwo);

		aManager.merge(aNewLaunch);

		assertTrue(aManager.contains(aNewLaunch));
		assertTrue(aManager.find(Launch.class, aNewLaunch.getRdfId()).getLaunchSite().getRdfId().equals(aNewSiteTwo.getRdfId()));
		assertFalse(aManager.contains(aNewSiteTwo));

		aManager.merge(aNewLaunchWithProxy);

		assertTrue(aManager.contains(aNewLaunchWithProxy));
		assertTrue(aManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getLaunchSite().equals(aNewSiteTwo));
		assertTrue(aManager.contains(aNewSiteTwo));

		aManager.remove(aNewLaunch);

		assertFalse(aManager.contains(aNewLaunch));
		assertTrue(aManager.contains(aNewSiteTwo));

		aManager.remove(aNewLaunchWithProxy);

		assertFalse(aManager.contains(aNewLaunchWithProxy));
		assertFalse(aManager.contains(aNewSiteTwo));
	}

	@Test
	public void testDirectTypingRead1() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		A a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));

		assertNotNull(a);
		assertEquals("A", a.getPropA());
		assertTrue(a instanceof EmpireGenerated);

		EmpireGenerated empireGenerated = (EmpireGenerated) a;
		assertEquals(7, empireGenerated.getAllTriples().size());
		assertEquals(6, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testDirectReadWrite() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		A a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));
		EmpireGenerated empireGenerated = (EmpireGenerated) a;
		assertEquals(7, empireGenerated.getAllTriples().size());

		aManager.merge(a);

		a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));

		empireGenerated = (EmpireGenerated) a;
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
		assertEquals(7, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(6, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testDirectTypingRead2() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		B b = aManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));

		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());

		assertTrue(b instanceof EmpireGenerated);

		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(3, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testDirectReadWrite2() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		B b = aManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(3, empireGenerated.getAllTriples().size());

		aManager.merge(b);

		b = aManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));

		empireGenerated = (EmpireGenerated) b;
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
		assertEquals(3, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testIndirectTypingRead1() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		A a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b1"));

		assertNotNull(a);
		assertEquals("A", a.getPropA());

		assertTrue(a instanceof B);

		B b = (B) a;

		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());

		assertTrue(b instanceof EmpireGenerated);

		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(3, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testIndirectReadWrite1() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

        assertFalse(((MutableDataSource)aManager.getDelegate()).ask("ask {?s ?p ?o}"));

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		A a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		EmpireGenerated empireGenerated = (EmpireGenerated) a;
		assertEquals(3, empireGenerated.getAllTriples().size());

		aManager.merge(a);

		a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b1"));

		empireGenerated = (EmpireGenerated) a;
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
		assertEquals(3, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());

		B b = aManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		empireGenerated = (EmpireGenerated) b;
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
		assertEquals(3, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());

	}

	@Test
	public void testDirectTypingRead3() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		B b = aManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b2"));

		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());

		assertTrue(b instanceof EmpireGenerated);

		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}


	@Test
	public void testIndirectTypingRead2() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		A a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b2"));

		assertNotNull(a);
		assertEquals("A", a.getPropA());

		assertTrue(a instanceof B);

		B b = (B) a;

		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());

		assertTrue(b instanceof EmpireGenerated);

		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testDirectTypingAlternativeRead() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		AnotherB b = aManager.find(AnotherB.class, URI.create("urn:clarkparsia.com:empire:test:b2"));

		assertNotNull(b);
		assertEquals("B", b.getPropB());

		assertTrue(b instanceof EmpireGenerated);

		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(1, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testDirectTypingAlternativeReadWrite() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		AnotherB b = aManager.find(AnotherB.class, URI.create("urn:clarkparsia.com:empire:test:b2"));
		EmpireGenerated empireGenerated = (EmpireGenerated) b;
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(1, empireGenerated.getInstanceTriples().size());

		aManager.merge(b);

		b = aManager.find(AnotherB.class, URI.create("urn:clarkparsia.com:empire:test:b2"));

		empireGenerated = (EmpireGenerated) b;
		assertEquals(4, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(1, empireGenerated.getInstanceTriples().size());
	}

	@Test
	public void testLinks() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(TYPING_FILE));

		A a = aManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));

		assertEquals(5, a.getA().size());

		for (A linkedA : a.getA()) {
			if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:a2")) {
				assertEquals("A-a2", linkedA.getPropA());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b1")) {
				assertEquals("A", linkedA.getPropA());

				assertTrue(linkedA instanceof B);
				assertEquals("B", ((B) linkedA).getPropB());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b2")) {
				assertEquals("A", linkedA.getPropA());

				assertTrue(linkedA instanceof B);
				assertEquals("B", ((B) linkedA).getPropB());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b3")) {
				assertEquals("A-b3", linkedA.getPropA());

				assertTrue(linkedA instanceof B);
				assertEquals("B-b3", ((B) linkedA).getPropB());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b4")) {
				assertEquals("A-b4", linkedA.getPropA());

				assertTrue(linkedA instanceof B);
				assertEquals("B-b4", ((B) linkedA).getPropB());
			}
		}
	}

	

	/*
	 * Set of tests that are basic core EntityManager functions, primarily the find, query, persist, etc. methods which depend on the underlying DataSource implementation.
	 */

	@Test @Ignore
	public void testTransactionSupport() {
		// TODO: implement test
	}

	@Test
	public void testLifeCycleExceptions() throws Exception {
		EntityManager aManager = createEntityManager();

		ExceptionInLifecycle aBean = new ExceptionInLifecycle();

		aManager.persist(aBean);

		try {
			aManager.remove(aBean);
			fail("Should not have removed bean");
		}
		catch (PersistenceException theE) {
			// expected
		}
	}

	public static class RemoveProhibitor {
		@PreRemove
		public void prohibit(Object o) {
			throw new PersistenceException(o.getClass().getName() + " instances may not be removed");
		}
	}

	@Entity
	@EntityListeners({RemoveProhibitor.class})
	@RdfsClass("urn:Foo")
	private static final class ExceptionInLifecycle implements SupportsRdfId {
		private final SupportsRdfIdImpl mId = new SupportsRdfIdImpl();

		@Override
		public RdfKey getRdfId() {
			return mId.getRdfId();
		}

		@Override
		public void setRdfId(final RdfKey theId) {
			mId.setRdfId(theId);
		}
	}

	@Test
	public void testPreAndPostHooks() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		TestEntityListener.clearState();

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  URI.create("http://nasa.dataincubator.org/spacecraft/1989-033B"));

		assertTrue(aCraft.postLoadCalled);
		assertTrue(TestEntityListener.postLoadCalled);

		TestEntityListener.clearState();
		aCraft.clearState();

		aCraft.setDescription("some new description");

		aCraft = aManager.merge(aCraft);

		assertTrue(aCraft.preUpdateCalled);
		assertTrue(aCraft.postUpdateCalled);
		assertTrue(TestEntityListener.preUpdateCalled);
		assertTrue(TestEntityListener.postUpdateCalled);

		TestEntityListener.clearState();
		aCraft.clearState();

		aManager.remove(aCraft);

		assertTrue(aCraft.preRemoveCalled);
		assertTrue(aCraft.postRemoveCalled);
		assertTrue(TestEntityListener.preRemoveCalled);
		assertTrue(TestEntityListener.postRemoveCalled);

		TestEntityListener.clearState();
		aCraft.clearState();

		Spacecraft aNewCraft = new Spacecraft();

		aNewCraft.setAgency("U.S.A");
		aNewCraft.setAlternateName(Collections.singletonList("67890"));
		aNewCraft.setDescription("The newer rocket to return to the moon");
		aNewCraft.setName("Ares 2");
		aNewCraft.setMass("5000");

		aManager.persist(aNewCraft);

		assertTrue(aNewCraft.prePersistCalled);
		assertTrue(aNewCraft.postPersistCalled);
		assertTrue(TestEntityListener.prePersistCalled);
		assertTrue(TestEntityListener.postPersistCalled);
	}

	@Test
	public void testFindAndContains() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  URI.create("http://nasa.dataincubator.org/spacecraft/1957-001A"));

		assertTrue(aCraft != null);

		// just a couple checks to see if this is the right space craft
		assertEquals(aCraft.getAgency(), "U.S.S.R");
		assertTrue(Lists.newArrayList(aCraft.getAlternateName()).equals(Lists.newArrayList(Collections.singletonList("00001"))));
		assertEquals(aCraft.getHomepage(), URI.create("http://nssdc.gsfc.nasa.gov/database/MasterCatalog?sc=1957-001A"));

		assertTrue(aManager.contains(aCraft));

		assertFalse(aManager.contains(new Spacecraft(URI.create("http://nasa.dataincubator.org/spacecraft/fakeSpacecraft"))));

		Spacecraft aCopy = aManager.find(Spacecraft.class, URI.create("http://nasa.dataincubator.org/spacecraft/1957-001A"));

		assertEquals(aCraft, aCopy);

		assertTrue(null == aManager.find(Spacecraft.class, URI.create("http://nasa.dataincubator.org/spacecraft/doesNotExist")));

		aCopy = aManager.getReference(Spacecraft.class, URI.create("http://nasa.dataincubator.org/spacecraft/1957-001A"));

		assertEquals(aCopy, aCraft);
	}

	@Test
	public void testQuerying() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		Query aNativeQuery = aManager.createNativeQuery(TEST_NATIVE_QUERY);
		Query aQuery = aManager.createQuery(TEST_NATIVE_FRAGMENT);

		// both of these query objects should return the same result set
		List aResults = aNativeQuery.getResultList();

		assertTrue(Lists.newArrayList(aResults).equals(Lists.newArrayList(aQuery.getResultList())));

		aNativeQuery = aManager.createNativeQuery(TEST_NATIVE_QUERY, String.class);
		aResults = aNativeQuery.getResultList();
		for (Object aResult : aResults) {
			assertTrue(aResult instanceof String);
		}

		aNativeQuery.setMaxResults(10);
		aResults = aNativeQuery.getResultList();

		assertEquals(aResults.size(), 10);

		aNativeQuery.setFirstResult(13);
		aResults = aNativeQuery.getResultList();
		List aResultsCopy = aNativeQuery.getResultList();

		assertTrue(Lists.newArrayList(aResults).equals(Lists.newArrayList(aResultsCopy)));

		assertEquals(aQuery, aQuery);
		assertFalse(aQuery.equals(aNativeQuery));

		aNativeQuery = aManager.createNativeQuery(TEST_AGENCY_QUERY);

		aQuery = aManager.createNativeQuery(TEST_AGENCY_WC_QUERY);

		// creating a value object here because our dataset doesnt type any literals, so the automatic typing of a
		// plain string, or double or whatever (string in this case) will not return results, so we have to do it the long way.
		aQuery.setParameter(1, SimpleValueFactory.getInstance().createLiteral("U.S.S.R"));

		aResults = aQuery.getResultList();

		assertTrue(Lists.newArrayList(aResults).equals(Lists.newArrayList(aNativeQuery.getResultList())));

		// get an equivalent named query and make sure that the results are equal
		assertTrue(Lists.newArrayList(aManager.createNamedQuery(TEST_QUERY_NAME).getResultList()).equals(Lists.newArrayList(aResults)));

		try {
			aQuery.getSingleResult();
			fail("NonUniqueException expected");
		}
		catch (NonUniqueResultException e) {
			// this is what we'd expect
		}

		aQuery.setParameter(1, SimpleValueFactory.getInstance().createLiteral("zjlkdiouasdfuoi"));

		try {
			aQuery.getSingleResult();
			fail("NoResultException expected");
		}
		catch (NoResultException e) {
			// this is what we'd expect
		}

		aQuery = aManager.createNativeQuery(TEST_PARAM_QUERY,
											Spacecraft.class);

		aQuery.setParameter(1, SimpleValueFactory.getInstance().createLiteral("U.S.S.R"));
		aQuery.setParameter("altName", SimpleValueFactory.getInstance().createLiteral("00001"));

		Object aObj = aQuery.getSingleResult();

		assertTrue(aObj != null);
		assertTrue(aObj instanceof Spacecraft);

		Spacecraft aCraft = (Spacecraft) aObj;

		assertEquals(aCraft.getAgency(), "U.S.S.R");
		assertEquals(aCraft.getAlternateName(), Collections.singletonList("00001"));
	}

	@Test
	public void testUpdate() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  URI.create("http://nasa.dataincubator.org/spacecraft/1957-002A"));

		String aNewAgency = "America";
		URI aNewHomepage = URI.create("http://nasa.gov");
		String aNewMass = "12345";

		aCraft.setMass(aNewMass);
		aCraft.setAgency(aNewAgency);
		aCraft.setHomepage(aNewHomepage);

		Spacecraft aUpdatedCraft = aManager.merge(aCraft);

		assertEquals(aUpdatedCraft.getAgency(), aNewAgency);
		assertEquals(aUpdatedCraft.getHomepage(), aNewHomepage);
		assertEquals(aUpdatedCraft.getMass(), aNewMass);

		assertEquals(aUpdatedCraft, aCraft);
	}

	@Test
	public void testRefresh() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  URI.create("http://nasa.dataincubator.org/spacecraft/1957-002A"));

		String aNewAgency = "United States of America";
		URI aNewHomepage = URI.create("http://null.gov");
		String aNewMass = "1234567890";

		aCraft.setMass(aNewMass);
		aCraft.setAgency(aNewAgency);
		aCraft.setHomepage(aNewHomepage);

		aManager.refresh(aCraft);

		assertFalse(aCraft.getAgency().equals(aNewAgency));
		assertFalse(aCraft.getHomepage().equals(aNewHomepage));
		assertFalse(aCraft.getMass().equals(aNewMass));
	}

	@Test(expected=EntityExistsException.class)
	public void testPersistExistingObj() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  URI.create("http://nasa.dataincubator.org/spacecraft/1957-001A"));

		assertTrue(aCraft != null);

		aManager.persist(aCraft);
	}

	@Test
	public void testPersist() {
		EntityManager aManager = createEntityManager();

		Spacecraft aNewCraft = new Spacecraft();

		aNewCraft.setAgency("USA");
		aNewCraft.setAlternateName(Collections.singletonList("12345"));
		aNewCraft.setDescription("The new rocket to return to the moon");
		aNewCraft.setName("Ares 1");
		aNewCraft.setMass("1000");

		aManager.persist(aNewCraft);

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  aNewCraft.getRdfId());

		assertEquals(aNewCraft, aCraft);
	}

	@Test
	public void testRemove() throws Exception {
		EntityManager aManager = createEntityManager();

		assumeTrue(aManager.getDelegate() instanceof MutableDataSource);

		insertData((MutableDataSource) aManager.getDelegate(), new File(DATA_FILE));

		Spacecraft aCraft = aManager.find(Spacecraft.class,
										  URI.create("http://nasa.dataincubator.org/spacecraft/1957-001B"));

		assertTrue(aCraft != null);

		aManager.remove(aCraft);

		assertFalse(aManager.contains(aCraft));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRemoveNonExistingObj() {
		Spacecraft aCraft = new Spacecraft();
		aCraft.setName("Not in the database");

		createEntityManager().remove(aCraft);
	}

	@Test @Ignore
	public void testLocking() {
		// TODO: devise a test for the locking stuff...once it's supported
	}
	
	/*
	 * These tests are still EntityManager/EntityManagerFactory specific, but are more general in that they check that Empire's
	 * implementations of these behave reasonably and are not really related to the underlying data source.  This probably 
	 * can be moved elsewhere.
	 */

	@Test
	public void testEntityManagerBasics() {
		EntityManager aManager = createEntityManager();

		assertTrue(aManager.isOpen());

		// we support this...
		aManager.setFlushMode(FlushModeType.AUTO);

		// ... but not this
		try {
			aManager.setFlushMode(FlushModeType.COMMIT);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException e) { /* expected */ }

		assertEquals(aManager.getFlushMode(), FlushModeType.AUTO);

		// should complete w/o error
		aManager.flush();
		aManager.clear();

		assertTrue(aManager.getDelegate() instanceof DataSource);
		assertTrue(aManager.getTransaction() != null);

		aManager.close();

		assertFalse(aManager.isOpen());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testGetReferenceWhenClosed() {
		createClosedEntityManager().getReference(Spacecraft.class, URI.create("urn:find:me"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetReferenceWithInvalidPk() {
		EntityManager aMgr = createEntityManager();
		try {
			aMgr.getReference(FoafPerson.class, "{invalid}");
		}
		finally {
			aMgr.close();
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetReferenceWithNullPk() {
		EntityManager aMgr = createEntityManager();
		try {
			aMgr.getReference(FoafPerson.class, null);
		}
		finally {
			aMgr.close();
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testContainsWithNullParam() {
		EntityManager aMgr = createEntityManager();
		try {
			aMgr.contains(null);
		}
		finally {
			aMgr.close();
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testContainsWithInvalidParam() {
		EntityManager aMgr = createEntityManager();
		try {
			aMgr.contains(42);
		}
		finally {
			aMgr.close();
		}
	}

	@Test(expected=IllegalStateException.class)
	public void testContainsWhenClosed() {
		createClosedEntityManager().contains(URI.create("urn:find:me"));
	}

	@Test(expected=IllegalStateException.class)
	public void testFindWhenClosed() {
		createClosedEntityManager().find(Spacecraft.class, URI.create("urn:find:me"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFindWithInvalidPk() {
		EntityManager aMgr = createEntityManager();

		try {
			aMgr.find(FoafPerson.class, new Date());
		}
		finally {
			aMgr.close();
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFindWithNullPk() {
		EntityManager aMgr = createEntityManager();

		try {
			aMgr.find(FoafPerson.class, null);
		}
		finally {
			aMgr.close();
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetInvalidNamedQuery() {
		createEntityManager().createNamedQuery("not an actual query name");
	}

	@Test
	public void testEntityManagerFactory() {
		EntityManagerFactory aFac = createEntityManagerFactory();

		assertTrue(aFac.isOpen());

		EntityManager aManager = aFac.createEntityManager();

		aFac.close();

		assertFalse(aFac.isOpen());
		assertFalse(aManager.isOpen());

		try {
			aFac.createEntityManager();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	@Test(expected=IllegalStateException.class)
	public void testCloseWhenClosed() {
		createClosedEntityManager().close();
	}

	@Test(expected=IllegalStateException.class)
	public void testSetFlushModeWhenClosed() {
		createClosedEntityManager().setFlushMode(FlushModeType.AUTO);
	}

	@Test(expected=IllegalStateException.class)
	public void testGetFlushModeWhenClosed() {
		createClosedEntityManager().getFlushMode();
	}
	@Test(expected=IllegalStateException.class)
	public void testClearWhenClosed() {
		createClosedEntityManager().clear();
	}

	@Test(expected=IllegalStateException.class)
	public void testFlushWhenClosed() {
		createClosedEntityManager().flush();
	}

	@Test(expected=IllegalStateException.class)
	public void testJoinTransactionWhenClosed() {
		createClosedEntityManager().joinTransaction();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRemoveWithInvalidObj() {
		createEntityManager().remove("not valid");
	}

	@Test(expected=IllegalStateException.class)
	public void testPersistWhenClosed() {
		createClosedEntityManager().persist(new Spacecraft());
	}

	@Test(expected=IllegalStateException.class)
	public void testRemoveWhenClosed() {
		createClosedEntityManager().persist(new Spacecraft());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRefreshWithNull() {
		createEntityManager().refresh(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRefreshWithInvalidObj() {
		createEntityManager().refresh(new Date());
	}

	@Test
	public void testQueries() {
		Query aQuery = createEntityManager().createQuery(TEST_QUERY);

		// setting a hint should work, though it does not do anything
		aQuery.setHint("test", new Object());

		aQuery.setFlushMode(FlushModeType.AUTO);

		try {
			aQuery.setFlushMode(FlushModeType.COMMIT);
			fail("Should not be able to set the flush mode to commit");
		}
		catch (Exception e) {
			// expected
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPersistWithInvalidObj() {
		createEntityManager().persist("not valid");
	}

	//////////////////////////////////////////////
	//// Test utility classes
	//////////////////////////////////////////////
	
	private void insertData(final MutableDataSource theSource, final File theFile) throws IOException, RDFParseException, DataSourceException {
		if (theSource instanceof SupportsTransactions) {
			((SupportsTransactions)theSource).begin();
		}

		theSource.add(ModelIO.read(theFile.toPath()));

		if (theSource instanceof SupportsTransactions) {
			((SupportsTransactions)theSource).commit();
		}
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

	
	@Entity
	@RdfsClass("http://empire.clarkparsia.com/OneWithList")
	public static class OneWithList extends BaseTestClass {
		@RdfProperty(value="http://empire.clarkparsia.com/list", isList=true)
		@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
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

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "Elem: " + name;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || !(o instanceof Elem)) {
				return false;
			}

			final Elem aElem = (Elem) o;

			if (name != null
				? !name.equals(aElem.name)
				: aElem.name != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return name != null
				   ? name.hashCode()
				   : 0;
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
}
