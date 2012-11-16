/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire.test.lazyload;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Namespaces({
				"empire",  "https://github.com/mhgrove/Empire/",
				"dcterms", "http://purl.org/dc/terms/" })
@Entity
@RdfsClass("empire:Business")
public class BusinessObject extends BaseRdfEntity
{
	@RdfId
	private String uri;
	@RdfProperty("dcterms:title")
	private String title;
	@RdfProperty("empire:event")
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	private Collection<Event> events = new TreeSet<Event>();

	public BusinessObject() {
		// NOP
	}
	public BusinessObject(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return this.uri;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String t) {
		this.title = t;
	}

	public void add(Event event) {
		this.events.add(event);
	}

	public Collection<Event> getEvents() {
		return new TreeSet<Event>(this.events);
	}

	public Collection<Event> getEvents(String uri) {
		return this.getEvents(Event.class, uri);
	}

	public <T extends Event> Collection<T> getEvents(Class<T> type, String uri) {
		if (type == null) {
			throw new IllegalArgumentException("type");
		}
		Collection<T> l = null;
		for (Event e : this.events) {
			if ((e.getClass().isAssignableFrom(type)) &&
				((uri == null) || (e.getSubject().equals(uri)))) {
				if (l == null) {
					l = new TreeSet<T>();
				}
				l.add(type.cast(e));
			}
		}
		if (l == null) {
			l = Collections.emptySet();
		}
		return l;
	}

	@Override
	protected void setId(String id) {
		this.uri = id;
	}

	@Override
	public String toString() {
		return "\n\"" + this.getUri() + "\" { title: \"" + this.getTitle()
			   + ", events: " + this.getEvents() + " }";
	}
}
