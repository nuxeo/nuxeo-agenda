package org.nuxeo.ecm.agenda;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class AgendaEventBuilder {
    protected String summary;

    protected Date dtStart;

    protected Date dtEnd;

    protected String location;

    protected String description;

    private static final String SCHEMA_PREFIX = "vevent:";

    protected AgendaEventBuilder() {
        // Empty constructor, just protected to be hidden
    }

    public static AgendaEventBuilder build(String summary, Date dtStart, Date dtEnd) {
        AgendaEventBuilder AgendaEvent = new AgendaEventBuilder();
        AgendaEvent.summary(summary);
        AgendaEvent.startDate(dtStart);
        AgendaEvent.endDate(dtEnd);
        return AgendaEvent;
    }

    public AgendaEventBuilder summary(String summary) {
        this.summary = summary;
        return this;
    }

    public AgendaEventBuilder startDate(Date dtStart) {
        this.dtStart = checkDate(dtStart);
        return this;
    }

    public AgendaEventBuilder endDate(Date dtEnd) {
        this.dtEnd = checkDate(dtEnd);
        return this;
    }

    public AgendaEventBuilder location(String location) {
        this.location = location;
        return this;
    }

    public AgendaEventBuilder description(String description) {
        this.description = description;
        return this;
    }

    public Map<String, Serializable> toMap() {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put("dc:title", summary);
        properties.put("dc:description", description);
        properties.put(SCHEMA_PREFIX + "dtstart", dtStart);
        properties.put(SCHEMA_PREFIX + "dtend", dtEnd);
        properties.put(SCHEMA_PREFIX + "location", location);
        return properties;
    }

    protected Date checkDate(Date date) {
        return date;
    }
}
