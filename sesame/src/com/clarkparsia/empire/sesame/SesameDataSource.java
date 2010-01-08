/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire.sesame;

import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.QueryException;
import com.clarkparsia.empire.DataSourceException;

import com.clarkparsia.empire.impl.AbstractDataSource;

import org.openrdf.sesame.repository.SesameRepository;

import org.openrdf.model.Graph;

import java.net.ConnectException;
import java.net.URI;

import com.clarkparsia.sesame.repository.ExtendedSesameRepository;
import com.clarkparsia.sesame.utils.SesameValueFactory;
import com.clarkparsia.sesame.utils.query.SesameQuery;

/**
 * <p>Implemention of the DataSource interface backed by a Sesame Repository.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class SesameDataSource extends AbstractDataSource implements MutableDataSource {

	/**
	 * The Sesame Repository that backs this DataSource
	 */
	private ExtendedSesameRepository mRepo;

	/**
	 * Create a new SesameDataSource
	 * @param theRepo the underlying repository
	 */
	public SesameDataSource(final SesameRepository theRepo) {
		this(new ExtendedSesameRepository(theRepo));
	}

	/**
	 * Create a new SesameDataSource
	 * @param theRepo the underyling Sesame repository
	 */
	public SesameDataSource(final ExtendedSesameRepository theRepo) {
		mRepo = theRepo;
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		setConnected(true);
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		setConnected(false);
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		try {
			return new SesameResultSet(mRepo.performSelectQuery(SesameQuery.serql(theQuery)));
		}
		catch (Exception ex) {
			throw new QueryException(ex);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		try {
			return mRepo.performConstructQuery(SesameQuery.serql(theQuery));
		}
		catch (Exception e) {
			throw new QueryException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final URI theURI) throws DataSourceException {
		return mRepo.describe(SesameValueFactory.instance().createURI(theURI));
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		try {
			mRepo.addGraph(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		try {
			// remember that sesame is fantastically, ungodly slow for remove operations.  This will be an expensive
			// call for all but the most trivially small KBs.
			mRepo.removeGraph(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}
}
