package com.clarkparsia.empire;

import org.openrdf.model.Graph;

/**
 * Title: SupportsNamedGraphs<br/>
 * Description: Interface for a MutableDataSource which supports operations on named graphs.<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public interface SupportsNamedGraphs extends MutableDataSource {
	/**
	 * Add the given set of triples to the specified named graph
	 * @param theGraphURI the named graph URI
	 * @param theGraph the graph of triples to add
	 * @throws DataSourceException thrown if there is an error while adding the triples
	 */
	public void add(java.net.URI theGraphURI, Graph theGraph) throws DataSourceException;

	/**
	 * Delete the named graph
	 * @param theGraphURI the named graph to delete
	 * @throws DataSourceException thrown if there is an error while deleting the named graph
	 */
	public void remove(java.net.URI theGraphURI) throws DataSourceException;

	/**
	 * Remove the specified triples from the given named graph
	 * @param theGraphURI the named graph
	 * @param theGraph the graph of the triples to remove
	 * @throws DataSourceException thrown if there is an error while deleting the triples from the named graph
	 */
	public void remove(java.net.URI theGraphURI, Graph theGraph) throws DataSourceException;
}
