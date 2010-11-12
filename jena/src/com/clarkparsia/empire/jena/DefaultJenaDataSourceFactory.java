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

import com.clarkparsia.empire.ds.Alias;
import com.clarkparsia.empire.ds.DataSource;

import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.config.EmpireConfiguration;

import com.clarkparsia.utils.io.Encoder;

import com.clarkparsia.utils.BasicUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.shared.JenaException;
import com.google.inject.name.Named;
import com.google.inject.Inject;

import java.util.Map;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>Default implementation for creating DataSources backed by Jena models</p>
 *
 * @author Michael Grove
 * @author uoccou
 * @since 0.6.3
 * @version 0.7
 */
@Alias("jena")
class DefaultJenaDataSourceFactory extends JenaDataSourceFactory implements JenaConfig {

	/**
	 * Application logger
	 */
	private final Logger LOGGER = LogManager.getLogger(this.getClass());

	/**
	 * Create a new DefaultJenaDataSourceFactory
	 * @param theContainerConfig the configuration of the container
	 */
	@Inject
	public DefaultJenaDataSourceFactory(@Named("ec") EmpireConfiguration theContainerConfig) {
		super(theContainerConfig);
	}

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
		
		DataSource aSource = null;
		Model aModel = createModel(theMap);
		
		if (aModel != null) {

			LOGGER.debug("Got a model - creating DataSource ");
			
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
				// only TDB needs special treatment
				aSource = new JenaDataSource(aModel);
				
			}
		}
		else {
			LOGGER.error("Could not get a model - not creating DataSource. ");
		}
		
		return aSource;
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
	 * @throws com.clarkparsia.empire.ds.DataSourceException if there is an error while parsing or reading the source RDF.
	 */
	private void load(Model theModel, Reader theReader, String theFormat, String theBase) throws DataSourceException {
		try {
			RDFReader aReader = theModel.getReader(theFormat);

			aReader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE");

			aReader.read(theModel, theReader, theBase != null ? theBase : "");
		}
		catch (JenaException e) {
			throw new DataSourceException("There was a Jena error while reading the source", e);
		}
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
