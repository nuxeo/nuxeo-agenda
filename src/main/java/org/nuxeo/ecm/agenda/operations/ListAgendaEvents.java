package org.nuxeo.ecm.agenda.operations;

import java.util.Date;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * Operation to list agendum between two dates to display them into a calendar
 * 
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
@Operation(id = CreateAgendaEvent.ID, category = Constants.CAT_DOCUMENT, label = "List Events", description = "List Events between two dates")
public class ListAgendaEvents {
    protected static final String ID = "VEVENT.List";

    @Param(name = "dtStart")
    protected Date dtStart;

    @Param(name = "dtEnd")
    protected Date dtEnd;

    @OperationMethod
    public DocumentModelList runVoid() {
        return null;
    }

    @OperationMethod
    public DocumentModelList runDocument(DocumentModel doc) {
        return null;
    }
}
