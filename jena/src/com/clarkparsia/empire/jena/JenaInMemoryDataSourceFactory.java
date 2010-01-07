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
 * Title: JenaInMemoryDataSourceFactory<br/>
 * Description: Implementation of the DataSourceFactory interface for creating in-memory Jena-backed data sources
 * from data files on disk.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 6, 2010 1:42:32 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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
