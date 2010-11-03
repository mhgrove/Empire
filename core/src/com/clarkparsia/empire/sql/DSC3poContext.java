package com.clarkparsia.empire.sql;

import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;


import org.apache.commons.dbcp.BasicDataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * javax.sql.Datasource using DBCP BasicDataSourceFactory
 * 
 * @author ultan
 *
 */
public class DSC3poContext extends AbstractSqlDS{
	
	
	
	private InitialContext ic = null;
	private DSSettings config = null;
//	private String contextFactoryName = "com.sun.jndi.fscontext.RefFSContextFactory";
//	private String providerUrl = "file:///tmp";
//	private String dataSourceFactory = "org.apache.commons.dbcp.BasicDataSourceFactory";
	
	private String contextName = "jdbc/ds";
	
	 
	
	public DSC3poContext(DSSettings config)  throws NamingException{
		super();
		this.config = config;
		init();
	}
	public DSC3poContext(DSSettings config, String contextName) throws NamingException{
		super();
		this.config = config;
		this.contextName = contextName;
		//@TODO : what if context name already exists in context ?
		init();
	}
	@PostConstruct
	public void init() throws NamingException {
		
		/*
		 * <Resource auth="Container" description="DB Connection" driverClass="com.mysql.jdbc.Driver" 
		 * maxPoolSize="4" minPoolSize="2" 
		 * acquireIncrement="1" 
		 * name="jdbc/TestDB" 
		 * user="test" 
		 * password="ready2go" 
		 * factory="org.apache.naming.factory.BeanFactory" 
		 * type="com.mchange.v2.c3p0.ComboPooledDataSource" 
		 * jdbcUrl="jdbc:mysql://localhost:3306/test?autoReconnect=true" /> 
		 */
		
		try {
			ic = new InitialContext();
			ComboPooledDataSource cpds = new ComboPooledDataSource(); 
			
			cpds.setDriverClass( getConfig().getDriver() );
			 
			//loads the jdbc driver 
			cpds.setJdbcUrl( getConfig().getUrl() ); 
			cpds.setUser(getConfig().getUser()); 
			cpds.setPassword(getConfig().getPassword());
			cpds.setMaxPoolSize( Integer.valueOf( getConfig().getMaxActive()) );
			cpds.setMinPoolSize( Integer.valueOf( getConfig().getMaxIdle() ) );
			cpds.setAcquireIncrement(1);
			
			ic.rebind( getContextName(), cpds);
			setDataSource( (DataSource)ic.lookup( getContextName() ) );
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException ne) {
			ne.printStackTrace();
		}
//		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, getContextFactoryName() );
//		System.setProperty(Context.PROVIDER_URL, getProviderUrl() );
//
//		//try {
//			ic = new InitialContext();
//	
//			// Construct BasicDataSource reference
//			Reference ref = new Reference("javax.sql.DataSource", getDataSourceFactory(), null);
//	
//			ref.add(new StringRefAddr("driverClassName", getConfig().getDriver() ) );
//			ref.add(new StringRefAddr("url", getConfig().getUrl() ) );
//			ref.add(new StringRefAddr("username", getConfig().getUser() ) ) ;
//			ref.add(new StringRefAddr("password", getConfig().getPassword() ) );
//			ref.add(new StringRefAddr("defaultAutoCommit", getConfig().getAutocommit() ) );
//			//ref.add(new StringRefAddr("defaultTransactionIsolation", getConfig().getIsolation() ) );
//			ref.add(new StringRefAddr("maxActive", getConfig().getMaxActive() ) );
//			ref.add(new StringRefAddr("maxIdle", getConfig().getMaxIdle() ) );
//			ref.add(new StringRefAddr("maxWait", getConfig().getMaxWait() ) );
////			String options = "defaultTransactionIsolation=" + getConfig().getIsolation();
////			options += ";defaultAutoCommit=" + getConfig().getAutocommit();
////			options += ";maxActive=" + getConfig().getMaxActive();
////			options += ";maxIdle=" + getConfig().getMaxIdle();;
////			options += ";maxWait=" + getConfig().getMaxWait();
//			//ref.add(new StringRefAddr("connectionProperties",options));
//			ref.add(new StringRefAddr("validationQuery", "/* ping */" ) );
//			
//			ic.rebind(getContextName(), ref);
			
			
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

	

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	public InitialContext getContext() {
		return ic;
	}

	public InitialContext getInitialContext() {
		return ic;
	}
	
	
	

}
