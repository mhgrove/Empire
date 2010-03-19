package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.ds.Alias;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import com.clarkparsia.utils.io.Encoder;

import com.clarkparsia.utils.BasicUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;

import java.util.Map;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URL;

/**
 * <p>Default implementation for creating DataSources backed by Jena models</p>
 *
 * @author Michael Grove
 * @since 0.6.3
 * @version 0.6.3
 */
@Alias("jena")
public class DefaultJenaDataSourceFactory extends JenaDataSourceFactory implements JenaConfig {

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, Object> theMap) {
		// for now, everything is valid.  if you don't have the right config options, we'll just create an empty model
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, Object> theMap) throws DataSourceException {
		Model aModel = createModel(theMap);

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

		return new JenaDataSource(aModel);
	}

	/**
	 * Return the unknown object as a Reader.  Supported conversions are provided for {@link Reader}, {@link InputStream},
	 * {@link java.io.File}, {@link java.net.URI}, and {@link java.net.URL}.
	 * @param theObj the object try to create as a Reader
	 * @return the object as a Reader
	 * @throws DataSourceException if there is an error while opening the Reader or it cannot be turned into a Reader
	 */
	private Reader asReader(Object theObj) throws DataSourceException {
		try {
			if (theObj instanceof Reader) {
				return (Reader) theObj;
			}
			else if (theObj instanceof InputStream) {
				return new InputStreamReader( (InputStream) theObj, Encoder.UTF8);
			}
			else if (theObj instanceof File) {
				return new InputStreamReader( new FileInputStream( (File) theObj), Encoder.UTF8);
			}
			else if (theObj instanceof URI) {
				return new InputStreamReader(((URI) theObj).toURL().openStream(), Encoder.UTF8);
			}
			else if (theObj instanceof URL) {
				return new InputStreamReader(((URL) theObj).openStream(), Encoder.UTF8);
			}
			else {
				throw new DataSourceException("Cannot read from the specified stream objects, it is not a Reader or an InputStream: " + theObj);
			}
		}
		catch (IOException e) {
			throw new DataSourceException("There was an error opening the reader/inputstream", e);
		}
	}

	/**
	 * Load data from the specifed Reader into the model.
	 * @param theModel the model to load the data into
	 * @param theReader the reader to load the data from
	 * @param theFormat the key for the RDF format the data is in
	 * @param theBase the base uri to be used when parsing the file
	 */
	private void load(Model theModel, Reader theReader, String theFormat, String theBase) {
		RDFReader aReader = theModel.getReader(theFormat);

		aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

		aReader.read(theModel, theReader, theBase != null ? theBase : "");
	}

	/**
	 * Read the list of comma separated file names and load them into the model.
	 * @param theModel the model to load the data into
	 * @param theFiles the comma separated list of file names to load
	 * @param theBase the base uri to use when parsing the files
	 * @throws DataSourceException if there is an error while reading the files or parsing.
	 */
	private void loadFiles(final Model theModel, final String theFiles, final String theBase) throws DataSourceException {

		for (String aFile : BasicUtils.split(theFiles, ",")) {
			RDFReader aReader = theModel.getReader();
			aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

			try {
				aReader.read(theModel, new FileInputStream(aFile.trim()), theBase);
			}
			catch (Exception e) {
				aReader = theModel.getReader("N3");
				aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

				try {
					aReader.read(theModel, new FileInputStream(aFile.trim()), theBase);
				}
				catch (Exception ex) {
					aReader = theModel.getReader("N-TRIPLE");
					aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

					try {
						aReader.read(theModel, new FileInputStream(aFile.trim()), theBase);
					}
					catch (Exception exc) {
						throw new DataSourceException("Cannot parse local files", e);
					}
				}
			}
		}
	}
}
