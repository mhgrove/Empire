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

/**
 * Title: PropertiesAnnotationProvider<br/>
 * Description: Implementation of the EmpireAnnotationProvider interface which reads a property file from disk and
 * uses that to create the index of classes with specified annotations.  The properties are expected to be in a simple
 * format, the key's in the file should be the fully qualified class names of the annotations and the values of
 * these keys should be a comma separated list of the fully qualified class names of all the classes which have
 * the specified annotation.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 5, 2010 8:24:51 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class PropertiesAnnotationProvider implements EmpireAnnotationProvider {

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
				// TODO: log me
				System.err.println("Reading empire.config properties for Annotation provider failed: " + e.getMessage());
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
						// TODO: log me
						System.err.println("Class specified in AnnotationProvider file '" + aName + "' " +
										   "does not actually have the specified annotation '" + theAnnotation + "'");
					}
				}
				catch (ClassNotFoundException e) {
					// TODO: log me
					System.err.println("Class specified in AnnotationProvider file '" + aName + "' cannot be found.");
				}
			}
		}

		return aClasses;
	}
}
