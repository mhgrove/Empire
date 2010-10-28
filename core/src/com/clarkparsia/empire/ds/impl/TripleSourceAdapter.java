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

package com.clarkparsia.empire.ds.impl;

import com.clarkparsia.empire.ds.TripleSource;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Graph;

import javax.persistence.Query;

/**
 * <p>Wraps a general {@link DataSource}, implementing getStatements using SPARQL queries.</p>
 *
 * @author Pedro Oliveira
 * @author Michael Grove
 * @since 0.7
 * @version 0.7
 */
public class TripleSourceAdapter extends DelegatingDataSource implements TripleSource {
	private static final String SUBJECT_FILTER = "FILTER (?s = ??ss)";

	private static final String OBJECT_FILTER = "FILTER (?o = ??oo)";

	private static final String SUBJECT_OBJECT_FILTER = "FILTER (?s = ??ss && ?o = ??oo)";

	public TripleSourceAdapter(DataSource source) {
		super(source);
	}

	/**
	 * @inheritDoc
	 */
	public Iterable<Statement> getStatements(Resource theSubject, URI thePredicate, Value theObject) throws DataSourceException {
		// Subject and object restrictions are implemented as filters, because some implementations can have problems
		// dealing with bnodes

		String aFilter = "";
		
		if (theSubject != null && theObject != null) {
			aFilter = SUBJECT_OBJECT_FILTER;
		}
		else if (theSubject != null) {
			aFilter = SUBJECT_FILTER;
		}
		else if (theObject != null) {
			aFilter = OBJECT_FILTER;
		}

		Query aQuery = getQueryFactory().createQuery("construct {?s ??p ?o} where { ?s ??p ?o . " + aFilter + " }");

		if (theSubject != null) {
			aQuery.setParameter("ss", theSubject);
		}

		if (thePredicate != null) {
			aQuery.setParameter("p", thePredicate);
		}

		if (theObject != null) {
			aQuery.setParameter("oo", theObject);
		}

		return (Graph) aQuery.getSingleResult();
	}
}
