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

package com.clarkparsia.empire.codegen;

import java.util.List;

import java.util.Set;

import javax.persistence.Entity;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0
 * @since 0
 */
@Entity
@RdfsClass("urn:Person")
public interface PersonWithWildcards<T> extends SupportsRdfId {

@RdfProperty("urn:hasContact")
public List<? extends PersonWithWildcards> getHasContact();
public void setHasContact(List<? extends PersonWithWildcards> theValue);

@RdfProperty( "urn:hasContact2")
public Set<? super PersonWithWildcards> getHasContact2();
public void setHasContact2(Set<? super PersonWithWildcards> theValue);
}
