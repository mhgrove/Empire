package com.clarkparsia.empire.util;

import java.util.Collection;

import java.lang.annotation.Annotation;

/**
 * Title: EmpireAnnotationProvider<br/>
 * Description: Interface to provide the set of classes using a given Annotation.  Only class-level annotations
 * are expected to be indexed by these providers, field & method annotations are not.  Implementations are free to
 * read the index from disk, or calculate it at runtime by the current classpath.  This will be called on Empire
 * start-up.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 5, 2010 8:24:39 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface EmpireAnnotationProvider {

	/**
	 * Return all classes in the classpath with the given annotation attached.
	 * @param theAnnotation the annotation to search for
	 * @return the collection of classes with the given annotation
	 */
	public Collection<Class> getClassesWithAnnotation(Class<? extends Annotation> theAnnotation);
}
