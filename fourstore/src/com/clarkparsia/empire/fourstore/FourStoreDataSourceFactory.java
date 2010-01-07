package com.clarkparsia.empire.fourstore;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.empire.fourstore.FourStoreDataSource;
import fourstore.impl.StoreFactory;
import fourstore.api.Store;

/**
 * Title: FourStoreDataSourceFactory<br/>
 * Description: Implementation of the {@link DataSourceFactory} interface for creating instances of DataSources
 * backed a 4Store database.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 6, 2010 9:02:59 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class FourStoreDataSourceFactory implements DataSourceFactory {

	/**
	 * Configuration key for the URL of the 4Store http endpoint
	 */
	public static final String KEY_URL = "url";

	/**
	 * Configuration key for setting the soft limit of the 4store instance which is used for query answering
	 */
	public static final String KEY_SOFT_LIMIT = "soft.limit";

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, String> theMap) {
		return theMap.containsKey(KEY_URL) && BasicUtils.isURL(theMap.get(KEY_URL));
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException();
		}

		String aURL = theMap.get(KEY_URL);

		try {
			Store aStore = StoreFactory.create(new URL(aURL));

			if (theMap.containsKey(KEY_SOFT_LIMIT)) {
				try {
					aStore.setSoftLimit(Integer.parseInt(theMap.get(KEY_SOFT_LIMIT)));
				}
				catch (NumberFormatException e) {
					// todo: log me
					System.err.println("Invalid soft limit value specified: " + theMap.get(KEY_SOFT_LIMIT));
				}
			}

			return new FourStoreDataSource(aStore);
		}
		catch (MalformedURLException e) {
			// todo: log me
			System.err.println("Creating URL for 4store failed: " + e.getMessage());
			throw new DataSourceException(e);
		}
	}
}
