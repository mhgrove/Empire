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

package com.clarkparsia.empire;

import org.openrdf.model.Graph;
import com.clarkparsia.empire.DataSource;

/**
 * <p>Interface for a {@link com.clarkparsia.empire.DataSource} which can be mutated, that is, it supports
 * add and remove operations.</p>
 *
 * @author Michael Grove
 * @since 0.1
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
