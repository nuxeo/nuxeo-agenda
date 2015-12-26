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

import java.util.Date;

import org.joda.time.DateTime;
import org.nuxeo.ecm.agenda.AgendaEventBuilder;
import org.nuxeo.ecm.agenda.AgendaService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;

/**
 * Operation to create a new Agenda Event document
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
@Operation(id = CreateAgendaEvent.ID, category = Constants.CAT_DOCUMENT, label = "Create Event", description = "Create a new Event document")
public class CreateAgendaEvent {
    protected static final String ID = "VEVENT.Create";

    @Context
    protected CoreSession session;

    @Context
    protected AgendaService agendaService;

    @Param(name = "summary")
    protected String summary;

    @Param(name = "dtStart")
    protected Date dtStart;

    @Param(name = "dtEnd", required = false)
    protected Date dtEnd;

    @Param(name = "contextPath", required = false)
    protected String contextPath;

    @Param(name = "description", required = false)
    protected String description = "";

    @Param(name = "location", required = false)
    protected String location = "";

    @OperationMethod
    public void run() {
        if (dtEnd == null) {
            dtEnd = new DateTime(dtStart).plusHours(1).toDate();
        }

        AgendaEventBuilder aeb = AgendaEventBuilder.build(summary, dtStart, dtEnd).description(description).location(
                location);
        agendaService.createEvent(session, contextPath, aeb.toMap());
    }
}
