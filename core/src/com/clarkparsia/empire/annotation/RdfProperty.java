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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Field level annotation to specify which RDF property a field and its value map to.</p>
 * <p>
 * Usage:
 * <code><pre>
 * &#64;Namespaces({"", "http://xmlns.com/foaf/0.1/",
 *			 "foaf", "http://xmlns.com/foaf/0.1/",
 * 		     "dc", "http://purl.org/dc/elements/1.1/"})
 * public class MyClass {
 *  ...
 * 	&#64;RdfProperty("foaf:firstName")
 *  public String firstName;
 * }
 * </pre></code>
 * </p>
 *
 * @author Michael Grove
 * @since 0.1
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfProperty {

	/**
	 * The URI value (or qname) of the RDF property the field or method is mapped to
	 * @return the property URI
	 */
	public String value();

	/**
	 * Where or not to process muliple values in a collection as an rdf:List
	 * @return True to process values as an rdf:List, false to process them as multiple assertions on the property.
	 * Default value is false.
	 */
	public boolean isList() default false;

	/**
	 * For literal valued properties, this specifies which language tag to retrieve and save from the RDF
	 * @return the language value, such as 'en' or 'fr' or the empty string for any language typed literals including
	 * those without language types specified.
	 */
	public String language() default "";

	/**
	 * <p>Attribute to control how java.net.URI valued field are handled.  The default behavior is that properties w/ the type of URI are converted to resources
	 * when serialized as RDF.  Setting this flag to 'true' will serialize them as literal values typed w/ xsd:anyURI.</p>
	 *
	 * <p>It is important to note that this does not affect the conversion into Java.  Literals typed with xsd:anyURI will always collapse into java.net.URI objects
	 * while resources can save into java.net.URI or a bean of the corresponding type.  It's this dual use of java.net.URI that makes this flag necessary so you
	 * can ensure you can symmetric I/O in your RDF.</p>
	 *
	 * @return whether or not java.net.URI objects should be treated as xsd:anyURI typed literal values.
	 */
	public boolean isXsdUri() default false;
}
