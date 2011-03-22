/*
 * Copyright (c) 2009-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.google.inject.internal.Sets;

import java.lang.instrument.Instrumentation;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

/**
 * <p>Simple -javaagent compatible instrumentor.  Provides a premain implementation which grabs a handle to the JVM {@link Instrumentation} and keeps a reference
 * to it for use later on.</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
public final class Instrumentor {

	/**
	 * The JVM instrumentation
	 */
	private static Instrumentation INSTANCE;

	/**
	 * Return the JVM instrumentation
	 * @return the instrumentation or null if this was not used as the java agent
	 */
	public static Instrumentation instrumentation() {
		return INSTANCE;
	}

	/**
	 * Premain method for use with -javaagent.
	 * @param theAgentArgs the cmd line args
	 * @param theJVMInst the JVM instrumentation
	 */
	public static void premain(final String theAgentArgs, final Instrumentation theJVMInst) {
		INSTANCE = theJVMInst;
	}

	/**
	 * Return all the classes loaded into the JVM which extend from the provided class
	 * @param theClass the class
	 * @param <T> the base class type
	 * @return all the classes extending from the parameter. An empty collection will be returned if this java agent is not installed
	 */
	public static <T> Collection<Class<? extends T>> instancesOf(Class<T> theClass) {
		Instrumentation aInst = instrumentation();

		if (aInst == null) {
			return Sets.newHashSet();
		}

		Set<Class<? extends T>> aClasses = Sets.newHashSet();

		for (Class<?> aCls : aInst.getAllLoadedClasses()) {
			if (theClass.isAssignableFrom(aCls)) {
				aClasses.add((Class<T>) aCls);
			}
		}

		return aClasses;
	}

	/**
	 * Return all the classes which have the given annotation applied to them
	 * @param theAnnotation the annotation
	 * @return the classes with the annotation.  An empty collection will be returned if this java agent is not installed
	 */
	public static Collection<Class<?>> annotatedWith(Class<? extends Annotation> theAnnotation) {
		Instrumentation aInst = instrumentation();
		if (aInst == null) {
			return Sets.newHashSet();
		}

		Set<Class<?>> aClasses = Sets.newHashSet();

		for (Class<?> aCls : aInst.getAllLoadedClasses()) {
			if (aCls.getAnnotation(theAnnotation) != null) {
				aClasses.add(aCls);
			}
		}

		return aClasses;
	}

    /**
     * Return whether or not the JVM instrumentation has been initialized via the stardog JVM agent
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return INSTANCE != null;
    }
}
