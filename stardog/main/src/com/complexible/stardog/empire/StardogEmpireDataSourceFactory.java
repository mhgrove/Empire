/*
 * Copyright (c) 2009-2015 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.stardog.empire;

import java.util.Map;

import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.DataSourceFactory;
import com.complexible.stardog.api.ConnectionConfiguration;

/**
 * <p>Implementation of the {@link DataSourceFactory} interface for creating Stardog connection objects.</p>
 *
 * @author  Hector Perez-Urbina
 * @since   0.9.0
 * @version 0.9.0
 */
@Alias(StardogEmpireDataSourceFactory.ALIAS)
public class StardogEmpireDataSourceFactory implements DataSourceFactory, StardogEmpireFactoryKeys {
	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, Object> theMap) {
		return theMap.containsKey(URL);
	}

	/**
	 * @inheritDoc
	 */
	public StardogEmpireDataSource create(final Map<String, Object> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException("Invalid configuration map: " + theMap);
		}

		try {
			String aConnURL = (String) theMap.get(URL);

			ConnectionConfiguration connConf = ConnectionConfiguration.from(aConnURL);
			
			StardogEmpireDataSource aDataSource = new StardogEmpireDataSource(connConf);
			
			if (theMap.containsKey(AUTO_COMMIT)) {
				System.out.println("Auto commit is no longer supported");
			}
			
			return aDataSource;
			
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}
}
