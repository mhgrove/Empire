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

package com.complexible.stardog.empire;

/**
 * <p>Set of valid keys and (some) values for use with the {@link StardogEmpireDataSourceFactory}.</p>
 *
 * @author Hector Perez-Urbina
 * @since 0.7
 * @version 0.7
 */
public interface StardogEmpireFactoryKeys {

	/**
	 * Global alias for this factory
	 */
	public static final String ALIAS = "stardog";

	/**
	 * Connection string for Stardog instance
	 */
	public static final String URL = "url";

	/**
	 * Flag that determines if the connection backing the StardogDataSource will be in auto-commit mode
	 */
	public static final String AUTO_COMMIT = "auto.commit";
}
