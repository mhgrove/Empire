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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.fourstore.FourStoreEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.impl.EntityManagerFactoryImpl;

import com.clarkparsia.empire.test.api.TestDataSourceFactory;
import com.clarkparsia.empire.test.util.TestModule;

import javax.persistence.Persistence;

import java.util.Collections;
import java.util.HashMap;

/**
 * <p>Test the Empire SPI code.</p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class TestSPI {
	// TODO: we could use some more tests here...

//	@BeforeClass
//	public static void beforeClass () {
//		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(), new FourStoreEmpireModule(),
//					new JenaEmpireModule(), new TestModule());
//
//	}

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
		assertEquals(Persistence.createEntityManagerFactory("test-data-source").getClass(), EntityManagerFactoryImpl.class);
		assertEquals(Persistence.createEntityManagerFactory("test-data-source", new HashMap()).getClass(), EntityManagerFactoryImpl.class);
	}
}
