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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.DataSourceFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.shared.JenaException;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0
 * @version 0
 */
public abstract class AbstractJenaDataSourceFactory implements JenaConfig, DataSourceFactory {
    protected final Map<String, Model> ontModelCache = Maps.newHashMap();

    protected void cacheOntModel(String name, Model m) {
        ontModelCache.put(name, m);
    }

    /**
     * Return the cached OntModel
     *
     * @param theName the model unit name
     * @return the cached OntModel, or null if one does not exist
     */
    protected Model getCachedOntModel(String theName) {
        return ontModelCache.get(theName);
    }

    /**
     * Create an ontology model and cache if config determines
     * <p/>
     * I've seen elsewhere that the ontmodel gets added to the data model but it didnt seem to work for me at the time - could
     * have been something I was doing :-) so leave protected rather than private for easy override
     *
     * @param name		the unit name
     * @param ontLocation the location of the ontology for the OntModel
     */
    protected void initOntologyModel(String name, String ontLocation) {
        if (null != ontLocation && ontLocation.length() > 0 && null == ontModelCache.get(name)) {
            Model ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
            ontModel.read(ontLocation);
            cacheOntModel(name, ontModel);
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
    protected void load(Model theModel, Reader theReader, String theFormat, String theBase) throws DataSourceException {
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
    protected void loadFiles(final Model theModel, final String theFiles, final String theBase) throws DataSourceException {

        for (String aFile : Splitter.on(",").omitEmptyStrings().trimResults().split(theFiles)) {
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

    /**
     * Return the unknown object as a Reader.  Supported conversions are provided for {@link Reader}, {@link java.io.InputStream},
     * {@link java.io.File}, {@link java.net.URI}, and {@link java.net.URL}.
     * @param theObj the object try to create as a Reader
     * @return the object as a Reader
     * @throws DataSourceException if there is an error while opening the Reader or it cannot be turned into a Reader
     */
    protected Reader asReader(Object theObj) throws DataSourceException {
        try {
            if (theObj instanceof Reader) {
                return (Reader) theObj;
            }
            else if (theObj instanceof InputStream) {
                return new InputStreamReader( (InputStream) theObj, Charsets.UTF_8);
            }
            else if (theObj instanceof File) {
                return new InputStreamReader( new FileInputStream( (File) theObj), Charsets.UTF_8);
            }
            else if (theObj instanceof URI) {
                return new InputStreamReader(((URI) theObj).toURL().openStream(), Charsets.UTF_8);
            }
            else if (theObj instanceof URL) {
                return new InputStreamReader(((URL) theObj).openStream(), Charsets.UTF_8);
            }
            else {
                throw new DataSourceException("Cannot read from the specified stream objects, it is not a Reader or an InputStream: " + theObj);
            }
        }
        catch (IOException e) {
            throw new DataSourceException("There was an error opening the reader/inputstream", e);
        }
    }
}
