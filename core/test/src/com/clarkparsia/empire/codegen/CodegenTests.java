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

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.Persistence;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.api.TestInterface;
import com.clarkparsia.empire.util.TestModule;
import com.clarkparsia.empire.util.TestUtil;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>Code generation unit tests</p>
 *
 * @author  Michael Grove
 * @since   0.7
 * @version 0.7
 */
public class CodegenTests {

    @BeforeClass
    public static void beforeClass () {
        System.setProperty("empire.configuration.file", new File(TestUtil.getProjectHome(), "core/test/test.empire.config.properties").getAbsolutePath());
        Empire.init(new TestModule());
    }

	@Test
	public void testCodeGenWithWildcards() throws Exception {
		InstanceGenerator.generateInstanceClass(PersonWithWildcards.class);
	}

	@Test
	public void testUseInterfaceWithWildcards() throws Exception {
		EntityManager aManager = Persistence.createEntityManagerFactory("test-data-source2").createEntityManager();

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


	@Test
	public void testInstGen() throws Exception {
		Class<TestInterface> aIntClass = InstanceGenerator.generateInstanceClass(TestInterface.class);

		TestInterface aInt = aIntClass.newInstance();

		// this should successfully re-use the previously generated class file.  we want to make sure
		// this can happen without error.
		TestInterface aInt2 = InstanceGenerator.generateInstanceClass(TestInterface.class).newInstance();

		URI aURI = URI.create("urn:uri");
		Integer aNumber = 5;
		String aStr = "some string value";
		SupportsRdfId.RdfKey aKey = new SupportsRdfId.URIKey(URI.create("urn:id"));
		SupportsRdfId.RdfKey aKey2 = new SupportsRdfId.URIKey(URI.create("urn:id2"));

		aInt.setURI(aURI);
		aInt.setInt(aNumber);
		aInt.setString(aStr);
		aInt.setRdfId(aKey);

		aInt2.setRdfId(aKey2);

		aInt.setObject(aInt2);

		assertEquals(aInt, aInt);
		assertEquals(aURI, aInt.getURI());
		assertEquals(aStr, aInt.getString());
		assertEquals(aNumber, aInt.getInt());
		assertEquals(aKey, aInt.getRdfId());
		assertEquals(aInt2, aInt.getObject());
		assertEquals(aKey.toString(), aInt.toString());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBadInstGen() throws Exception {
		InstanceGenerator.generateInstanceClass(BadTestInterface.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNoSupportsInstGen() throws Exception {
		InstanceGenerator.generateInstanceClass(NoSupportsTestInterface.class);
	}

	@Test
	public void testGenerateSuperInterfaces() {
		try {
			BaseInterface aBase = InstanceGenerator.generateInstanceClass(BaseInterface.class).newInstance();

			aBase.setFoo("some value");
			assertEquals("some value", aBase.getFoo());

			ChildInterface aChild = InstanceGenerator.generateInstanceClass(ChildInterface.class).newInstance();

			aChild.setBar(23);
			assertEquals(23, aChild.getBar());

			aChild.setFoo("some value");
			assertEquals("some value", aChild.getFoo());
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFunkyInstGenWithBoolean() throws Exception {
		IFoo aFoo = InstanceGenerator.generateInstanceClass(FooImpl.class).newInstance();

		assertTrue(aFoo.isDereferenced());
	}
	
	@Test
	public void testMultipleMethodAnnotationInheritance() throws Exception {
	    // issue #105
	    PersonWithMultipleMethodAnnotations p = InstanceGenerator.generateInstanceClass(PersonWithMultipleMethodAnnotations.class).newInstance();
	    Annotation[] annos = p.getClass().getDeclaredMethod("getHasContact").getAnnotations();
	    assertEquals(2, annos.length);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testInstGenRdfIdSetter() throws Exception {
		/*
		 * We want the generated instance to disallow rdfId being modified after it is set
		 * https://github.com/mhgrove/Empire/pull/115
		 */
		TestInterface aInt = InstanceGenerator.generateInstanceClass(TestInterface.class).newInstance();
		SupportsRdfId.RdfKey aKey = new SupportsRdfId.URIKey(URI.create("urn:id"));
		SupportsRdfId.RdfKey aKey2 = new SupportsRdfId.URIKey(URI.create("urn:id2"));
		aInt.setRdfId(aKey);
		aInt.setRdfId(aKey2);
	}

	public interface NoSupportsTestInterface {
		public String getBar();
		public void setBar(String theStr);
	}

	public interface BadTestInterface extends SupportsRdfId {
		public void foo();

		public String getBar();
		public void setBar(String theStr);
	}

	public interface BaseInterface extends SupportsRdfId {
		public String getFoo();
		public void setFoo(String theString);
	}

	public interface ChildInterface extends BaseInterface {
		public long getBar();
		public void setBar(long theString);
	}

	public static abstract class FooImpl extends AbstractFoo implements IFoo {
	}

	public static interface IFoo {
		public boolean isDereferenced();
	}

	@MappedSuperclass
	public static abstract class AbstractFoo implements SupportsRdfId {
		/**
		 * @inheritDoc
		 */
		public boolean isDereferenced() {
			return true;
		}
	}
}
