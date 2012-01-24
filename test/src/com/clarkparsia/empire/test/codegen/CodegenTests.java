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

package com.clarkparsia.empire.test.codegen;

import java.net.URI;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import com.clarkparsia.empire.codegen.InstanceGenerator;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.test.EmpireTestSuite;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * <p>Code generation unit tests</p>
 *
 * @author Michael Grove
 * @version 0.7
 * @since 0.7
 */
public class CodegenTests {

	@Test
	public void testCodeGenWithWildcards() throws Exception {
		InstanceGenerator.generateInstanceClass(PersonWithWildcards.class);
	}

	@Test
	public void testUseInterfaceWithWildcards() throws Exception {
		EntityManager aManager = Persistence.createEntityManagerFactory("test-data-source").createEntityManager();

		try {
			PersonWithWildcards wc = InstanceGenerator.generateInstanceClass(PersonWithWildcards.class).newInstance();
			PersonWithWildcards wc2 = InstanceGenerator.generateInstanceClass(PersonWithWildcards.class).newInstance();

			wc.setRdfId(new SupportsRdfId.URIKey(URI.create("urn:wc")));
			wc2.setRdfId(new SupportsRdfId.URIKey(URI.create("urn:wc2")));

			wc.setHasContact(Collections.singletonList(wc2));
			
			aManager.persist(wc);

			assertTrue(aManager.find(PersonWithWildcards.class, wc.getRdfId().value()) != null);
		}
		finally {
			aManager.close();
		}
	}
}
