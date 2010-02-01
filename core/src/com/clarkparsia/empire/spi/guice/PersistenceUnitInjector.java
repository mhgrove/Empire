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
import com.clarkparsia.empire.spi.EmpirePersistenceProvider;
import com.google.inject.MembersInjector;

import javax.persistence.PersistenceUnit;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import javax.sql.DataSource;

import java.lang.reflect.Field;

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * <p>Implementation of the Guice {@link MembersInjector} interface that injects values,
 * {@link javax.persistence.EntityManagerFactory} instances, based on the presence of the {@link PersistenceUnit}
 * annotation.</p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class PersistenceUnitInjector<T> implements MembersInjector<T> {
    /**
     * The field to inject values to
     */
    private Field mField;

    /**
     * Create a new PersistenceUnitInjector
     * @param theField the field values will be injected to
     */
    public PersistenceUnitInjector(final Field theField) {
        mField = theField;
    }

    /**
     * @inheritDoc
     */
    public void injectMembers(final T theT) {
        boolean isAccessible = mField.isAccessible();

        mField.setAccessible(true);

        PersistenceUnit aUnit = mField.getAnnotation(PersistenceUnit.class);

        try {
            mField.set(theT,
					   Empire.get().persistenceProvider().createContainerEntityManagerFactory(new EmpirePersistenceUnitInfo(aUnit.unitName()),
																							  new HashMap<String, String>()));
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        finally {
            mField.setAccessible(isAccessible);
        }
    }

    /**
     * <p>Minimal implementation of the {@link PersistenceUnitInfo} interface.</p>
     * @since 0.6
     */
    private class EmpirePersistenceUnitInfo implements PersistenceUnitInfo {
        /**
         * the name of the unit
         */
        private String mName;

        /**
         * The collection of registered class transformers
         */
        private Collection<ClassTransformer> mTransformers = new HashSet<ClassTransformer>();

        /**
         * Create a new EmpirePersistenceUnitInfo
         * @param theName the name of the per
         */
        private EmpirePersistenceUnitInfo(final String theName) {
            mName = theName;
        }

        /**
         * @inheritDoc
         */
        public String getPersistenceUnitName() {
            return mName;
        }

        /**
         * @inheritDoc
         */
        public String getPersistenceProviderClassName() {
            return EmpirePersistenceProvider.class.getName();
        }

        /**
         * @inheritDoc
         */
        public PersistenceUnitTransactionType getTransactionType() {
            return PersistenceUnitTransactionType.RESOURCE_LOCAL;
        }

        /**
         * @inheritDoc
         */
        public DataSource getJtaDataSource() {
            return null;
        }

        /**
         * @inheritDoc
         */
        public DataSource getNonJtaDataSource() {
            return null;
        }

        /**
         * @inheritDoc
         */
        public List<String> getMappingFileNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @inheritDoc
         */
        public List<URL> getJarFileUrls() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @inheritDoc
         */
        public URL getPersistenceUnitRootUrl() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @inheritDoc
         */
        public List<String> getManagedClassNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @inheritDoc
         */
        public boolean excludeUnlistedClasses() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @inheritDoc
         */
        public Properties getProperties() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * @inheritDoc
         */
        public ClassLoader getClassLoader() {
            return this.getClass().getClassLoader();
        }

        /**
         * @inheritDoc
         */
        public void addTransformer(final ClassTransformer theClassTransformer) {
            mTransformers.add(theClassTransformer);
        }

        /**
         * @inheritDoc
         */
        public ClassLoader getNewTempClassLoader() {
            return this.getClass().getClassLoader();
        }
    }
}
