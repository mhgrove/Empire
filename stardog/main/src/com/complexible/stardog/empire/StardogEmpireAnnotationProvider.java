/*
 * Copyright (c) 2009-2015 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.stardog.empire;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.clarkparsia.empire.util.EmpireAnnotationProvider;

/**
 * @author  Evren Sirin
 * @version 0.9.0
 * @since   0.9.0
 */
public class StardogEmpireAnnotationProvider implements EmpireAnnotationProvider {

	private static Map<Class<? extends Annotation>,Collection<Class<?>>> mAnnotatedClasses = new HashMap<Class<? extends Annotation>,Collection<Class<?>>>();
	
	public static void setAnnotatedClasses(Class<? extends Annotation> theAnnotation, Collection<Class<?>> theClasses) {
		mAnnotatedClasses.put(theAnnotation, theClasses);
	}
	
	@Override
	public Collection<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> theAnnotation) {
		return mAnnotatedClasses.containsKey(theAnnotation)? mAnnotatedClasses.get(theAnnotation) 
														   : Collections.<Class<?>>emptySet();
	}
}
