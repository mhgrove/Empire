package com.clarkparsia.empire.test.api;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;
import java.util.HashMap;
import java.io.FileInputStream;

import com.clarkparsia.sesame.utils.ExtendedGraph;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 30, 2009 3:47:08 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class MutableTestDataSourceFactory implements DataSourceFactory {
	private static Map<String, DataSource> mSourceCache = new HashMap<String, DataSource>();

	public boolean canCreate(final Map<String, String> theMap) {
		return true;
	}

	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		// tests should reuse the same source.
		if (theMap.containsKey("files") && mSourceCache.containsKey(theMap.get("files"))) {
			return mSourceCache.get(theMap.get("files"));
		}

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

		DataSource aSource = new MutableTestDataSource(aGraph);

		mSourceCache.put(theMap.get("files"), aSource);

		return aSource;
	}
}
