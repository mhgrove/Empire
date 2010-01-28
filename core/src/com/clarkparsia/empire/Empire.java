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

package com.clarkparsia.empire;

import com.clarkparsia.empire.spi.EmpirePersistenceProvider;
import com.clarkparsia.empire.util.EmpireAnnotationProvider;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Inject;

import javax.persistence.EntityManager;

import java.util.HashMap;

/**
 * <p>Access class for the RDF ORM/JPA layer to get the local {@link EntityManager} instance.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class Empire {

//	/**
//	 * A thread-local reference to an instance of Empire
//	 */
//	private static ThreadLocal<Empire> mLocalInst = new ThreadLocal<Empire>();
//
//	/**
//	 * The current EntityManager
//	 */
//	private EntityManager mEntityManager;
//
//	/**
//	 * Create a new Empire instance with the given {@link EntityManager}
//	 * @param theEntityManager the entity manager
//	 */
//	private Empire(final EntityManager theEntityManager) {
//		mEntityManager = theEntityManager;
//	}
//
//	/**
//	 * Return whether or not Empire has been initialized for the local thread context
//	 * @return true if it has been initialized, false otherwise
//	 */
//	public static boolean isInitialized() {
//		return mLocalInst.get() != null;
//	}
//
//	/**
//	 * Return the thread local entity manager
//	 * @return the entity manager
//	 */
//	static Empire get() {
//		Empire aEmpire = mLocalInst.get();
//		if (aEmpire == null) {
//			throw new IllegalStateException("Empire not initialized.");
//		}
//
//		return aEmpire;
//	}
//
//	/**
//	 * Return the current Empire EntityManager
//	 * @return the current EntityManager
//	 */
//	public static EntityManager em() {
//		return get().getEntityManager();
//	}
//
//	/**
//	 * Create an instance of Empire using the specified {@link EntityManager}.  All subsequent operations will be performed
//	 * on the given EntityManager
//	 * @param theManager the enw EntityManager
//	 * @return this instance
//	 */
//	public static Empire create(EntityManager theManager) {
//		if (mLocalInst.get() != null) {
//			mLocalInst.get().em().close();
//
//			mLocalInst.remove();
//		}
//
//		Empire aEmpire = new Empire(theManager);
//
//		mLocalInst.set(aEmpire);
//
//		return aEmpire;
//	}
//
//	/**
//	 * Return the current {@link EntityManager}.
//	 * @return the entity manager
//	 */
//	private EntityManager getEntityManager() {
//		return mEntityManager;
//	}
//
//	/**
//	 * Close Empire
//	 */
//	public static void close() {
//		if (mLocalInst.get() != null) {
//			if (mLocalInst.get().em().isOpen()) {
//				mLocalInst.get().em().close();
//			}
//
//			mLocalInst.remove();
//		}
//	}


	////

	private static ThreadLocal<Empire> mLocalInst = new ThreadLocal<Empire>();
	public static void close() {
		if (mLocalInst.get() != null) {
			mLocalInst.remove();
		}
	}
	public static Empire get() {
		Empire aEmpire = mLocalInst.get();
		if (aEmpire == null) {
			aEmpire = injector.getInstance(Empire.class);
			mLocalInst.set(aEmpire);
		}

		return aEmpire;
	}

	private EmpirePersistenceProvider mProvider;
	private EmpireAnnotationProvider mAnnotationProvider;

	public EmpirePersistenceProvider persistenceProvider() {
		return mProvider;
	}

	/**
	 * Return the {@link EmpireAnnotationProvider} to use to get information about Annotations in the system.
	 * @return the EmpireAnnotationProvider
	 */
	public EmpireAnnotationProvider getAnnotationProvider() {
		return mAnnotationProvider;
	}

	@Inject
	Empire(EmpirePersistenceProvider theProvider, EmpireAnnotationProvider theAnnotationProvider) {
		mProvider = theProvider;
		mAnnotationProvider = theAnnotationProvider;
	}

	private static Injector injector = Guice.createInjector(new DefaultEmpireModule());


	public static void init(Module... theModules) {
		close();
		
		injector = Guice.createInjector(theModules);
	}
}
