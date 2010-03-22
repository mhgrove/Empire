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

import com.google.inject.multibindings.Multibinder;

import com.google.inject.AbstractModule;

import com.clarkparsia.empire.DataSourceFactory;

import com.clarkparsia.empire.util.EmpireModule;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * <p>Guice module for installing the Jena 'plugin' for Empire.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.6.3
 */
public class JenaEmpireModule extends AbstractModule implements EmpireModule {

	/**
	 * @inheritDoc
	 */
	@Override
	protected void configure() {
//		JenaParameters.disableBNodeUIDGeneration = true;
			
		Multibinder.newSetBinder(binder(), DataSourceFactory.class)
				.addBinding().to(JenaTestDataSourceFactory.class);

		Multibinder.newSetBinder(binder(), DataSourceFactory.class)
				.addBinding().to(DefaultJenaDataSourceFactory.class);
	}

	public static void main(String[] args) {
//		String q = "construct {?s ?p ?o}\n" +
//				   "where {?s ?p ?o. filter(?s = <_:-407d4d90:127876dd080:-8000>) }";
		String q = "construct {?s ?p ?o}\n" +
				   "where {?s ?p ?o. filter(?s = <_:700b9d59:127879c2ac0:-8000>) }";

		QueryExecutionFactory.create(QueryFactory.create(q, Syntax.syntaxSPARQL), ModelFactory.createDefaultModel());

	}

//	public static void main(String[] args) throws Exception {
//		Map<String, Object> aFactoryMap = new HashMap<String, Object>();
//		InputStream aStream = new FileInputStream("foo");
//
//		aFactoryMap.put(JenaConfig.FACTORY, "jena");
//
//		EntityManagerFactory aFactory = Empire.get().persistenceProvider().createEntityManagerFactory("", aFactoryMap);
//
//		Map<String, Object> aMap = new HashMap<String, Object>();
//
//		aMap.put(JenaConfig.STREAM, aStream);
//		aMap.put(JenaConfig.FORMAT, "N3");
//
//		EntityManager aManager = aFactory.createEntityManager(aMap);
//	}
}
