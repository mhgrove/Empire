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

import com.clarkparsia.empire.Empire;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.EntityManagerFactory;

import java.util.Map;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class JPAPersistenceProvider implements PersistenceProvider {

	/**
	 * The actual persistence provider
	 */
	private EmpirePersistenceProvider mPersistenceProvider;

	/**
	 * @inheritDoc
	 */
	public EntityManagerFactory createEntityManagerFactory(final String theName, final Map theMap) {
		return getPersistenceProvider().createEntityManagerFactory(theName, theMap);
	}

	/**
	 * @inheritDoc
	 */
	public EntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo thePersistenceUnitInfo, final Map theMap) {
		return getPersistenceProvider().createContainerEntityManagerFactory(thePersistenceUnitInfo, theMap);
	}

	/**
	 * Return the current EmpirePersistenceProvider
	 * @return the Empire persistence provider
	 */
	private PersistenceProvider getPersistenceProvider() {
		if (mPersistenceProvider == null) {
			mPersistenceProvider = Empire.get().persistenceProvider();
		}

		return mPersistenceProvider;
	}
}
