package com.clarkparsia.empire;

import com.clarkparsia.empire.DataSource;

import java.util.Map;

/**
 * Title: DataSourceFactory<br/>
 * Description: Factory interface for creating instances of a {@link DataSource}<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 17, 2009 9:46:01 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public interface DataSourceFactory {

	/**
	 * Returns whether or not a {@link com.clarkparsia.empire.DataSource} can be created by this factory for the given set of data.
	 * @param theMap the data to use to create the data source
	 * @return true if this can create a data source, false otherwise
	 */
	public boolean canCreate(Map<String, String> theMap);

	/**
	 * Create a {@link DataSource} from the given parameters
	 * @param theMap the parameters
	 * @return a new DataSource
	 * @throws DataSourceException thrown if the Map contains insufficient data, the data source cannot be constructed
	 * from the data, or if there was an error establishing a connection to the data source.
	 */
	public DataSource create(Map<String, String> theMap) throws DataSourceException;
}
