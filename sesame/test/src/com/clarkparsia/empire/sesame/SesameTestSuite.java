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

package com.clarkparsia.empire.sesame;

import java.io.File;
import java.net.URL;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.util.DefaultEmpireModule;

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
@Suite.SuiteClasses({SesameEntityManagerTestSuite.class, TestSesameDS.class })
public class SesameTestSuite {

    @BeforeClass
    public static void beforeClass () {
        TestUtil.setConfigSystemProperty( "test.empire.config.properties" );
        Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule());
    }
}
