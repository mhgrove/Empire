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

package com.clarkparsia.empire.util;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.spi.guice.PersistenceInjectionModule;
import com.clarkparsia.utils.io.IOUtil;

import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>Standard Guice module for Empire</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.6.1
 */
public class DefaultEmpireModule extends AbstractModule implements EmpireModule {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(DefaultEmpireModule.class);

	/**
	 * Application configuration properties
	 */
	private Map<String, String> mConfig;

	/**
	 * Create a new DefaultEmpireModule
	 */
	public DefaultEmpireModule() {
		this(new HashMap<String, String>());

		Properties aProps = new Properties();
		InputStream aStream = null;

		if (System.getProperty("empire.configuration.file") != null) {
			try {
				aStream = new FileInputStream(System.getProperty("empire.configuration.file"));
			}
			catch (FileNotFoundException e) {
				LOGGER.error("Could not find specified empire configuration file", e);
			}
		}
		else if (new File("empire.properties").exists()) {
			try {
				aStream = new FileInputStream("empire.properties");
			}
			catch (IOException e) {
				LOGGER.error("Could not find specified empire configuration file", e);
			}
		}

		if (aStream != null) {
			try {
				aProps.load(aStream);
			}
			catch (IOException e) {
				LOGGER.error("Error while reading default Empire configuration file from the path", e);
			}
			finally {
				try {
					aStream.close();
				}
				catch (IOException e) {
					LOGGER.error("Failed to close properties input stream", e);
				}
			}
		}

		for (Object aKey : aProps.keySet()) {
			mConfig.put(aKey.toString(), aProps.getProperty(aKey.toString()));
		}
	}

	/**
	 * Create a new DefaultEmpireModule
	 * @param theConfig the container config
	 */
	public DefaultEmpireModule(final Map<String, String> theConfig) {
		mConfig = theConfig;
	}

	 /**
	  * @inheritDoc
	  */
	 @Override
	 protected void configure() {
		 install(new PersistenceInjectionModule());

		 bind(new TypeLiteral<Map<String, String>>(){}).annotatedWith(Names.named("ec")).toInstance(mConfig);

		 // todo: should this be based on what's in the config file?
		 bind(EmpireAnnotationProvider.class).to(PropertiesAnnotationProvider.class);
	 }
 }
