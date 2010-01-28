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
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.test.util.TestModule;

import org.junit.runners.Suite;

import org.junit.runner.RunWith;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import org.junit.BeforeClass;
import org.junit.AfterClass;

/**
 * <p>JUnit 3.8 style suite runner.  Also includes a main that prints the bare minimum of statistics.</p>
 *
 * @author Michael Grove
 */
//@Deprecated
@RunWith(Suite.class)
@Suite.SuiteClasses({TestRdfConvert.class, TestJPA.class, TestSPI.class})
public class EmpireTestSuite {
	@BeforeClass
	public static void beforeClass () {
		Empire.init(new DefaultEmpireModule(), new TestModule());
	}

	@AfterClass
	public static void afterClass() {
		Empire.close();
	}

//	public static void main(String[] args) {
//		Empire.init(new DefaultEmpireModule(), new TestModule());
//
//		System.err.println(formatResult(JUnitCore.runClasses(TestRdfConvert.class, TestSPI.class)));
//	}
//
//	private static String formatResult(final Result theResult) {
//		StringBuffer aBuffer = new StringBuffer();
//		aBuffer.append("Success: " ).append(theResult.getRunCount()).append("\n");
//		aBuffer.append("Failures: " ).append(theResult.getFailureCount()).append("\n");
//		aBuffer.append("Ignore: " ).append(theResult.getIgnoreCount()).append("\n");
//		return aBuffer.toString();
//	}
}
