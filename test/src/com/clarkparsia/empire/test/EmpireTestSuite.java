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

package com.clarkparsia.empire.test;

import java.io.File;

import com.clarkparsia.empire.Empire;

import com.clarkparsia.empire.jena.JenaEntityManagerTestSuite;
import com.clarkparsia.empire.sesame.OpenRdfEmpireModule;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.sesame.SesameEntityManagerTestSuite;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.util.TestModule;

import com.clarkparsia.empire.util.TestUtil;
import org.junit.runners.Suite;

import org.junit.runner.RunWith;

import org.junit.BeforeClass;

/**
 * <p>Empire test suite.</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7.1
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SesameEntityManagerTestSuite.class, JenaEntityManagerTestSuite.class})
public class EmpireTestSuite {

	@BeforeClass
	public static void beforeClass () {
        System.setProperty("empire.configuration.file", new File(TestUtil.getProjectHome(), "test/test.empire.config.properties").getAbsolutePath());
		
		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(),
					new JenaEmpireModule(), new TestModule());
	}
}
