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

package com.clarkparsia.empire.util;

import java.util.Collection;

import java.lang.annotation.Annotation;

/**
 * <p>Interface to provide the set of classes using a given Annotation.  Only class-level annotations
 * are expected to be indexed by these providers, field & method annotations are not.  Implementations are free to
 * read the index from disk, or calculate it at runtime by the current classpath.  This will be called on Empire
 * start-up.</p>
 *
 * @author Michael Grove
 */
public interface EmpireAnnotationProvider {

	/**
	 * Return all classes in the classpath with the given annotation attached.
	 * @param theAnnotation the annotation to search for
	 * @return the collection of classes with the given annotation
	 */
	public Collection<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> theAnnotation);
}
