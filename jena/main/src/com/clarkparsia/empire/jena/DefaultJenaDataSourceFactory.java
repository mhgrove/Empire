/*
 * Copyright (c) 2009-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.ds.DataSource;

import com.clarkparsia.empire.ds.DataSourceException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.shared.JenaException;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;

import java.util.Map;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Default implementation for creating DataSources backed by Jena models</p>
 *
 * @author  Michael Grove
 * @author  uoccou
 * @since   0.6.3
 * @version 1.0
 */
@Alias("jena")
final class DefaultJenaDataSourceFactory extends BaseJenaDataSourceFactory implements JenaConfig {

	/**
	 * Application logger
	 */
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean canCreate(final Map<String, Object> theMap) {
		// for now, everything is valid.  if you don't have the right config options, we'll just create an empty model
		return true;
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public DataSource create(final Map<String, Object> theMap) throws DataSourceException {
		
		DataSource aSource = null;
		Model aModel = createModel(theMap);
		
		if (aModel != null) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Got a model - creating DataSource ");
			}
			
			if (theMap.containsKey(STREAM) && theMap.containsKey(FORMAT)) {
				load(aModel, asReader(theMap.get(STREAM)),
					 theMap.get(FORMAT).toString(),
					 theMap.containsKey(BASE) ? theMap.get(BASE).toString() : "");
			}
	
			if (theMap.containsKey(FILES)) {
				loadFiles(aModel,
						  theMap.get(FILES).toString(),
						  theMap.containsKey(BASE) ? theMap.get(BASE).toString() : "");
			}
	
			if (isTdb(theMap)) {
				aSource = new TDBJenaDataSource(aModel);

				//@uoccou would be nicer to use TransactionalDataSource but needs something like
				//TDB.sync(((TDBModel)((JenaDataSource)DataSourceUtil.asTripleStore(mDataSource)).getModel()))
				//
				//aSource = new TransactionalDataSource(new JenaDataSource(aModel));
			}
			else {
				// only TDB needs special treatment, otherwise use the default implementations

				if (aModel.supportsTransactions()) {
					aSource = new JenaDataSourceSupportingTransactions(aModel);
				}
				else {
					aSource = new JenaDataSource(aModel);
				}
			}
		}
		else {
			LOGGER.error("Could not get a model - not creating DataSource. ");
		}
		
		return aSource;
	}
}
