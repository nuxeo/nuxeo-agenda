/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Arnaud Kervern
 */
package org.nuxeo.ecm.agenda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.ecm.agenda.AgendaComponent.VEVENT_TYPE;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@Deploy("org.nuxeo.ecm.agenda")
@Deploy("org.nuxeo.ecm.platform.userworkspace.core")
@Deploy("org.nuxeo.ecm.platform.userworkspace.types")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class AgendaServiceTest {
    public static final String QUERY_LIST_ALL_EVENTS = "Select * from "
            + VEVENT_TYPE;

    public static final String QUERY_LIST_EVENTS_DIR = "Select * from "
            + VEVENT_TYPE + " where ecm:path STARTSWITH '%s'";

    @Inject
    protected AgendaService agendaService;

    @Inject
    protected UserWorkspaceService userWorkspaceService;

    @Inject
    protected CoreSession session;

    @Before
    public void beforeTest() {
        // Ensure to commit after UserWorkspace creation.
        getUserWorkspace();
        session.save();
        assertEquals(0, session.query(QUERY_LIST_ALL_EVENTS).size());
    }

    @Test
    public void testEventCreation() {
        assertNotNull(agendaService);
        AgendaEventBuilder build = AgendaEventBuilder.build("mon event",
                NOW().toDate(), NOW().plusDays(2).toDate());
        assertNotNull(agendaService.createEvent(session, "/", build.toMap()));

        build.summary("second event");
        assertNotNull(agendaService.createEvent(session, null, build.toMap()));

        build.summary("third event");
        assertNotNull(agendaService.createEvent(session, "/default-domain/",
                build.toMap()));
        session.save();

        DocumentModelList res = session.query(String.format(
                QUERY_LIST_EVENTS_DIR, getUserWorkspace().getPathAsString()));
        assertEquals(2, res.size());

        res = session.query(QUERY_LIST_ALL_EVENTS);
        assertEquals(3, res.size());
    }

    @Test
    public void testEventsList() {
        AgendaEventBuilder pastEvent = AgendaEventBuilder.build("past event",
                NOW().minusDays(10).toDate(), NOW().minusDays(9).toDate());
        AgendaEventBuilder incomingEvent = AgendaEventBuilder.build(
                "incoming event", NOW().plusDays(2).toDate(),
                NOW().plusDays(3).toDate());
        AgendaEventBuilder currentEvent = AgendaEventBuilder.build(
                "current event", NOW().minusDays(1).toDate(),
                NOW().plusDays(1).toDate());
        AgendaEventBuilder todayEvent = AgendaEventBuilder.build("today event",
                NOW().withHourOfDay(4).toDate(),
                NOW().withHourOfDay(6).toDate());
        AgendaEventBuilder longCurrentEvent = AgendaEventBuilder.build(
                "today event", NOW().minusDays(10).toDate(),
                NOW().plusDays(20).toDate());

        agendaService.createEvent(session, null, pastEvent.toMap());
        agendaService.createEvent(session, null, incomingEvent.toMap());
        agendaService.createEvent(session, null, currentEvent.toMap());
        agendaService.createEvent(session, null, todayEvent.toMap());
        agendaService.createEvent(session, null, longCurrentEvent.toMap());
        session.save();

        assertEquals(5, session.query(QUERY_LIST_ALL_EVENTS).size());

        DocumentModelList events = agendaService.listEvents(session, "/",
                NOW().withTime(0, 0, 0, 0).toDate(),
                NOW().withTime(23, 59, 59, 999).toDate());
        assertEquals(3, events.size());

        events = agendaService.listEvents(session, "/",
                NOW().withTime(0, 0, 0, 0).toDate(), NOW().plusDays(5).toDate());
        assertEquals(4, events.size());

        events = agendaService.listEvents(session, "/",
                NOW().minusDays(12).toDate(), NOW().minusDays(2).toDate());
        assertEquals(2, events.size());

        events = agendaService.listEvents(session, "/",
                NOW().minusDays(3).toDate(), NOW().minusDays(2).toDate());
        assertEquals(1, events.size());

        events = agendaService.listEvents(session, "/",
                NOW().minusDays(12).toDate(), NOW().minusDays(11).toDate());
        assertEquals(0, events.size());
    }

    @Test
    public void testListWithLimit() {
        AgendaEventBuilder incEvent = AgendaEventBuilder.build("inc event",
                NOW().plusDays(1).toDate(), NOW().plusDays(2).toDate());
        AgendaEventBuilder pastEvent = AgendaEventBuilder.build("inc event",
                NOW().minusDays(1).toDate(), NOW().minusDays(1).toDate());
        // create 2 past events
        agendaService.createEvent(session, null, pastEvent.toMap());
        agendaService.createEvent(session, null, pastEvent.toMap());

        // create 5 inc events
        agendaService.createEvent(session, null, incEvent.toMap());
        agendaService.createEvent(session, null, incEvent.toMap());
        agendaService.createEvent(session, null, incEvent.toMap());
        agendaService.createEvent(session, null, incEvent.toMap());
        agendaService.createEvent(session, null, incEvent.toMap());
        session.save();

        assertEquals(3, agendaService.listEvents(session, "/", 3).size());
        assertEquals(5, agendaService.listEvents(session, "/", 10).size());
    }

    @Test
    public void testLimitCase() {
        AgendaEventBuilder midnightTickParty = AgendaEventBuilder.build(
                "past event", NOW().withTime(0, 0, 0, 0).toDate(),
                NOW().withTime(0, 0, 0, 0).toDate());
        agendaService.createEvent(session, null, midnightTickParty.toMap());

        midnightTickParty = AgendaEventBuilder.build("past event",
                NOW().plusDays(1).withTime(0, 0, 0, 0).toDate(),
                NOW().plusDays(2).withTime(0, 0, 0, 0).toDate());
        agendaService.createEvent(session, null, midnightTickParty.toMap());
        session.save();

        assertEquals(2, session.query(QUERY_LIST_ALL_EVENTS).size());
        DocumentModelList events = agendaService.listEvents(session, "/",
                NOW().withTime(0, 0, 0, 0).toDate(), null);
        assertEquals(1, events.size());
    }

    @Test(expected = NuxeoException.class)
    public void withStartNull() {
        agendaService.listEvents(session, "/", null, NOW().toDate());
    }

    @Test(expected = NuxeoException.class)
    public void withEndBeforeStart() {
        agendaService.listEvents(session, "/", NOW().plusDays(2).toDate(),
                NOW().toDate());
    }

    @Test
    public void testBuilder() {
        Date dtStart = NOW().withTime(0, 0, 0, 0).toDate();
        Date dtEnd = NOW().withTime(1, 0, 0, 0).toDate();
        AgendaEventBuilder aeb = AgendaEventBuilder.build("summary", dtStart,
                dtEnd);
        aeb.description("description");
        aeb.location("location");

        Map<String, Serializable> properties = aeb.toMap();
        properties.put("dummy:content", "should not be thrown");
        DocumentModel event = agendaService.createEvent(session, null,
                properties);

        assertEquals(event.getPropertyValue("dc:title"), "summary");
        assertEquals(event.getPropertyValue("dc:description"), "description");
        assertEquals(
                ((GregorianCalendar) event.getPropertyValue("vevent:dtstart")).getTime(),
                dtStart);
        assertEquals(
                ((GregorianCalendar) event.getPropertyValue("vevent:dtend")).getTime(),
                dtEnd);
        assertEquals(event.getPropertyValue("vevent:location"), "location");
        assertEquals(event.getPropertyValue("vevent:status"), "CONFIRMED");
        assertEquals(event.getPropertyValue("vevent:transp"), "OPAQUE");
    }

    protected static DateTime NOW() {
        return new DateTime();
    }

    protected DocumentModel getUserWorkspace() {
        return userWorkspaceService.getCurrentUserPersonalWorkspace(session,
                session.getRootDocument());
    }
}
