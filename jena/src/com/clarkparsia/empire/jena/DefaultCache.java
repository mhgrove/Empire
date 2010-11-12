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

import com.google.inject.internal.Maps;

import java.util.Map;
import java.util.Collections;

/**
 * <p>A basic cache implementation backed by a Map</p>
 *
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
class DefaultCache<K,V> implements Cache<K,V> {

	/**
	 * The actual cache
	 */
	private Map<K,V> cache = Collections.synchronizedMap(Maps.<K,V>newHashMap());

	/**
	 * @inheritDoc
	 */
	public V get(final K theKey) {
		return cache.get(theKey);
	}

	/**
	 * @inheritDoc
	 */
	public Cache<K, V> add(final K theKey, final V theValue) {
		if (!cache.containsKey(theKey)) {
			cache.put(theKey, theValue);
		}

		return this;
	}
}
