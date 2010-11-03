package com.clarkparsia.empire.sql;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Dumb Mutable value object for DataSource connection settings
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
 * @author uoccou
 *
http://purl.org/skytwenty/regperson.owl#Location/http://purl.org/skytwenty/regperson.owl#Location/http%3A%2F%2Fpurl.org%2Fskytwenty%2Fregperson.owl%23OpenIDRegisteredPerson%2Fhttp%253A%252F%252Fwww.google.com%252Fprofiles%252FTreasureCorpIsland%252F_ANY_Midp21+Device_84766997133853%2F
 */
public class DSSettings {
	protected final Log _logger = LogFactory.getLog(this.getClass());

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@PostConstruct
	public void init() {
		try {
			// Instantiate database driver
			Class.forName(getDriver());
		} catch (ClassNotFoundException e) {

			_logger.error("Unable to declare DB Driver: " + getDriver(), e);

		}
	}

	public String getAutocommit() {
		return autocommit;
	}

	public void setAutocommit(String autocommit) {
		this.autocommit = autocommit;
	}

	public String getIsolation() {
		return isolcation;
	}

	public void setIsolation(String isolcation) {
		this.isolcation = isolcation;
	}

	public String getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(String maxActive) {
		this.maxActive = maxActive;
	}

	public String getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(String maxWait) {
		this.maxWait = maxWait;
	}

	public String getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(String maxIdle) {
		this.maxIdle = maxIdle;
	}
}
