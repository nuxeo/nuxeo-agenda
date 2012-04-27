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
import org.nuxeo.ecm.core.api.ClientException;
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
    public void run() throws ClientException {
        if (dtEnd == null) {
            dtEnd = new DateTime(dtStart).plusHours(1).toDate();
        }

        AgendaEventBuilder aeb = AgendaEventBuilder.build(summary, dtStart,
                dtEnd).description(description).location(location);
        agendaService.createEvent(session, contextPath, aeb.toMap());
    }
}
