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

package com.complexible.stardog.concurrent;

import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.DataSourceFactory;
import com.complexible.common.base.Option;
import com.complexible.common.base.Options;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.empire.StardogEmpireDataSource;
import com.complexible.stardog.empire.StardogEmpireFactoryKeys;

import java.util.Map;

/**
 * <p>Implementation of the {@link com.clarkparsia.empire.ds.DataSourceFactory} interface for creating Stardog connection objects.</p>
 *
 * @author  Anand
 * @since   0.9.0
 * @version 0.9.0
 */
@Alias(PooledStardogEmpireDataSourceFactory.ALIAS)
public class PooledStardogEmpireDataSourceFactory implements DataSourceFactory, StardogEmpireFactoryKeys {

    private static final String STARDOG_POOL_MIN = "stardog.pool.min";
    private static final String STARDOG_POOL_MAX = "stardog.pool.max";
    private static final String STARDOG_POOL_EXP = "stardog.pool.expiration.ms";
    private static final String STARDOG_POOL_BLOCK = "stardog.pool.block.at.ms";
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
            Options opts = Options.create();

            if(theMap.containsKey(STARDOG_POOL_MIN) && theMap.get(STARDOG_POOL_MIN) != null) {
                Integer minPool = Integer.parseInt(theMap.get(STARDOG_POOL_MIN).toString());
                opts.set(Option.create(PooledStardogEmpireDataSource.STARDOG_POOL_MIN, PooledStardogEmpireDataSource.STARDOG_POOL_MIN_DEF), minPool);
            }

            if(theMap.containsKey(STARDOG_POOL_MAX) && theMap.get(STARDOG_POOL_MAX) != null) {
                Integer maxPool = Integer.parseInt(theMap.get(STARDOG_POOL_MAX).toString());
                opts.set(Option.create(PooledStardogEmpireDataSource.STARDOG_POOL_MAX, PooledStardogEmpireDataSource.STARDOG_POOL_MAX_DEF), maxPool);
            }

            if(theMap.containsKey(STARDOG_POOL_EXP) && theMap.get(STARDOG_POOL_EXP) != null) {
                Long expiration = Long.parseLong(theMap.get(STARDOG_POOL_EXP).toString());
                opts.set(Option.create(PooledStardogEmpireDataSource.STARDOG_POOL_EXP, PooledStardogEmpireDataSource.STARDOG_POOL_EXP_DEF), expiration);
            }

            if(theMap.containsKey(STARDOG_POOL_BLOCK) && theMap.get(STARDOG_POOL_BLOCK) != null) {
                Long blockAt = Long.parseLong(theMap.get(STARDOG_POOL_BLOCK).toString());
                opts.set(Option.create(PooledStardogEmpireDataSource.STARDOG_POOL_BLOCK, PooledStardogEmpireDataSource.STARDOG_POOL_BLOCK_DEF), blockAt);
            }

            connConf.with(opts);
			
			StardogEmpireDataSource aDataSource = new PooledStardogEmpireDataSource(connConf);
			
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
