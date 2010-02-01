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

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;

/**
 * <p>Implementation of the Guice {@link TypeListener} interface which will scan classes being created by Guice
 * to see if they are eligible for injection based on {@link PersistenceContext} annotations.</p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class PersistenceContextTypeListener implements TypeListener {
    /**
     * @inheritDoc
     */
    public <I> void hear(final TypeLiteral<I> theTypeLiteral, final TypeEncounter<I> theTypeEncounter) {
        for (Field aField : theTypeLiteral.getRawType().getDeclaredFields()) {
            if (aField.getType() == EntityManager.class &&
                aField.isAnnotationPresent(PersistenceContext.class)) {
                theTypeEncounter.register(new PersistenceContextInjector<I>(aField));
            }
        }
    }
}
