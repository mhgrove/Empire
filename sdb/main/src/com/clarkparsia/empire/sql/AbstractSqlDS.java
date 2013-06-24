/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * Copyright (c) 2010, Ultan O'Carroll
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

package com.clarkparsia.empire.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract basic functionality for DataSource wrappers
 *
 * @author 	uoccou
 * @author 	Michael Grove
 * @version 0.7
 * @since 	0.7
 */
abstract class AbstractSqlDS implements DataSource {

	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
