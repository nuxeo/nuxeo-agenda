package org.nuxeo.ecm.agenda;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
public interface AgendaService {
    /**
     * List user readable agendum between both date
     * 
     * @param sesion CoreSession used to querying
     * @param startDate the start date
     * @param endDate the end date (included)
     * @return matching Agendum as a DocumentModelList object
     */
    DocumentModelList listAgendum(CoreSession session, Date startDate,
            Date endDate) throws ClientException;

    /**
     * Create a new Agendum doc to the specific path. If the path is blank or
     * "/", Agendum wil be created into the UserWorkspace
     * 
     * @param sesion CoreSession used to create the new doc
     * @param path base path, if it is blank or "/"; Agendum will be created
     *            into the UserWorkspace
     * @param properties corresponding metadata
     * @return a new Agendum created
     */
    DocumentModel createAgendum(CoreSession session, String path,
            Map<String, Serializable> properties) throws ClientException;
}
