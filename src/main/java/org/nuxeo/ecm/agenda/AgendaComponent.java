package org.nuxeo.ecm.agenda;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
public class AgendaComponent extends DefaultComponent implements AgendaService {

    public static final String VEVENT_TYPE = "VEVENT";

    protected static final String QUERY = "SELECT * FROM VEVENT WHERE ... '%s' '%s'";

    private static final Log log = LogFactory.getLog(AgendaComponent.class);

    @Override
    public DocumentModelList listEvents(CoreSession session, Date dtStart,
            Date dtEnd) throws ClientException {
        return session.query(String.format(QUERY, dtStart, dtEnd));
    }

    @Override
    public DocumentModel createEvent(CoreSession session, String path,
            Map<String, Serializable> properties) throws ClientException {
        if (StringUtils.isBlank(path) || "/".equals(path)) {
            path = getCurrentUserWorkspacePath(session);
        }
        DocumentModel doc = session.createDocumentModel(VEVENT_TYPE);
        doc.setPathInfo(path, null);
        for (String key : properties.keySet()) {
            try {
                doc.setPropertyValue(key, properties.get(key));
            } catch (PropertyException pe) {
                log.info("Trying to set an unknown property " + key);
            }
        }
        return session.createDocument(doc);
    }

    protected String getCurrentUserWorkspacePath(CoreSession session)
            throws ClientException {
        UserWorkspaceService userWorkspaceService = Framework.getLocalService(UserWorkspaceService.class);
        DocumentModel userPersonalWorkspace = userWorkspaceService.getUserPersonalWorkspace(
                session.getPrincipal().getName(), session.getRootDocument());
        return userPersonalWorkspace.getPathAsString();
    }
}