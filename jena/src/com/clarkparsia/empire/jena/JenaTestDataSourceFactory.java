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

package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;

import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.config.EmpireConfiguration;

import com.clarkparsia.utils.BasicUtils;

import java.util.Map;
import java.util.HashMap;

import java.io.FileInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.google.inject.name.Named;
import com.google.inject.Inject;

/**
 * <p>Implementation of the DataSourceFactory interface for creating in-memory Jena-backed data sources
 * from data files on disk.  Created models are stored locally to save the overhead of re-parsing the
 * files each time.  Intended to only be used for testing.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.7
 */
@Alias("jena-test")
class JenaTestDataSourceFactory extends JenaDataSourceFactory {

	/**
	 * Cache of data sources
	 */
	private static Map<String, DataSource> mSourceCache = new HashMap<String, DataSource>();

	/**
	 * Create a new JenaTestDataSourceFactory
	 * @param theContainerConfig the configuration of the container
	 */
	@Inject
	public JenaTestDataSourceFactory(@Named("ec") EmpireConfiguration theContainerConfig) {
		super(theContainerConfig);
	}

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, Object> theMap) {
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, Object> theMap) throws DataSourceException {
		// tests should reuse the same source.
		if (theMap.containsKey("files") && mSourceCache.containsKey(theMap.get("files"))) {
			return mSourceCache.get(theMap.get("files"));
		}

		Model aModel = createModel(theMap);

		if (theMap.containsKey("files")) {
			for (String aFile : BasicUtils.split(theMap.get("files").toString(), ",")) {
				RDFReader aReader = aModel.getReader();
				aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

				try {
					aReader.read(aModel, new FileInputStream(aFile.trim()), "");
				}
				catch (Exception e) {
					aReader = aModel.getReader("N3");
					aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");
					
					try {
						aReader.read(aModel, new FileInputStream(aFile.trim()), "");
					}
					catch (Exception ex) {
						aReader = aModel.getReader("N-TRIPLE");
						aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

						try {
							aReader.read(aModel, new FileInputStream(aFile.trim()), "");
						}
						catch (Exception exc) {
							throw new DataSourceException(e);
						}
					}
				}
			}
		}

		DataSource aSource = new JenaDataSource(aModel);

		//mSourceCache.put(theMap.get("files").toString(), aSource);

		return aSource;
	}
}
