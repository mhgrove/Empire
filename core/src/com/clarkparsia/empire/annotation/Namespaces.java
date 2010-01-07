package com.clarkparsia.empire.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Title: Namespaces<br/>
 * Description: <p>Key-value pairs of qname & uri to specify the namespaces used during annotation of an object or
 * set of objects.  There must be an even number of elements in the array, and they are grabbed in pairs, ie 0 & 1,
 * 1 & 2, etc.  The first element of the pair is assumed to be the qname and the other the URI.</p>
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
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 11, 2009 3:33:27 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Namespaces {
	public String[] value();
}
