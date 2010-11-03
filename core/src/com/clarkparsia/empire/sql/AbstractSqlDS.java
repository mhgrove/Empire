package com.clarkparsia.empire.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractSqlDS implements DataSource {
	protected final Logger LOGGER = LogManager.getLogger(this.getClass());
	private DataSource ds = null;
	private DSSettings config = null;
	
	public abstract InitialContext getInitialContext();
	public abstract String getContextName(); 
	
	public Connection getConnection() {

	      Connection con = null;
	      
	      try {
			con = getDataSource().getConnection();
			con.setAutoCommit(false);
	        //reset from default MySQL of repeatable_read
	        //con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	        
		} catch (SQLException e) {
			LOGGER.warn("Exception getting connection to database : "
	              + e.toString());
		}

	      return con;
	  }
	public Connection getConnection(String username, String password) {

	      Connection con = null;
	      
	      try {
			con = getDataSource().getConnection(username,password);
			con.setAutoCommit(false);
	        //reset from default MySQL of repeatable_read
	        con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	        
		} catch (SQLException e) {
			LOGGER.warn("Exception getting connection to database : "
	              + e.toString());
		}

	      return con;
	  }
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return ds.getLogWriter();
	}
	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return ds.getLoginTimeout();
	}
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		ds.setLogWriter(out);
	}
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		ds.setLoginTimeout(seconds);
	}
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return ds.isWrapperFor(iface);
	}
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return ds.unwrap(iface);
	}
	public DataSource getDataSource() {
		DataSource ds = null;
		try {
			ds = (DataSource)getInitialContext().lookup( getContextName() );
		} catch (NamingException e) {
			// TODO Auto-generated catch block
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
