package com.clarkparsia.empire.sql;

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
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
class DSContext extends AbstractSqlDS {

	private String contextFactoryName = "com.sun.jndi.fscontext.RefFSContextFactory";

	private String providerUrl = "file:///tmp";

	private String dataSourceFactory = "org.apache.commons.dbcp.BasicDataSourceFactory";

	DSContext(DSSettings config, String contextFactoryName,
					 String providerUrl, String dataSourceFactory) throws NamingException {
		super(config);
		this.contextFactoryName = contextFactoryName;
		this.providerUrl = providerUrl;
		this.dataSourceFactory = dataSourceFactory;
		init();
	}

	DSContext(DSSettings config) throws NamingException {
		super(config);
		init();
	}

	public void init() throws NamingException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, getContextFactoryName());
		System.setProperty(Context.PROVIDER_URL, getProviderUrl());

		setInitialContext(new InitialContext());

		// Construct BasicDataSource reference
		Reference ref = new Reference("javax.sql.DataSource", getDataSourceFactory(), null);

		ref.add(new StringRefAddr("driverClassName", getConfig().getDriver()));
		ref.add(new StringRefAddr("url", getConfig().getUrl()));
		ref.add(new StringRefAddr("username", getConfig().getUser()));
		ref.add(new StringRefAddr("password", getConfig().getPassword()));
		ref.add(new StringRefAddr("defaultAutoCommit", getConfig().getAutocommit()));

		ref.add(new StringRefAddr("maxActive", getConfig().getMaxActive()));
		ref.add(new StringRefAddr("maxIdle", getConfig().getMaxIdle()));
		ref.add(new StringRefAddr("maxWait", getConfig().getMaxWait()));
		ref.add(new StringRefAddr("validationQuery", "/* ping */"));

		getInitialContext().rebind(getContextName(), ref);
		setDataSource((DataSource) getInitialContext().lookup(getContextName()));
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
}
