package com.clarkparsia.empire.impl;

import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.sesame.utils.query.Binding;

import java.util.Iterator;

/**
 * Title: AbstractResultSet<br/>
 * Description: Abstract implementation of the ResultSet interface that is an adapter for the underlying Iterator.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 7, 2010 9:19:45 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public abstract class AbstractResultSet implements ResultSet {

	/**
	 * The underlying iterator
	 */
	private Iterator<Binding> mIter;

	/**
	 * Create a new AbstractResultSet
	 * @param theIter the iterator to back this result set.
	 */
	public AbstractResultSet(final Iterator<Binding> theIter) {
		mIter = theIter;
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasNext() {
		return mIter.hasNext();
	}

	/**
	 * @inheritDoc
	 */
	public Binding next() {
		return mIter.next();
	}

	/**
	 * @inheritDoc
	 */
	public void remove() {
		mIter.remove();
	}
}
