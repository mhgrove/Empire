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

import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.StringReader;
import java.io.IOException;
import java.io.FileInputStream;

import com.clarkparsia.empire.config.io.ConfigReader;
import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;
import com.clarkparsia.empire.config.io.impl.XmlConfigReader;
import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.EmpireException;

import com.clarkparsia.empire.sesametwo.RepositoryDataSourceFactory;
import com.clarkparsia.empire.util.EmpireAnnotationProvider;
import com.clarkparsia.empire.util.PropertiesAnnotationProvider;

/**
 * <p>Test class for configuration related items</p>
 *
 * @author Michael Grove
 * @since 0.6.4
 * @version 0.6.4
 */
@RunWith(Parameterized.class)
public class TestConfig {

	private ConfigReader mReader;
	private String mConfigFile;

	public TestConfig(ConfigReader theReader, String theConfig) {
		mReader = theReader;
		mConfigFile = theConfig;
	}

	@Parameterized.Parameters
	public static Collection configurations() {
		return Arrays.asList(new Object[][] {
				{ new PropertiesConfigReader(), getProperiesFormatConfig() },
				{ new XmlConfigReader(), getXmlFormatConfig() }
		});
	}

	private static String getXmlFormatConfig() {
		return "<empire>\n" +
				   "	<annotationProvider>com.clarkparsia.empire.util.PropertiesAnnotationProvider</annotationProvider>\n" +
			   	   "	<foo>bar</foo>\n" +
				   "	<baz>biz</baz>\n" +
				   "\n" +
				   "	<unit>\n" +
			   	   "		<name>context1</name>\n" +
			       "		<factory>com.clarkparsia.empire.sesametwo.RepositoryDataSourceFactory</factory>\n" +
			       "		<url>http://localhost:8080/openrdf-sesame/</url>\n" +
			       "		<repo>mem-rdf-db</repo>\n" +
				   "	</unit>\n" +
			       "\n" +
				   "	<unit>\n" +
			       "		<name>context2</name>\n" +
			       "		<factory>jena-test</factory>\n" +
			       "		<files>foo, bar, baz</files>\n" +
			       "	</unit>\n" +
			       "</empire>";
	}

	private static String getProperiesFormatConfig() {
		return "annotation.provider = com.clarkparsia.empire.util.PropertiesAnnotationProvider\n" +
			   "foo=bar\n" +
			   "baz=biz\n" +
			   "\n" +
			   "0.name = context1\n" +
			   "0.factory = com.clarkparsia.empire.sesametwo.RepositoryDataSourceFactory\n" +
			   "0.url = http://localhost:8080/openrdf-sesame/\n" +
			   "0.repo = mem-rdf-db\n" +
			   "\n" +
			   "1.name = context2\n" +
			   "1.factory = jena-test\n" +
			   "1.files = foo, bar, baz\n";
	}

	@Test
	public void testReader() throws IOException, EmpireException {
		EmpireConfiguration aConfig = mReader.read(new StringReader(getConfigFile()));

		assertTrue(aConfig != null);

		assertTrue(aConfig.getAnnotationProvider() != null);
		assertEquals(aConfig.getAnnotationProvider(), PropertiesAnnotationProvider.class);

		assertTrue(aConfig.hasUnit("context1"));
		assertTrue(aConfig.hasUnit("context2"));
		assertFalse(aConfig.hasUnit("context3"));

		assertEquals(aConfig.get("foo"), "bar");
		assertEquals(aConfig.get("baz"), "biz");
		assertTrue(aConfig.get("cheese") == null);

		assertEquals(aConfig.getUnitConfig("context1").get("factory"), RepositoryDataSourceFactory.class.getName());
		assertEquals(aConfig.getUnitConfig("context1").get("url"), "http://localhost:8080/openrdf-sesame/");
		assertEquals(aConfig.getUnitConfig("context1").get("repo"), "mem-rdf-db");

		assertEquals(aConfig.getUnitConfig("context2").get("factory"), "jena-test");
		assertEquals(aConfig.getUnitConfig("context2").get("files"), "foo, bar, baz");
	}

	@Test(expected=IOException.class)
	public void testInvalidFileFormat() throws IOException, EmpireException {
		mReader.read(new StringReader(getInvalidConfigFile()));
	}

	@Test(expected=IOException.class)
	public void testInvalidIOReader() throws IOException, EmpireException {
		mReader.read(new FileInputStream("not a valid file"));
	}

	@Test
	public void testEmpireConfigFile() {
		Map<String, String> aGeneralConfig = new HashMap<String, String>();

		Map<String, Map<String, String>> aUnitsConfig = new HashMap<String, Map<String, String>>();

		Map<String, String> aUnit = new HashMap<String, String>();
		aUnit.put("property", "value");
		aUnit.put("property2", "value2");

		aGeneralConfig.put("a", "foo");
		aGeneralConfig.put("b", "bar");
		aGeneralConfig.put("c", "baz");

		aUnitsConfig.put("unit1", aUnit);

		EmpireConfiguration aConfig = new EmpireConfiguration(aGeneralConfig, aUnitsConfig);

		aConfig.setAnnotationProvider(EmpireAnnotationProvider.class);

		assertEquals(aConfig.getAnnotationProvider(), EmpireAnnotationProvider.class);

		assertEquals(aConfig.get("a"), aGeneralConfig.get("a"));
		assertEquals(aConfig.get("b"), aGeneralConfig.get("b"));
		assertTrue(aConfig.get("d") == null);

		assertTrue(aConfig.hasUnit("unit1"));
		assertFalse(aConfig.hasUnit("not a unit"));

		assertEquals(aConfig.getUnitConfig("unit1"), aUnit);
		assertTrue(aConfig.getUnitConfig("asdf") == null);
		
		// TODO: test binding installation
	}

	private String getInvalidConfigFile() {
		return new StringBuffer().append('\\').append('u').append("u3456").toString();
	}

	private String getConfigFile() {
		return mConfigFile;
	}
}
