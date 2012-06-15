package org.nuxeo.ecm.agenda;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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

    public static final String SCHEDULABLE_TYPE = "Schedulable";

    protected static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();

    protected static final String QUERY_BETWEEN_DATES = "SELECT * FROM Document WHERE "
            + "ecm:mixinType = '"
            + SCHEDULABLE_TYPE
            + "' "
            + "AND ((vevent:dtstart BETWEEN TIMESTAMP '%s' AND TIMESTAMP '%s') "
            + "OR (vevent:dtend BETWEEN TIMESTAMP '%s' AND TIMESTAMP '%s') "
            + "OR (vevent:dtstart < TIMESTAMP '%s' AND vevent:dtend > TIMESTAMP '%s') "
            + "OR (vevent:dtstart > TIMESTAMP '%s' AND vevent:dtend < TIMESTAMP '%s')) "
            + "AND ecm:currentLifeCycleState != 'deleted' "
            + "AND ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0 "
            + "AND ecm:path STARTSWITH '%s' ORDER BY vevent:dtstart";

    protected static final String QUERY_LIMIT = "SELECT * FROM Document WHERE "
            + "ecm:mixinType = '" + SCHEDULABLE_TYPE + "' "
            + "AND vevent:dtend > TIMESTAMP '%s' "
            + "AND ecm:currentLifeCycleState != 'deleted' "
            + "AND ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0 "
            + "AND ecm:path STARTSWITH '%s' ORDER BY vevent:dtstart";

    private static final Log log = LogFactory.getLog(AgendaComponent.class);

    @Override
    public DocumentModelList listEvents(CoreSession session, String path,
            Date dtStart, Date dtEnd) throws ClientException {
        if (dtStart == null) {
            throw new ClientException("Start datetime should not be null");
        }
        if (dtEnd == null) {
            dtEnd = new Date(dtStart.getTime() + 24 * 3600);
        }
        if (dtEnd.before(dtStart)) {
            throw new ClientException("End datetime is before start datetime");
        }

        String strStart = formatDate(dtStart);
        String strEnd = formatDate(dtEnd);
        return session.query(String.format(QUERY_BETWEEN_DATES, strStart,
                strEnd, strStart, strEnd, strStart, strEnd, strStart, strEnd,
                path));
    }

    @Override
    public DocumentModelList listEvents(CoreSession session, String path,
            int limit) throws ClientException {
        if (limit <= 0) {
            throw new ClientException("Limit must be greater than 0");
        }

        return session.query(
                String.format(QUERY_LIMIT, formatDate(new Date()), path), limit);
    }

    protected static String formatDate(Date date) {
        return new DateTime(date.getTime()).toString(dateTimeFormatter);
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