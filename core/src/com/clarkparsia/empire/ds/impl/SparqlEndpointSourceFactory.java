package com.clarkparsia.empire.ds.impl;

import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * <p>DataSourceFactory implementation to create a Sparql endpoint backed data source.</p>
 *
 * @author Michael Grove
 * @version 0.6.5
 * @since 0.6.5
 * @see SparqlEndpointDataSource
 */
@Alias("sparql")
public class SparqlEndpointSourceFactory implements DataSourceFactory {

	/**
	 * Configuration map key for the URL of the sparql endpoint.
	 */
	public static final String KEY_URL = "url";

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, Object> theMap) {
		return theMap.containsKey(KEY_URL);
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, Object> theMap) throws DataSourceException {
		if (canCreate(theMap)) {
			try {
				return new SparqlEndpointDataSource(new URL(theMap.get(KEY_URL).toString()));
			}
			catch (MalformedURLException e) {
				throw new DataSourceException(e);
			}
		}
		else {
			throw new DataSourceException("Invalid configuration map, missing required key '" + KEY_URL + "'.");
		}
	}
}
