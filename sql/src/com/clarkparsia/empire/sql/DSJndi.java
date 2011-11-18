package com.clarkparsia.empire.sql;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

/**
 * Datasource using JNDI context to provide connections.
 *
 * @author uoccou
 * @author Michael Grove
 * @version 0.7
 * @since 0.7
 */
class DSJndi extends AbstractSqlDS {

	/*
	 * Create a JNDI Initial context to be able to
	 *  lookup  the DataSource
	 *
	 * In production-level code, this should be cached as
	 * an instance or static variable, as it can
	 * be quite expensive to create a JNDI context.
	 *
	 * Note: This code only works when you are using servlets
	 * or EJBs in a J2EE application server. If you are
	 * using connection pooling in standalone Java code, you
	 * will have to create/configure datasources using whatever
	 * mechanisms your particular connection pooling library
	 * provides.
	 * @see DSContext
	 */
	DSJndi(DSSettings theConfig, InitialContext theContext) throws NamingException {
		super(theConfig);

		setInitialContext(theContext);

		init();
	}

	DSJndi(DSSettings theConfig) throws NamingException {
		super(theConfig);
		init();
	}

	/**
	 * @inheritDoc
	 */
	public void init() throws NamingException {
		if (getInitialContext() == null) {
			setInitialContext(new InitialContext());
		}

		setDataSource((DataSource) getInitialContext().lookup(getContextName()));
	}

	/**
	 * Added due to DataSource evolution in Java7. Sorry, but I can't understand the beginning of that
	 * @return
	 * @throws SQLFeatureNotSupportedException
	 * @see javax.sql.CommonDataSource#getParentLogger()
	 */
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException(new UnsupportedOperationException("method "+DSJndi.class.getName()+"#getParentLogger has not yet been implemented AT ALL"));
	}
}
