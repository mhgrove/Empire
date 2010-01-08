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

package com.clarkparsia.empire.impl.sparql;

import com.clarkparsia.empire.Dialect;

/**
 * <p>Represents the SPARQL query language.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class SPARQLDialect implements Dialect {
	/**
	 * the singleton instance
	 */
	private static SPARQLDialect INSTANCE;

	/**
	 * Create a new SPARQLDialect
	 */
	private SPARQLDialect() {
	}

	/**
	 * Return the single instance of SPARQLDialect
	 * @return the SPARQLDialect
	 */
	public static SPARQLDialect instance() {
		if (INSTANCE == null) {
			INSTANCE = new SPARQLDialect();
		}

		return INSTANCE;
	}
}
