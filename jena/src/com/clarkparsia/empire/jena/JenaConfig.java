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

package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.config.ConfigKeys;

/**
 * <p>The list of valid Jena configuration parameters for creating Jena backed datasources.</p>
 *
 * @author Michael Grove
 * @since 0.6.3
 * @version 0.6.3
 */
public interface JenaConfig extends ConfigKeys {

	/**
	 * Key for specifying the Jena model implementation to create
	 * @see JenaModelType
	 */
	public static final String TYPE = "type";

	/**
	 * Key for providing an {@link InputStream} or {@link Reader} to the factory to load the model with the
	 * data contained in the stream/reader.  Will be parsed using the specified format and optionally, the base uri.
	 * @see #FORMAT
	 * @see #BASE
	 */
	public static final String STREAM = "stream";

	/**
	 * Specifies the RDF format of the data in the stream.  Valid values are specified by the Jena API, (N3, RDFXML, N-TRIPLES).
	 * This is a required key if you are using {@link #STREAM}.
	 * @see #STREAM
	 */
	public static final String FORMAT = "format";

	/**
	 * Specifies a comma separated list of local file paths to be loaded into the model.  The {@link #BASE} key can
	 * be used to specify a base URI for reading the files.
	 */
	public static final String FILES = "files";

	/**
	 * Key for specifying the base URI of the RDF to be parsed
	 */
	public static final String BASE = "base-uri";

	/**
	 * Overloaded Key/Value (of {@link #TYPE} for specifying a particular model as the thing to be wrapped by any resulting EntityManagers.  
	 * This will override any other provided type options.
	 * @see #TYPE
	 */
	public static final String MODEL = "model";

	/**
	 * Enumeration of the various types of Jena models.
	 */
	public enum JenaModelType {
		Memory,
		TDB,
		SDB
	}
}
