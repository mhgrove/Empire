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

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;

import com.clarkparsia.utils.BasicUtils;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.UnknownRepositoryException;
import org.openrdf.sesame.config.ConfigurationException;

import org.openrdf.sesame.repository.SesameService;
import org.openrdf.sesame.repository.local.LocalService;

/**
 * <p>Provides a factory for creating EntityManagers backed by SesameDataSources</p>
 *
 * @author Michael Grove
 */
public class SesameDataSourceFactory implements DataSourceFactory {

	/**
	 * Configuration key for the URL of the sesame service
	 */
	public static final String URL = "url";

	/**
	 * Configuration key for the name of the sesame repository
	 */
	public static final String REPO = "repo";

	/**
	 * Configuration key for the username of the sesame repository
	 */
	public static final String USER = "username";

	/**
	 * Configuration key for the password of the sesame repository
	 */
	public static final String PASSWORD = "password";

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, String> theMap) {
		String aURL = theMap.get(URL);
		String aRepo = theMap.get(REPO);
		String aUser = theMap.get(USER);
		String aPW = theMap.get(PASSWORD);

		return (aURL == null || (aURL != null && BasicUtils.isURL(aURL)))
				&& (aRepo != null && !aRepo.equals(""))
				&& ((aUser == null && aPW == null) || (aUser != null && aPW != null && !aUser.equals("") && !aPW.equals("")));
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException("Invalid parameter map");
		}

		String aUser = theMap.get(USER);
		String aPW = theMap.get(PASSWORD);
		String aURL = theMap.get(URL);

		SesameService aService = null;

		try {
			aService = aURL != null ? Sesame.getService(new java.net.URL(aURL)) : Sesame.getService();

			if (aUser != null && aPW != null) {
				aService.login(aUser, aPW);
			}

			return new SesameDataSource(aService.getRepository(theMap.get(REPO)));
		}
		catch (UnknownRepositoryException e) {
			if (aService instanceof LocalService) {
				try {
					return new SesameDataSource(((LocalService)aService).createRepository(theMap.get(REPO), false));
				}
				catch (ConfigurationException ex) {
					throw new DataSourceException(ex);
				}
			}
			else {
				throw new DataSourceException(e);	
			}
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}
}
