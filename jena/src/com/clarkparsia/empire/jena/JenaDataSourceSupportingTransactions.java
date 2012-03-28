/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.SupportsTransactions;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * <p>Jena based {@link DataSource} for {@link Model models} which {@link SupportsTransactions support transactions}.</p>
 *
 * @author Michael Grove
 * @version 0.7.1
 * @since 0.7.1
 */
public class JenaDataSourceSupportingTransactions extends JenaDataSource implements SupportsTransactions{

	/**
	 * Create a new Jena-backed Data Source
	 *
	 * @param theModel the model
	 */
	JenaDataSourceSupportingTransactions(final Model theModel) {
		super(theModel);
	}

	/**
	 * @inheritDoc
	 */
	public void begin() throws DataSourceException {
		getModel().begin();
	}

	/**
	 * @inheritDoc
	 */
	public void commit() throws DataSourceException {
		getModel().commit();
	}

	/**
	 * @inheritDoc
	 */
	public void rollback() throws DataSourceException {
		getModel().abort();
	}
}
