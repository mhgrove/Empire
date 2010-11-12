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

package com.clarkparsia.empire.annotation;

import com.clarkparsia.empire.util.BeanReflectUtil;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedFields;
import static com.clarkparsia.empire.util.BeanReflectUtil.getAnnotatedGetters;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.EmpireException;
import com.clarkparsia.empire.EmpireOptions;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

/**
 * <p>Utility class for checking that a class has the required annotations on it and that it's annotations are used
 * correctly.  For example, OneToMany annotations should be used on a field or method that returns a Collection,
 * and all classes need an {@link RdfsClass} annotation.</p>
 *
 * @author Michael Grove
 * @author uoccou
 * @since 0.6.5
 * @version 0.7
 */
public class AnnotationChecker {
	/**
	 * Return whether or not the class has the minimum set of annotations required for use by Empire and that all
	 * other annotations, such as OneToMany, are used correctly.
	 * @param theClass the class to validate.
	 * @throws com.clarkparsia.empire.EmpireException if
	 */
	public static void assertValid(final Class theClass) throws EmpireException {
		if (!isEmpireCompatible(theClass)) {
			throw new EmpireException("Missing a required annotation (Entity & RdfsClass) or does not implement SupportsRdfId");
		}

		Collection<AccessibleObject> aAccessors = new HashSet<AccessibleObject>();
		aAccessors.addAll(getAnnotatedFields(theClass));
		aAccessors.addAll(getAnnotatedGetters(theClass, true));

		for (AccessibleObject aObj : aAccessors) {
			if (aObj.getAnnotation(OneToMany.class) != null ||
				aObj.getAnnotation(ManyToMany.class) != null) {
				Class aType = aObj instanceof Field
							  ? ((Field)aObj).getType()
							  : ((Method)aObj).getReturnType();

				if (!Collection.class.isAssignableFrom(aType)) {
					throw new EmpireException("Using OneToMany or ManyToMany annotation on a non-collection field : " + theClass + "." + aType);
				}
			}
		}
	}

	/**
	 * Equivalent to {@link #assertValid} but returns the validity as a boolean rather than throwing an exception.
	 * @param theClass the class to validate
	 * @return true if the class is valid, false otherwise
	 */
	public static boolean isValid(final Class theClass) {
		try {
			assertValid(theClass);
			return true;
		}
		catch (EmpireException e) {
			return false;
		}
	}

	/**
	 * For OWL based restrictions, make sure the values on the java bean respect the annotations placed on the
	 * fields.
	 * @param theObj the object to validate
	 * @throws Exception thrown if the object is not in a valid state wrt to its data.
	 */
	public static void validateState(final Object theObj) throws Exception {
		// TODO: implement me
	}

	/**
	 * Return whether or not the given class instances is compatible with Empire, in that it has all the required
	 * annotations, and can have an rdf:ID.
	 * @param theClass the class to check
	 * @return true if its Empire compatible, false otherwise.
	 */
	private static boolean isEmpireCompatible(final Class theClass) {
		return (!EmpireOptions.ENFORCE_ENTITY_ANNOTATION || BeanReflectUtil.hasAnnotation(theClass, Entity.class)) &&
			   BeanReflectUtil.hasAnnotation(theClass, RdfsClass.class) &&
			   SupportsRdfId.class.isAssignableFrom(theClass);
	}
}
