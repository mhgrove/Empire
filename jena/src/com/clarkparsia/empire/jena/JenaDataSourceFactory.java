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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.clarkparsia.empire.ds.DataSourceFactory;

import com.clarkparsia.empire.sql.DSSettings;

import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;

import com.clarkparsia.empire.config.EmpireConfiguration;

import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.tdb.TDBFactory;

import com.google.inject.name.Named;
import com.google.inject.Inject;


/**
 * <p>Abstract base class for creating Jena-backed data sources.  Provides a single method for creating an appropriate
 * type of Jena model based on some configuration parameters.</p>
 *
 * @author Michael Grove
 * @author uoccou
 * @since 0.6.3
 * @version 0.7
 */
abstract class JenaDataSourceFactory implements DataSourceFactory, JenaConfig {

	/**
	 * The application logger
	 */
	private final Logger LOGGER = LogManager.getLogger(this.getClass());

	// @uoccou : simple caches to avoid having to recreate models on each call
	private Cache<String, Model> tdbModelCache = new DefaultCache<String, Model>();
	private Cache<String, Model> ontModelCache = new DefaultCache<String, Model>();

	private static Cache<String, DataSource> nameSdbDsCache = new DefaultCache<String, DataSource>();//ds name to DataSource
	private static Cache<String, DataSource> unitSdbDsCache = new DefaultCache<String, DataSource>();//configName to DataSource

	private EmpireConfiguration mConfig;

	@Inject
	public JenaDataSourceFactory(@Named("ec") EmpireConfiguration theContainerConfig) {
		mConfig = theContainerConfig;
	}

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
			else if (isSdb(aType)) {
				aModel = getSDBModel(theConfig);
				initSDBIfRequired(theConfig);
			}
		}
		else {
			aModel = ModelFactory.createDefaultModel();
		}

		LOGGER.debug("Created Jena model : " + (System.currentTimeMillis() - start));

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
	 * Check for a locally defined non-container Datasource ref and create a DBCP based DataSource.
	 * if not available check for a JNDI data source ref and try and get a DataSource from JNDI.
	 *
	 * Override to provide direct context lookup if and when the datasource name gets passed from the persistenceUnitInfo
	 *
	 * @param theConfign the app configuration
	 * @return the DataSource for theConfig, or null
	 */
	protected DataSource getDataSource(Map<String, Object> theConfig) {
		String unitName = theConfig.get(PropertiesConfigReader.KEY_NAME).toString();
		DataSource ds = unitSdbDsCache.get(unitName);
		Map<String, Object> unitConfig = createUnitConfig(unitName);

		if (ds == null) {
			if (unitConfig.containsKey(LOCAL_DS)) {

				String localDsName = unitConfig.get(LOCAL_DS).toString();
				if (null != localDsName) {
					ds = createLocalDS(unitName, localDsName, unitConfig);
				}
			}
			else if (unitConfig.containsKey(JNDI_DS)) {
				String jndiDsName = unitConfig.get(JNDI_DS).toString();
				if (null != jndiDsName) {
					ds = createJndiDS(unitName, jndiDsName);
				}
			}
		}
		return ds;
	}


	/**
	 * Create a DataSource using a JNDI context lookup
	 *
	 * @param unitName the unit name
	 * @param jndiDsName the ds name
	 * @return the DataSource via JNDI
	 */
	private DataSource createJndiDS(String unitName, String jndiDsName) {
		DataSource ds = nameSdbDsCache.get(jndiDsName);

		if (ds == null) {
			try {
				ds = DSSettings.jndi(jndiDsName).build();
			}
			catch (NamingException ne) {
				LOGGER.error("Cant get JNDI name '" + jndiDsName + "' for config unit '" + unitName);
				LOGGER.error("There will be connection and SQL exceptions !");
			}
		}

		if (ds != null) {
			cacheDataSource(jndiDsName, unitName, ds);
		}

		return ds;
	}

	/**
	 * create a Local (non-container) datasource, cache it by name, and by unitName
	 * Uses empire.properties to define DataSource
	 *
	 * @param unitName
	 * @param dsName
	 * @param theConfig
	 * @return
	 */
	private DataSource createLocalDS(String unitName, String dsName, Map<String, Object> theConfig) {
		DataSource ds = nameSdbDsCache.get(dsName);

		if (ds == null) {
			DSSettings config = new DSSettings(dsName);

			if (theConfig.containsKey(dsName + ".url")) {
				config.setUrl(theConfig.get(dsName + ".url").toString());
			}
			if (theConfig.containsKey(dsName + ".db")) {
				config.setDb(theConfig.get(dsName + ".db").toString());
			}
			if (theConfig.containsKey(dsName + ".driver")) {
				config.setDriver(theConfig.get(dsName + ".driver").toString());
			}
			if (theConfig.containsKey(dsName + ".user")) {
				config.setUser(theConfig.get(dsName + ".user").toString());
			}
			if (theConfig.containsKey(dsName + ".password")) {
				config.setPassword(theConfig.get(dsName + ".password").toString());
			}
			if (theConfig.containsKey(dsName + ".autocommit")) {
				config.setAutocommit(theConfig.get(dsName + ".autocommit").toString());
			}
			if (theConfig.containsKey(dsName + ".isolation")) {
				config.setIsolation(theConfig.get(dsName + ".isolation").toString());
			}
			if (theConfig.containsKey(dsName + ".maxActive")) {
				config.setMaxActive(theConfig.get(dsName + ".maxActive").toString());
			}
			if (theConfig.containsKey(dsName + ".maxIdle")) {
				config.setMaxIdle(theConfig.get(dsName + ".maxIdle").toString());
			}
			if (theConfig.containsKey(dsName + ".maxWait")) {
				config.setMaxWait(theConfig.get(dsName + ".maxWait").toString());
			}

			//create the data source and bind the context name
			//@TODO : what if context name already exists in context ?
			try {
				ds = config.c3po().build();
			}
			catch (NamingException ne) {
				LOGGER.error("Cant get local Datasource of name '" + dsName + "' for config unit '" + unitName);
				LOGGER.error("There will be connection and SQL exceptions !");
			}
		}

		cacheDataSource(dsName, unitName, ds);

		return ds;
	}

	private void cacheDataSource(String dsName, String unitName, DataSource ds) {
		if (ds != null) {
			nameSdbDsCache.add(dsName, ds);

			//shold this be preserved as well ? will this ever happen ? - can a datasource
			//for a unit config be changed dynamically ?
			unitSdbDsCache.add(unitName, ds);
		}
	}

	/**
	 * Create the unit specific configuration properties by grabbing unit configuration from the Empire config
	 *
	 * @param theUnitName the name of the persistence unit configuration to retrieve
	 * @return the unit configuration
	 */
	private Map<String, Object> createUnitConfig(final String theUnitName) {

		Map<String, Object> aConfig = new HashMap<String, Object>();

		if (mConfig.hasUnit(theUnitName)) {
			aConfig.putAll(mConfig.getUnitConfig(theUnitName));
		}

		aConfig.putAll(mConfig.getGlobalConfig());

		return aConfig;
	}

	/**
	 * if the config contains {@link JenaConfig.INIT_SDB}=true then initialise the database in the DataStore
	 * for this config. If not present, no initialisation check is done, or performed - so use it first time, then remove
	 * to avoid checking
	 */
	private void initSDBIfRequired(Map<String, Object> theConfig) {

		if (theConfig.containsKey(INIT_SDB)) {

			String isInit = theConfig.get(INIT_SDB).toString();

			if (Boolean.valueOf(isInit)) {
				// next get a Jena Store

				DataSource ds = getDataSource(theConfig);

				Store store = null;
				Connection jdbcConn = null;
				try {
					jdbcConn = ds.getConnection();

					if (null != jdbcConn) {

						store = getSDBStore(theConfig, jdbcConn);


						try {
							if (!StoreUtils.isFormatted(store)) {
								store.getTableFormatter().create();
							}
						}
						catch (SQLException e) {
							e.printStackTrace();
						}
						finally {
							store.close();
						}
					}
				}
				catch (SQLException sqle) {
					LOGGER.error("Cant get connection to datasource " + ds.toString() + " - null model will be returned !");
				}
			}
		}

	}

	/**
	 * Create and SDB model for a particular configUnit. If the unit has ontology, then create an ontModel and add the
	 * created SDB model. Initial call gets a DataSource depending on presence of JenaConfig.JNDI or JenaConfig.DS.
	 * <p/>
	 * It is possible to get a Null return from this method, if factory fails, or if the SDB model cannot be added to the ontModel.
	 *
	 * @param theConfig the configuration
	 * @return an SDB model
	 */
	private Model getSDBModel(Map<String, Object> theConfig) {

		String configName = theConfig.get(PropertiesConfigReader.KEY_NAME).toString();

		DataSource ds = getDataSource(theConfig);

		Model m = null;
		SDBModelWithStore ms = null;
		Store store = null;
		Connection jdbcConn = null;

		if (null != ds) {
			try {
				jdbcConn = ds.getConnection();

				if (null != jdbcConn) {
					store = getSDBStore(theConfig, jdbcConn);
					//next connect to the store.  You can connect to the default graph or to a named graph
					//connect to the default graph

					//@TODO NameGraph/model support
					Model aModel = SDBFactory.connectDefaultModel(store);

					m = getOntModel(aModel, configName, (String) theConfig.get(ONTOLOGY), JenaModelType.SDB);
				}
			}
			catch (SQLException sqle) {
				LOGGER.error("Cant get connection to datasource " + ds.toString() + " - null model will be returned !");
			}
		}

		if (null != m) {
			ms = new SDBModelWithStore(m, jdbcConn); // allow JPA (in theory) to control jdbc and model commits and close
		}

		return ms;
		//return m; //if you do this, rather than return a SDBModelWithStore, Jena will consume all connections in the pool
	}


	/**
	 * Get a Jena Store object so that we can connect to DB and create Model
	 *
	 * @param theConfig the app configuration
	 * @param jdbcConn  the JDBC connection
	 * @return the SDB store
	 */
	private Store getSDBStore(Map<String, Object> theConfig, Connection jdbcConn) {
		SDBConnection conn;
		Store store = null;
		if (null != jdbcConn) {
			conn = new SDBConnection(jdbcConn);
			//store.getLoader().setUseThreading(true);
			//store.getLoader().setChunkSize(128);
			//@TODO cache the StoreDesc ?, Store (theyre lightweight, maybe not much advantage)
			String layout = theConfig.containsKey(LAYOUT_TYPE) ? theConfig.get(LAYOUT_TYPE).toString() : LayoutType.LayoutTripleNodesIndex.getName();
			String databaseType = theConfig.containsKey(DATABASE_TYPE) ? theConfig.get(DATABASE_TYPE).toString() : DatabaseType.MySQL.getName();
			StoreDesc desc = new StoreDesc(layout, databaseType);
			store = SDBFactory.connectStore(conn, desc);
		}
		return store;
	}

	/**
	 * Get a Jena Store object so that we can connect to DB and create Model
	 *
	 * @param theConfig the app config
	 * @param jdbcConn the JDBC connectino for SDB
	 * @return the SDB store
	 */
	private Store getSDBStore(Map<String, Object> theConfig, DataSource jdbcConn) {
		SDBConnection conn = null;
		Store store = null;
		if (null != jdbcConn) {
			try {
				conn = new SDBConnection(jdbcConn);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			//store.getLoader().setUseThreading(true);
			//store.getLoader().setChunkSize(128);
			//@TODO cache the StoreDesc ?, Store (theyre lightweight, maybe not much advantage)
			String layout = theConfig.containsKey(LAYOUT_TYPE) ? theConfig.get(LAYOUT_TYPE).toString() : LayoutType.LayoutTripleNodesIndex.getName();
			String databaseType = theConfig.containsKey(DATABASE_TYPE) ? theConfig.get(DATABASE_TYPE).toString() : DatabaseType.MySQL.getName();
			StoreDesc desc = new StoreDesc(layout, databaseType);
			store = SDBFactory.connectStore(conn, desc);
		}
		return store;
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
		if (theType == JenaModelType.TDB || theType == JenaModelType.SDB) {
			rcModel = getCachedOntModel(theName).add(theModel);
		}
		else {
			rcModel = theModel;
		}

		return rcModel;
	}

	protected void cacheTdbModel(String name, Model m) {
		tdbModelCache.add(name, m);
	}

	protected void cacheOntModel(String name, Model m) {
		ontModelCache.add(name, m);
	}

	/**
	 * Return the cached OntModel
	 *
	 * @param theName the model unit name
	 * @return the cached OntModel, or null if one does not exist
	 */
	protected Model getCachedOntModel(String theName) {
		return ontModelCache.get(theName);
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
	 * Create an ontology model and cache if config determines
	 * <p/>
	 * I've seen elsewhere that the ontmodel gets added to the data model but it didnt seem to work for me at the time - could
	 * have been something I was doing :-) so leave protected rather than private for easy override
	 *
	 * @param name		the unit name
	 * @param ontLocation the location of the ontology for the OntModel
	 */
	protected void initOntologyModel(String name, String ontLocation) {
		if (null != ontLocation && ontLocation.length() > 0 && null == ontModelCache.get(name)) {
			Model ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
			ontModel.read(ontLocation);
			cacheOntModel(name, ontModel);
		}
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
	 * Return whether or not the value specifies SDB
	 *
	 * @param theType the value
	 * @return true if its for SDB, false otherwise
	 */
	protected boolean isSdb(final String theType) {
		return JenaModelType.SDB.toString().equals(theType);
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
