package com.clarkparsia.empire.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Title: RdfId</p>
 * <p>Description: Field level annotation to specify the field that should be used, if possible, to generate the objects
 * rdf:ID from.  This annotation will only be used the first time an object is turned into RDF, subsequent operations
 * will use the pre-existing rdf:ID and this annotation will be ignored.</p>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfId {
	public static final String DEFAULT = "urn:clarkparsia.com:empire:";

	/**
	 * Return the namespace prefix to use when generating the rdf:ID
	 * @return the namespace prefix
	 */
	String namespace() default DEFAULT;
}
