package com.clarkparsia.empire;

import org.openrdf.model.Graph;
import com.clarkparsia.empire.DataSource;

/**
 * Title: MutableDataSource<br/>
 * Description: Interface for a {@link com.clarkparsia.empire.DataSource} which can be mutated, that is, it supports add and remove operations.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 14, 2009 3:33:51 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public interface MutableDataSource extends DataSource {
	/**
	 * Add the triples in the graph to the data source
	 * @param theGraph the graph to add
	 * @throws DataSourceException thrown if there is an error while adding the triples
	 */
	public void add(Graph theGraph) throws DataSourceException;

	/**
	 * Remove the triples in the graph from the data source
	 * @param theGraph the graph to remove
	 * @throws DataSourceException thrown if there is an error while removing the triples
	 */
	public void remove(Graph theGraph) throws DataSourceException;
}
