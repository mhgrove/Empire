/*
 * Copyright (c) 2009-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

import com.google.inject.name.Names;
import com.google.common.io.Closeables;

import com.clarkparsia.empire.EmpireException;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.ds.DataSourceFactory;

import com.clarkparsia.empire.ds.impl.SparqlEndpointSourceFactory;

import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.config.ConfigKeys;

import com.clarkparsia.empire.config.io.ConfigReader;

import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;
import com.clarkparsia.empire.config.io.impl.XmlConfigReader;

import com.clarkparsia.empire.spi.guice.PersistenceInjectionModule;
import com.clarkparsia.empire.spi.Instrumentor;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Standard Guice module for Empire</p>
 *
 * @author 	Michael Grove
 * @since 	0.6
 * @version 0.7
 */
public final class DefaultEmpireModule extends AbstractModule implements EmpireModule {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEmpireModule.class);

	/**
	 * Application configuration properties
	 */
	private EmpireConfiguration mConfig;

	/**
	 * Create a new DefaultEmpireModule
	 */
	public DefaultEmpireModule() {
		mConfig = readConfiguration();
		if (mConfig == null) {
			mConfig = new EmpireConfiguration();
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

		 if (Instrumentor.isInitialized()) {
			 bind(EmpireAnnotationProvider.class).to(InstrumentorAnnotationProvider.class);
		 }
		 else {
			 if (mConfig.getAnnotationProvider() != null && mConfig.getAnnotationProvider().equals(PropertiesAnnotationProvider.class)) {
				 bind(File.class)
				     .annotatedWith(Names.named("annotation.index"))
				     .toProvider(new Provider<File>() {
					     public File get() {
						     if (mConfig.getGlobalConfig().containsKey(ConfigKeys.ANNOTATION_INDEX)) {
							     File aFile = new File(mConfig.get(ConfigKeys.ANNOTATION_INDEX));
                                 if (aFile.isAbsolute()) {
                                     return aFile;
                                 }
                                 else if (System.getProperty("empire.configuration.file") != null && new File(System.getProperty("empire.configuration.file")).exists()) {
                                     return new File(new File(System.getProperty("empire.configuration.file")).getParentFile(), mConfig.get(ConfigKeys.ANNOTATION_INDEX));
                                 }
                                 else {
                                     return aFile;
                                 }
						     }
						     else {
							     return new File("empire.annotation.index");
						     }
					     }
				 });
			 }

			 bind(EmpireAnnotationProvider.class).to(mConfig.getAnnotationProvider());
		 }

		 Multibinder.newSetBinder(binder(), DataSourceFactory.class).addBinding().to(SparqlEndpointSourceFactory.class);
	 }

	public static EmpireConfiguration readConfiguration() {
		InputStream aConfigStream = null;

		// the default configuration reader
		ConfigReader aReader = new PropertiesConfigReader();

		try {
			// not ideal, really we want just a single standard config file name with the system property which can override
			// that.  but since we don't have a standard yet, we'll check a bunch of them.
			if (System.getProperty("empire.configuration.file") != null && new File(System.getProperty("empire.configuration.file")).exists()) {
				aConfigStream = new FileInputStream(System.getProperty("empire.configuration.file"));
			}
			// check inside the jar to see if the config file is there
			else if (DefaultEmpireModule.class.getResourceAsStream("/empire.configuration") != null) {
				aConfigStream = DefaultEmpireModule.class.getResourceAsStream("/empire.configuration");
			}
			// this is the default non-jar location
			else if (new File("empire.configuration").exists()) {
				aConfigStream = new FileInputStream("empire.configuration");
				aReader = new PropertiesConfigReader();
			}

			// these locations are @deprecated in 0.7, to be removed in 0.9
			else if (new File("empire.config").exists()) {
				aConfigStream = new FileInputStream("empire.config");
				aReader = new PropertiesConfigReader();
			}
			else if (new File("empire.properties").exists()) {
				aConfigStream = new FileInputStream("empire.properties");
				aReader = new PropertiesConfigReader();
			}
			else if (new File("empire.config.properties").exists()) {
				aConfigStream = new FileInputStream("empire.config.properties");
				aReader = new PropertiesConfigReader();
			}
			else if (new File("empire.xml").exists()) {
				aConfigStream = new FileInputStream("empire.xml");
				aReader = new XmlConfigReader();
			}
			else if (new File("empire.config.xml").exists()) {
				aConfigStream = new FileInputStream("empire.config.xml");
				aReader = new XmlConfigReader();
			}
		}
		catch (FileNotFoundException e) {
			LOGGER.error("Count not find config file: " + e.getMessage());
		}

		EmpireConfiguration aConfig = null;

		if (aConfigStream == null) {
			LOGGER.warn("No configuration found or specified, Empire may not start or function correctly.");
			return null;
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
		}

		if (aConfigStream != null && aReader != null) {
			try {
				aConfig = aReader.read(aConfigStream);
			}
			catch (IOException e) {
				LOGGER.error("Error while reading default Empire configuration file from the path", e);
			}
			catch (EmpireException e) {
				LOGGER.error("There was an error while reading the Empire configuration file, file appears to be invalid: " + e.getMessage());
			}
			finally {
				Closeables.closeQuietly(aConfigStream);
			}
		}
		else {
			LOGGER.warn("No appropriate reader found, unable to read Empire configuration.");
		}

		return aConfig;
	}
}
