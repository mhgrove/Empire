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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify the rdf:type of the individual corresponding to instances of the Java class.</p>
 * <p>
 * Usage:
 * <code><pre>
 * &#64;RdfClass("foaf:Person")
 * public class Foo implements SupportsRdfId {
 *   ...
 * }
 * </pre></code>
 * </p>
 * <p>All resulting instances of the class Foo will be typed as a foaf:Person.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfsClass {

	/**
	 * The URI value of the class this object will be typed as
	 * @return the URI (or qname) of the class
	 */
	public String value();
}
