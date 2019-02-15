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
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;

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

    public static final String QUERY_LIST_ALL_EVENTS = "Select * from " + VEVENT_TYPE;

    public static final String QUERY_LIST_EVENTS_DIR = "Select * from " + VEVENT_TYPE
            + " where ecm:path STARTSWITH '%s'";

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
        AgendaEventBuilder build = AgendaEventBuilder.build("mon event", new Date(),
                Date.from(ZonedDateTime.now().plusDays(2).toInstant()));
        assertNotNull(agendaService.createEvent(session, "/", build.toMap()));

        build.summary("second event");
        assertNotNull(agendaService.createEvent(session, null, build.toMap()));

        build.summary("third event");
        assertNotNull(agendaService.createEvent(session, "/default-domain/", build.toMap()));
        session.save();

        DocumentModelList res = session.query(
                String.format(QUERY_LIST_EVENTS_DIR, getUserWorkspace().getPathAsString()));
        assertEquals(2, res.size());

        res = session.query(QUERY_LIST_ALL_EVENTS);
        assertEquals(3, res.size());
    }

    @Test
    public void testEventsList() {
        AgendaEventBuilder pastEvent = AgendaEventBuilder.build("past event",
                Date.from(ZonedDateTime.now().minusDays(10).toInstant()),
                Date.from(ZonedDateTime.now().minusDays(9).toInstant()));
        AgendaEventBuilder incomingEvent = AgendaEventBuilder.build("incoming event",
                Date.from(ZonedDateTime.now().plusDays(2).toInstant()),
                Date.from(ZonedDateTime.now().plusDays(3).toInstant()));
        AgendaEventBuilder currentEvent = AgendaEventBuilder.build("current event",
                Date.from(ZonedDateTime.now().minusDays(1).toInstant()),
                Date.from(ZonedDateTime.now().plusDays(1).toInstant()));
        AgendaEventBuilder todayEvent = AgendaEventBuilder.build("today event",
                Date.from(ZonedDateTime.now().withHour(4).toInstant()),
                Date.from(ZonedDateTime.now().withHour(6).toInstant()));
        AgendaEventBuilder longCurrentEvent = AgendaEventBuilder.build("today event",
                Date.from(ZonedDateTime.now().minusDays(10).toInstant()),
                Date.from(ZonedDateTime.now().plusDays(20).toInstant()));

        agendaService.createEvent(session, null, pastEvent.toMap());
        agendaService.createEvent(session, null, incomingEvent.toMap());
        agendaService.createEvent(session, null, currentEvent.toMap());
        agendaService.createEvent(session, null, todayEvent.toMap());
        agendaService.createEvent(session, null, longCurrentEvent.toMap());
        session.save();

        assertEquals(5, session.query(QUERY_LIST_ALL_EVENTS).size());

        DocumentModelList events = agendaService.listEvents(session, "/",
                Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()),
                Date.from(ZonedDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999).toInstant()));
        assertEquals(3, events.size());

        events = agendaService.listEvents(session, "/",
                Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()),
                Date.from(ZonedDateTime.now().plusDays(5).toInstant()));
        assertEquals(4, events.size());

        events = agendaService.listEvents(session, "/", Date.from(ZonedDateTime.now().minusDays(12).toInstant()),
                Date.from(ZonedDateTime.now().minusDays(2).toInstant()));
        assertEquals(2, events.size());

        events = agendaService.listEvents(session, "/", Date.from(ZonedDateTime.now().minusDays(3).toInstant()),
                Date.from(ZonedDateTime.now().minusDays(2).toInstant()));
        assertEquals(1, events.size());

        events = agendaService.listEvents(session, "/", Date.from(ZonedDateTime.now().minusDays(12).toInstant()),
                Date.from(ZonedDateTime.now().minusDays(11).toInstant()));
        assertEquals(0, events.size());
    }

    @Test
    public void testListWithLimit() {
        AgendaEventBuilder incEvent = AgendaEventBuilder.build("inc event",
                Date.from(ZonedDateTime.now().plusDays(1).toInstant()),
                Date.from(ZonedDateTime.now().plusDays(2).toInstant()));
        AgendaEventBuilder pastEvent = AgendaEventBuilder.build("inc event",
                Date.from(ZonedDateTime.now().minusDays(1).toInstant()),
                Date.from(ZonedDateTime.now().minusDays(1).toInstant()));
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

        AgendaEventBuilder midnightTickParty = AgendaEventBuilder.build("past event",
                Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()),
                Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()));
        agendaService.createEvent(session, null, midnightTickParty.toMap());

        midnightTickParty = AgendaEventBuilder.build("past event", Date.from(
                ZonedDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()),
                Date.from(ZonedDateTime.now()
                                       .plusDays(2)
                                       .withHour(0)
                                       .withMinute(0)
                                       .withSecond(0)
                                       .withNano(0)
                                       .toInstant()));
        agendaService.createEvent(session, null, midnightTickParty.toMap());
        session.save();

        assertEquals(2, session.query(QUERY_LIST_ALL_EVENTS).size());
        DocumentModelList events = agendaService.listEvents(session, "/",
                Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()), null);
        assertEquals(1, events.size());
    }

    @Test(expected = NuxeoException.class)
    public void withStartNull() {
        agendaService.listEvents(session, "/", null, new Date());
    }

    @Test(expected = NuxeoException.class)
    public void withEndBeforeStart() {
        agendaService.listEvents(session, "/", Date.from(ZonedDateTime.now().plusDays(2).toInstant()), new Date());
    }

    @Test
    public void testBuilder() {
        Date dtStart = Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant());
        Date dtEnd = Date.from(ZonedDateTime.now().withHour(1).withMinute(0).withSecond(0).withNano(0).toInstant());

        AgendaEventBuilder aeb = AgendaEventBuilder.build("summary", dtStart, dtEnd);
        aeb.description("description");
        aeb.location("location");

        Map<String, Serializable> properties = aeb.toMap();
        properties.put("dummy:content", "should not be thrown");
        DocumentModel event = agendaService.createEvent(session, null, properties);

        assertEquals(event.getPropertyValue("dc:title"), "summary");
        assertEquals(event.getPropertyValue("dc:description"), "description");
        assertEquals(((GregorianCalendar) event.getPropertyValue("vevent:dtstart")).getTime(), dtStart);
        assertEquals(((GregorianCalendar) event.getPropertyValue("vevent:dtend")).getTime(), dtEnd);
        assertEquals(event.getPropertyValue("vevent:location"), "location");
        assertEquals(event.getPropertyValue("vevent:status"), "CONFIRMED");
        assertEquals(event.getPropertyValue("vevent:transp"), "OPAQUE");
    }

    protected DocumentModel getUserWorkspace() {
        return userWorkspaceService.getCurrentUserPersonalWorkspace(session, session.getRootDocument());
    }
}
