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

import org.nuxeo.ecm.agenda.AgendaService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.query.core.DocumentModelListPageProvider;

/**
 * Operation to list events between two dates to display them into a calendar
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
@Operation(id = ListAgendaEvents.ID, category = Constants.CAT_DOCUMENT, label = "List Events", description = "List Events between two dates")
public class ListAgendaEvents {

    protected static final String ID = "VEVENT.List";

    @Context
    protected OperationContext context;

    @Context
    protected AgendaService agendaService;

    @Context
    protected CoreSession session;

    @Param(name = "contextPath")
    protected String contextPath;

    @Param(name = "dtStart", required = false)
    protected Date dtStart;

    @Param(name = "dtEnd", required = false)
    protected Date dtEnd;

    @Param(name = "limit", required = false)
    protected int limit = 5;

    @Param(name = "documentLinkBuilder", required = false)
    protected String documentLinkBuilder;

    @OperationMethod
    public PaginableDocumentModelListImpl run() {
        DocumentModelList events;
        if (dtStart != null) {
            events = agendaService.listEvents(session, contextPath, dtStart, dtEnd);
        } else {
            events = agendaService.listEvents(session, contextPath, limit);
        }
        return new PaginableDocumentModelListImpl(new DocumentModelListPageProvider(events), documentLinkBuilder);
    }
}
