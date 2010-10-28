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

package com.clarkparsia.empire.annotation.runtime;

import java.util.AbstractList;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Implementation of a {@link List} in which allows a mix of objects, but is typed for a particular object.  It is
 * designed to allow elements of the list type <b>and</b> {@link Proxy} objects for objects of that type in the list.</p>
 * <p>For example, you can declare this list as List&lt;Book&gt; but instead of only Book objects, you can add
 * Proxy&lt;Book&gt; objects.  When you call {@link #get} to get an element from the list, it will either return
 * the element straight away if it is of the correct type, or when it's a proxy object, it will get the actual object
 * from the proxy.  This allows the proxies to defer the work of create/fetching the object until its actually used,
 * but it makes the use of the {@link #get} method potentially much more expensive.  The overhead of this unwrapping
 * is only paid when proxy objects are in the list, otherwise the runtime performance should mirror a normal list
 * implementation.</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
public class ProxyAwareList<T> extends AbstractList<T> implements List<T> {

	/**
	 * The actual list of data
	 */
	private List mList = new ArrayList();

	/**
	 * Create a new ProxyAwareList
	 */
	public ProxyAwareList() {
		this(new ArrayList<T>());
	}

	/**
	 * Create a ProxyAwareList
	 * @param theList the list of elements to add to the data
	 */
	public ProxyAwareList(final List<T> theList) {
		mList = theList;
	}

	/**
	 * Method to add a proxied object to the list.  Because the list is typed so you can only add objects of
	 * the type &lt;T&gt; this allows you to add Proxy objects which proxy for objects of the same type.
	 * @param theProxiedObject the proxy object to add
	 */
	@SuppressWarnings("unchecked")
	public void add(Proxy<T> theProxiedObject) {
		mList.add(theProxiedObject);
	}

	/**
	 * Returns the value at the given index.  If the value is a proxied object, it will be retrieved and returned,
	 * which can be an expensive operation.  Thus calling this method can be slower than what one would normally
	 * see from a List.
	 * @inheritDoc
	 */
	public T get(final int index) {
		Object aObj = mList.get(index);
		return unwrap(aObj);
	}

	/**
	 * Given an object from the underlying list, either cast it and return it, or if it's a proxy, get the proxied
	 * object.
	 * @param theObj the object to "unwrap"
	 * @return the actual object
	 */
	@SuppressWarnings("unchecked")
	private T unwrap(Object theObj) {
		if (theObj != null && theObj instanceof Proxy) {
			return ((Proxy<T>)theObj).value();
		}
		else {
			return (T) theObj;
		}
	}

	/**
	 * @inheritDoc
	 */
	public int size() {
		return mList.size();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void add(int theIndex, T theElement) {
		mList.add(theIndex, theElement);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T set(int theIndex, T theElement) {
		Object aObj = mList.set(theIndex, theElement);

		return unwrap(aObj);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T remove(int theIndex) {
		return unwrap(mList.remove(theIndex));
	}
}
