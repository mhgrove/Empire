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

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;
import java.util.HashMap;

import java.io.FileInputStream;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;

/**
 * <p>Implementation of the DataSourceFactory interface for creating in-memory Jena-backed data sources
 * from data files on disk.</p>
 *
 * @author Michael Grove
 */
public class JenaInMemoryDataSourceFactory implements DataSourceFactory {
	// TODO: probably remove this, it's really only useful for testing
	private static Map<String, DataSource> mSourceCache = new HashMap<String, DataSource>();
	
	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, String> theMap) {
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		// tests should reuse the same source.
		if (theMap.containsKey("files") && mSourceCache.containsKey(theMap.get("files"))) {
			return mSourceCache.get(theMap.get("files"));
		}

		// TODO: this could abstract out to create TDB,SDB, Oracle, etc. backed models, but we dont need that yet

		Model aModel = ModelFactory.createDefaultModel();

		if (theMap.containsKey("files")) {
			for (String aFile : theMap.get("files").split(",")) {
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

		mSourceCache.put(theMap.get("files"), aSource);

		return aSource;
	}
}
