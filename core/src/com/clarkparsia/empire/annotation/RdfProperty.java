package com.clarkparsia.empire.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Title: RdfProperty<br/>
 * Description: <p>Field level annotation to specify which RDF property a field and its value map to.</p>
 * <p>
 * Usage:<br/>
 * <code>
 * @Namespaces({"", "http://xmlns.com/foaf/0.1/",
 *			 "foaf", "http://xmlns.com/foaf/0.1/",
 * 		     "dc", "http://purl.org/dc/elements/1.1/"})
 * public class MyClass {
 *  ...
 * 	@RdfProperty("foaf:firstName")
 *  public String firstName;
 * }
 * </code>
 * </p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 3:10:21 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfProperty {
	public String value();
	public boolean isList() default false;
}
