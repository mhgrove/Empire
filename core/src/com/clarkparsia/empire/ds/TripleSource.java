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

package com.clarkparsia.empire.ds;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * <p>Interface which extends a {@link DataSource} to provide triple-level access to the 
 * contents of the data.  Where DataSource only provides query level access, TripleSource provides the ability to make
 * SPO "queries" into the database.  When the underlying implementation natively supports this interface, there is
 * usually a performance benefit of the "native" access when compared to writing a SPARQL query that will retrieve
 * the same set of statements.</p>
 * 
 * @author Pedro Oliveira
 * @author Michael Grove
 *
 * @since 0.7
 * @version 0.7
 */
public interface TripleSource extends DataSource {

	/**
	 * Returns all the statements with the given subject, predicate, and object. Null parameters represent wildcards.
	 * 
	 * @param theSubject the subject to match, or null for a wildcard
	 * @param thePredicate the predicate to match, or null for a wildcard
	 * @param theObject the object to match, or null for a wildcard
	 * @return an Iterable set of matching statements.
	 * @throws DataSourceException
	 *             thrown if there is an error while getting the statements
	 */
	public Iterable<Statement> getStatements(Resource theSubject, URI thePredicate, Value theObject) throws DataSourceException;
}
