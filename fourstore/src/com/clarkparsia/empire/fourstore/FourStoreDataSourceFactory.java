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

package com.clarkparsia.empire.fourstore;

import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.Alias;

import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;

import com.clarkparsia.utils.BasicUtils;

import com.clarkparsia.fourstore.impl.StoreFactory;
import com.clarkparsia.fourstore.api.Store;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>Implementation of the {@link DataSourceFactory} interface for creating instances of DataSources
 * backed a 4Store database.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 */
@Alias(FourStoreDataSourceFactory.ALIAS)
public class FourStoreDataSourceFactory implements DataSourceFactory {

	/**
	 * The global alias for this factory
	 */
	public static final String ALIAS = "4store";

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FourStoreDataSource.class.getName());

	/**
	 * Configuration key for the URL of the 4Store http endpoint
	 */
	public static final String KEY_URL = "url";

	/**
	 * Configuration key for setting the soft limit of the 4store instance which is used for query answering
	 */
	public static final String KEY_SOFT_LIMIT = "soft.limit";

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, Object> theMap) {
		return theMap.containsKey(KEY_URL) && BasicUtils.isURL(theMap.get(KEY_URL).toString());
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, Object> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException();
		}

		String aURL = theMap.get(KEY_URL).toString();

		try {
			Store aStore = StoreFactory.create(new URL(aURL));

			if (theMap.containsKey(KEY_SOFT_LIMIT)) {
				try {
					aStore.setSoftLimit(Integer.parseInt(theMap.get(KEY_SOFT_LIMIT).toString()));
				}
				catch (NumberFormatException e) {
					LOGGER.warn("Invalid soft limit value specified: " + theMap.get(KEY_SOFT_LIMIT));
				}
			}
			else {
				// this is our default soft limit
				aStore.setSoftLimit(100000);
			}

			return new FourStoreDataSource(aStore);
		}
		catch (MalformedURLException e) {
			LOGGER.error("Creating URL for 4store failed.", e);

			throw new DataSourceException(e);
		}
	}
}
