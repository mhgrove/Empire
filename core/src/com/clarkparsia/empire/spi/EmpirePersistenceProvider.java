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

package com.clarkparsia.empire.spi;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.impl.EntityManagerFactoryImpl;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Implementation of the JPA {@link PersistenceProvider} interface.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.6.2
 */
public class EmpirePersistenceProvider implements PersistenceProvider {
    // TODO: should we keep factories created so that to factories created w/ the same name are == ?

	/**
	 * Current DataSourceFactory "plugins"
	 */
    private final Set<DataSourceFactory> mFactories;

	/**
	 * Application container configuration
	 */
    private final EmpireConfiguration mContainerConfig;

	/**
	 * Key constant for finding the class name of the factory we want to create
	 */
    public static final String FACTORY = "factory";

	/**
	 * Create a new EmpirePersistenceProvider
	 * @param theFactories the list of DataSourceFactory objects available
	 * @param theContainerConfig the current empire configuration
	 */
    @Inject
	EmpirePersistenceProvider(Set<DataSourceFactory> theFactories,
							  @Named("ec") EmpireConfiguration theContainerConfig) {
        mFactories = theFactories;
        mContainerConfig = theContainerConfig;
    }

	/**
	 * @inheritDoc
	 */
    public EntityManagerFactory createEntityManagerFactory(final String theUnitName, final Map theMap) {
        Map<String, String> aConfig = new HashMap<String, String>();

		if (mContainerConfig.hasUnit(theUnitName)) {
			aConfig.putAll(mContainerConfig.getUnitConfig(theUnitName));
		}

		if (theMap != null) {
        	aConfig.putAll(mapOfStrings(theMap));
		}

        if (!aConfig.containsKey(FACTORY)) {
            return null;
        }

		// TODO: is there a better way to do this than having the user encode the fully qualified class name of the factory?
        final String aName = aConfig.get(FACTORY);

        for (DataSourceFactory aFactory  : mFactories) {
            if (aFactory.getClass().getName().equals(aName)) {
                return new EntityManagerFactoryImpl(aFactory, aConfig);
            }
        }

        return null;
    }

	/**
	 * @inheritDoc
	 */
    public EntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo thePersistenceUnitInfo,
                                                                    final Map theMap) {
        // TODO: there's a lot more options on PersistenceUnitInfo that we can use here.
        return createEntityManagerFactory(thePersistenceUnitInfo.getPersistenceUnitName(), theMap);
    }

	/**
	 * Convert a Map to a Map of String's
	 * @param theMap the map to convert
	 * @return the original map copied as a map of strings (calling toString on all keys and values).
	 */
    private Map<String, String> mapOfStrings(Map theMap) {
        Map<String, String> aMap = new HashMap<String, String>();

        for (Object aKey : theMap.keySet()) {
            aMap.put(aKey.toString(), theMap.get(aKey).toString());
        }

        return aMap;
    }
}
