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

package com.clarkparsia.empire.jena;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;
import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.ds.DataSourceException;

import com.clarkparsia.empire.sql.DSSettings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DataSourceFactory implementation for creating a DataSource instance backed by SDB.</p>
 *
 * @author  Michael Grove
 * @author  uoccou
 * @since   1.0
 * @version 1.0
 */
@Alias("sdb")
public class SDBDataSourceFactory extends AbstractJenaDataSourceFactory {
    /**
     * the logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SDBDataSourceFactory.class);

    /**
     * Configuration parameter for specifying the key name of a NON container (jndi) datasource
     *
     * there should be corresponding global config entries on that keyname that can be used
     * to populate {@link DSSettings}
     */
    public static final String LOCAL_DS = "localDS";

    /**
     * Configuration parameter for specifying the key name of a container (jndi) datasource
     */
    public static final String JNDI_DS = "jndiDS";

    /**
     * Configuration parameter for specifying the database type Jena expects for SDB StoreDesc
     * {@link DatabaseType}
     * will default to "MySQL" if not present
     */
    public static final String DATABASE_TYPE = "databaseType";

    /**
     * Configuration parameter for specifying the layout type Jena expects for SDB StoreDesc
     * {@link LayoutType}
     * will default to "layout2/index" if not present
     */
    public static final String LAYOUT_TYPE = "layoutType";

    /**
     * Configuration parameter for specifying that SDB database initialisation should happen. Check NOT performed unless
     * this is set. That is, by default the database is assumed to have been initialised.
     */
    public static final String INIT_SDB = "initSDB";

    private static Map<String, DataSource> nameSdbDsCache = Maps.newHashMap();//ds name to DataSource
    private static Map<String, DataSource> unitSdbDsCache = Maps.newHashMap();//configName to DataSource

    private EmpireConfiguration mConfig;

    @Inject
    public SDBDataSourceFactory(@Named("ec") EmpireConfiguration theContainerConfig) {
        mConfig = theContainerConfig;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean canCreate(final Map<String, Object> theMap) {
        return true;
    }

    @Override
    public com.clarkparsia.empire.ds.DataSource create(final Map<String, Object> theMap) throws DataSourceException {

        com.clarkparsia.empire.ds.DataSource aSource = null;
        Model aModel = getSDBModel(theMap);
        initSDBIfRequired(theMap);

        if (aModel != null) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Got a model - creating DataSource ");
            }

            if (theMap.containsKey(STREAM) && theMap.containsKey(FORMAT)) {
                load(aModel,
                     asReader(theMap.get(STREAM)),
                     theMap.get(FORMAT).toString(),
                     theMap.containsKey(BASE) ? theMap.get(BASE).toString() : "");
            }

            if (theMap.containsKey(FILES)) {
                loadFiles(aModel,
                          theMap.get(FILES).toString(),
                          theMap.containsKey(BASE) ? theMap.get(BASE).toString() : "");
            }

            if (aModel.supportsTransactions()) {
                aSource = new JenaDataSourceSupportingTransactions(aModel);
            }
            else {
                aSource = new JenaDataSource(aModel);
            }
        }
        else {
            LOGGER.error("Could not get a model - not creating DataSource. ");
        }

        return aSource;
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
            nameSdbDsCache.put(dsName, ds);

            //shold this be preserved as well ? will this ever happen ? - can a datasource
            //for a unit config be changed dynamically ?
            unitSdbDsCache.put(unitName, ds);
        }
    }

    /**
     * if the config contains {@link com.clarkparsia.empire.jena.JenaConfig.INIT_SDB}=true then initialise the database in the DataStore
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

                    Model mm = getCachedOntModel(configName);
                    if (mm != null) {
                        mm.add(m);
                        m = mm;
                    }
                    else {
                        m = aModel;
                    }
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
}
