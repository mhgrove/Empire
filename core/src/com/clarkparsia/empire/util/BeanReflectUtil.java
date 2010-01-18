package com.clarkparsia.empire.util;

import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.InvalidRdfException;
import com.clarkparsia.empire.annotation.RdfId;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.lang.annotation.Annotation;

import java.util.Collection;
import java.util.HashSet;

/**
 * <p>Some utility methods which use the Java reflect stuff to do a lot of the runtime accessing of fields and methods
 * on objects used to transform between Java and RDF.</p>
 *
 * @author Michael Grove
 * @since 0.5.1
 */
public class BeanReflectUtil {

	/**
	 * Return the field on the class which has an {@link RdfId} annotation.  If the fields on the class do not have the
	 * annotation, the super class will be checked.
	 * @param theClass the class
	 * @return the field with an RdfId annotation, or null if one is not found
	 * @throws InvalidRdfException thrown if there are multiple fields with the RdfId annotation
	 */
	public static Field getIdField(Class theClass) throws InvalidRdfException {
		Field aIdField = null;

		for (Field aField : theClass.getDeclaredFields()) {
			if (aField.getAnnotation(RdfId.class) != null) {
				if (aIdField != null) {
					throw new InvalidRdfException("Cannot have multiple id properties");
				}
				else {
					aIdField = aField;
				}
			}
		}

		if (aIdField == null && theClass.getSuperclass() != null) {
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
	public static <T extends Annotation> T getAnnotation(Class theClass, Class<T> theAnnotation) {
		T aAnnotation = null;

		if (theClass.isAnnotationPresent(theAnnotation)) {
			aAnnotation = (T) theClass.getAnnotation(theAnnotation);
		}
		else {
			if (theClass.getSuperclass() != null) {
				aAnnotation = getAnnotation(theClass.getSuperclass(), theAnnotation);
			}

			if (aAnnotation == null && theClass.getInterfaces() != null) {
				for (Class aInt : theClass.getInterfaces()) {
					aAnnotation = getAnnotation(aInt, theAnnotation);

					if (aAnnotation != null) {
						break;
					}
				}
			}
		}

		return aAnnotation;
	}

	/**
	 * Return whether or not the class has the given annotation.  If the class itself does not have the annotation,
	 * it's super class and the interfaces it implements are checked.
	 * @param theClass the class to check
	 * @param theAnnotation the annotation to look for
	 * @return if the class has the annotation, or one of its parents does and it "inherited" the annotation, false otherwise
	 */
	public static boolean hasAnnotation(Class theClass, Class<? extends Annotation> theAnnotation) {
		boolean aHasAnnotation = false;
		if (theClass.isAnnotationPresent(theAnnotation)) {
			aHasAnnotation = true;
		}
		else {
			if (theClass.getSuperclass() != null) {
				aHasAnnotation = hasAnnotation(theClass.getSuperclass(), theAnnotation);
			}

			if (!aHasAnnotation && theClass.getInterfaces() != null) {
				for (Class aInt : theClass.getInterfaces()) {
					aHasAnnotation = hasAnnotation(aInt, theAnnotation);

					if (aHasAnnotation) {
						break;
					}
				}
			}
		}

		return aHasAnnotation;
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
     * Return whether or not the given accessor, either a {@link Field} or a {@link Method} is accessible
     * @param theAccess the Field or Method
     * @return true if its accessible, false otherwise, or if it is not a Field or Method
     */
	@Deprecated
    public static boolean isAccessible(AccessibleObject theAccess) {
		return theAccess.isAccessible();
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
		Collection<Method> aMethods = new HashSet<Method>();

		for (Method aMethod : theClass.getDeclaredMethods()) {
			if (aMethod.getAnnotation(RdfProperty.class) != null
				&& !aMethod.getGenericReturnType().equals(Void.class)
				&& aMethod.getParameterTypes().length == 0) {

				aMethods.add(aMethod);
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

					if (!aMethods.contains(aGetter)) {
						aMethods.add(aGetter);
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

						if (!aMethods.contains(aGetter)) {
							aMethods.add(aGetter);
						}
					}
					catch (NoSuchMethodException e) {
						// no biggie, setter doesn't exist, we'll just move on
					}
				}
			}
		}

		return aMethods;
	}

	/**
	 * Return a list of all the fields on the given class which are annotated with the {@link RdfProperty} annotation.
	 * @param theClass the class to scan
	 * @return the list of annotated fields on the class
	 */
	public static Collection<Field> getAnnotatedFields(Class theClass) {
		Collection<Field> aProps = new HashSet<Field>();

		for (Field aField : theClass.getDeclaredFields()) {
			if (aField.getAnnotation(RdfProperty.class) != null) {
				aProps.add(aField);
			}
		}

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
        // field can be used for access just fine
        if (theAccess instanceof Field && arrayContains(theClass.getDeclaredFields(), theAccess)) {
            return theAccess;
        }
        else {
            // try to find a setter method
            StringBuffer aName = new StringBuffer();

            if (theAccess instanceof Field) {
                // this probably cannot happen, this would mean the accessor is a field, but not in the list of
                // declared fields, which i dont think will ever occur.  so this is probably overkill.
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

            for (Method aMethod : theClass.getDeclaredMethods()) {
                if (aMethod.getName().equals(aName.toString())) {
                    return aMethod;
                }
            }

            // ok, so no method by the name of the setter.
            // so lets rip off the set prefix, toLower the first letter, and search for a field with that name

            aName.delete(0, 3);
            aName.setCharAt(0, String.valueOf(aName.charAt(0)).toLowerCase().charAt(0));

            try {
                return theClass.getDeclaredField(aName.toString());
            }
            catch (NoSuchFieldException e) {
                return null;
            }
        }
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
}
