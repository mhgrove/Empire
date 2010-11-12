package com.clarkparsia.empire.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Abstract basic functionality for DataSource wrappers
 *
 * @author uoccou
 * @author Michael Grove
 * @version 0.7
 * @since 0.7
 */
abstract class AbstractSqlDS implements DataSource {

	protected final Logger LOGGER = LogManager.getLogger(this.getClass());

	private DataSource ds = null;

	private DSSettings config = null;

	private InitialContext ctx = null;

	/*
	 * Lookup the DataSource, which will be backed by a pool
	 * that the application server provides. DataSource instances
	 * are also a good candidate for caching as an instance
	 * variable, as JNDI lookups can be expensive as well.
	 */
	private String contextName = "java/ds";

	AbstractSqlDS(final DSSettings theConfig) {
		config = theConfig;

		contextName = theConfig.getContextName();
	}

	public abstract void init() throws NamingException;

	public InitialContext getInitialContext() {
		return ctx;
	}

	public void setInitialContext(InitialContext ctx) {
		this.ctx = ctx;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	/**
	 * @inheritDoc
	 */
	public Connection getConnection() {

		Connection con = null;

		try {
			con = getDataSource().getConnection();
			con.setAutoCommit(false);
		}
		catch (SQLException e) {
			LOGGER.warn("Exception getting connection to database : " + e.toString());
		}

		return con;
	}

	/**
	 * @inheritDoc
	 */
	public Connection getConnection(String username, String password) {

		Connection con = null;

		try {
			con = getDataSource().getConnection(username, password);
			con.setAutoCommit(false);
			//reset from default MySQL of repeatable_read
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

		}
		catch (SQLException e) {
			LOGGER.warn("Exception getting connection to database : " + e.toString());
		}

		return con;
	}

	/**
	 * @inheritDoc
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return ds.getLogWriter();
	}

	/**
	 * @inheritDoc
	 */
	public int getLoginTimeout() throws SQLException {
		return ds.getLoginTimeout();
	}

	/**
	 * @inheritDoc
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		ds.setLogWriter(out);
	}

	/**
	 * @inheritDoc
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		ds.setLoginTimeout(seconds);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return ds.isWrapperFor(iface);
	}

	/**
	 * @inheritDoc
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return ds.unwrap(iface);
	}

	public DataSource getDataSource() {
		DataSource ds = null;

		try {
			ds = (DataSource) getInitialContext().lookup(getContextName());
		}
		catch (NamingException e) {
			e.printStackTrace();
		}

		return ds;
	}

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public DSSettings getConfig() {
		return config;
	}

	public void setConfig(DSSettings config) {
		this.config = config;
	}
}
