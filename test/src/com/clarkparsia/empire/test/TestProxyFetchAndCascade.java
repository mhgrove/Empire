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

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class TestProxyFetchAndCascade {

//	@BeforeClass
//	public static void beforeClass () {
//		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(), new FourStoreEmpireModule(),
//					new JenaEmpireModule(), new TestModule());
//	}

	@Test
	public void testProxying() {
		// this is not a bullet proof proxy detection, but it should prove whether or not proxying is correctly
		// inserted into the chain of events.

		String javaAssistMarker = "$$_javassist";

		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(ConfigKeys.FACTORY, MutableTestDataSourceFactory.class.getName());
		aMap.put("files", TestJPA.DATA_FILE);

		EntityManager aManager = Empire.get().persistenceProvider().createEntityManagerFactory("test", aMap).createEntityManager();

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

	@Test
	public void testTargetEntity() {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(ConfigKeys.FACTORY, MutableTestDataSourceFactory.class.getName());
		aMap.put("files", TestJPA.DATA_FILE);

		EntityManager aManager = Empire.get().persistenceProvider().createEntityManagerFactory("test", aMap).createEntityManager();

		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";

		Launch aLaunch = aManager.find(Launch.class, aLaunchURI);
		LaunchUsingProxy aProxySupportingLaunch = aManager.find(LaunchUsingProxy.class, aLaunchURI);

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

	@Test @Ignore
	public void testCascading() {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(ConfigKeys.FACTORY, MutableTestDataSourceFactory.class.getName());
		aMap.put("files", TestJPA.DATA_FILE);

		EntityManager aManager = Empire.get().persistenceProvider().createEntityManagerFactory("test", aMap).createEntityManager();

		String aLaunchURI = "http://nasa.dataincubator.org/launch/SATURNSA1";
		String aOtherLaunchURI = "http://nasa.dataincubator.org/launch/POLAIRE";

		// =============== test merge cascade
		LaunchUsingProxy aExistingLaunchWithProxy = aManager.find(LaunchUsingProxy.class, aLaunchURI);
		Launch aExistingLaunch = aManager.find(Launch.class, aOtherLaunchURI);

		Spacecraft aNewSpacecraft = new Spacecraft();
		aNewSpacecraft.setRdfId(new SupportsRdfId.URIKey(URI.create("http://empire.clarkparsia.com/test/merge/cascade/aNewOtherSpacecraft")));
		aNewSpacecraft.setAgency("agency");

		aExistingLaunch.setOtherSpacecraft(aNewSpacecraft);
		aExistingLaunchWithProxy.setOtherSpacecraft(aNewSpacecraft);

		aManager.merge(aExistingLaunch);

		assertTrue(aManager.contains(aExistingLaunch));
		// it was merged as a relation...
		assertTrue(aManager.find(Launch.class, aOtherLaunchURI).getOtherSpacecraft().equals(aNewSpacecraft));

		// but merge does not cascade
		assertFalse(aManager.contains(aNewSpacecraft));

		aManager.merge(aExistingLaunchWithProxy);

		assertTrue(aManager.contains(aExistingLaunchWithProxy));

		// this should be true now because the merge was cascaded
		assertTrue(aManager.contains(aNewSpacecraft));

		// ================= test remove cascade
		aExistingLaunchWithProxy = aManager.find(LaunchUsingProxy.class, aLaunchURI);
		aExistingLaunch = aManager.find(Launch.class, aOtherLaunchURI);

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
		assertTrue(aManager.find(Launch.class, aNewLaunch.getRdfId()).getOtherSpacecraft().equals(aNewOtherSpacecraft));

		// but persist does not cascade
		assertFalse(aManager.contains(aNewOtherSpacecraft));

		aManager.persist(aNewLaunchWithProxy);

		assertTrue(aManager.contains(aNewLaunchWithProxy));

		// it was persisted as a relation...
		assertTrue(aManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getOtherSpacecraft().equals(aNewOtherSpacecraft));

		// and this should be true now because the persist was cascaded
		assertTrue(aManager.contains(aNewOtherSpacecraft));

		// ============ test all cascade
		aNewLaunch = new Launch();
		aNewLaunchWithProxy = new LaunchUsingProxy();

		LaunchSite aNewSiteOne = new LaunchSite();
		aNewSiteOne.setLabel(Arrays.asList("new launch site one"));

		LaunchSite aNewSiteTwo = new LaunchSite();
		aNewSiteTwo.setLabel(Arrays.asList("new launch site two"));

		aNewLaunch.setLaunchSite(aNewSiteOne);
		aNewLaunchWithProxy.setLaunchSite(aNewSiteOne);

		aManager.persist(aNewLaunch);

		assertTrue(aManager.contains(aNewLaunch));
		assertTrue(aManager.find(Launch.class, aNewLaunch.getRdfId()).getLaunchSite().equals(aNewSiteOne));
		assertFalse(aManager.contains(aNewSiteOne));

		aManager.persist(aNewLaunchWithProxy);

		assertTrue(aManager.contains(aNewLaunchWithProxy));
		assertTrue(aManager.find(LaunchUsingProxy.class, aNewLaunchWithProxy.getRdfId()).getLaunchSite().equals(aNewSiteOne));
		assertTrue(aManager.contains(aNewSiteOne));

		aNewLaunch.setLaunchSite(aNewSiteTwo);
		aNewLaunchWithProxy.setLaunchSite(aNewSiteTwo);

		aManager.merge(aNewLaunch);

		assertTrue(aManager.contains(aNewLaunch));
		assertTrue(aManager.find(Launch.class, aNewLaunch.getRdfId()).getLaunchSite().equals(aNewSiteTwo));
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
}
