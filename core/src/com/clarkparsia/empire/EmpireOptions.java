package com.clarkparsia.empire;

import com.clarkparsia.empire.util.PropertiesAnnotationProvider;
import com.clarkparsia.empire.util.EmpireAnnotationProvider;

/**
 * Title: EmpireOptions<br/>
 * Description: Catch-all class for global Empire options and configuration<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com> <br/>
 * Created: Jan 1, 2010 12:59:12 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class EmpireOptions {

	/**
	 * Whether or not to force strong typing of literals during I/O from the database.  When this is true, literals
	 * written to the database will always contain a datatype, and on input are expected to have a datatype or else
	 * the conversion will fail.  When false, datatype information will be ignored both during reads and writes.
	 * The recommended value is true because that will give the most accurate conversions, and allow the most
	 * appropriate design of your Java beans, but if you are using 3rd party data which does not use datatypes
	 * disabling this mode can be useful.  The default value is true.
	 */
    public static boolean STRONG_TYPING = true;

	/**
	 * The {@link EmpireAnnotationProvider} to use to get information about Annotations in the system.
	 */
	public static EmpireAnnotationProvider ANNOTATION_PROVIDER = new PropertiesAnnotationProvider();
}
