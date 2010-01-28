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

import static org.junit.Assert.assertTrue;

import com.clarkparsia.empire.Empire;

import com.clarkparsia.empire.test.api.TestDataSourceFactory;

import java.util.Collections;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class TestSPI {
	@Test
	public void testInvalidFactory() {
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("no a factory", Collections.singletonMap("factory", "not a factory class name")));
//		mFactory.createEntityManager(Collections.singletonMap(EntityManagerFactoryImpl.FACTORY, "Not.a.class.name"));
	}

	@Test
	public void testNoFactory() {
//		mFactory.createEntityManager();
		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("no a factory", Collections.EMPTY_MAP));
	}

	@Test
	public void testNotMutableDataSource() {
//		mFactory.createEntityManager(Collections.singletonMap(EntityManagerFactoryImpl.FACTORY,
//															  TestDataSourceFactory.class.getName()));

		assertTrue(null == Empire.get().persistenceProvider().createEntityManagerFactory("not mutable", Collections.singletonMap("factory", TestDataSourceFactory.class.getName())));
	}
}
