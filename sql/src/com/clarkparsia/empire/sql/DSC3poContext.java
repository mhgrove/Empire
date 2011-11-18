package com.clarkparsia.empire.sql;

import java.beans.PropertyVetoException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * javax.sql.Datasource using DBCP BasicDataSourceFactory
 * 
 * @author uoccou
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
class DSC3poContext extends AbstractSqlDS {

	DSC3poContext(DSSettings theConfig) throws NamingException {
		super(theConfig);

		init();
	}

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
			InitialContext aContext = new InitialContext();
			setInitialContext(aContext);

			ComboPooledDataSource cpds = new ComboPooledDataSource();

			cpds.setDriverClass(getConfig().getDriver());

			//loads the jdbc driver 
			cpds.setJdbcUrl(getConfig().getUrl());
			cpds.setUser(getConfig().getUser());
			cpds.setPassword(getConfig().getPassword());
			cpds.setMaxPoolSize(Integer.valueOf(getConfig().getMaxActive()));
			cpds.setMinPoolSize(Integer.valueOf(getConfig().getMaxIdle()));
			cpds.setAcquireIncrement(1);

			aContext.rebind(getContextName(), cpds);
			setDataSource((DataSource) aContext.lookup(getContextName()));
		}
		catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		catch (NamingException ne) {
			ne.printStackTrace();
		}
	}

	/**
	 * Added due to DataSource evolution in Java7. Sorry, but I can't understand the beginning of that
	 * @return
	 * @throws SQLFeatureNotSupportedException
	 * @see javax.sql.CommonDataSource#getParentLogger()
	 */
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException(new UnsupportedOperationException("method "+DSC3poContext.class.getName()+"#getParentLogger has not yet been implemented AT ALL"));
	}
}
