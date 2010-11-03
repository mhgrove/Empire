package com.clarkparsia.empire.sql;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

/**
 * javax.sql.Datasource using DBCP BasicDataSourceFactory
 * 
 * @author uoccou
 *
 */
public class DSContext extends AbstractSqlDS{
	
	
	
	private InitialContext ic = null;
	private DSSettings config = null;
	private String contextFactoryName = "com.sun.jndi.fscontext.RefFSContextFactory";
	private String providerUrl = "file:///tmp";
	private String dataSourceFactory = "org.apache.commons.dbcp.BasicDataSourceFactory";
	
	private String contextName = "jdbc/ds";
	
	 
	public DSContext(DSSettings config, String contextFactoryName,
			String providerUrl, String dataSourceFactory, String contextName)  throws NamingException {
		super();
		this.config = config;
		this.contextFactoryName = contextFactoryName;
		this.providerUrl = providerUrl;
		this.dataSourceFactory = dataSourceFactory;
		this.contextName = contextName;
		init();
	}
	public DSContext(DSSettings config)  throws NamingException{
		super();
		this.config = config;
		init();
	}
	public DSContext(DSSettings config, String contextName) throws NamingException{
		super();
		this.config = config;
		this.contextName = contextName;
		//@TODO : what if context name already exists in context ?
		init();
	}
	@PostConstruct
	public void init() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, getContextFactoryName() );
		System.setProperty(Context.PROVIDER_URL, getProviderUrl() );

		//try {
			ic = new InitialContext();
	
			// Construct BasicDataSource reference
			Reference ref = new Reference("javax.sql.DataSource", getDataSourceFactory(), null);
	
			ref.add(new StringRefAddr("driverClassName", getConfig().getDriver() ) );
			ref.add(new StringRefAddr("url", getConfig().getUrl() ) );
			ref.add(new StringRefAddr("username", getConfig().getUser() ) ) ;
			ref.add(new StringRefAddr("password", getConfig().getPassword() ) );
			ref.add(new StringRefAddr("defaultAutoCommit", getConfig().getAutocommit() ) );
			//ref.add(new StringRefAddr("defaultTransactionIsolation", getConfig().getIsolation() ) );
			ref.add(new StringRefAddr("maxActive", getConfig().getMaxActive() ) );
			ref.add(new StringRefAddr("maxIdle", getConfig().getMaxIdle() ) );
			ref.add(new StringRefAddr("maxWait", getConfig().getMaxWait() ) );
//			String options = "defaultTransactionIsolation=" + getConfig().getIsolation();
//			options += ";defaultAutoCommit=" + getConfig().getAutocommit();
//			options += ";maxActive=" + getConfig().getMaxActive();
//			options += ";maxIdle=" + getConfig().getMaxIdle();;
//			options += ";maxWait=" + getConfig().getMaxWait();
			//ref.add(new StringRefAddr("connectionProperties",options));
			ref.add(new StringRefAddr("validationQuery", "/* ping */" ) );
			
			ic.rebind(getContextName(), ref);
			setDataSource( (DataSource)ic.lookup( getContextName() ) );
			
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			
//			LOGGER.warn("Problem establising context for data source : " + e.toString() );
//			
//		}

	}
//	@PostConstruct
//	public void init(){
//		
//		 BasicDataSource ds = new BasicDataSource();
//		 ds.setDriverClassName( getConfig().getDriver());
//		 ds.setUsername(getConfig().getUser() );
//		 ds.setPassword(getConfig().getPassword() );
//		 ds.setUrl( getConfig().getUrl() );
//		  
//		 
//		
//
//	}  
	public DSSettings getConfig() {
		return config;
	}

	public void setConfig(DSSettings config) {
		this.config = config;
	}

	public String getContextFactoryName() {
		return contextFactoryName;
	}

	public void setContextFactoryName(String contextFactoryName) {
		this.contextFactoryName = contextFactoryName;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	public String getDataSourceFactory() {
		return dataSourceFactory;
	}

	public void setDataSourceFactory(String dataSourceFactory) {
		this.dataSourceFactory = dataSourceFactory;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	public InitialContext getContext() {
		return ic;
	}
//	public String getUrl() {
//		return url;
//	}
//	public void setUrl(String url) {
//		this.url = url;
//	}
//	public String getDb() {
//		return db;
//	}
//	public void setDb(String db) {
//		this.db = db;
//	}
//	public String getDriver() {
//		return driver;
//	}
//	public void setDriver(String driver) {
//		this.driver = driver;
//	}
//	public String getUser() {
//		return user;
//	}
//	public void setUser(String user) {
//		this.user = user;
//	}
//	public String getPassword() {
//		return password;
//	}
//	public void setPassword(String password) {
//		this.password = password;
//	}
//	public String getAutocommit() {
//		return autocommit;
//	}
//	public void setAutocommit(String autocommit) {
//		this.autocommit = autocommit;
//	}
//	public String getIsolcation() {
//		return isolcation;
//	}
//	public void setIsolcation(String isolcation) {
//		this.isolcation = isolcation;
//	}
//	public String getMaxActive() {
//		return maxActive;
//	}
//	public void setMaxActive(String maxActive) {
//		this.maxActive = maxActive;
//	}
//	public String getMaxIdle() {
//		return maxIdle;
//	}
//	public void setMaxIdle(String maxIdle) {
//		this.maxIdle = maxIdle;
//	}
//	public String getMaxWait() {
//		return maxWait;
//	}
//	public void setMaxWait(String maxWait) {
//		this.maxWait = maxWait;
//	}
	public InitialContext getInitialContext() {
		return ic;
	}
	
	
	

}
