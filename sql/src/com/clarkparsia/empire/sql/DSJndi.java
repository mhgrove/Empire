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
