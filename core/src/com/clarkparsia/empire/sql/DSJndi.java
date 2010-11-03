package com.clarkparsia.empire.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Datasource using JNDI context to provide connections
 * @author ultan
 *
 */
public class DSJndi extends AbstractSqlDS{
	
	
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

    public DSJndi(InitialContext ctx, String contextName) throws NamingException  {
		super();
		this.ctx = ctx;
		this.contextName = contextName;
		init();
	}


	public DSJndi(String contextName) throws NamingException {
		super();
		this.contextName = contextName;
		init();
	}


	private InitialContext ctx = null;

     /*
      * Lookup the DataSource, which will be backed by a pool
      * that the application server provides. DataSource instances
      * are also a good candidate for caching as an instance
      * variable, as JNDI lookups can be expensive as well.
      */
      
    
    private String contextName = "java/ds";

	
  
  @PostConstruct
  public void init() throws NamingException{
	  
	//try {
		if ( null == ctx )
			ctx = new InitialContext();
		setDataSource( (DataSource)ctx.lookup( getContextName() ) ) ;		
//	} catch (NamingException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
	  
  }


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
}
