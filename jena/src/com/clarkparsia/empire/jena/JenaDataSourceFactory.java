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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.sql.DSEmpireModule;
import com.clarkparsia.empire.sql.DataSourceManager;
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
import static com.clarkparsia.empire.config.io.impl.PropertiesConfigReader.*;


/**
 * <p>Abstract base class for creating Jena-backed data sources.  Provides a single method for creating an appropriate
 * type of Jena model based on some configuration parameters.</p>
 *
 * @author Michael Grove
 * @author uoccou
 * @since 0.6.3
 * @version 0.7
 */
public abstract class JenaDataSourceFactory implements DataSourceFactory, JenaConfig {
	private final Logger LOGGER = LogManager.getLogger(this.getClass());
	
	//@uoccou : simple caches to avoid having to recreate models on each call
	private Map <String,Model> tdbModelCache = new HashMap<String,Model>();
	private Map <String,Model> ontModelCache = new HashMap<String,Model>();
	private Map <String,DataSource> sdbDsCache = new HashMap<String,DataSource>();//configName to DataSource
	
	/**
	 * Create a Jena model from the values specified in the configuration.
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
				aModel = getTDBModel(aModel,theConfig);
			   //aModel = TDBFactory.createModel(theConfig.get(LOCATION).toString());
			} 
			else if ( isSdb(aType)) {
				aModel = getSDBModel(aModel,theConfig);				
				initSDBIfRequired(theConfig);
			}
			
		} else {
			aModel = ModelFactory.createDefaultModel();
		}
		LOGGER.debug("Created Jena model : " + (System.currentTimeMillis()-start));

		return aModel;
	}
	
	
	
	/**
	 * Create and cache a TDB model for a particular configUnit. If the unit has ontology, then create an ontModel and add the 
	 * created TDB model. Subsequent calls will return the cached model. Persisted changes are assumed to be committed and synced.
	 * It is possible to get a Null return from this method, if factory fails, or if the TDB model cannot be added to the ontModel. 
	 * @param aModel
	 * @param theConfig
	 * @return
	 */
	private Model getTDBModel(Model aModel, Map<String, Object> theConfig) {
		
		String configName = theConfig.get(KEY_NAME).toString();
		Model m = getCachedTdbModel(configName);
		if (null == m ) {
			Model ontModel = null; //may have concurrency issues with this...but means getModel is 10x faster
			//Model finalModel = null;
			
			aModel = TDBFactory.createModel(theConfig.get(LOCATION).toString());
			//m = aModel;
			//if we have ontModel config settings
			m = getModel( aModel, configName, theConfig, JenaModelType.TDB);

			//cache what we've created
			if ( null != m )
				cacheTdbModel(configName,m);
		}
		return m;
	}
	/**
	 * check for a locally defined non-container Datasource ref and create a DBCP based DataSource. 
	 * </br>if not available check for a JNDI data source ref and try and get a DataSource from JNDI.
	 *
	 * Override to provide direct context lookup if and when the datasource name gets passed from the persistenceUnitInfo
	 * <p/>
	 * create a data source with the help of the {@link DSEmpireModule}
	 *  
	 * @param theConfig
	 * @return the DataSource for theConfig, or null
	 */
	protected DataSource getDataSource( Map<String, Object> theConfig) {
		
		String configName = theConfig.get(KEY_NAME).toString();
		return DataSourceManager.getInstance().getDataSource(configName);
		
		
	}
	

	
	/**
	 * if the config contains {@link JenaConfig.INIT_SDB}=true then initialise the database in the DataStore
	 * for this config. If not present, no initialisation check is done, or performed - so use it first time, then remove
	 * to avoid checking
	 * 
	 */
	private void initSDBIfRequired(Map<String, Object> theConfig) {
		
		String configName = theConfig.get(KEY_NAME).toString();
		
		if ( theConfig.containsKey(INIT_SDB) ) {
		
			String isInit = theConfig.get(INIT_SDB).toString();
			
			if ( Boolean.TRUE.equals( new Boolean(isInit) ) ) {
				//next get a Jena Store
				//if ( null == defModel ){
				DataSource ds = getDataSource(theConfig);
				
//				Model m = null;
//				SDBConnection conn = null;
				Store store = null;
				Connection jdbcConn = null;
				try {
					jdbcConn = ds.getConnection();
					
					if ( null != jdbcConn ) {
												
						store = getSDBStore(theConfig, jdbcConn);
						
						
						try {
							if ( !StoreUtils.isFormatted(store) ){
								store.getTableFormatter().create();
							} 
				//			else {
				//				store.getTableFormatter().truncate();
				//				store.getTableFormatter().create();
				//			}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {

							store.close();
							
						}
					}
				} catch ( SQLException sqle ) {
					//yuch
					LOGGER.error("Cant get connection to datasource " + ds.toString() + " - null model will be returned !" );
				}
			}
		}

	}
	/**
	 * Create and SDB model for a particular configUnit. If the unit has ontology, then create an ontModel and add the 
	 * created SDB model. Initial call gets a DataSource depending on presence of JenaConfig.JNDI or JenaConfig.DS.
	 * 
	 * It is possible to get a Null return from this method, if factory fails, or if the SDB model cannot be added to the ontModel.
	 *  
	 * @param aModel
	 * @param theConfig
	 * @return
	 */
	private Model getSDBModel(Model aModel, Map<String, Object> theConfig) {
		
		String configName = theConfig.get(KEY_NAME).toString();
		//next get a Jena Store
		//if ( null == defModel ){
		DataSource ds = getDataSource(theConfig);
		
		Model m = null;
		ModelWithStore ms = null;
		SDBConnection conn = null;
		Store store = null;
		Connection jdbcConn = null;
		
		if ( null != ds ) {
			try {
				jdbcConn = ds.getConnection();
				
				if ( null != jdbcConn ) {
					store = getSDBStore(theConfig, jdbcConn);
					//next connect to the store.  You can connect to the default graph or to a named graph
					//connect to the default graph
					
					//@TODO NameGraph/model support
					aModel = SDBFactory.connectDefaultModel(store);
					
					//m = aModel;
					m = getModel( aModel, configName, theConfig, JenaModelType.SDB);
					
					
				}
			} catch ( SQLException sqle ) {
				//yuch
				LOGGER.error("Cant get connection to datasource " + ds.toString() + " - null model will be returned !" );
			}
		}
		
		
		//System.out.println( "Model " + Thread.currentThread() );
		if ( null != m )
			ms = new ModelWithStore(m, jdbcConn); //allow JPA (in theory) to control jdbc and model commits and close
		return ms;
		//return m; //if you do this, rather than return a ModelWithStore, Jena will consume all connections in the pool
	}


	/**
	 * get a Jena Store object so that we can connect to DB and create Model 
	 * 
	 * @param theConfig
	 * @param jdbcConn
	 * @return
	 */
	private Store getSDBStore(Map<String, Object> theConfig, Connection jdbcConn) {
		SDBConnection conn;
		Store store = null;
		if (null != jdbcConn) {
			conn = new SDBConnection(jdbcConn);	
			//store.getLoader().setUseThreading(true);
			//store.getLoader().setChunkSize(128);
			//@TODO cache the StoreDesc ?, Store (theyre lightweight, maybe not much advantage)
			String layout =  theConfig.containsKey(LAYOUT_TYPE) ? theConfig.get(LAYOUT_TYPE).toString()  : LayoutType.LayoutTripleNodesIndex.getName() ;
			String databaseType = theConfig.containsKey(DATABASE_TYPE) ? theConfig.get(DATABASE_TYPE).toString() : DatabaseType.MySQL.getName();
			StoreDesc desc = new StoreDesc( layout, databaseType );
			store = SDBFactory.connectStore(conn, desc);
		}
		return store;
	}
	/**
	 * get a Jena Store object so that we can connect to DB and create Model 
	 * 
	 * @param theConfig
	 * @param jdbcConn
	 * @return
	 */
	private Store getSDBStore(Map<String, Object> theConfig, DataSource jdbcConn) {
		SDBConnection conn = null;
		Store store = null;
		if (null != jdbcConn) {
			try {
				conn = new SDBConnection(jdbcConn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//store.getLoader().setUseThreading(true);
			//store.getLoader().setChunkSize(128);
			//@TODO cache the StoreDesc ?, Store (theyre lightweight, maybe not much advantage)
			String layout =  theConfig.containsKey(LAYOUT_TYPE) ? theConfig.get(LAYOUT_TYPE).toString()  : LayoutType.LayoutTripleNodesIndex.getName() ;
			String databaseType = theConfig.containsKey(DATABASE_TYPE) ? theConfig.get(DATABASE_TYPE).toString() : DatabaseType.MySQL.getName();
			StoreDesc desc = new StoreDesc( layout, databaseType );
			store = SDBFactory.connectStore(conn, desc);
		}
		return store;
	}
	/**
	 * get a Model that is aware of a specified ontology, if present
	 * @param aModel
	 * @param configName
	 * @param theConfig
	 * @param type
	 * @return
	 */
	private Model getModel(Model aModel, String configName, Map<String, Object> theConfig, JenaModelType type) {
		//if we have ontModel config settings
		Model m = aModel ;
		if ( null != aModel && null != theConfig.get(ONTOLOGY) ) {
			
			//make sure the ontModel is initialised, only need to do once
			initOntologyModel(configName, (String)theConfig.get(ONTOLOGY) );
			
			//add the model to the ontmodel 
			m = getCompleteModel(configName, aModel, type);
			
		}
		return m;

	}
	/**
	 * get and/or cache data model with already cached ontmodel,  for use with this datasource
	 * @param name
	 * @param m
	 * @param type
	 * @return
	 */
	protected Model getCompleteModel(String name, Model m, JenaModelType type) {
		Model rcModel = null;
		if (type==JenaModelType.TDB || type==JenaModelType.SDB)
			rcModel = getCachedOntModel(name).add( m );
		else 
			rcModel = m;
		
		return rcModel;
	}
	protected void cacheTdbModel(String name, Model m) {
		tdbModelCache.put(name, m);
	}
	protected void cacheOntModel(String name, Model m) {
		ontModelCache.put(name, m);
	}
	protected Model getCachedOntModel(String name) {
		return ontModelCache.get(name);
	}
	protected Model getCachedTdbModel(String name) {
		return tdbModelCache.get(name);
	}
	/**
	 * Create an ontology model and cache if config determines
	 * 
	 * Ive seen elsewhere that the ontmodel gets added to the data model but it didnt seem to work for me at the time - could
	 * have been something I was doing :-) so leave protected rather than private for easy override
	 * @param name
	 * @param ontLocation
	 */
	protected void initOntologyModel(String name, String ontLocation){
		//OntModel m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
		//boolean rc = false;
		
		if ( null != ontLocation && ontLocation.length() > 0 && null == ontModelCache.get(name) ) {
			Model ontModel = null;
			ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
			ontModel.read(ontLocation);
			cacheOntModel(name,ontModel);
			//rc = true;
		} 
//		else if ( null != ontModelCache.get(name) )
//			rc =true;
//		
//		return rc;
	}
	protected boolean isTdb(final String theType) {
		return JenaModelType.TDB.toString().equals(theType);
	}
	protected boolean isSdb(final String theType) {
		return JenaModelType.SDB.toString().equals(theType);
	}

}
