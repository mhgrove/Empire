/*
 * Copyright (c) 2009-2013 Complexible. <http://www.complexible.com>
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

import com.clarkparsia.empire.codegen.CodegenTests;

import com.clarkparsia.empire.lazyload.TestLazyCollectionLoad;

import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.util.TestModule;
import com.clarkparsia.empire.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   1.0
 * @version 1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestLazyCollectionLoad.class, TestRdfConvert.class, TestMisc.class,
                     TestConfig.class, TestDS.class, CodegenTests.class })
public class TestEmpireCore {

    @BeforeClass
    public static void beforeClass () {
        TestUtil.setConfigSystemProperty( "test.empire.config.properties" );
        Empire.init(new DefaultEmpireModule(), new TestModule());

        // TODO: tests for TripleSource stuff
        // TODO: tests for persistence injectors
        // TODO: tests for transactions
        // TODO: more failure tests -- badly annotated beans, misconfigured datasources, etc.
        // TODO: delegating data source tests
        // TODO: named query tests
    }
}
