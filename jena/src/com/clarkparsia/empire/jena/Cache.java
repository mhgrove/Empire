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

/**
 * <p>Basic interface for a cache.</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
interface Cache<K,V> {

	/**
	 * Get a value from the cache
	 * @param theKey the key of the item to get from the cache
	 * @return the cached object, or null if it is not in the cache
	 */
	public V get(K theKey);

	/**
	 * Add a value to the cache
	 * @param theKey the key of the value to add
	 * @param theValue the object being cached
	 * @return this cache
	 */
	public Cache<K,V> add(K theKey, V theValue);
}
