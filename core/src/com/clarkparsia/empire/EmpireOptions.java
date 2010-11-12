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

package com.clarkparsia.empire;

/**
 * <p>Catch-all class for global Empire options and configuration</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 */
public class EmpireOptions {

	/**
	 * Whether or not to force strong typing of literals during I/O from the database.  When this is true, literals
	 * written to the database will always contain a datatype, and on input are expected to have a datatype or else
	 * the conversion will fail.  When false, datatype information will be ignored both during reads and writes.
	 * The recommended value is true because that will give the most accurate conversions, and allow the most
	 * appropriate design of your Java beans, but if you are using 3rd party data which does not use datatypes
	 * disabling this mode can be useful.  The default value is true.
	 */
    public static boolean STRONG_TYPING = true;

	/**
	 * Flag to enable whether or not language tags are used when setting the values for fields from RDF string literals.
	 * By specifying a language on the {@link com.clarkparsia.empire.annotation.RdfProperty} and enabling this mode,
	 * only literal values with the specified language will be considered for valid values for a field.
	 */
	public static boolean ENABLE_LANG_AWARE = false;

	/**
	 * When query results are returned, if they are bound to a bean class, setting this flag to true will return proxied
	 * objects, the bean's will not be loaded from the database until you iterate to them in the result set.  This
	 * allows the initial query to be much faster as it defers most of the load time until iteration.
	 */
	public static boolean ENABLE_QUERY_RESULT_PROXY = true;

	/**
	 * Flag to signal whether or not the @Entity annotation should be required on Empire-enabled beans.  Strictly speaking,
	 * it is not required for an Empire-only stack; @RdfsClass handles the same responsibility.  But if you want to use Empire
	 * in conjunction with a standard JPA provider, such as Hibernate & the Play! web framework, you will want this annotation
	 * on all of your beans. 
	 */
	public static boolean ENFORCE_ENTITY_ANNOTATION = true;

	/**
	 * Enable this flag if you want Empire to behave in it's legacy transience mode.  That is, when enabled, Empire will not
	 * bind any fields/properties which are not annotated with @RdfProperty.  Thus, transience is implied by the presence
	 * (or lack thereof) of the @RdfProperty annotation, which differs from normal JPA implementations in that they will always
	 * bind a field unless it's marked with @Transient
	 */
	public static boolean USE_LEGACY_TRANSIENT_BEHAVIOR = true;

	/**
	 * Flag to control how strict the various failure condition in things like RDF<->Java conversion or bean generation are handled.
	 * The default is true, which means all potentially fatal errors will throw unchecked exceptions (Runtime/IllegalArgument/IllegalState).
	 * When set to false, these situations are ignored, and the operations continue as best as they can, all errors in this mode are
	 * logged as warnings to the logger.
	 */
	public static boolean STRICT_MODE = true;
}
