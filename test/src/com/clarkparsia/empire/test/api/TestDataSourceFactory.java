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

package com.clarkparsia.empire.test.api;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;
import java.io.FileInputStream;
import java.io.IOException;

import com.clarkparsia.sesame.utils.ExtendedGraph;
import org.openrdf.rio.ParseException;

/**
 * <p>DataSourceFactory implementation to create a DataSource used for testing</p>
 *
 * @author Michael Grove
 */
public class TestDataSourceFactory implements DataSourceFactory {

	public boolean canCreate(final Map<String, String> theMap) {
		return true;
	}

	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		ExtendedGraph aGraph = new ExtendedGraph();

		if (theMap.containsKey("files")) {
			for (String aFile : theMap.get("files").split(",")) {
				try {
					aGraph.read(new FileInputStream(aFile.trim()));
				}
				catch (Exception e) {
					throw new DataSourceException(e);
				}
			}
		}

		return new TestDataSource(aGraph);
	}
}
