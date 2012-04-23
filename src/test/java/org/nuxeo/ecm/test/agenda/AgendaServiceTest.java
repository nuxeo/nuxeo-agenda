package org.nuxeo.ecm.test.agenda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.agenda.AgendaEventBuilder;
import org.nuxeo.ecm.agenda.AgendaService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@Deploy({ "org.nuxeo.ecm.agenda", "org.nuxeo.ecm.platform.userworkspace.core",
        "org.nuxeo.ecm.platform.userworkspace.types" })
@RepositoryConfig(type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
public class AgendaServiceTest {
    @Inject
    protected AgendaService agendaService;

    @Inject
    protected UserWorkspaceService userWorkspaceService;

    @Inject
    protected CoreSession session;

    @Test
    public void testEventCreation() throws ClientException {
        assertNotNull(agendaService);
        AgendaEventBuilder build = AgendaEventBuilder.build("mon event",
                NOW().toDate(), NOW().plusDays(2).toDate());
        assertNotNull(agendaService.createEvent(session, "/", build.toMap()));

        build.summary("second event");
        assertNotNull(agendaService.createEvent(session, null, build.toMap()));

        build.summary("third event");
        assertNotNull(agendaService.createEvent(session, "/default-domain/",
                build.toMap()));

        DocumentModelList res = session.query("Select * from VEVENT where ecm:path STARTSWITH '"
                + getUserWorkspace().getPathAsString() + "'");
        assertEquals(2, res.size());

        res = session.query("Select * from VEVENT");
        assertEquals(3, res.size());
    }

    protected static DateTime NOW() {
        return new DateTime();
    }

    protected DocumentModel getUserWorkspace() throws ClientException {
        return userWorkspaceService.getCurrentUserPersonalWorkspace(session,
                session.getRootDocument());
    }
}
