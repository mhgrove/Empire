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
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 30, 2009 10:18:52 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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
