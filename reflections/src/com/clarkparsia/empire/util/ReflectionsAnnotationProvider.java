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


import org.reflections.Reflections;

import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;

import java.util.Collection;
import java.lang.annotation.Annotation;

/**
 * <p>Implementation of the {@link EmpireAnnotationProvider} interface backed by the Reflections API which
 * provides the neccessary annotation information at runtime.</p>
 *
 * @author Michael Grove
 * @since 0.5.2
 * @see <a href="http://code.google.com/p/reflections/">Reflections API</a>
 */
public class ReflectionsAnnotationProvider implements EmpireAnnotationProvider {
	private static Reflections REFLECTIONS;

	static {
		REFLECTIONS = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.getUrlsForCurrentClasspath())
				.setScanners(new FieldAnnotationsScanner(),
							 new MethodAnnotationsScanner(),
							 new TypeAnnotationsScanner()));
	}

	/**
	 * @inheritDoc
	 */
	public Collection<Class<?>> getClassesWithAnnotation(final Class<? extends Annotation> theAnnotation) {
		return REFLECTIONS.getTypesAnnotatedWith(theAnnotation);
	}
}
