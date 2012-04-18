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
@Operation(id = CreateAgendum.ID, category = Constants.CAT_DOCUMENT, label = "Create Event", description = "Create a new Event document")
public class CreateAgendum {
    protected static final String ID = "Event.Create";

    @Context
    protected CoreSession session;

    @Param(name = "title")
    protected String title;

    @Param(name = "startDate")
    protected Date startDate;

    @Param(name = "endDate")
    protected Date endDate;

    @Param(name = "contextPath")
    protected String contextPath;

    @Param(name = "description", required = false)
    protected String description = "";

    @Param(name = "place", required = false)
    protected String place = "";

    @Param(name = "startTime", required = false)
    protected String startTime = "";

    @Param(name = "endTime", required = false)
    protected String endTime = "";

    @OperationMethod
    public DocumentModel run(DocumentModel doc) {
        return null;
    }
}
