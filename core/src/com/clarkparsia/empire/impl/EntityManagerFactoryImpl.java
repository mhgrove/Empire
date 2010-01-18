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

package com.clarkparsia.empire.impl;

import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSourceException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.net.ConnectException;

/**
 * <p>Implementation of the JPA {@link EntityManagerFactory} class to support creating Empire based
 * {@link EntityManager EntityManagers}.  Uses an instance of a {@link com.clarkparsia.empire.DataSourceFactory} to dynamically
 * create the DataSource used by the new EntityManager on the fly.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class EntityManagerFactoryImpl implements EntityManagerFactory {
	public static final String FACTORY = "factory";

	/**
	 * Whether or not this EntityManagerFactory is open.
	 */
	private boolean mIsOpen = true;

	/**
	 * The list of EntityManager's created by this factory.
	 */
	private Collection<EntityManager> mManagers;

	/**
	 * Create a new AbstractEntityManagerFactory
	 */
	public EntityManagerFactoryImpl() {
		mManagers = new HashSet<EntityManager>();
	}

	/**
	 * Create a new instance of an {@link EntityManager} based on the parameters in the map
	 * @param theMap the data to use to create the new EntityManager
	 * @return a new EntityManager
	 * @throws IllegalArgumentException thrown if the map does not contain the required set of properties to
	 * create a new instance of the EntityManager
	 */
	protected EntityManager newEntityManager(Map theMap) {
		// TODO: should we re-use data source's here when possible?
		DataSourceFactory aFactory = null;

		if (theMap.containsKey(FACTORY)) {
			// TODO: this could be cleaner...
			try {
				aFactory = (DataSourceFactory) Class.forName(theMap.get(FACTORY).toString()).newInstance();
			}
			catch (Throwable e) {
				throw new IllegalArgumentException("There was an error while instantiating the DataSourceFactory");
			}
		}
		else {
			throw new IllegalArgumentException("No data source factory specified");
		}

		try {
			DataSource aSource = aFactory.create(theMap);

			if (!(aSource instanceof MutableDataSource)) {
				throw new IllegalArgumentException("Cannot use Empire with a non-mutable Data source");
			}

			aSource.connect();

			return new EntityManagerImpl( (MutableDataSource) aSource);
		}
		catch (ConnectException e) {
			throw new IllegalStateException("Could not connect to the data source");
		}
		catch (DataSourceException e) {
			throw new IllegalArgumentException("There was an error creating the data source for the new EntityManager", e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public EntityManager createEntityManager() {
		return createEntityManager(new HashMap());
	}

	/**
	 * @inheritDoc
	 */
	public EntityManager createEntityManager(final Map theMap) {
		assertOpen();

		EntityManager aManager = newEntityManager(theMap);

		mManagers.add(aManager);

		return aManager;
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		mIsOpen = false;

		cleanup();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isOpen() {
		return mIsOpen;
	}

	/**
	 * Free up all resources used by this EntityManagerFactory.  This includes closing all EntityManager's created
	 * by this factory.
	 */
	protected void cleanup() {
		for (EntityManager aManager : mManagers) {
			if (aManager.isOpen()) {
				aManager.close();
			}
		}
	}

	/**
	 * Enforce that this factory is open before allowing any operations to be performed on it
	 * @throws IllegalStateException thrown if the factory is not open
	 */
	private void assertOpen() {
		if (!isOpen()) {
			throw new IllegalStateException("EntityManagerFactory is not open");
		}
	}
}
