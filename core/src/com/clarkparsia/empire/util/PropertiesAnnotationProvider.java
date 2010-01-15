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

package com.clarkparsia.empire.util;

import com.clarkparsia.utils.BasicUtils;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import java.lang.annotation.Annotation;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * <p>Implementation of the EmpireAnnotationProvider interface which reads a property file from disk and
 * uses that to create the index of classes with specified annotations.  The properties are expected to be in a simple
 * format, the key's in the file should be the fully qualified class names of the annotations and the values of
 * these keys should be a comma separated list of the fully qualified class names of all the classes which have
 * the specified annotation.</p>
 *
 * @author Michael Grove
 */
public class PropertiesAnnotationProvider implements EmpireAnnotationProvider {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(PropertiesAnnotationProvider.class.getName());

	/**
	 * The file to read the Annotations index from
	 */
	private File mFile;

	/**
	 * The properties read in from disk
	 */
	private Properties mProperties;

	/**
	 * Create a new PropertiesAnnotationProvider which will read it's Annotation information from the default location,
	 * a file in the top level of the application called "empire.config"
	 */
	public PropertiesAnnotationProvider() {
		this(new File("empire.config"));
	}

	/**
	 * Create a new PropertiesAnnotationProvider
	 * @param theFile the file to read the annotation properties from
	 */
	public PropertiesAnnotationProvider(File theFile) {
		mFile = theFile;
	}

	/**
	 * Return the Properties for this AnnotationProvider
	 * @return the properties read from the specified file
	 */
	private Properties getProperties() {
		if (mProperties == null) {
			mProperties = new Properties();

			try {
				mProperties.load(new FileInputStream(mFile));
			}
			catch (IOException e) {
				LOGGER.error("Reading empire.config properties for Annotation provider failed", e);
			}
		}

		return mProperties;
	}

	/**
	 * @inheritDoc
	 */
	public Collection<Class> getClassesWithAnnotation(final Class<? extends Annotation> theAnnotation) {
		Set<Class> aClasses = new HashSet<Class>();

		Properties aProps = getProperties();

		String aVal = aProps.getProperty(theAnnotation.getName());

		if (aVal != null) {
			for (String aName : BasicUtils.split(aVal, ",")) {
				try {
					Class aClass = Class.forName(aName);

					if (aClass.isAnnotationPresent(theAnnotation)) {
						aClasses.add(aClass);
					}
					else {
						LOGGER.warn("Class specified in AnnotationProvider file '" + aName + "' " +
									"does not actually have the specified annotation '" + theAnnotation + "'");
					}
				}
				catch (ClassNotFoundException e) {
					LOGGER.warn("Class specified in AnnotationProvider file '" + aName + "' cannot be found.");
				}
			}
		}

		return aClasses;
	}
}
