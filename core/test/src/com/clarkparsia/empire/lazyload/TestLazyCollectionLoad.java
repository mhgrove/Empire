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

package com.clarkparsia.empire.lazyload;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.annotation.RdfGenerator;

import com.clarkparsia.empire.lazyload.Event.Status;

import com.clarkparsia.empire.util.TestModule;
import com.clarkparsia.empire.util.TestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLazyCollectionLoad {

	private final static String TITLE = "Test";

	@BeforeClass
	public static void beforeClass() {
        TestUtil.setConfigSystemProperty( "test.empire.config.properties" );
		Empire.init(new TestModule());

        RdfGenerator.init(Sets.<Class<?>>newHashSet(BusinessObjectImpl.class, EventImpl.class));
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

        EntityManagerFactory f = Persistence.createEntityManagerFactory("test-data-source2");

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

		List<Event> aEvents = Lists.newArrayList(aBusinessObject.getEvents());
        Collections.sort(aEvents, new Comparator<Event>() {
            public int compare(final Event theEvent, final Event theEvent2) {
                return theEvent.getParameters().compareTo(theEvent2.getParameters());
            }
        });
        aIter = aEvents.iterator();

        assertEvent1(aIter.next());
		assertEvent2(aIter.next());
		assertEvent3(aIter.next());

        // Read object final state.
		aBusinessObject = addEvent(id, null, aEntityManager);
		assertEquals(TITLE, aBusinessObject.getTitle());
		assertEquals(id, aBusinessObject.getUri());
		assertEquals(3, aBusinessObject.getEvents().size());

        aEvents = Lists.newArrayList(aBusinessObject.getEvents());
        Collections.sort(aEvents, new Comparator<Event>() {
            public int compare(final Event theEvent, final Event theEvent2) {
                return theEvent.getParameters().compareTo(theEvent2.getParameters());
            }
        });
        aIter = aEvents.iterator();

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
        BusinessObject b = new BusinessObjectImpl(uri);

		b.setTitle(TITLE);
        b.add(new EventImpl(b.getUri(), "Event #1", Status.Complete, null));
        m.persist(b);
        m.flush();
        return b;
    }

    private static BusinessObject addEvent(String uri, String title, EntityManager m) {
        BusinessObject b = m.find(BusinessObjectImpl.class, uri);
        if (title != null) {
            Event e = new EventImpl(uri, title, Status.Complete, null);
            m.persist(e);
            b.add(e);
            b = m.merge(b);
            m.flush();
        }
        return b;
    }
}
