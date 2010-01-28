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
import com.clarkparsia.empire.sesame.SesameDataSourceFactory;

import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>Standard Guice module for Empire</p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class DefaultEmpireModule extends AbstractModule {

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

		if (System.getProperty("empire.configuration.file") != null) {
			try {
				aProps.load(new FileInputStream(System.getProperty("empire.configuration.file")));
			}
			catch (IOException e) {
				LOGGER.error("Error while reading default Empire configuration file from the path", e);
			}
		}
		else if (new File("empire.properties").exists()) {
			try {
				aProps.load(new FileInputStream("empire.properties"));
			}
			catch (IOException e) {
				LOGGER.error("Error while reading default Empire configuration file from the path", e);
			}
		}

		for (String aKey : aProps.stringPropertyNames()) {
			mConfig.put(aKey, aProps.getProperty(aKey));
		}
	}

	public DefaultEmpireModule(final Map<String, String> theConfig) {
		mConfig = theConfig;
	}

	 /**
	  * @inheritDoc
	  */
	 @Override
	 protected void configure() {
//            bind(Map.class).annotatedWith(Names.named("ec")).toInstance(mConfig);
		 bind(new TypeLiteral<Map<String, String>>(){}).annotatedWith(Names.named("ec")).toInstance(mConfig);


		 bind(EmpireAnnotationProvider.class).to(PropertiesAnnotationProvider.class);
//            bind(EntityManagerFactory.class).to(EntityManagerFactoryImpl.class);

		 // bind default data source
		// each plugin's module would do this, but for now, lets do it all here.
		Multibinder<DataSourceFactory> aBinder = Multibinder.newSetBinder(binder(), DataSourceFactory.class);

		aBinder.addBinding().to(SesameDataSourceFactory.class);
//            aBinder.addBinding().to(JenaInMemoryDataSourceFactory.class);
//            aBinder.addBinding().to(FourStoreDataSourceFactory.class);

//			 requestStaticInjection(EmpireOptions.class);
	 }
 }
