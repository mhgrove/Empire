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

package com.clarkparsia.empire.config.io.impl;

import com.clarkparsia.empire.config.io.ConfigReader;
import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.EmpireException;
import com.clarkparsia.empire.util.EmpireAnnotationProvider;
import com.clarkparsia.empire.util.BeanReflectUtil;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <p>ConfigReader implementation for reading configuration information from simple XML files.</p>
 *
 * @author Michael Grove
 * @since 0.6.4
 * @version 0.6.5
 */
public class XmlConfigReader implements ConfigReader {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(ConfigReader.class.getName());

	private static final String NODE_ANNOTATION_PROVIDER = "annotationProvider";
	private static final String NODE_UNIT = "unit";

	private static final String NODE_NAME = "name";
	private static final String NODE_FACTORY = "factory";

	/**
	 * @inheritDoc
	 */
	public EmpireConfiguration read(final InputStream theStream) throws IOException, EmpireException {
		return read(new InputStreamReader(theStream));
	}

	/**
	 * @inheritDoc
	 */
	public EmpireConfiguration read(final Reader theStream) throws IOException, EmpireException {
		try {
			Document aDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(theStream));

			Map<String, String> aGeneralConfig = new HashMap<String, String>();
			Map<String, Map<String, String>> aUnitConfig = new HashMap<String, Map<String, String>>();

			Class<EmpireAnnotationProvider> aProvider = null;

			NodeList aEmpireChildren = aDoc.getDocumentElement().getChildNodes();
			for (int aIndex = 0; aIndex < aEmpireChildren.getLength(); aIndex++) {
				Node aChild = aEmpireChildren.item(aIndex);

				if (aChild.getNodeName().equals(NODE_ANNOTATION_PROVIDER)) {
					String aValue = aChild.getTextContent();

					try {
						aProvider = (Class<EmpireAnnotationProvider>) BeanReflectUtil.loadClass(aValue);
					}
					catch (ClassNotFoundException e) {
						LOGGER.warn("Annotation provider implementation '" + aValue + "' cannot be found, please check your classpath.");
					}
					catch (ClassCastException e) {
						LOGGER.warn("Specified annotation provider implementation '" + aValue + "' is not a valid EmpireAnnotationProvider.");
					}
				}
				else if (aChild.getNodeName().equals(NODE_UNIT)) {
					Map<String, String> aUnit = unitFromNode(aChild);

					aUnitConfig.put(aUnit.get(NODE_NAME), aUnit);
				}
				else {
					aGeneralConfig.put(aChild.getNodeName(), aChild.getTextContent());
				}
			}

			EmpireConfiguration aConfig = new EmpireConfiguration(aGeneralConfig, aUnitConfig);
			aConfig.setAnnotationProvider(aProvider);

			return aConfig;
		}
		catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
	}

	private Map<String, String> unitFromNode(final Node theUnitNode) {
		Map<String, String> aUnit = new HashMap<String, String>();

		NodeList aChildren = theUnitNode.getChildNodes();
		for (int aIndex = 0; aIndex < aChildren.getLength(); aIndex++) {
			Node aChild = aChildren.item(aIndex);

			aUnit.put(aChild.getNodeName(), aChild.getTextContent());
		}

		return aUnit;
	}

	public static void main(String[] args) {
		String s = "<empire>\n" +
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
			       "		<factory>com.clarkparsia.empire.jena.JenaTestDataSourceFactory</factory>\n" +
			       "		<files>foo, bar, baz</files>\n" +
			       "	</unit>\n" +
			       "</empire>";
	}
}
