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

package com.clarkparsia.empire.jena;

import java.util.Map;

import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;


import com.google.common.collect.Maps;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Abstract base class for creating Jena-backed data sources.  Provides a single method for creating an appropriate
 * type of Jena model based on some configuration parameters.</p>
 *
 * @author 	Michael Grove
 * @author 	uoccou
 * @since 	0.6.3
 * @version 1.0
 */
abstract class BaseJenaDataSourceFactory extends AbstractJenaDataSourceFactory {

	/**
	 * The application logger
	 */
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private Map<String, Model> tdbModelCache = Maps.newHashMap();

	/**
	 * Create a Jena model from the values specified in the configuration.
	 *
	 * @param theConfig the configuration parameters
	 * @return a new Jena model of the appropriate type
	 * @see JenaConfig
	 */
	protected Model createModel(Map<String, Object> theConfig) {
		Model aModel = null;

		long start = System.currentTimeMillis();
		if (theConfig.containsKey(TYPE)) {
			String aType = theConfig.get(TYPE).toString();

			if (aType.equals(MODEL)) {
				aModel = (Model) theConfig.get(MODEL);
			}
			else if (isTdb(aType)) {
				aModel = getTDBModel(theConfig);
			}
		}
		else {
			aModel = ModelFactory.createDefaultModel();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created Jena model in {} ms ", (System.currentTimeMillis() - start));
		}

		return aModel;
	}

	/**
	 * Create and cache a TDB model for a particular configUnit. If the unit has ontology, then create an ontModel and add the
	 * created TDB model. Subsequent calls will return the cached model. Persisted changes are assumed to be committed and synced.
	 * It is possible to get a Null return from this method, if factory fails, or if the TDB model cannot be added to the ontModel.
	 *
	 * @param theConfig the configuration parameters passed in from the user
	 * @return a TDB model based on the configuration parameters
	 */
	private Model getTDBModel(Map<String, Object> theConfig) {

		String configName = theConfig.get(PropertiesConfigReader.KEY_NAME).toString();

		Model m = getCachedTdbModel(configName);
		if (null == m) {
			//may have concurrency issues with this...but means getModel is 10x faster
			Model ontModel = TDBFactory.createModel(theConfig.get(LOCATION).toString());

			//if we have ontModel config settings
			m = getOntModel(ontModel, configName, (String) theConfig.get(ONTOLOGY), JenaModelType.TDB);

			//cache what we've created
			if (null != m) {
				cacheTdbModel(configName, m);
			}
		}
		return m;
	}

	/**
	 * Get a Model that is aware of a specified ontology, if present
	 *
	 * @param theModel			the original model
	 * @param theUnitName		 the unit name for the model
	 * @param theOntologyLocation the location of the ontology, or null if there is not one
	 * @param theType			 the type of jena model being created
	 * @return the model
	 */
	private Model getOntModel(Model theModel, String theUnitName, final String theOntologyLocation, JenaModelType theType) {
		//if we have ontModel config settings
		Model m = theModel;

		if (null != theModel && theOntologyLocation != null) {

			//make sure the ontModel is initialised, only need to do once
			initOntologyModel(theUnitName, theOntologyLocation);

			//add the model to the ontmodel 
			m = getCompleteModel(theUnitName, theModel, theType);
		}

		return m;
	}

	/**
	 * Get and/or cache data model with already cached ontmodel,  for use with this datasource
	 *
	 * @param theName
	 * @param theModel
	 * @param theType
	 * @return
	 */
	protected Model getCompleteModel(String theName, Model theModel, JenaModelType theType) {
		Model rcModel = null;
		if (theType == JenaModelType.TDB) {
			rcModel = getCachedOntModel(theName).add(theModel);
		}
		else {
			rcModel = theModel;
		}

		return rcModel;
	}

	protected void cacheTdbModel(String name, Model m) {
		tdbModelCache.put(name, m);
	}

	/**
	 * Return cached copy of a TDB model
	 *
	 * @param theUnitName the model unit name
	 * @return the cached model, or null if there is not a cached copy
	 */
	protected Model getCachedTdbModel(String theUnitName) {
		return tdbModelCache.get(theUnitName);
	}

	/**
	 * Return whether or not the value specifies TDB
	 *
	 * @param theType the value
	 * @return true if its for TDB, false otherwise
	 */
	protected boolean isTdb(final String theType) {
		return JenaModelType.TDB.toString().equals(theType);
	}

	/**
	 * Return whether the configuration is requesting a TDB backed data source
	 *
	 * @param theConfig the configuration map
	 * @return true if the configuration is requesting a TDB backed data source, false otherwise
	 */
	protected boolean isTdb(Map<String, Object> theConfig) {
		boolean aIsTDB = false;

		if (theConfig.containsKey(TYPE)) {
			String aType = theConfig.get(TYPE).toString();

			if (isTdb(aType)) {
				aIsTDB = true;
			}
		}

		return aIsTDB;
	}
}
