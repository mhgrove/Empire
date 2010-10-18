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

package com.clarkparsia.empire.spi.guice;

import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.Empire;

import com.google.inject.MembersInjector;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.EntityManagerFactory;

import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Implementation of the Guice {@link MembersInjector} interface that injects {@link javax.persistence.EntityManager}
 * instances into fields based on the {@link PersistenceContext} annotation.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.7
 */
public class PersistenceContextInjector<T> implements MembersInjector<T> {
    /**
     * Field to inject the value to
     */
    private Field mField;

    /**
     * Create a new PersistenceContextInjector
     * @param theField the field values will be injected to
     */
    PersistenceContextInjector(final Field theField) {
        mField = theField;
    }

    /**
     * @inheritDoc
     */
    public void injectMembers(final T theT) {
        boolean isAccessible = mField.isAccessible();

        mField.setAccessible(true);

        PersistenceContext aContext = mField.getAnnotation(PersistenceContext.class);

        Map<String, String> aMap = new HashMap<String, String>();
        for (PersistenceProperty aProp : aContext.properties()) {
            aMap.put(aProp.name(), aProp.value());
        }

        try {
			EntityManagerFactory aFactory = Empire.get().persistenceProvider().createEntityManagerFactory(aContext.name(), aMap);
			if (aFactory == null) {
				throw new RuntimeException("Factory incorrectly initialized, or a factory provided that does not exists was specified.  Cannot create EntityManager");
			}

            mField.set(theT, aFactory.createEntityManager());
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        finally {
            mField.setAccessible(isAccessible);
        }
    }
}
