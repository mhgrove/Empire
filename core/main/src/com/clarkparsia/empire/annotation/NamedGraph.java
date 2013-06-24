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
 * <p>Annotation for specifying the named graph instances of a Java object should be persisted to.  When
 * the type is {@link NamedGraph.NamedGraphType#Instance} the resulting RDF will be persisted to a graph whose
 * name is the same as the rdf:ID of the instance being saved.  When the type is {@link NamedGraph.NamedGraphType#Static}
 * you must also specify a value, and the specified URI value is what is used as the named graph uri during persistence.</p>
 * <p>
 * Usage:<br/>
 * <code><pre>
 * &#64;RdfClass("foaf:Person")
 * &#64;NamedGraph(type = NamedGraph.NamedGraphType.Instance)
 * public class Foo implements SupportsRdfId {
 *   ...
 * }
 * </pre></code>
 * </p>
 *
 * @author Michael Grove
 * @since 0.1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedGraph {

	/**
	 * What kind of named graph scheme to use when persisting RDF
	 */
	public enum NamedGraphType {

		/**
		 * Persist to a named graph with the same URI as the individiual being persisted
		 */
		Instance,

		/**
		 * Persist to a specific named graph
		 */
		Static,
	}

	/**
	 * The NamedGraph persistence type
	 * @return the type
	 */
	public NamedGraphType type() default NamedGraphType.Instance;

	/**
	 * The URI of the named graph to persist to
	 * @return the named graph URI
	 */
	public String value() default "";
}
