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

package com.clarkparsia.empire.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.openrdf.model.Resource;
import org.openrdf.model.BNode;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.clarkparsia.empire.codegen.InstanceGenerator;
import com.clarkparsia.empire.test.api.TestInterface;
import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.util.EmpireUtil;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;

import java.net.URI;
import java.net.URL;

/**
 * <p>Various miscellaneous tests for non-JPA parts of the Empire API.</p>
 *
 * @author Michael Grove
 * @version 0.6.4
 * @since 0.6.4
 */
public class TestMisc {
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
	public void testEmpireUtil() throws Exception {
		SupportsRdfId aId = new SupportsRdfIdImpl();

		assertTrue(EmpireUtil.asResource(aId) == null);

		Resource aRes = EmpireUtil.asResource(new SupportsRdfIdImpl(new SupportsRdfId.BNodeKey("asdf")));
		assertTrue(aRes instanceof BNode);
		assertEquals(((BNode)aRes).getID(), "asdf");

		aId = EmpireUtil.asSupportsRdfId(java.net.URI.create("urn:foo"));
		assertTrue(aId.getRdfId() instanceof SupportsRdfId.URIKey);
		assertEquals(aId.getRdfId().value(), java.net.URI.create("urn:foo"));

		assertTrue(EmpireUtil.getNamedGraph("") == null);

		SupportsRdfId.RdfKey aKey = EmpireUtil.asPrimaryKey(new URL("http://example.org"));
		assertTrue(aKey instanceof SupportsRdfId.URIKey);
		assertEquals(aKey.value(), new URL("http://example.org").toURI());

		BNode aAnon = ValueFactoryImpl.getInstance().createBNode("foobar");
		aKey = EmpireUtil.asPrimaryKey(aAnon);
		assertTrue(aKey instanceof SupportsRdfId.BNodeKey);
		assertEquals(aKey.value(), "foobar");
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

	public interface BaseInterface extends SupportsRdfId {
		public String getFoo();
		public void setFoo(String theString);
	}

	public interface ChildInterface extends BaseInterface {
		public long getBar();
		public void setBar(long theString);
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
}
