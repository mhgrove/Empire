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

package com.clarkparsia.empire.test.lazyload;

import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;

import com.clarkparsia.empire.test.lazyload.Event.Status;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLazyCollectionLoad {

	private final static String TITLE = "Test";

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("empire.configuration.file", "test.empire.config.properties");
		Empire.init(new OpenRdfEmpireModule());
	}

	/**
	 * Test case based on Laurent's bug report where lazy loaded collections where getting mangled over
	 * repeated merges.
	 *
	 * @throws Exception test error
	 */
	@Test
    public void testLazyLoad() throws Exception {
        String id = "http://localhost:8080/empire/foo";

        EntityManagerFactory f = Persistence.createEntityManagerFactory("test-data-source");

        // First transaction (web service call).
		final EntityManager aEntityManager = f.createEntityManager();

		BusinessObject aBusinessObject = newBusiness(id, aEntityManager);

		assertEquals(TITLE, aBusinessObject.getTitle());
		assertEquals(id, aBusinessObject.getUri());
		assertEquals(1, aBusinessObject.getEvents().size());

		assertEvent1(aBusinessObject.getEvents().iterator().next());

        // Second transaction (web service call).
        aBusinessObject = addEvent(id, "Event #2", aEntityManager);
		assertEquals(TITLE, aBusinessObject.getTitle());
		assertEquals(id, aBusinessObject.getUri());
		assertEquals(2, aBusinessObject.getEvents().size());

		Iterator<Event> aIter = aBusinessObject.getEvents().iterator();
		assertEvent1(aIter.next());
		assertEvent2(aIter.next());

        // Third transaction (web service call).
		aBusinessObject = addEvent(id, "Event #3", aEntityManager);
		assertEquals(TITLE, aBusinessObject.getTitle());
		assertEquals(id, aBusinessObject.getUri());
		assertEquals(3, aBusinessObject.getEvents().size());

		aIter = aBusinessObject.getEvents().iterator();
		assertEvent1(aIter.next());
		assertEvent2(aIter.next());
		assertEvent3(aIter.next());

        // Read object final state.
		aBusinessObject = addEvent(id, null, aEntityManager);
		assertEquals(TITLE, aBusinessObject.getTitle());
		assertEquals(id, aBusinessObject.getUri());
		assertEquals(3, aBusinessObject.getEvents().size());

		aIter = aBusinessObject.getEvents().iterator();
		assertEvent1(aIter.next());
		assertEvent2(aIter.next());
		assertEvent3(aIter.next());
    }

	private void assertEvent1(final Event theEvent) {
		assertEquals(Status.Complete, theEvent.getStatus());
		assertEquals("Event #1", theEvent.getParameters());
	}

	private void assertEvent2(final Event theEvent) {
		assertEquals(Status.Complete, theEvent.getStatus());
		assertEquals("Event #2", theEvent.getParameters());
	}

	private void assertEvent3(final Event theEvent) {
		assertEquals(Status.Complete, theEvent.getStatus());
		assertEquals("Event #3", theEvent.getParameters());
	}

	private static BusinessObject newBusiness(String uri, EntityManager m) {
        BusinessObject b = new BusinessObject(uri);

		b.setTitle(TITLE);
        b.add(new Event(b.getUri(), "Event #1", Status.Complete, null));
        m.persist(b);
        m.flush();
        return b;
    }

    private static BusinessObject addEvent(String uri, String title, EntityManager m) {
        BusinessObject b = m.find(BusinessObject.class, uri);
        if (title != null) {
            Event e = new Event(uri, title, Status.Complete, null);
            m.persist(e);
            b.add(e);
            b = m.merge(b);
            m.flush();
        }
        return b;
    }
}
