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

package com.clarkparsia.empire.lazyload;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;

@MappedSuperclass
public abstract class BaseRdfEntity implements SupportsRdfId
{
    private SupportsRdfId rdfId = new SupportsRdfIdImpl();

    abstract protected void setId(String id);

    @Override
    public RdfKey<?> getRdfId() {
        return this.rdfId.getRdfId();
    }
    @Override @SuppressWarnings("unchecked")
    public void setRdfId(RdfKey id) {
        this.rdfId.setRdfId(id);
        this.setId(String.valueOf(id));
    }

    @Override
    public boolean equals(Object o) {
        return ((o != null) &&
                o.getClass().equals(this.getClass()) &&
                String.valueOf(this.getRdfId()).equals(
                                String.valueOf(((BaseRdfEntity)o).getRdfId())));
    }

    @Override
    public int hashCode() {
        return (this.getRdfId() != null)? this.getRdfId().hashCode():
                                          System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return String.valueOf(this.getRdfId());
    }

    protected final Date copy(final Date date) {
        return (date != null)? new Date(date.getTime()): null;
    }
}
