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

package com.clarkparsia.empire.sesametwo;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;
import com.clarkparsia.utils.BasicUtils;

import java.util.Map;
import java.io.File;
import java.io.FileInputStream;

import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.model.Statement;

/**
 * <p>Implementation of the {@link DataSourceFactory} interface for creating Sesame 2.x Repository objects.</p>
 *
 * @author Michael Grove
 * @since 0.6
 */
public class RepositoryDataSourceFactory implements DataSourceFactory {
	/**
	 * Configuration key for the URL of the sesame service
	 */
	public static final String URL = "url";

	/**
	 * Configuration key for the name of the sesame repository
	 */
	public static final String REPO = "repo";

	/**
	 * Configuration key for the files to load for the local sesame repository
	 */
	public static final String FILES = "files";

	/**
	 * Configuration key for the local sesame data directory
	 */
	public static final String DIR = "dir";


	public boolean canCreate(final Map<String, String> theMap) {
		String aURL = theMap.get(URL);
		String aRepo = theMap.get(REPO);
		String aFiles = theMap.get(FILES);
		String aDir = theMap.get(DIR);

		return (aURL != null && aRepo != null) || aFiles != null || aDir != null;
	}

	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException("Invalid configuration map: " + theMap);
		}

		String aURL = theMap.get(URL);
		String aRepo = theMap.get(REPO);
		String aFiles = theMap.get(FILES);
		String aDir = theMap.get(DIR);

		Repository aRepository = null;


		try {

		if (aURL != null) {
			aRepository = new HTTPRepository(aURL, aRepo);

			aRepository.initialize();
			
		}
		else if (aFiles != null) {
			aRepository = new SailRepository(new MemoryStore());

			try {
				aRepository.initialize();
				
				RepositoryConnection aConn = aRepository.getConnection();

				for (String aFile : BasicUtils.split(aFiles, ",")) {
					RDFParser aParser = Rio.createParser(Rio.getParserFormatForFileName(aFile));

					aParser.setRDFHandler(new SailBuilderRDFHandler(aConn));

					aParser.parse(new FileInputStream(aFile), "");
				}

				aConn.commit();
			}
			catch (Exception e) {
				throw new DataSourceException(e);
			}
		}
		else {
			aRepository = new SailRepository(new MemoryStore(new File(aDir)));

			aRepository.initialize();
		}



			return new RepositoryDataSource(aRepository);
		}
		catch (RepositoryException e) {
			throw new DataSourceException(e);
		}
	}

	private static class SailBuilderRDFHandler extends RDFHandlerBase {
		private RepositoryConnection mConnection;

		private SailBuilderRDFHandler(final RepositoryConnection theConnection) {
			mConnection = theConnection;
		}

		@Override
		public void handleStatement(Statement theStmt) {
			try {
				mConnection.add(theStmt.getSubject(), theStmt.getPredicate(), theStmt.getObject(), theStmt.getSubject());
			}
			catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
