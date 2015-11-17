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

import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.EmpireGenerated;
import com.complexible.common.util.PrefixMapping;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.lang.annotation.Annotation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>Some utility methods which use the Java reflect stuff to do a lot of the runtime accessing of fields and methods
 * on objects used to transform between Java and RDF.</p>
 *
 * @author  Michael Grove
 * @since   0.5.1
 * @version 0.7
 */
public final class BeanReflectUtil {

	/**
	 * Small cache so we don't have to recalcuation information via java.lang.reflect every time, which can be expensive
	 */
	private final static Map<Class<?>, BeanReflectCacheEntry> cache = new HashMap<Class<?>, BeanReflectCacheEntry>();

	/**
	 * Cannot create instances of this class
	 */
	private BeanReflectUtil() {
	}

	/**
	 * More or less a more robust version of Class.forName.  Attempts to get around custom class loaders and
	 * different class loaders in the current Thread context by trying *all* of them to load a class.
	 * @param theName the class to load
	 * @return the loaded class
	 * @throws ClassNotFoundException if the class is not found.
	 */
	public static Class<?> loadClass(String theName) throws ClassNotFoundException {
		try {
			return Class.forName(theName);
		}
		catch (ClassNotFoundException e) {
			try {
				return Thread.currentThread().getContextClassLoader().loadClass(theName);
			}
			catch (ClassNotFoundException ex) {
				return ClassLoader.getSystemClassLoader().loadClass(theName);
			}
		}
	}

	/**
	 * Return the field on the class which has an {@link RdfId} annotation.  If the fields on the class do not have the
	 * annotation, the super class will be checked.
	 * @param theClass the class
	 * @return the field with an RdfId annotation, or null if one is not found
	 * @throws InvalidRdfException thrown if there are multiple fields with the RdfId annotation
	 */
	public static Field getIdField(Class theClass) throws InvalidRdfException {
		Field aIdField = null;

		for (Field aField : getAllDeclaredFields(theClass)) {
			if (aField.getAnnotation(RdfId.class) != null) {
				if (aIdField != null) {
					throw new InvalidRdfException("Cannot have multiple id properties");
				}
				else {
					aIdField = aField;
				}
			}
		}

		if (aIdField == null && shouldInspectSuperClass(theClass)) {
			aIdField = getIdField(theClass.getSuperclass());
		}

		return aIdField;
	}

	/**
	 * Return the given annotation from the class.  If the class does not have the annotation, it's parent class and any
	 * interfaces will also be checked.
	 * @param theClass the class to inspect
	 * @param theAnnotation the annotation to retrieve
	 * @return the class's annotation, or it's "inherited" annotation, or null if the annotation cannot be found.
	 */
	public static <T extends Annotation> T getAnnotation(Class<?> theClass, Class<T> theAnnotation) {
		BeanReflectCacheEntry entry = cache.get(theClass);
		if (entry == null) {
			entry = new BeanReflectCacheEntry();
			cache.put(theClass, entry);
		}
		if (entry.mAnnotations.containsKey(theAnnotation)) {
			return (T) entry.mAnnotations.get(theAnnotation);
		}

		T aAnnotation = null;

		if (theClass.isAnnotationPresent(theAnnotation)) {
			aAnnotation = theClass.getAnnotation(theAnnotation);
		}
		else {
			if (shouldInspectSuperClass(theClass)) {
				aAnnotation = getAnnotation(theClass.getSuperclass(), theAnnotation);
			}

			if (aAnnotation == null) {
				for (Class aInt : theClass.getInterfaces()) {
					aAnnotation = getAnnotation(aInt, theAnnotation);

					if (aAnnotation != null) {
						break;
					}
				}
			}
		}

		entry.mAnnotations.put(theAnnotation, aAnnotation);

		return aAnnotation;
	}

	/**
	 * Return whether or not it is ok to inspect the super class of the provided class for persistence information.  This can be done
	 * if there is a super class, and if so, if that superclass is either annotated with the JPA annotation @MappedSuperclass OR the
	 * *current* class is an instance of EmpireGenerated.  In the latter case, this means that the superclass is the actual persistent
	 * object type as EmpireGenerated is just a stub created internally for some bookkeeping purposes.
	 *
	 * @param theClass 	the class
	 * @return			true if its ok to inspect the superclass for persistence information, false otherwise
	 */
	private static boolean shouldInspectSuperClass(final Class theClass) {
		return theClass.getSuperclass() != null
			   && (hasAnnotation(theClass.getSuperclass(), MappedSuperclass.class) || EmpireGenerated.class.isAssignableFrom(theClass));
	}

	/**
	 * Returns a Method on the object with the given annotation
	 * @param theClass the class whose methods should be scanned
	 * @param theAnnotation the annotation to look for
	 * @return a method with the given annotation, or null if one is not found.
	 */
	public static Collection<Method> getAnnotatedMethods(final Class theClass, final Class<? extends Annotation> theAnnotation) {
		Collection<Method> aMethods = new HashSet<Method>();

		for (Method aMethod : theClass.getMethods()) {
			if (aMethod.getAnnotation(theAnnotation) != null) {
				aMethods.add(aMethod);
			}
		}

		if (shouldInspectSuperClass(theClass)) {
			aMethods.addAll(getAnnotatedMethods(theClass.getSuperclass(), theAnnotation));
		}

		for (Class aInterface : theClass.getInterfaces()) {
			aMethods.addAll(getAnnotatedMethods(aInterface, theAnnotation));
		}

		return aMethods;
	}

	/**
	 * Return whether or not the class has the given annotation.  If the class itself does not have the annotation,
	 * it's super class and the interfaces it implements are checked.
	 * @param theClass the class to check
	 * @param theAnnotation the annotation to look for
	 * @return if the class has the annotation, or one of its parents does and it "inherited" the annotation, false otherwise
	 */
	public static boolean hasAnnotation(Class theClass, Class<? extends Annotation> theAnnotation) {
		return getAnnotation(theClass, theAnnotation) != null;
	}

    /**
     * Toggle the accessibility of the parameter, which should be an instance of {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}.  If
     * not, then nothing will occur.
     * @param theAccessor the object toggle accessibility of
     * @param theAccess the new accessibility level, true to make it accessible, false otherwise
     * @return the old accessibility of the accessor
     */
    public static boolean setAccessible(AccessibleObject theAccessor, boolean theAccess) {
        boolean aOldAccess = theAccessor.isAccessible();

		theAccessor.setAccessible(theAccess);

        return aOldAccess;
    }

    /**
     * Set the specified value on the the given object using the provided accessor, either a {@link Field} or a
     * {@link Method}.  If the accessor is neither a field or a method, then no operation takes place.  This method
     * will toggle the accessibility of the accessor so an IllegalAccessException is not thrown.
     * @param theAccessor the accessor, a Field or a Method
     * @param theObj the object to set the value on
     * @param theValue the value to set via the aceessor
     * @throws java.lang.reflect.InvocationTargetException thrown if there was an error setting the value on the field/method
     */
    public static void safeSet(AccessibleObject theAccessor, Object theObj, Object theValue) throws InvocationTargetException {
        boolean aOldAccess = setAccessible(theAccessor, true);

        try {
            if (theAccessor instanceof Field) {
                ((Field) theAccessor).set(theObj, theValue);
            }
            else if (theAccessor instanceof Method) {
                ((Method) theAccessor).invoke(theObj, theValue);
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        finally {
            setAccessible(theAccessor, aOldAccess);
        }
    }

    /**
     * Set the specified value on the the given object using the provided accessor, either a {@link Field} or a
     * {@link Method}.  If the accessor is neither a field or a method, then no operation takes place.
     * @param theAccessor the accessor, a Field or a Method
     * @param theObj the object to set the value on
     * @param theValue the value to set via the aceessor
     * @throws java.lang.reflect.InvocationTargetException thrown if there was an error setting the value on the field/method
     * @throws IllegalAccessException thrown if you cannot access the field/method
     */
    public static void set(AccessibleObject theAccessor, Object theObj, Object theValue) throws InvocationTargetException, IllegalAccessException {
        if (theAccessor instanceof Field) {
            ((Field) theAccessor).set(theObj, theValue);
        }
        else if (theAccessor instanceof Method) {
            ((Method) theAccessor).invoke(theObj, theValue);
        }
    }

    /**
	 * Return the list of annotated setter methods on the class.  Anything with an {@link com.clarkparsia.empire.annotation.RdfProperty} annotation
	 * that returns a (non-void) value will be found by this method.  When the infer flag is set to true, we will
	 * inspect the getter methods, if there is a annotated getter, but no annotated setter for a property, we'll infer
	 * the annotation for the property so you get the expected paired behavior of the annotation.
	 * @param theClass the class
	 * @param theInfer true to infer setters from annotated getters, false otherwise.
	 * @return the list of annotated setter methods
	 */
	public static Collection<Method> getAnnotatedSetters(Class theClass, boolean theInfer) {
		BeanReflectCacheEntry entry = cache.get(theClass);
		
		if (entry == null) {
			entry = new BeanReflectCacheEntry();
			cache.put(theClass, entry);
		}

		if (theInfer && entry.mInferredSetters != null) {
			return entry.mInferredSetters;
		}
		else if (!theInfer && entry.mSetters != null) {
			return entry.mSetters;
		}

		Collection<Method> aMethods = new HashSet<Method>();

		for (Method aMethod : theClass.getDeclaredMethods()) {
			if (aMethod.getAnnotation(RdfProperty.class) != null
				&& (aMethod.getGenericReturnType().equals(Void.class) || aMethod.getGenericReturnType().toString().equals("void"))
				&& aMethod.getParameterTypes().length == 1) {

				aMethods.add(aMethod);
			}
		}

		// now let's infer setters.  if you only applied the annotation to the getter, we'll carry it over to the
		// setter if one exists.

		if (theInfer) {
			Collection<Method> aGetters = getAnnotatedGetters(theClass, false);
			for (Method aGetterMethod : aGetters) {
				String aSetterName = aGetterMethod.getName().replaceFirst("get", "set");
				boolean tryIs = false;

				try {
					Method aSetter = theClass.getMethod(aSetterName, aGetterMethod.getReturnType());

					// so we have a setter for a annotated getter, so here we will add this to the list
					// of setters to infer the annotation on the setter even though its not explicit

					if (!aMethods.contains(aSetter)) {
						aMethods.add(aSetter);
					}
				}
				catch (NoSuchMethodException e) {
					// no biggie, setter doesn't exist, we'll just move on, try looking for isXXXX
					tryIs = true;
				}

				if (tryIs) {
					try {
						Method aSetter = theClass.getMethod(aGetterMethod.getName().replaceFirst("is", "set"), aGetterMethod.getReturnType());

						// so we have a setter for a annotated getter, so here we will add this to the list
						// of setters to infer the annotation on the setter even though its not explicit

						if (!aMethods.contains(aSetter)) {
							aMethods.add(aSetter);
						}
					}
					catch (NoSuchMethodException e) {
						// no biggie, setter doesn't exist, we'll just move on
					}
				}
			}
		}

		if (shouldInspectSuperClass(theClass)) {
			aMethods.addAll(getAnnotatedSetters(theClass.getSuperclass(), theInfer));
		}

		for (Class aInterface : theClass.getInterfaces()) {
			aMethods.addAll(getAnnotatedSetters(aInterface, theInfer));
		}

		if (theInfer) {
			entry.mInferredSetters = aMethods;
		}
		else {
			entry.mSetters = aMethods;
		}

		return aMethods;
	}

	/**
	 * Return the list of annotated get style methods from the class.  Anything w/ an {@link RdfProperty} annotation
	 * that does not return a value will be found by this method.  When the inter flag is set to true, we will inspect
	 * the setter methods, if there is an annotated setter, but no annotatted getter for a property, we'll infer
	 * the annotation for the property so you get the expected paired behavior of the annotation.
	 * @param theClass the class
	 * @param theInfer true to infer getters from annotated setters, false otherwise.
	 * @return the list of annotated get methods
	 */
	public static Collection<Method> getAnnotatedGetters(Class theClass, boolean theInfer) {
		BeanReflectCacheEntry entry = cache.get(theClass);

		if (entry == null) {
			entry = new BeanReflectCacheEntry();
			cache.put(theClass, entry);
		}

		if (theInfer && entry.mInferredGetters != null) {
			return entry.mInferredGetters;
		}
		else if (!theInfer && entry.mGetters != null) {
			return entry.mGetters;
		}

		Map<String, Method> aMethods = new HashMap<String, Method>();

		for (Method aMethod : theClass.getDeclaredMethods()) {
			if (aMethod.getAnnotation(RdfProperty.class) != null
				&& !aMethod.getGenericReturnType().equals(Void.class)
				&& aMethod.getParameterTypes().length == 0) {

				if (!aMethods.containsKey(aMethod.getName())) {
					aMethods.put(aMethod.getName(), aMethod);
				}
			}
		}

		if (theInfer) {
			Collection<Method> aSetters = getAnnotatedSetters(theClass, false);
			for (Method aSetterMethod : aSetters) {
				String aGetterName = aSetterMethod.getName().replaceFirst("set", "get");
				boolean tryIs = false;

				try {
					Method aGetter = theClass.getMethod(aGetterName);

					// so we have a setter for a annotated getter, so here we will add this to the list
					// of setters to infer the annotation on the setter even though its not explicit

					if (!aMethods.containsKey(aGetter.getName())) {
						aMethods.put(aGetter.getName(), aGetter);
					}
				}
				catch (NoSuchMethodException e) {
					// no biggie, setter doesn't exist, we'll just move on
					tryIs = true;
				}

				if (tryIs) {
					// didn't find a getter w/ the getXXX form, so lets try isXXXX which I think is normal (valid)
					// convention for boolean properties
					aGetterName = aSetterMethod.getName().replaceFirst("set", "is");

					try {
						Method aGetter = theClass.getMethod(aGetterName);

						// so we have a setter for a annotated getter, so here we will add this to the list
						// of setters to infer the annotation on the setter even though its not explicit

						if (!aMethods.containsKey(aGetter.getName())) {
							aMethods.put(aGetter.getName(), aGetter);
						}
					}
					catch (NoSuchMethodException e) {
						// no biggie, setter doesn't exist, we'll just move on
					}
				}
			}
		}

		if (shouldInspectSuperClass(theClass)) {
			for (Method m : getAnnotatedGetters(theClass.getSuperclass(), theInfer)) {
				if (!aMethods.containsKey(m.getName())) {
					aMethods.put(m.getName(), m);
				}
			}
		}

		for (Class aInterface : theClass.getInterfaces()) {
			for (Method m : getAnnotatedGetters(aInterface, theInfer)) {
				if (!aMethods.containsKey(m.getName())) {
					aMethods.put(m.getName(), m);
				}
			}
		}

		if (theInfer) {
			entry.mInferredGetters = aMethods.values();
		}
		else {
			entry.mGetters = aMethods.values();
		}

		return aMethods.values();
	}

	/**
	 * Return a list of all the fields on the given class which are annotated with the {@link RdfProperty} annotation.
	 * @param theClass the class to scan
	 * @return the list of annotated fields on the class
	 */
	public static Collection<Field> getAnnotatedFields(Class theClass) {
		BeanReflectCacheEntry entry = cache.get(theClass);

		if (entry == null) {
			entry = new BeanReflectCacheEntry();
			cache.put(theClass, entry);
		}

		if (entry.mFields != null) {
			return entry.mFields;
		}

		Collection<Field> aProps = new HashSet<Field>();

		for (Field aField : getAllDeclaredFields(theClass)) {
			if (aField.getAnnotation(Transient.class) != null
				|| javassist.util.proxy.ProxyObject.class.isAssignableFrom(theClass)) {
				continue;
			}

			if (aField.getAnnotation(RdfProperty.class) != null) {
				aProps.add(aField);
			}
			else if (!EmpireOptions.USE_LEGACY_TRANSIENT_BEHAVIOR && !aField.getType().isAssignableFrom(SupportsRdfId.class)) {
				// we want to auto-include fields not marked w/ transient, but lacking an @RdfProperty assertion when this
				// mode is disabled, however, we always want to ignore the implementation/support of SupportsRdfId from concrete classes.
				aProps.add(aField);
			}
		}

		if (shouldInspectSuperClass(theClass)) {
			aProps.addAll(getAnnotatedFields(theClass.getSuperclass()));
		}

		for (Class aInterface : theClass.getInterfaces()) {
			aProps.addAll(getAnnotatedFields(aInterface));
		}

		entry.mFields = aProps;

		return aProps;
	}

	/**
	 * Get the value on the object from the specified accessor, either a {@link Field} or {@link Method}.  This method
     * will toggle the accessibility of the accessor so an IllegalAccessException is not thrown.
	 * @param theAccess the accessor
	 * @param theObject the object to get a value from
	 * @return the value of the field or (getter) method on an object.
	 * @throws InvocationTargetException thrown if there is an error while invoking the getter method.  Usually because
	 * it's not a bean-style getter w/ no parameters.
	 */
	public static Object safeGet(AccessibleObject theAccess, Object theObject) throws InvocationTargetException {
        boolean aOldAccess = setAccessible(theAccess, true);

        try {
            if (theAccess instanceof Field) {
                return ((Field)theAccess).get(theObject);
            }
            else if (theAccess instanceof Method) {
                return ((Method)theAccess).invoke(theObject);
            }
            else {
                return null;
            }
        }
        catch (IllegalAccessException ex) {
            // we should not get this since we toggled the access, if we do, something foul is afoot.
            throw new RuntimeException(ex);
        }
        finally {
            setAccessible(theAccess, aOldAccess);
        }
    }
	/**
	 * Get the value on the object from the specified accessor, either a {@link Field} or {@link Method}
	 * @param theAccess the accessor
	 * @param theObject the object to get a value from
	 * @return the value of the field or (getter) method on an object.
	 * @throws IllegalAccessException thrown if you cannot access the field or method
	 * @throws InvocationTargetException thrown if there is an error while invoking the getter method.  Usually because
	 * it's not a bean-style getter w/ no parameters.
	 */
	public static Object get(AccessibleObject theAccess, Object theObject) throws IllegalAccessException, InvocationTargetException {
		if (theAccess instanceof Field) {
			return ((Field)theAccess).get(theObject);
		}
		else if (theAccess instanceof Method) {
			return ((Method)theAccess).invoke(theObject);
		}
		else {
			return null;
		}
	}

    /**
     * Given a class and an accessor, either a {@link Field} or a {@link Method}, try to return a setter which can
     * set the value of the property represented by the accessor.
     * @param theClass the class
     * @param theAccess the accessor
     * @return a setter object, or null if one is not found.
     */
    public static AccessibleObject asSetter(final Class<?> theClass, final AccessibleObject theAccess) {
		// use the class provided ...
		Class<?> aClass = theClass;

		// unless its an instance of EmpireGenerated, in which case we want to directly inspect the superclass
		// since that is the actual object and not our Empire stub
		if (EmpireGenerated.class.equals(theClass)) {
			aClass = theClass.getSuperclass();
		}

        // field can be used for access just fine
        if (theAccess instanceof Field && getAllDeclaredFields(theClass).contains(theAccess)) {
            return theAccess;
        }
        else {
            // try to find a setter method
            StringBuffer aName = new StringBuffer();
                        
            if (theAccess instanceof Field) {
                // this probably cannot happen, this would mean the accessor is a field, but not in the list of
                // declared fields, which i dont think will ever occur.  so this is probably overkill.
            	
            	// Update: Yes, this can actually happen. Declared fields do not include inherited fields,
            	// but they can still be accessor (even more, if the field was annotated in one class, and then Empire
            	// generated a subclass implementation for that class ...)
                aName.append(((Field)theAccess).getName());
                aName.setCharAt(0, String.valueOf(aName.charAt(0)).toUpperCase().charAt(0));    
            }
            else if (theAccess instanceof Method) {
                aName.append(((Method)theAccess).getName());

                String aPrefix = aName.substring(0,3);
                if (aPrefix.startsWith("get")) {
                    aName.delete(0, 3);
                }
                else if (aPrefix.startsWith("is")) {
                    aName.delete(0, 2);
                }
            }

            aName.insert(0, "set");
                        
            for (Method aMethod : aClass.getMethods()) {
                if (aMethod.getName().equals(aName.toString())) {
                    return aMethod;
                }
            }

            // ok, so no method by the name of the setter.
            // so lets rip off the set prefix, toLower the first letter, and search for a field with that name

            aName.delete(0, 3);
            aName.setCharAt(0, String.valueOf(aName.charAt(0)).toLowerCase().charAt(0));

            try {
                return aClass.getDeclaredField(aName.toString());
            }
            catch (NoSuchFieldException e) {
                return null;
            }
        }
    }

    public static Set<Field> getAllDeclaredFields(final Class<?> theClass) {
        Set<Field> aFields = Sets.newHashSet();
        Class<?> aClass = theClass;
        while (aClass != null) {
            aFields.addAll(Sets.newHashSet(aClass.getDeclaredFields()));
            aClass = aClass.getSuperclass();
        }

        return aFields;
    }

    /**
     * Return whether or not the array contains the object
     * @param theArray the array to search
     * @param theObj the object to look for
     * @param <T> the type of the objects in the array
     * @return true if the element was found, false otherwise
     */
    private static <T> boolean arrayContains(T[] theArray, T theObj) {
        if (theObj == null) {
            return false;
        }

        for (T aObj : theArray) {
            if (aObj.equals(theObj)) {
                return true;
            }
        }

        return false;
    }

	/**
	 * Create an instance of the specifiec collection.  If the type cannot be instantiated directly, such as List, or Set,
	 * we'll try and figure out which type of collection is desired and return the standard implementation for
	 * that type, for example ArrayList or HashSet.
	 * @param theValueType the type of collection to instantiate
	 * @return the instantiated collection
	 * @throws RuntimeException if there is no way to instantiate any matching collection
	 */
	public static Collection<Object> instantiateCollectionFromField(Class theValueType) {
		try {
			// try creating a new instance.  this will work if they've specified a concrete type
			return (Collection<Object>) theValueType.newInstance();
		}
		catch (Throwable e) {
			// TODO: make this less brittle -- should we have some sort of facade collection or something in front
			// that we generate at runtime?  or is there a better way to handle this situation?

			// if the above failed, that means the type of the field is something like List, or Set, which is not
			// directly instantiable.  If it's a known type, we'll hand instantiate something here.
			if (List.class.isAssignableFrom(theValueType)) {
				return new ArrayList<>();
			}
			else if (Set.class.isAssignableFrom(theValueType)) {
				if (SortedSet.class.isAssignableFrom(theValueType)) {
					return new TreeSet<>();
				}
				else {
					return new LinkedHashSet<>();
				}
			}
			else if (Collection.class.equals(theValueType)) {
				return new LinkedHashSet<>();
			}
			else {
				// last option is Map, but i dunno what the hell to do in that case, it doesn't map to our use here.
				throw new RuntimeException("Unknown or unsupported collection type for a field: " + theValueType);
			}
		}
	}

	/**
	 * Return whether or not the given object is a Java primitive type (String is included as a primitive).
	 * @param theObj the object
	 * @return true if its a primitive, false otherwise.
	 */
    public static boolean isPrimitive(Object theObj) {
        return (Boolean.class.isInstance(theObj) || Integer.class.isInstance(theObj) || Long.class.isInstance(theObj)
                || Short.class.isInstance(theObj) || Double.class.isInstance(theObj) || Float.class.isInstance(theObj)
                || Date.class.isInstance(theObj) || String.class.isInstance(theObj) || Character.class.isInstance(theObj));
    }

	/**
	 * Return whether or not the given class represents a Java primitive type (String is included as a primitive).
	 * @param theObj the object
	 * @return true if its a primitive, false otherwise.
	 */
    public static boolean isPrimitive(Class theObj) {
        return (Boolean.class.equals(theObj) || Integer.class.equals(theObj) || Long.class.equals(theObj)
                || Short.class.equals(theObj) || Double.class.equals(theObj) || Float.class.equals(theObj)
                || Date.class.equals(theObj) || String.class.equals(theObj) || Character.class.equals(theObj));
    }

	/**
	 * Return whether or not the accessor is marked with a {@link FetchType#LAZY} annotation.  If there is no
	 * {@link OneToOne}, {@link OneToMany}, {@link ManyToOne}, {@link OneToOne}, or {@link ManyToMany} annotation,
	 * or they do not specify a fetch type, the default value is assumed to be {@link FetchType#EAGER}.  If the
	 * provided accessor is not a Field or Method, the FetchType is also assumed to be EAGER.
	 * @param theAccessor the accessor
	 * @return true if the accessor is marked with {@link FetchType#LAZY}, false otherwise.
	 */
	public static boolean isFetchTypeLazy(Object theAccessor) {
		FetchType aFetchType = null;

		if (theAccessor instanceof AccessibleObject) {
			AccessibleObject aObject = (AccessibleObject) theAccessor;

			if (aObject.getAnnotation(OneToMany.class) != null) {
				aFetchType = aObject.getAnnotation(OneToMany.class).fetch();
			}
			else if (aObject.getAnnotation(OneToOne.class) != null) {
				aFetchType = aObject.getAnnotation(OneToOne.class).fetch();
			}
			else if (aObject.getAnnotation(ManyToOne.class) != null) {
				aFetchType = aObject.getAnnotation(ManyToOne.class).fetch();
			}
			else if (aObject.getAnnotation(ManyToMany.class) != null) {
				aFetchType = aObject.getAnnotation(ManyToMany.class).fetch();
			}
		}

		if (aFetchType == null) {
			aFetchType = FetchType.EAGER;
		}

		return aFetchType.equals(FetchType.LAZY);
	}

	/**
	 * Return the value of the targetEntity for the accessor if it has a {@link OneToOne}, {@link OneToMany},
	 * {@link ManyToOne}, {@link OneToOne}, or {@link ManyToMany} annotation.  This will return null if the accessor
	 * is not an {@link AccessibleObject} or if it does not have one of the aforementioned annotations, or if the
	 * targetEntity is not set for the annotation
	 * @param theAccessor the accessor
	 * @return the targetEntity for the accessor, or null if not specified.
	 */
	public static Class<?> getTargetEntity(Object theAccessor) {
		Class<?> aClass = null;

		if (theAccessor instanceof AccessibleObject) {
			AccessibleObject aObject = (AccessibleObject) theAccessor;

			if (aObject.getAnnotation(OneToMany.class) != null) {
				aClass = aObject.getAnnotation(OneToMany.class).targetEntity();
			}
			else if (aObject.getAnnotation(OneToOne.class) != null) {
				aClass = aObject.getAnnotation(OneToOne.class).targetEntity();
			}
			else if (aObject.getAnnotation(ManyToOne.class) != null) {
				aClass = aObject.getAnnotation(ManyToOne.class).targetEntity();
			}
			else if (aObject.getAnnotation(ManyToMany.class) != null) {
				aClass = aObject.getAnnotation(ManyToMany.class).targetEntity();
			}
		}

		return aClass;
	}

	/**
	 * Check whether or not the accessor has a {@link CascadeType} specified, and if so, if that cascade type indicates
	 * that Remove operations should be cascaded.
	 * @param theAccessor the accessor to inspect
	 * @return true if remove operations should be cascaded, false otherwise.
	 */
	public static boolean isRemoveCascade(Object theAccessor) {
		Collection<CascadeType> aCascade = getCascadeTypes(theAccessor);
		return aCascade.contains(CascadeType.REMOVE) || aCascade.contains(CascadeType.ALL);
	}

	/**
	 * Check whether or not the accessor has a {@link CascadeType} specified, and if so, if that cascade type indicates
	 * that Persist operations should be cascaded.
	 * @param theAccessor the accessor to inspect
	 * @return true if persist operations should be cascaded, false otherwise.
	 */
	public static boolean isPersistCascade(Object theAccessor) {
		Collection<CascadeType> aCascade = getCascadeTypes(theAccessor);
		return aCascade.contains(CascadeType.PERSIST) || aCascade.contains(CascadeType.ALL);
	}

	/**
	 * Check whether or not the accessor has a {@link CascadeType} specified, and if so, if that cascade type indicates
	 * that refresh operations should be cascaded.
	 * @param theAccessor the accessor to inspect
	 * @return true if refresh operations should be cascaded, false otherwise.
	 */
	public static boolean isRefreshCascade(Object theAccessor) {
		Collection<CascadeType> aCascade = getCascadeTypes(theAccessor);
		return aCascade.contains(CascadeType.REFRESH) || aCascade.contains(CascadeType.ALL);
	}

	/**
	 * Check whether or not the accessor has a {@link CascadeType} specified, and if so, if that cascade type indicates
	 * that Merge operations should be cascaded.
	 * @param theAccessor the accessor to inspect
	 * @return true if merge operations should be cascaded, false otherwise.
	 */
	public static boolean isMergeCascade(Object theAccessor) {
		Collection<CascadeType> aCascade = getCascadeTypes(theAccessor);
		return aCascade.contains(CascadeType.MERGE) || aCascade.contains(CascadeType.ALL);
	}

	/**
	 * Return all {@link CascadeType CascadeTypes} specified for the provided accessor.  If the access is not a Field
	 * or Method, or if it does not have any of the value multiplicity annotations ({@link OneToOne}, {@link OneToMany},
	 * {@link ManyToOne}, {@link OneToOne}, or {@link ManyToMany}) this will return false, otherwise it will collect
	 * the values of the casade property of any of the aforementioned annotations used on the accessor.
	 * @param theAccessor the accessor to inspect
	 * @return the collection of CascadeTypes specified, or an empty list if none is specified.
	 */
	private static Collection<CascadeType> getCascadeTypes(Object theAccessor) {
		Collection<CascadeType> aCascade = new HashSet<CascadeType>();

		if (theAccessor instanceof AccessibleObject) {
			AccessibleObject aObject = (AccessibleObject) theAccessor;

			if (aObject.getAnnotation(OneToMany.class) != null) {
				aCascade.addAll(Arrays.asList(aObject.getAnnotation(OneToMany.class).cascade()));
			}
			else if (aObject.getAnnotation(OneToOne.class) != null) {
				aCascade.addAll(Arrays.asList(aObject.getAnnotation(OneToOne.class).cascade()));
			}
			else if (aObject.getAnnotation(ManyToOne.class) != null) {
				aCascade.addAll(Arrays.asList(aObject.getAnnotation(ManyToOne.class).cascade()));
			}
			else if (aObject.getAnnotation(ManyToMany.class) != null) {
				aCascade.addAll(Arrays.asList(aObject.getAnnotation(ManyToMany.class).cascade()));
			}
		}

		return aCascade;
	}

	/**
	 * Return the Annotation of the specified type on the accessor, either a {@link java.lang.reflect.Field} or a {@link java.lang.reflect.Method}
	 * @param theAccess the accessor
	 * @param theAnnotation the annotation to get
	 * @param <T> the type of annotation to retrieve
	 * @return the value of the annotation on the accessor, or null if one is not found, or the accessor is not a Field or Method.
	 */
	public static <T extends Annotation> T getAnnotation(Object theAccess, Class<T> theAnnotation) {
		if (theAccess instanceof Field) {
			return ((Field)theAccess).getAnnotation(theAnnotation);
		}
		else if (theAccess instanceof Method) {
			Method aMethod = (Method) theAccess;
			T aAnnotation = aMethod.getAnnotation(theAnnotation);

			if (aAnnotation == null) {
				// if this is the case, it might be that this is from an "inferred" method.  so let's check it's twin,
				// either a getter or setter to see if it has the annotation.  If not, I don't know what the hell
				// happened

				try {
					Method aPairedMethod = null;
					if (aMethod.getName().startsWith("get")) {
						aPairedMethod = aMethod.getDeclaringClass().getMethod(aMethod.getName().replaceFirst("get", "set"),
																			  aMethod.getReturnType());
					}
					else if (aMethod.getName().startsWith("set")) {
						try {
							aPairedMethod = aMethod.getDeclaringClass().getMethod(aMethod.getName().replaceFirst("set", "get"));
						}
						catch (Exception e) {
							// no-op
						}

						if (aPairedMethod == null) {
							aPairedMethod = aMethod.getDeclaringClass().getMethod(aMethod.getName().replaceFirst("set", "is"));
						}
					}

					if (aPairedMethod != null) {
						aAnnotation = aPairedMethod.getAnnotation(theAnnotation);
					}
				}
				catch (NoSuchMethodException e) {
					// could not find a paired getter/setter.  don't know why.  probably the user put the annotations
					// on methods of non-bean compliant code, which is screwing things up.  we'll try to fail as
					// gracefully as possible
				}
			}

			return aAnnotation;
		}
		else {
			return null;
		}
	}

	/**
	 * Return the Class type of the accessor.  For a {@link java.lang.reflect.Field} it's the declared type of the field, for a
	 * {@link java.lang.reflect.Method}, which should be a bean-style setter, it's the type of the single parameter to the method.
	 * @param theAccessor the accessor
	 * @return the Class type of the Field/Method
	 * @throws RuntimeException thrown if you don't pass in a Field or Method, or if the Method is not of the expected
	 * bean-style setter variety.
	 */
	public static Class classFrom(Object theAccessor) {
		Class<?> aClass;

		if (theAccessor instanceof Field) {
			aClass = ((Field)theAccessor).getType();
		}
		else if (theAccessor instanceof Method) {
			// this should be a setter style bean method, taking one param which corresponds to the type of the property
			// it represents

			Method aMethod = (Method) theAccessor;
			if (aMethod.getParameterTypes().length == 1) {
				aClass = aMethod.getParameterTypes()[0];
			}
			else {
				throw new RuntimeException("Unknown or unsupported accessor method type");
			}
		}
        else if (theAccessor instanceof Class) {
            aClass = (Class) theAccessor;
        }
		else {
			throw new RuntimeException("Unknown or unsupported accessor type: " + theAccessor);
		}

		return aClass;
	}
	 
	/**
	 * Checks whether both classes have RdfsClass annotation that refers to the same type.
	 * 
	 * @param clazz1 the first class to be compared
	 * @param clazz2 the second class to be compared
	 * @return true if and only if the both classes have RdfsClass annotation and both of the annotations refer
	 * to the same type. 
	 */
	public static boolean sameRdfsClass(Class clazz1, Class clazz2) {
		if (!BeanReflectUtil.hasAnnotation(clazz1, RdfsClass.class)) {
			return false;
		}
		
		if (!BeanReflectUtil.hasAnnotation(clazz2, RdfsClass.class)) {
			return false;
		}
		
		RdfsClass rdfsClass1 = BeanReflectUtil.getAnnotation(clazz1, RdfsClass.class);
		RdfsClass rdfsClass2 = BeanReflectUtil.getAnnotation(clazz2, RdfsClass.class);
		
		String type1 = PrefixMapping.GLOBAL.uri(rdfsClass1.value());
		String type2 = PrefixMapping.GLOBAL.uri(rdfsClass2.value());
		
		return type1.equals(type2);
	}

	private static class BeanReflectCacheEntry {
		public Field mIdField;

		public Collection<Field> mFields;
		public Collection<Method> mSetters;
		public Collection<Method> mGetters;

		public Collection<Method> mInferredSetters;
		public Collection<Method> mInferredGetters;

		public Map<Class<? extends Annotation>, Annotation> mAnnotations = Maps.newHashMap();
	}
}
