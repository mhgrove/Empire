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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.naming.NamingException;

/**
 * <p>Dumb Mutable value object for DataSource connection settings</p>
 * <ul>
 * <li>url (default : "jdbc:mysql://localhost:3306/")</li>
 * <li>db (name)</li>
 * <li>driver (FQ jdbc driver Class name)</li>
 * <li>user</li>
 * <li>password</li>
 * <li>autocommit (default false)</li>
 * <li>isolation (default TRANSACTION_READ_COMMITTED)</li>
 * <li>maxActive (default 10)</li>
 * <li>maxIdle (default 5)</li>
 * <li>maxWait (default 5000)</li>
 * </ul>
 * http://purl.org/skytwenty/regperson.owl#Location/http://purl.org/skytwenty/regperson.owl#Location/http%3A%2F%2Fpurl.org%2Fskytwenty%2Fregperson.owl%23OpenIDRegisteredPerson%2Fhttp%253A%252F%252Fwww.google.com%252Fprofiles%252FTreasureCorpIsland%252F_ANY_Midp21+Device_84766997133853%2F
 *
 * @author 	uoccou
 * @author 	Michael Grove
 * @since 	0.7
 * @version 0.7
 */
public class DSSettings {
	protected final Logger _logger = LoggerFactory.getLogger(this.getClass());

	private String url = "jdbc:mysql://localhost:3306/";
	private String db = "abcdef";
	private String driver = "com.mysql.jdbc.Driver";
	private String user = "root";
	private String password = "abcdef";
	private String autocommit = "false";
	private String isolcation = "TRANSACTION_READ_COMMITTED";
	private String maxActive = "10";
	private String maxIdle = "5";
	private String maxWait = "5000"; //ms waiting for a conn

	private String contextName = JDBC_CONTEXT_NAME;

	private static final String JNDI_CONTEXT_NAME = "java/js";
	private static final String JDBC_CONTEXT_NAME = "jdbc/js";

	public static DSSettings jndi(final String theJndiDsName) {
		return new DSSettings(theJndiDsName).jndi();
	}

	private enum Type {
		JNDI, C3PO, Plain
	}

	private Type type = Type.Plain;

	public DSSettings() {
	}

	public DSSettings(final String theContextName) {
		contextName = theContextName;
	}

	public DataSource build() throws NamingException {
		switch (type) {
			case JNDI:
				return new DSJndi(this);
			case C3PO:
				return new DSC3poContext(this);
			default:
				return new DSContext(this);
		}
	}

	public DSSettings setContextName(String theName) {
		contextName = theName;
		return this;
	}

	public String getContextName() {
		return contextName;
	}

	public DSSettings jndi() {
		type = Type.JNDI;
		return this;
	}

	public DSSettings jdbc() {
		type = Type.Plain;
		return this;
	}

	public DSSettings c3po() {
		type = Type.C3PO;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public DSSettings setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getDriver() {
		return driver;
	}

	public DSSettings setDriver(String driver) {
		this.driver = driver;
		return this;
	}

	public String getUser() {
		return user;
	}

	public DSSettings setUser(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public DSSettings setPassword(String password) {
		this.password = password;
		return this;
	}

	public void init() {
		try {
			// Instantiate database driver
			Class.forName(getDriver());
		}
		catch (ClassNotFoundException e) {
			_logger.error("Unable to declare DB Driver: " + getDriver(), e);
		}
	}

	public String getAutocommit() {
		return autocommit;
	}

	public DSSettings setAutocommit(String autocommit) {
		this.autocommit = autocommit;
		return this;
	}

	public String getIsolation() {
		return isolcation;
	}

	public DSSettings setIsolation(String isolcation) {
		this.isolcation = isolcation;
		return this;
	}

	public String getMaxActive() {
		return maxActive;
	}

	public DSSettings setMaxActive(String maxActive) {
		this.maxActive = maxActive;
		return this;
	}

	public String getMaxWait() {
		return maxWait;
	}

	public DSSettings setMaxWait(String maxWait) {
		this.maxWait = maxWait;
		return this;
	}

	public String getMaxIdle() {
		return maxIdle;
	}

	public DSSettings setMaxIdle(String maxIdle) {
		this.maxIdle = maxIdle;
		return this;
	}
}
