package com.clarkparsia.empire.lazyload;


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
public class BusinessObjectImpl extends BaseRdfEntity
                                implements BusinessObject
{
    @RdfId
    private String uri;
    @RdfProperty("dcterms:title")
    private String title;
    @RdfProperty("empire:event")
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    private Collection<Event> events = new TreeSet<Event>();

    public BusinessObjectImpl() {
        // NOP
    }
    public BusinessObjectImpl(String uri) {
        this.uri = uri;
    }

    @Override
    public String getUri() {
        return this.uri;
    }

    @Override
    public String getTitle() {
        return this.title;
    }
    @Override
    public void setTitle(String t) {
        this.title = t;
    }

    @Override
    public void add(Event event) {
        this.events.add(event);
    }

    @Override
    public Collection<Event> getEvents() {
        return new TreeSet<Event>(this.events);
    }

    @Override
    public Collection<Event> getEvents(String uri) {
        return this.getEvents(Event.class, uri);
    }

    @Override
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