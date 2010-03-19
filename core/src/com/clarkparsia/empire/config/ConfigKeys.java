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

package com.clarkparsia.empire.config;

/**
 * <p>Collection of standard configuration keys found in an {@link EmpireConfiguration}</p>
 *
 * @author Michael Grove
 * @version 0.6.2
 * @since 0.6.3
 */
public interface ConfigKeys {

	/**
	 * Key to get the file on disk where the annotation index is located
	 */
	public String ANNOTATION_INDEX = "annotation.index";

	/**
	 * Key constant for finding the class name of the factory we want to create
	 */
	public String FACTORY = "factory";
}
