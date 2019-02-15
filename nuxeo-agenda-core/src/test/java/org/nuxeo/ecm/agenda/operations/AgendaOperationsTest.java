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
package org.nuxeo.ecm.agenda.operations;

import static java.time.ZonedDateTime.now;
import static java.util.Date.from;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.nuxeo.ecm.agenda.AgendaComponent.VEVENT_TYPE;
import static org.nuxeo.ecm.agenda.AgendaServiceTest.QUERY_LIST_ALL_EVENTS;

import java.util.Date;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.agenda.AgendaEventBuilder;
import org.nuxeo.ecm.agenda.AgendaService;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@RunWith(FeaturesRunner.class)
@Deploy("org.nuxeo.ecm.agenda")
@Deploy("org.nuxeo.ecm.platform.userworkspace.core")
@Deploy("org.nuxeo.ecm.platform.userworkspace.types")
@Deploy("org.nuxeo.ecm.platform.types.api")
@Deploy("org.nuxeo.ecm.platform.types.core")
@Deploy("org.nuxeo.ecm.platform.url.core")
@Features(EmbeddedAutomationServerFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class AgendaOperationsTest {

    @Inject
    protected CoreSession session;

    @Inject
    protected Session clientSession;

    @Inject
    protected AgendaService agendaService;

    @Inject
    protected AutomationService automationService;

    @Before
    public void assertEmpty() {
        assertEquals(0, session.query(QUERY_LIST_ALL_EVENTS).size());
    }

    @Test
    public void testCreateOperation() throws Exception {
        Date start = from(now().plusDays(1).toInstant());
        Date end = from(now().plusDays(2).toInstant());
        Object obj = clientSession.newRequest(CreateAgendaEvent.ID)
                                  .set("summary", "my new Event")
                                  .set("dtStart", start)
                                  .set("dtEnd", end)
                                  .set("description", "description")
                                  .set("location", "location")
                                  .execute();
        assertNull(obj);
        session.save();

        // MySQL needs to commit the transaction to see the updated state
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        DocumentModelList docs = session.query(QUERY_LIST_ALL_EVENTS);
        assertEquals(1, docs.size());
        DocumentModel event = docs.get(0);
        assertEquals(event.getPropertyValue("dc:title"), "my new Event");
        assertEquals(event.getPropertyValue("dc:description"), "description");
        assertEquals(event.getPropertyValue("vevent:location"), "location");
    }

    @Test
    public void testCreateWithContextPath() throws Exception {
        Date start = from(now().toInstant());
        clientSession.newRequest(CreateAgendaEvent.ID)
                     .set("summary", "my new Event")
                     .set("dtStart", start)
                     .set("contextPath", "/default-domain/")
                     .execute();

        // MySQL needs to commit the transaction to see the updated state
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        String query = "Select * from " + VEVENT_TYPE + " where ecm:path startswith '/default-domain/'";
        assertEquals(1, session.query(query).size());
    }

    @Test
    public void testListEventsWithDates() throws Exception {
        Date start = from(now().minusDays(1).toInstant());
        Date end = from(now().plusDays(1).toInstant());

        AgendaEventBuilder anEvent = AgendaEventBuilder.build("current event", start, end);

        agendaService.createEvent(session, "/default-domain/", anEvent.toMap());
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        start = from(now().minusDays(4).toInstant());
        end = from(now().plusDays(3).toInstant());

        Documents docs = (Documents) clientSession.newRequest(ListAgendaEvents.ID)
                                                  .set("dtStart", start)
                                                  .set("dtEnd", end)
                                                  .set("contextPath", "/")
                                                  .execute();
        assertEquals(1, docs.size());

        start = from(now().plusDays(10).toInstant());
        end = from(now().plusDays(11).toInstant());

        docs = (Documents) clientSession.newRequest(ListAgendaEvents.ID)
                                        .set("dtStart", start)
                                        .set("dtEnd", end)
                                        .set("contextPath", "/")
                                        .execute();
        assertEquals(0, docs.size());
    }

    @Test
    public void testListEventsWithLimit() throws Exception {

        Date start = from(now().plusDays(1).toInstant());
        Date end = from(now().plusDays(2).toInstant());

        AgendaEventBuilder incEvent = AgendaEventBuilder.build("inc event", start, end);
        // create 6 events
        agendaService.createEvent(session, "/default-domain/", incEvent.toMap());
        agendaService.createEvent(session, "/default-domain/", incEvent.toMap());
        agendaService.createEvent(session, "/default-domain/", incEvent.toMap());
        agendaService.createEvent(session, "/default-domain/", incEvent.toMap());
        agendaService.createEvent(session, "/default-domain/", incEvent.toMap());
        agendaService.createEvent(session, "/default-domain/", incEvent.toMap());
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        Documents docs = (Documents) clientSession.newRequest(ListAgendaEvents.ID).set("contextPath", "/").execute();
        assertEquals(5, docs.size());
        docs = (Documents) clientSession.newRequest(ListAgendaEvents.ID)
                                        .set("limit", 4)
                                        .set("contextPath", "/")
                                        .execute();
        assertEquals(4, docs.size());
        docs = (Documents) clientSession.newRequest(ListAgendaEvents.ID)
                                        .set("limit", 15)
                                        .set("contextPath", "/")
                                        .execute();
        assertEquals(6, docs.size());
    }

}
