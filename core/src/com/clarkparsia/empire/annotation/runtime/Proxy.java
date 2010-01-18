package com.clarkparsia.empire.annotation.runtime;

import com.clarkparsia.empire.DataSource;

import com.clarkparsia.empire.annotation.RdfGenerator;

import java.net.URI;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class Proxy<T> {
	private T mValue;
	private Class<T> mClass;
	private URI mURI;
	private DataSource mDataSource;

	public Proxy(Class<T> theClass, URI theURI, DataSource theSource) {
		mClass = theClass;
		mURI = theURI;
		mDataSource = theSource;
	}

	public T value() {
		if (mValue == null) {
			try {
				mValue = RdfGenerator.fromRdf(mClass, mURI, mDataSource);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return mValue;
	}
}
