/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     rcattiau@gmail.com
 */
package org.nuxeo.ecm.agenda.seam;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.automation.InvalidChainException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.relations.api.DocumentRelationManager;
import org.nuxeo.ecm.platform.relations.api.exceptions.RelationAlreadyExistsException;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

/**
 * @author loopingz
 * @since 5.9.3
 */
@Name("agendaAddToEventAction")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class AddToEventAction implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 3930167063686290160L;

    private static final String PREDICATE_TYPE = "http://purl.org/dc/terms/References";

    /**
     * Get the agenda selected
     */
    private String selectedAgendaId = null;

    @In(create = true)
    protected Map<String, String> messages;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected DocumentRelationManager documentRelationManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(required = false)
    protected transient Principal currentUser;

    /**
     * Get the event selected
     */
    private String selectedEventId = null;

    public String getSelectedAgendaId() {
        return selectedAgendaId;
    }

    public void setSelectedAgendaId(String selectedAgendaId) {
        this.selectedAgendaId = selectedAgendaId;
        this.selectedEventId = null;
    }

    public String getSelectedEventId() {
        return selectedEventId;
    }

    public void setSelectedEventId(String selectedEventId) {
        this.selectedEventId = selectedEventId;
    }

    public void addToEvent() throws InvalidChainException, OperationException, Exception {
        // Do the job
        DocumentModel doc = documentManager.getDocument(new IdRef(selectedEventId));
        // Create link
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        try {
            documentRelationManager.addRelation(documentManager, currentDoc, doc, PREDICATE_TYPE, true);

            facesMessages.add(StatusMessage.Severity.INFO, messages.get("label.relation.created"));
        } catch (RelationAlreadyExistsException e) {
            facesMessages.add(StatusMessage.Severity.WARN, messages.get("label.relation.already.exists"));
        }
    }

    public void cancelAddToEvent() {
        selectedAgendaId = selectedEventId = null;
    }

}
