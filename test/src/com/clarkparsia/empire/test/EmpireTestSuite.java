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

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.fourstore.FourStoreEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.test.util.TestModule;

import org.junit.runners.Suite;

import org.junit.runner.RunWith;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import org.junit.BeforeClass;
import org.junit.AfterClass;

/**
 * <p>Empire test suite.</p>
 *
 * @author Michael Grove
 * @since 0.6.4
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestRdfConvert.class, TestJPA.class, TestSPI.class, TestMisc.class,
					 TestConfig.class, TestProxyFetchAndCascade.class, TestDS.class})
public class EmpireTestSuite {

	@BeforeClass
	public static void beforeClass () {
		System.setProperty("empire.configuration.file", "test.empire.config.properties");
		
		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(), new FourStoreEmpireModule(),
					new JenaEmpireModule(), new TestModule());

		// TODO: tests for TripleSource stuff
		// TODO: tests for persistence injectors
		// TODO: tests for transactions
		// TODO: more failure tests -- badly annotated beans, misconfigured datasources, etc.
		// TODO: 4store & sparql endpoint test configurations
		// TODO: delegating data source tests
		// TODO: named query tests
	}
}
