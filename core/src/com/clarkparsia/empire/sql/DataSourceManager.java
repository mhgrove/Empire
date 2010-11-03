package com.clarkparsia.empire.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.EmpireException;
import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.config.io.ConfigReader;
import com.clarkparsia.empire.config.io.impl.PropertiesConfigReader;
import com.clarkparsia.empire.config.io.impl.XmlConfigReader;
import com.clarkparsia.empire.util.BeanReflectUtil;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import static com.clarkparsia.empire.jena.JenaConfig.*;
/**
 * Creates and caches Data sources defined in empire.properties and from JNDI.
 * Uses instance of {@link DSSettings}
 * Duplicates most of EmpireConfiguration initialisation as seen in DefaultEmpireModule. 
 * @TODO replace duplicate code with Singleton EmpireConfigration ?
 * @author ultan
 *
 */
public class DataSourceManager {
	
	
	
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(DataSourceManager.class);
	private static DataSourceManager utils = new DataSourceManager();

	/**
	 * Application configuration properties
	 */
	private static EmpireConfiguration mConfig;
	//simple caching - do it better - may be sync issues ?
	private static Map <String,DataSource> nameSdbDsCache = new HashMap<String,DataSource>();//ds name to DataSource
	private static Map <String,DataSource> unitSdbDsCache = new HashMap<String,DataSource>();//configName to DataSource
	private static String configPath = null;
	
	private DataSourceManager() {		
		init();
		// TODO Auto-generated constructor stub
	}
	public static DataSourceManager getInstance() {
        return utils;
    }

	/**
	 * this is a duplicate of DefaultEmpireModule, with the addition of a settable configPath. Should this procedure be statically encapsulated in EmpireConfiguration
	 * so that it can be used throughout ?
	 * 
	 * Other custom configurations can have their own configuration mechanism if required.
	 *
	 */
	private void init() {
		readConfig();		
		//pre load and instantiate DataSources ?
	}

	/**
	 * Create the unit specific configuration properties by grabbing unit configuration from the Empire config
	 * 
	 * @param theUnitName the name of the persistence unit configuration to retrieve
	 * 
	 * @return the unit configuration
	 */
	private Map<String, Object> createUnitConfig(final String theUnitName) {
		
		Map<String, Object> aConfig = new HashMap<String, Object>();

		if (getConfiguration().hasUnit(theUnitName)) {
			aConfig.putAll(getConfiguration().getUnitConfig(theUnitName));
		}

		aConfig.putAll(getConfiguration().getGlobalConfig());

//		if (theMap != null) {
//			aConfig.putAll(theMap);
//		}

		return aConfig;
	}
	/**
	 * get Datasource defined for a configName
	 * lazy caches datasources for each configName
	 * wont recreate datasources already instantiated
	 * 
	 * @param configName
	 * @return
	 */
	public DataSource getDataSource(String unitName) {
		DataSource ds = null;
		Map<String,Object> unitConfig = createUnitConfig(unitName);
		
		if ( unitSdbDsCache.containsKey(unitName) )
			ds = unitSdbDsCache.get(unitName);
		else {
			if ( unitConfig.containsKey(LOCAL_DS) ) {
				
				String localDsName = unitConfig.get(LOCAL_DS).toString();
				if (null != localDsName )
					ds = createLocalDS(unitName, localDsName, unitConfig);	
			} else if ( unitConfig.containsKey(JNDI_DS) ) {
				
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
	 * @param unitName
	 * @param jndiDsName
	 * @return
	 */
	private DataSource createJndiDS(String unitName,  String jndiDsName) {
		DataSource ds = null;
		
		if( nameSdbDsCache.containsKey(jndiDsName) ){
			ds = nameSdbDsCache.get(jndiDsName);
		}else {
			try {
				ds = new DSJndi(jndiDsName);
				//cache it
				
			} catch (NamingException ne) {
				LOGGER.error("Cant get JNDI name '" + jndiDsName + "' for config unit '" + unitName);
				LOGGER.error("There will be connection and SQL exceptions !");
			}
		}
		if (null != ds )
			cacheDataSource(jndiDsName,unitName,ds);
			

		return ds;
	}

	/**
	 * create a Local (non-container) datasource, cache it by name, and by unitName
	 * Uses empire.properties to define DataSource
	 * @param unitName
	 * @param dsName
	 * @param theConfig
	 * @return
	 */
	private DataSource createLocalDS(String unitName, String dsName,Map<String, Object> theConfig) {
		DataSource ds = null;
		
		//try and get the named Datasource from the DS cache
		if ( nameSdbDsCache.containsKey(dsName) ) {
			ds = nameSdbDsCache.get(dsName);
		} else {
			DSSettings config = new DSSettings();
			if ( theConfig.containsKey(dsName+".url") )
				config.setUrl(theConfig.get(dsName+".url").toString() );
			if ( theConfig.containsKey(dsName+".db") )
				config.setDb(theConfig.get(dsName+".db").toString() );
			if ( theConfig.containsKey(dsName+".driver") )
				config.setDriver(theConfig.get(dsName+".driver").toString() );
			if ( theConfig.containsKey(dsName+".user") )
				config.setUser(theConfig.get(dsName+".user").toString() );
			if ( theConfig.containsKey(dsName+".password") )
				config.setPassword(theConfig.get(dsName+".password").toString() );
			if ( theConfig.containsKey(dsName+".autocommit") )
				config.setAutocommit(theConfig.get(dsName+".autocommit").toString() );
			if ( theConfig.containsKey(dsName+".isolation") )
				config.setIsolation(theConfig.get(dsName+".isolation").toString() );
			if ( theConfig.containsKey(dsName+".maxActive") )
				config.setMaxActive(theConfig.get(dsName+".maxActive").toString() );
			if ( theConfig.containsKey(dsName+".maxIdle") )
				config.setMaxIdle(theConfig.get(dsName+".maxIdle").toString() );
			if ( theConfig.containsKey(dsName+".maxWait") )
				config.setMaxWait(theConfig.get(dsName+".maxWait").toString() );
			
			//create the data source and bind the context name
			//@TODO : what if context name already exists in context ?
			try {
				//ds = new DSContext(config, dsName);
				ds = new DSC3poContext(config, dsName);
				
			} catch (NamingException ne) {
				LOGGER.error("Cant get local Datasource of name '" + dsName + "' for config unit '" + unitName);
				LOGGER.error("There will be connection and SQL exceptions !");
			}
		}
		//cache it for the named unit
		
		cacheDataSource(dsName,unitName,ds);
		
		
		return ds;
	}
	
	private void cacheDataSource(String dsName,String unitName,DataSource ds) {
		if (null != ds ) {
			//preserve existing datasource cache entry if it exists
			if( !nameSdbDsCache.containsKey(dsName) ) 
					nameSdbDsCache.put(dsName, ds);
			//shold this be preserved as well ? will this ever happen ? - can a datasource 
			//for a unit config be changed dynamically ?
			unitSdbDsCache.put(unitName,ds);
		}
		
	}
	private void readConfig() {
		File aConfigFile = null;

		
		LOGGER.debug("Trying built in paths to get config File");
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

	public static EmpireConfiguration getConfiguration() {
		return mConfig;
	}

	
}
