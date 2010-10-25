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

package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;

import javax.persistence.Entity;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@Namespaces({"space", "http://purl.org/net/schemas/space/",
			 "foaf", "http://xmlns.com/foaf/0.1/",
			 "dc", "http://purl.org/dc/elements/1.1/"})
@Entity
@RdfsClass("space:Discipline")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Discipline extends BaseTestClass {
	@RdfProperty("rdfs:label")
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(final String theLabel) {
		label = theLabel;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Discipline)) {
			return false;
		}

		final Discipline that = (Discipline) o;

		if (getLabel() != null ? !getLabel().equals(that.getLabel()) : that.getLabel() != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), that.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getLabel() != null ? getLabel().hashCode() : 0;
	}
}
