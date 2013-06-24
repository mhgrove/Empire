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

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>Key-value pairs of qname & uri to specify the namespaces used during annotation of an object or
 * set of objects.  There must be an even number of elements in the array, and they are grabbed in pairs, ie 0 & 1,
 * 1 & 2, etc.  The first element of the pair is assumed to be the qname and the other the URI.</p>
 * <p>
 * Usage:<br/>
 * <code>
 * &#64;Namespaces({"", "http://xmlns.com/foaf/0.1/",
 *			 "foaf", "http://xmlns.com/foaf/0.1/",
 * 		     "dc", "http://purl.org/dc/elements/1.1/"})
 * public class MyClass {
 *  ...
 * 	&#64;RdfProperty("foaf:firstName")
 *  public String firstName;
 * }
 * </code>
 * </p>
 *
 * @author Michael Grove
 * @since 0.1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Namespaces {

	/**
	 * The array of namespace prefixes and uri's.
	 * @return the namespaces
	 */
	public String[] value();
}
