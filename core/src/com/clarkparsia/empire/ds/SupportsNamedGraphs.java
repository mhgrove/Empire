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

package com.clarkparsia.empire.ds;

import org.openrdf.model.Graph;
import com.clarkparsia.empire.ds.DataSourceException;

/**
 * <p>Interface for a {@link MutableDataSource} which supports operations on named graphs.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.7
 * @see MutableDataSource
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
