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

import com.clarkparsia.empire.config.ConfigKeys;
import com.clarkparsia.empire.test.api.MutableTestDataSourceFactory;
import com.clarkparsia.empire.test.api.nasa.LaunchUsingProxy;
import com.clarkparsia.empire.test.api.nasa.LaunchSite;
import com.clarkparsia.empire.test.api.nasa.Launch;
import com.clarkparsia.empire.test.api.nasa.Spacecraft;
import com.clarkparsia.empire.test.util.TestModule;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.fourstore.FourStoreEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import static com.clarkparsia.utils.collections.CollectionUtil.contentsEqual;

import javax.persistence.EntityManager;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.net.URI;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Before;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class TestProxyFetchAndCascade {

	private EntityManager mManager;

	@BeforeClass
	public static void beforeClass () {
		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(), new FourStoreEmpireModule(),
					new JenaEmpireModule(), new TestModule());
	}

	@Before
	public void before() {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(ConfigKeys.FACTORY, "test-source");
		aMap.put("files", TestJPA.DATA_FILE);
		aMap.put("use.cache", "false");

		mManager = Empire.get().persistenceProvider().createEntityManagerFactory("test", aMap).createEntityManager();
	}

	@Test
	public void testProxying() {
		// this is not a bullet proof proxy detection, but it should prove whether or not proxying is correctly
		// inserted into the chain of events.

		String javaAssistMarker = "$$_javassist";

		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";

		Launch aLaunch = mManager.find(Launch.class, aLaunchURI);

		LaunchUsingProxy aProxySupportingLaunch = mManager.find(LaunchUsingProxy.class, aLaunchURI);

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

	@Test
	public void testTargetEntity() {
		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";

		Launch aLaunch = mManager.find(Launch.class, aLaunchURI);
		LaunchUsingProxy aProxySupportingLaunch = mManager.find(LaunchUsingProxy.class, aLaunchURI);

		assertTrue(aProxySupportingLaunch.getSpacecraft().size() > 0);

		// the values for this are all space craft in the data
		// but the mapping just specifies an uptyped collection (a List).
		// we're using the targetEntity property of the OneToMany annotation to
		// specify the type, so we want to make sure that everything returned by this
		// function is a space craft.  The fact that it returns at all pretty much
		// guarantees that, but we want to make sure.
		for (Object aObj : aProxySupportingLaunch.getSpacecraft()) {
			assertTrue(aObj instanceof Spacecraft);
		}

		// "normal" mapping should be equal to the proxied, targetEntity mapped version
		assertTrue(contentsEqual(aLaunch.getSpacecraft(), aProxySupportingLaunch.getSpacecraft()));
	}

	@Test
	public void testRemoveCascading() {
		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";
		String aOtherLaunchURI = "http://nasa.dataincubator.org/launch/PION2";

		// ================= test remove cascade
		LaunchUsingProxy aExistingLaunchWithProxy = mManager.find(LaunchUsingProxy.class, aLaunchURI);
		Launch aExistingLaunch = mManager.find(Launch.class, aOtherLaunchURI);

		List<Spacecraft> aExistingSpacecraft = aExistingLaunch.getSpacecraft();

		assertTrue(aExistingSpacecraft.size() > 0);

		aExistingLaunch.getSpacecraft().clear();

		mManager.remove(aExistingLaunch);

		// delete shoudl have worked...
		assertFalse(mManager.contains(aExistingLaunch));

		// but not cascaded
		for (Spacecraft aCraft : aExistingSpacecraft) {
			assertTrue(mManager.contains(aCraft));
		}

		aExistingSpacecraft = aExistingLaunchWithProxy.getSpacecraft();
		assertTrue(aExistingSpacecraft.size() > 0);

		mManager.remove(aExistingLaunchWithProxy);

		// delete should have worked...
		assertFalse(mManager.contains(aExistingLaunchWithProxy));

		// and delete should have cascaded
		for (Spacecraft aCraft : aExistingSpacecraft) {
			assertFalse(mManager.contains(aCraft));
		}
	}

	@Test
	public void testPersistCascading() {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(ConfigKeys.FACTORY, MutableTestDataSourceFactory.class.getName());
		aMap.put("files", TestJPA.DATA_FILE);

		EntityManager aManager = Empire.get().persistenceProvider().createEntityManagerFactory("test", aMap).createEntityManager();

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

	@Test
	public void testMergeCascading() {
		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";
		String aOtherLaunchURI = "http://nasa.dataincubator.org/launch/PION2";

		// =============== test merge cascade
		LaunchUsingProxy aExistingLaunchWithProxy = mManager.find(LaunchUsingProxy.class, aLaunchURI);
		Launch aExistingLaunch = mManager.find(Launch.class, aOtherLaunchURI);

		Spacecraft aNewSpacecraft = new Spacecraft();
		aNewSpacecraft.setRdfId(new SupportsRdfId.URIKey(URI.create("http://empire.clarkparsia.com/test/merge/cascade/aNewOtherSpacecraft")));
		aNewSpacecraft.setAgency("agency");

		aExistingLaunch.setOtherSpacecraft(aNewSpacecraft);
		aExistingLaunchWithProxy.setOtherSpacecraft(aNewSpacecraft);

		mManager.merge(aExistingLaunch);

		assertTrue(mManager.contains(aExistingLaunch));
		// it was merged as a relation...
		assertTrue(mManager.find(Launch.class, aOtherLaunchURI).getOtherSpacecraft().getRdfId().equals(aNewSpacecraft.getRdfId()));

		// but merge does not cascade
		assertFalse(mManager.contains(aNewSpacecraft));

		mManager.merge(aExistingLaunchWithProxy);

		assertTrue(mManager.contains(aExistingLaunchWithProxy));

		// this should be true now because the merge was cascaded
		assertTrue(mManager.contains(aNewSpacecraft));

	}

	@Test
	public void testAllCascade() {
		// ============ test all cascade
		Launch aNewLaunch = new Launch();
		LaunchUsingProxy aNewLaunchWithProxy = new LaunchUsingProxy();

		LaunchSite aNewSiteOne = new LaunchSite();
		aNewSiteOne.setLabel(Arrays.asList("new launch site one"));

		LaunchSite aNewSiteTwo = new LaunchSite();
		aNewSiteTwo.setLabel(Arrays.asList("new launch site two"));

		aNewLaunch.setLaunchSite(aNewSiteOne);
		aNewLaunchWithProxy.setLaunchSite(aNewSiteOne);

		mManager.persist(aNewLaunch);

		assertTrue(mManager.contains(aNewLaunch));
		assertTrue(mManager.find(Launch.class, aNewLaunch.getRdfId()).getLaunchSite().getRdfId().equals(aNewSiteOne.getRdfId()));
		assertFalse(mManager.contains(aNewSiteOne));

		mManager.persist(aNewLaunchWithProxy);

		assertTrue(mManager.contains(aNewLaunchWithProxy));
		assertTrue(mManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getLaunchSite().equals(aNewSiteOne));
		assertTrue(mManager.contains(aNewSiteOne));

		aNewLaunch.setLaunchSite(aNewSiteTwo);
		aNewLaunchWithProxy.setLaunchSite(aNewSiteTwo);

		mManager.merge(aNewLaunch);

		assertTrue(mManager.contains(aNewLaunch));
		assertTrue(mManager.find(Launch.class, aNewLaunch.getRdfId()).getLaunchSite().getRdfId().equals(aNewSiteTwo.getRdfId()));
		assertFalse(mManager.contains(aNewSiteTwo));

		mManager.merge(aNewLaunchWithProxy);

		assertTrue(mManager.contains(aNewLaunchWithProxy));
		assertTrue(mManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getLaunchSite().equals(aNewSiteTwo));
		assertTrue(mManager.contains(aNewSiteTwo));

		mManager.remove(aNewLaunch);

		assertFalse(mManager.contains(aNewLaunch));
		assertTrue(mManager.contains(aNewSiteTwo));

		mManager.remove(aNewLaunchWithProxy);

		assertFalse(mManager.contains(aNewLaunchWithProxy));
		assertFalse(mManager.contains(aNewSiteTwo));
	}
}
