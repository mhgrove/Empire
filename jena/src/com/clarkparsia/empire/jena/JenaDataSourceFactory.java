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

import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.ds.DataSource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * <p>Abstract base class for creating Jena-backed data sources.  Provides a single method for creating an appropriate
 * type of Jena model based on some configuration parameters.</p>
 *
 * @author Michael Grove
 * @since 0.6.3
 * @version 0.6.3
 */
public abstract class JenaDataSourceFactory implements DataSourceFactory, JenaConfig {

	/**
	 * Create a Jena model from the values specified in the configuration.
	 * @param theConfig the configuration parameters
	 * @return a new Jena model of the appropriate type
	 * @see JenaConfig
	 */
	protected Model createModel(Map<String, Object> theConfig) {
		Model aModel = ModelFactory.createDefaultModel();

		if (theConfig.containsKey(TYPE)) {
			String aType = theConfig.get(TYPE).toString();

			if (aType.equals(MODEL)) {
				aModel = (Model) theConfig.get(MODEL);
			}

			// TODO: create other types of jena models from the values in the config file here
			// if (isTdb(config))
			//   aModel = TDBFactory.createModel(...);
			// else if isSdb(config)
			//   aModel = SDBFactory.createModel(...);
			//
			// and so on.  then any source factory implementations can use this.
		}

		return aModel;
	}

}
