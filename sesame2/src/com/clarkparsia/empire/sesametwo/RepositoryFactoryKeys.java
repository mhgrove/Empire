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

package com.clarkparsia.empire.sesametwo;

/**
 * <p>Set of valid keys and (some) values for use with the {@link RepositoryDataSourceFactory}.</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
public interface RepositoryFactoryKeys {

	/**
	 * Global alias for this factory
	 */
	public static final String ALIAS = "sesame";

	/**
	 * Configuration key for the URL of the sesame service
	 */
	public static final String URL = "url";

	/**
	 * Configuration key for the name of the sesame repository
	 */
	public static final String REPO = "repo";

	/**
	 * Configuration key for the files to load for the local sesame repository
	 */
	public static final String FILES = "files";

	/**
	 * Configuration key for the local sesame data directory
	 */
	public static final String DIR = "dir";

	/**
	 * Configuration key for controlling which query dialect is used by the RepositoryDataSource
	 */
	public static final String QUERY_LANG = "queryLang";

	/**
	 * Constant value for the SERQL query language
	 * @see #QUERY_LANG
	 */
	public static final String LANG_SERQL = "serql";
}
