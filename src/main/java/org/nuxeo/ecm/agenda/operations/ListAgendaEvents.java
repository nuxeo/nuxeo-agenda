package org.nuxeo.ecm.agenda.operations;

import java.util.Date;

import org.nuxeo.ecm.agenda.AgendaService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;

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
    protected AgendaService agendaService;

    @Context
    protected CoreSession session;

    @Param(name = "dtStart", required = false)
    protected Date dtStart;

    @Param(name = "dtEnd", required = false)
    protected Date dtEnd;

    @Param(name = "limit", required = false)
    protected int limit = 5;

    @OperationMethod
    public DocumentModelList run() throws ClientException {
        if (dtStart != null) {
            return agendaService.listEvents(session, dtStart, dtEnd);
        } else {
            return agendaService.listEvents(session, limit);
        }
    }
}
