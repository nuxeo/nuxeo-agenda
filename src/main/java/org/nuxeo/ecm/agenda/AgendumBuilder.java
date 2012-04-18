package org.nuxeo.ecm.agenda;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class AgendumBuilder {
    protected String title;

    protected Date startDate;

    protected Date endDate;

    protected String startTime;

    protected String endTime;

    protected String place;

    protected String description;

    private static final String SCHEMA_PREFIX = "agd:";

    protected AgendumBuilder() {
        // Empty constructor, just protected to be hidden
    }

    public static AgendumBuilder title(String title, Date startDate,
            Date endDate) {
        AgendumBuilder agendum = new AgendumBuilder();
        agendum.title(title);
        agendum.startDate(startDate);
        agendum.endDate(endDate);
        return agendum;
    }

    public AgendumBuilder title(String title) {
        this.title = title;
        return this;
    }

    public AgendumBuilder startDate(Date startDate) {
        this.startDate = checkDate(startDate);
        return this;
    }

    public AgendumBuilder endDate(Date endDate) {
        this.endDate = checkDate(endDate);
        return this;
    }

    public AgendumBuilder place(String place) {
        this.place = place;
        return this;
    }

    public AgendumBuilder description(String description) {
        this.description = description;
        return this;
    }

    public AgendumBuilder startTime(String startTime) {
        this.startTime = checkTime(startTime);
        return this;
    }

    public AgendumBuilder endTime(String endTime) {
        this.endTime = checkTime(endTime);
        return this;
    }

    public Map<String, Serializable> toMap() {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put("dc:title", title);
        properties.put("dc:description", description);
        properties.put(SCHEMA_PREFIX + "startDate", startDate);
        properties.put(SCHEMA_PREFIX + "endDate", endDate);
        properties.put(SCHEMA_PREFIX + "startTime", startTime);
        properties.put(SCHEMA_PREFIX + "endTime", endTime);
        properties.put(SCHEMA_PREFIX + "place", place);
        return properties;
    }

    protected String checkTime(String startTime) {
        return startTime;
    }

    protected Date checkDate(Date date) {
        return date;
    }
}
