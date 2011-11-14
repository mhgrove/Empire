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

package com.clarkparsia.empire.test.typing;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.CascadeType;

import java.util.List;
import java.util.ArrayList;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p>A class with the same @RdfsClass annotation like B, but not inheriting from A</p>
 * 
 * This class is here merely to be in the classpath and make sure that none of the Empire code
 * picks it when needing a class that inherits from A.
 *
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
@MappedSuperclass
@Entity
@RdfsClass("urn:clarkparsia.com:empire:test:B")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public abstract class AnotherB implements SupportsRdfId {
	@RdfProperty("urn:clarkparsia.com:empire:test:propB")
	public abstract String getPropB();
	public abstract void setPropB(String b);
}
