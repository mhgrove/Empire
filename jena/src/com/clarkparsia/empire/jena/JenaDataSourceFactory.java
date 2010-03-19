package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.DataSourceFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.util.Map;

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
