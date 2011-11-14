package com.clarkparsia.empire.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.EmpireGenerated;
import com.clarkparsia.empire.EmpireOptions;
import com.clarkparsia.empire.jena.JenaEmpireModule;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
import com.clarkparsia.empire.test.typing.A;
import com.clarkparsia.empire.test.typing.AnotherB;
import com.clarkparsia.empire.test.typing.B;
import com.clarkparsia.empire.test.util.TestModule;
import com.clarkparsia.empire.util.DefaultEmpireModule;
import com.clarkparsia.empire.util.EmpireUtil;

public class TestTyping {
	public static final String DATA_FILE = "test/data/typing.ttl";
	
	private static boolean enforceEntityAnnotation;
	
    private static EntityManager mManager;
	
	@BeforeClass
	public static void beforeClass () {
		System.setProperty("empire.configuration.file", "test.empire.config.properties");
		enforceEntityAnnotation = EmpireOptions.ENFORCE_ENTITY_ANNOTATION;
		//EmpireOptions.ENFORCE_ENTITY_ANNOTATION = false;
		
		Empire.init(new DefaultEmpireModule(), new OpenRdfEmpireModule(),
					new JenaEmpireModule(), new TestModule());
	}
	
	@AfterClass
	public static void afterClass() {
		//EmpireOptions.ENFORCE_ENTITY_ANNOTATION = true;
	}

	@Before
	public void before() {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put("factory", "sesame");
		aMap.put("files", DATA_FILE);

		mManager = Empire.get().persistenceProvider().createEntityManagerFactory("test", aMap).createEntityManager();
	}
	
	@Test
	public void testDirectTypingRead1() {
		A a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));
		
		assertNotNull(a);
		assertEquals("A", a.getPropA());
		assertTrue(a instanceof EmpireGenerated);
		
		EmpireGenerated empireGenerated = (EmpireGenerated) a;			
		assertEquals(7, empireGenerated.getAllTriples().size());
		assertEquals(6, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testDirectReadWrite() {
		A a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));
		EmpireGenerated empireGenerated = (EmpireGenerated) a;			
		assertEquals(7, empireGenerated.getAllTriples().size());
		
		mManager.merge(a);
		
		a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));
		
		empireGenerated = (EmpireGenerated) a;					
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
 		assertEquals(7, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(6, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testDirectTypingRead2() {
		B b = mManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		
		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());
		
		assertTrue(b instanceof EmpireGenerated);
		
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(3, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testDirectReadWrite2() {
		B b = mManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(3, empireGenerated.getAllTriples().size());
		
		mManager.merge(b);
		
		b = mManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		
		empireGenerated = (EmpireGenerated) b;			
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
 		assertEquals(3, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testIndirectTypingRead1() {
		A a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		
		assertNotNull(a);
		assertEquals("A", a.getPropA());
		
		assertTrue(a instanceof B);
		
		B b = (B) a;
		
		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());
		
		assertTrue(b instanceof EmpireGenerated);
		
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(3, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testIndirectReadWrite1() {
		A a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		EmpireGenerated empireGenerated = (EmpireGenerated) a;			
		assertEquals(3, empireGenerated.getAllTriples().size());
		
		mManager.merge(a);
		
		a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		
		empireGenerated = (EmpireGenerated) a;			
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
 		assertEquals(3, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
		
		B b = mManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b1"));
		empireGenerated = (EmpireGenerated) b;			
		// TODO: Empire is now generating duplicates which is probably OK, but annoying with Sesame
 		assertEquals(3, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());

	}

	
	@Test
	public void testDirectTypingRead3() {
		B b = mManager.find(B.class, URI.create("urn:clarkparsia.com:empire:test:b2"));
		
		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());
		
		assertTrue(b instanceof EmpireGenerated);
		
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}

	
	@Test
	public void testIndirectTypingRead2() {
		A a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:b2"));
		
		assertNotNull(a);
		assertEquals("A", a.getPropA());
		
		assertTrue(a instanceof B);
		
		B b = (B) a;
		
		assertNotNull(b);
		assertEquals("A", b.getPropA());
		assertEquals("B", b.getPropB());
		
		assertTrue(b instanceof EmpireGenerated);
		
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(2, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testDirectTypingAlternativeRead() {
		AnotherB b = mManager.find(AnotherB.class, URI.create("urn:clarkparsia.com:empire:test:b2"));
		
		assertNotNull(b);
		assertEquals("B", b.getPropB());
		
		assertTrue(b instanceof EmpireGenerated);
		
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(1, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testDirectTypingAlternativeReadWrite() {
		AnotherB b = mManager.find(AnotherB.class, URI.create("urn:clarkparsia.com:empire:test:b2"));
		EmpireGenerated empireGenerated = (EmpireGenerated) b;			
		assertEquals(4, empireGenerated.getAllTriples().size());
		assertEquals(1, empireGenerated.getInstanceTriples().size());
		
		mManager.merge(b);
		
		b = mManager.find(AnotherB.class, URI.create("urn:clarkparsia.com:empire:test:b2"));
		
		empireGenerated = (EmpireGenerated) b;			
		assertEquals(4, new HashSet<Statement>(empireGenerated.getAllTriples()).size());
		assertEquals(1, empireGenerated.getInstanceTriples().size());
	}
	
	@Test
	public void testLinks() {
		A a = mManager.find(A.class, URI.create("urn:clarkparsia.com:empire:test:a1"));
		
		assertEquals(5, a.getA().size());
		
		for (A linkedA : a.getA()) {
			if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:a2")) {
				assertEquals("A-a2", linkedA.getPropA());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b1")) {
				assertEquals("A", linkedA.getPropA());
				
				assertTrue(linkedA instanceof B);
				assertEquals("B", ((B) linkedA).getPropB());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b2")) {
				assertEquals("A", linkedA.getPropA());
				
				assertTrue(linkedA instanceof B);
				assertEquals("B", ((B) linkedA).getPropB());
			}
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b3")) {
				assertEquals("A-b3", linkedA.getPropA());
				
				assertTrue(linkedA instanceof B);
				assertEquals("B-b3", ((B) linkedA).getPropB());
			}			
			else if (linkedA.getRdfId().value().toString().equals("urn:clarkparsia.com:empire:test:b4")) {
				assertEquals("A-b4", linkedA.getPropA());
				
				assertTrue(linkedA instanceof B);
				assertEquals("B-b4", ((B) linkedA).getPropB());
			}
		}
	}
}
