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
import com.google.inject.multibindings.Multibinder;

import com.google.inject.name.Names;

import com.clarkparsia.empire.EmpireException;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.ds.DataSourceFactory;

import com.clarkparsia.empire.ds.impl.SparqlEndpointSourceFactory;

import com.clarkparsia.empire.config.EmpireConfiguration;

import com.clarkparsia.empire.config.io.ConfigReader;

import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;
import com.clarkparsia.empire.config.io.impl.XmlConfigReader;

import com.clarkparsia.empire.spi.guice.PersistenceInjectionModule;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>Standard Guice module for Empire</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.6.6
 */
public class DefaultEmpireModule extends AbstractModule implements EmpireModule {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(DefaultEmpireModule.class);

	/**
	 * Application configuration properties
	 */
	private EmpireConfiguration mConfig;

	/**
	 * Create a new DefaultEmpireModule
	 */
	public DefaultEmpireModule() {
		File aConfigFile = null;

		// not ideal, really we want just a single standard config file name with the system property which can override
		// that.  but since we don't have a standard yet, we'll check a bunch of them.
		if (System.getProperty("empire.configuration.file") != null && new File(System.getProperty("empire.configuration.file")).exists()) {
			aConfigFile = new File(System.getProperty("empire.configuration.file"));
		}
		else if (new File("empire.config").exists()) {
			aConfigFile = new File("empire.config");
		}
		else if (new File("empire.properties").exists()) {
			aConfigFile = new File("empire.properties");
		}
		else if (new File("empire.config.properties").exists()) {
			aConfigFile = new File("empire.config.properties");
		}
		else if (new File("empire.xml").exists()) {
			aConfigFile = new File("empire.xml");
		}
		else if (new File("empire.config.xml").exists()) {
			aConfigFile = new File("empire.config.xml");
		}

		ConfigReader aReader = null;

		if (aConfigFile == null) {
			// TODO: should this just be an Error -- throw a RTE?
			mConfig = new EmpireConfiguration();

			LOGGER.warn("No configuration found or specified, Empire may not start or function correctly.");
		}
		else {
			// TODO: need a more sophisticated method of selection which reader to use =)
			if (System.getProperty("empire.config.reader") != null) {
				try {
					@SuppressWarnings("unchecked") // it's ok if this throws a cast exception, we handle that
					Class<ConfigReader> aClass = (Class<ConfigReader>) BeanReflectUtil.loadClass(System.getProperty("empire.config.reader"));

					aReader =  Empire.get().instance(aClass);
				}
				catch (Exception e) {
					LOGGER.error("Unable to find or create specified configuration reader class: " + System.getProperty("empire.config.reader"), e);
				}
			}
			else if (aConfigFile.getName().endsWith(".xml")) {
				aReader = new XmlConfigReader();

			}
			else {
				aReader = new PropertiesConfigReader();
			}
		}

		InputStream aStream = null;
		if (aReader != null) {
			try {
				aStream = new FileInputStream(aConfigFile);
				mConfig = aReader.read(aStream);
			}
			catch (IOException e) {
				LOGGER.error("Error while reading default Empire configuration file from the path", e);
			}
			catch (EmpireException e) {
				LOGGER.error("There was an error while reading the Empire configuration file, file appears to be invalid: " + e.getMessage());
			}
			finally {
				try {
					if (aStream != null) {
						aStream.close();
					}
				}
				catch (IOException e) {
					LOGGER.info("Failed to close configuration input stream", e);
				}
			}
		}
		else {
			if (mConfig == null) {
				mConfig = new EmpireConfiguration();

				LOGGER.warn("No appropriate reader found, unable to read Empire configuration.");
			}
		}
	}

	/**
	 * Create a new DefaultEmpireModule
	 * @param theConfig the container config
	 */
	public DefaultEmpireModule(final EmpireConfiguration theConfig) {
		mConfig = theConfig;
	}

	 /**
	  * @inheritDoc
	  */
	 @Override
	 protected void configure() {
		 install(new PersistenceInjectionModule());

		 bind(EmpireConfiguration.class).annotatedWith(Names.named("ec")).toInstance(mConfig);

		 mConfig.installBindings(this.binder());

		 bind(EmpireAnnotationProvider.class).to(mConfig.getAnnotationProvider());

		 Multibinder.newSetBinder(binder(), DataSourceFactory.class).addBinding().to(SparqlEndpointSourceFactory.class);
	 }
 }
