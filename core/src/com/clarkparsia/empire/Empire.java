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
import com.clarkparsia.empire.util.EmpireModule;
import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.utils.Predicate;
import com.clarkparsia.utils.NamespaceUtils;

import static com.clarkparsia.utils.collections.CollectionUtil.find;

import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;

/**
 * <p>Access class for the RDF ORM/JPA layer to get the local {@link Empire} instance.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.5
 */
public class Empire {

	/**
	 * "the" instance of Empire
	 */
	private static Empire INSTANCE;

	/**
	 * The Guice injector used by Empire
	 */
	private static Injector injector;

	/**
	 * The EmpirePersistenceProvider
	 */
	private EmpirePersistenceProvider mProvider;

	/**
	 * The EmpireAnnotationProvider
	 */
	private EmpireAnnotationProvider mAnnotationProvider;

	/**
	 * The collection of installed modules in Empire.  We only allow one module for each type.  If you install another
	 * module of the same type later on, it will overwrite the previous module.
	 */
	private static Map<Class, Module> mModules = new HashMap<Class, Module>();

	static {
		// add default namespaces
		NamespaceUtils.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		NamespaceUtils.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		NamespaceUtils.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
	}

	/**
	 * Get a handle to Empire for the current thread
	 * @return Empire
	 */
	public static Empire get() {
		if (INSTANCE == null) {
			INSTANCE = injector().getInstance(Empire.class);
		}

		return INSTANCE;
	}

	/**
	 * Create a new Empire instance
	 * @param theProvider the persistence provider to use
	 * @param theAnnotationProvider the annotation provider to use
	 */
	@Inject
	public Empire(EmpirePersistenceProvider theProvider, EmpireAnnotationProvider theAnnotationProvider) {
		mProvider = theProvider;
		mAnnotationProvider = theAnnotationProvider;
	}

	/**
	 * Return the current PersistenceProvider for this instance of Empire
	 * @return the persistance provider
	 */
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

	/**
	 * Initialize Empire with the given configuration
	 * @param theConfig the container configuration for Empire
	 */
	public static void init(EmpireConfiguration theConfig) {
		init(new DefaultEmpireModule(theConfig));
	}

	/**
	 * Initialize Empire with the given configuration
	 * @param theConfig the container configuration for Empire
	 * @param theModules the modules to use with Empire
	 */
	public static void init(EmpireConfiguration theConfig, EmpireModule... theModules) {
		Collection<EmpireModule> aModules = new HashSet<EmpireModule>(Arrays.asList(theModules));

		if (!find(aModules, new FindDefaultEmpireModulePredicate())) {
			aModules.add(new DefaultEmpireModule(theConfig));
		}

		init(aModules.toArray(new EmpireModule[aModules.size()]));
	}

	/**
	 * Initialize Empire with the given set of Guice Modules
	 * @param theModules the modules to use with Empire
	 */
	public static void init(EmpireModule... theModules) {
		mModules.clear();

		Collection<EmpireModule> aModules = new HashSet<EmpireModule>(Arrays.asList(theModules));

		if (!find(aModules, new FindDefaultEmpireModulePredicate())) {
			aModules.add(new DefaultEmpireModule());
		}

		// keep track of the modules we've "installed"
		for (Module aModule : aModules) {
			mModules.put(aModule.getClass(), aModule);
		}
		
		injector = Guice.createInjector(mModules.values());
	}

	/**
	 * Create an instance of the given class in the current Empire context.  The provided class usually should
	 * have a default constructor, but if all of its constructor parameters are marked with @Inject and appropriately
	 * instantiated from a plugin module, that is also sufficient.
	 * @param theClass the class to create
	 * @param <T> the type of object that will be created
	 * @return the new instance
	 */
	public <T> T instance(Class<T> theClass) {
		return injector().getInstance(theClass);
	}

	/**
	 * Predicate to use for finding an instance of {@link DefaultEmpireModule}
	 */
	private static class FindDefaultEmpireModulePredicate implements Predicate<EmpireModule> {
		/**
		 * @inheritDoc
		 */
		public boolean accept(EmpireModule theModule) {
			return theModule instanceof DefaultEmpireModule;
		}
	}

	/**
	 * Return the Guice injector.
	 * @return the Guice injector for Empire
	 */
	private static Injector injector() {
		if (injector == null) {
			injector = Guice.createInjector(new DefaultEmpireModule());
		}

		return injector;
	}
}
