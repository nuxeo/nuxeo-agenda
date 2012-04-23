package org.nuxeo.ecm.agenda.operations;

import java.util.Date;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Operation to create a new Agendum document
 * 
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
@Operation(id = CreateAgendaEvent.ID, category = Constants.CAT_DOCUMENT, label = "Create Event", description = "Create a new Event document")
public class CreateAgendaEvent {
    protected static final String ID = "VEVENT.Create";

    @Context
    protected CoreSession session;

    @Param(name = "summary")
    protected String summary;

    @Param(name = "dtStart")
    protected Date dtStart;

    @Param(name = "dtEnd")
    protected Date dtEnd;

    @Param(name = "contextPath")
    protected String contextPath;

    @Param(name = "description", required = false)
    protected String description = "";

    @Param(name = "location", required = false)
    protected String location = "";

    @OperationMethod
    public DocumentModel run(DocumentModel doc) {
        return null;
    }
}
