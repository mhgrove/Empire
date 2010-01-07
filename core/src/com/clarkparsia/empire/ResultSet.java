package com.clarkparsia.empire;

import com.clarkparsia.sesame.utils.query.Binding;

import java.util.Iterator;

/**
 * Title: ResultSet<br/>
 * Description: Simple stub interface for a set of results to a select query.  This is considered to be an
 * {@link Iterator} set of {@link Binding} objects.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 14, 2009 1:17:44 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public interface ResultSet extends Iterator<Binding> {

	/**
	 * Close this result set and release any resources it holds.
	 */
	public void close();
}
