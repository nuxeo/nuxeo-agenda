var prefs = new gadgets.Prefs();
var divContent, divTools;

var formTemplate = '<table>{{#rows}}<tr>{{#columns}}<td>' + '<span class="{{labelClass}}">{{label}}</span></td><td colspan="{{colspan}}">' + '<input type="{{input}}" name="{{name}}" class="{{inputClass}}" />' + '</td>{{/columns}}</tr>{{/rows}}</table>';

var createEventLayout = {
    maxColumns: 2,
    rows: [{
        nbColumns: 1,
        columns: [{
            colspan: 3,
            input: "text",
            name: "summary",
            label: prefs.getMsg('label.vevent.summary'),
            labelClass: "required"
        }]
    }, {
        nbColumns: 1,
        columns: [{
            colspan: 3,
            input: "text",
            name: "description",
            label: prefs.getMsg('label.vevent.description')
        }]
    }, {
        nbColumns: 2,
        columns: [{
            input: "text",
            inputClass: "inputDate",
            name: "dtStart",
            label: prefs.getMsg('label.vevent.startDate'),
            labelClass: "required"
        }, {
            input: "text",
            inputClass: "inputDate",
            name: "dtEnd",
            label: prefs.getMsg('label.vevent.endDate')
        }]
    }, {
        nbColumns: 1,
        columns: [{
            colspan: 3,
            input: "text",
            name: "location",
            label: prefs.getMsg('label.vevent.place')
        }]
    }]
};

function log(txt) {
    console.log(txt)
}

function buildUrl(entry) {
    return NXGadgetContext.clientSideBaseUrl + "nxdoc/default/" + entry.uid + "/view_documents"
}

function initAgenda() {
    moment.lang('en'); // set 'en' first, to prevent from DE as default.
    moment.lang(prefs.getLang());

    jQuery(function($) {
        $.timepicker.setDefaults($.timepicker.regional['']);
        $.datepicker.setDefaults($.datepicker.regional['']);
    });

    divContent = jQuery("#content");
    divTools = jQuery("div.tools");
    initContextPanel(divTools)
}

function displayCalendar() {

    divContent.fadeOut(300, function() {
        divContent.empty();
        divContent.fadeIn(300, function() {
            gadgets.window.adjustHeight();
        });
        var params = jQuery.extend({
            theme: true,
            timeFormat: 'H(:mm)',
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'month,agendaWeek,agendaDay'
            },
            eventClick: function(event) {
                if (event.url) {
                    window.open(event.url, "_top");
                    return false;
                }
            },
            events: function(start, end, callback) {
                fetchEvent({
                    dtStart: start,
                    dtEnd: end
                }, function(entries, nxParams) {
                    var events = [];
                    for (var index in entries) {
                        var entry = entries[index];

                        var dtStart = moment(entry.properties["vevent:dtstart"]);
                        var dtEnd = moment(entry.properties["vevent:dtend"]);
                        var allDay = dtEnd.diff(dtStart, 'days') > 0;
                        events.push({
                            title: entry.properties["dc:title"],
                            start: moment(entry.properties["vevent:dtstart"]).toDate(),
                            end: moment(entry.properties["vevent:dtend"]).toDate(),
                            allDay: allDay,
                            url: buildUrl(entry)
                        })
                    }

                    callback(events);
                })
            }
        }, jQuery.fullCalendar.regional[prefs.getLang()]);

        jQuery('<div id="calendar" />').appendTo(divContent).fullCalendar(params);
    });
}

function initCreateEvent() {
    divContent.fadeOut(300, function() {
        jQuery("#filters .selected").removeClass("selected");
        divContent.empty();
        var form = jQuery("<form />").addClass('creationForm').submit(function(eventObject) {
            var that = $(this);

            // check required fields
            var isFormValid = true;
            $('.required').parents("td").next().find(":last").each(function() {
                if (!$(this).val()) {
                    $(this).addClass("warning");
                    isFormValid = false;
                } else {
                    $(this).removeClass("warning");
                }
            });

            var dtStart = that.find("[name='dtStart']")[0];
            var dtEnd = that.find("[name='dtEnd']")[0];
            if (isFormValid && dtEnd.value) {
                $(dtEnd).removeClass("warning");

                var start = moment(dtStart.value, "YYYY-MM-DD HH:mm");
                var end = moment(dtEnd.value, "YYYY-MM-DD HH:mm");
                if (end.diff(start) < 0) {
                    isFormValid = false;

                    $(dtEnd).addClass("warning");
                }
            }

            if (!isFormValid) {
                return false;
            }
            // build params
            var formParams = $(this).serializeArray();
            var params = {};
            for (var index in formParams) {
                var field = formParams[index];
                if (field.value) {
                    if (field.name.match(/^dt/i)) {
                        field.value = moment(field.value, "YYYY-MM-DD HH:mm").toDate()
                    }

                    params[field.name] = field.value
                }
            }

            createEvent(params, createEventCallback);
            return false;
        });

        jQuery("<h3>" + prefs.getMsg("label.vevent.create") + "</h3>").appendTo(form);

        form.html(Mustache.render(formTemplate, createEventLayout));

        var actions = jQuery('<div class="actions"><input type="submit" value="' + prefs.getMsg('command.create') + '" /></div>').appendTo(form);
        jQuery('<input type="submit" value="' + prefs.getMsg('command.cancel') + '" />').click(function(obj) {
            fetchEventWithFade(buildListOperationParams());
            return false;
        }).appendTo(actions);

        form.appendTo(divContent);

        divContent.fadeIn(300, function() {
            var args = jQuery.extend(jQuery.datepicker.regional[prefs.getLang()], jQuery.timepicker.regional[prefs.getLang()], {
                dateFormat: 'yy-mm-dd',
                touchonly: false,
                stepMinute: 5
            });
            divContent.find(".inputDate").datetimepicker(args);

            // XXX Should not be hardcoded
            gadgets.window.adjustHeight(300);
        });

        //gadgets.window.adjustHeight(jQuery("#ui-datepicker-div").outerHeight() + 10)
        //gadgets.window.adjustHeight(gadgets.window.getViewportDimensions().height + jQuery("#ui-datepicker-div").height())
    });
}

function initContextPanel(node) {
    // Create Plus div
    var newEvent = jQuery("<div />").addClass("floatL");
    var content = "<img src='" + NXGadgetContext.clientSideBaseUrl + "icons/action_add.gif' alt='" + prefs.getMsg("command.add") + "' title='" + prefs.getMsg("command.add") + "' />" + prefs.getMsg("command.add");
    jQuery("<a />").addClass("linkButton").attr('href', '#').click(initCreateEvent).html(content).appendTo(newEvent);
    node.append(newEvent);

    // Create calendar div
    var all = jQuery("<div />").addClass("floatR");
    jQuery("<a />").attr('href', '#').click(function() {
        var state = jQuery("#filters").toggle().css("display");
        jQuery(this).children().toggle();
        newEvent.toggle();
        console.log("YOYO: " + state)
        if (state == 'none') {
            displayCalendar();
        } else {
            jQuery("#filters .selected").click();
        }
    }).html("<span>» " + prefs.getMsg('command.vevent.calendar') + '</span><span style="display:none;">« ' + prefs.getMsg('command.vevent.list') + '</span>').appendTo(all);
    node.append(all);

    // Create filter div
    var parag = jQuery('<div id="filters"/>').addClass("floatR paddingR");
    var values = ["incoming", "month", "week", "today"];
    var clickHandler = function(value) {
            return function(event) {
                fetchEventWithFade(buildListOperationParams(value));
                var target = jQuery(event.target);
                target.parent().children().each(function() {
                    jQuery(this).removeClass("selected")
                });
                jQuery(event.target).addClass('selected');

                return false;
            }
        };

    for (var index in values) {
        if (index != 0) {
            parag.append("<span>&nbsp;/&nbsp;</span>")
        }
        var currNode = jQuery("<a/>").attr('href', '#').click(clickHandler(values[index])).html(prefs.getMsg('command.vevent.' + values[index]));
        if (index == 0) {
            currNode.addClass('selected')
        }
        parag.append(currNode);
    }
    node.append(parag);

    //clear both
    node.append(jQuery('<div class="clear" />'));

    gadgets.window.adjustHeight();
}

function buildListOperationParams(period) {
    var dtStart = moment().sod(),
        dtEnd;

    var addTime;
    switch (period) {
    case 'month':
        addTime = 'months';
        break;
    case 'week':
        addTime = 'weeks';
        break;
    case 'today':
        addTime = 'days';
        break;
    default:
        return {};
    }

    dtEnd = moment(dtStart).add(addTime, 1);
    return {
        dtStart: dtStart.toDate(),
        dtEnd: dtEnd.toDate()
    }
}

function fillTables(table, entries) {
    table.empty();
    var now = moment();

    for (var index in entries) {
        var entry = entries[index];

        var dtStart = moment(entry.properties["vevent:dtstart"]);
        var dtEnd = moment(entry.properties["vevent:dtend"]);
        var currState = 'incoming';
        if (now.diff(dtStart) >= 0) {
            if (now.diff(dtEnd) >= 0) {
                currState = 'done'
            } else {
                currState = 'inProgress'
            }
        }

        var tr = jQuery("<tr/>").addClass(currState);
        tr.append('<td><img src="'+ NXGadgetContext.clientSideBaseUrl +'icons/agenda.png" alt="' + prefs.getMsg('VEVENT') + '" title="' + prefs.getMsg('VEVENT') + '" /></td>');
        tr.append('<td><a class="boldLabel" target="_top" href="' + buildUrl(entry) + '">' + entry.properties["dc:title"] + "</a></td>");
        tr.append("<td>" + dtStart.calendar() + "</td>");
        tr.append("<td>" + dtEnd.calendar() + "</td>");
        var location = entry.properties["vevent:location"];
        if (location == 'null') { location = '' };
        tr.append("<td>" + location + "</td>");
        table.append(tr);
    }
}

function mkTable(nodeId) {
    var table = jQuery("<table/>").attr('id', nodeId).addClass("dataList");
    return table.appendTo(divContent);
}

function mkBanner(nodeId) {
    var banner = jQuery("<h2/>").attr('id', nodeId);
    return banner.appendTo(divContent);
}

function findOrCreate(nodeId, creationMethod) {
    var charly = jQuery("#" + nodeId),
        ret;
    if (charly.length <= 0) {
        charly = creationMethod(nodeId);
    }
    return charly;
}

function displayEvents(entries, nxParams) {
    divContent.empty();
    // Fill between banner
    var banner = findOrCreate('betweenBanner', mkBanner);
    if (nxParams.operationParams.dtStart) {
        var pattern = "ddd LL";
        var dtStart = moment(nxParams.operationParams.dtStart).format(pattern);
        var dtEnd = moment(nxParams.operationParams.dtEnd).format(pattern);

        banner.html(prefs.getMsg('label.vevent.between') + " " + dtStart + " " + prefs.getMsg('label.vevent.between.and') + " " + dtEnd);
    } else {
        banner.html(prefs.getMsg('label.vevent.incoming'));
    }

    if (!entries || entries.length <= 0) {
        jQuery('<p>' + prefs.getMsg('label.no.vevents') + '</p>').appendTo(divContent)
    } else {
        // Fill Results Table
        var tableResults = findOrCreate("agenda", mkTable);
        fillTables(tableResults, entries)
    }
    divContent.fadeIn(300, function() {
        gadgets.window.adjustHeight();
    })
}

function operationExecutedCallback(response, params) {
    //console.log("Operation executed")
}

function createEventCallback(response, params) {
    jQuery("#waitMessage").empty();
    jQuery("#errorMessage").empty();
    jQuery('#debugInfo').empty();
    if (response.rc >= 500) {
        //Server error
    } else {
        fetchEventWithFade(buildListOperationParams());
    }
}

function fetchEventWithFade(params, displayMethod) {
    divContent.fadeOut(300, function() {
        fetchEvent(params, displayMethod)
    })
}

function fetchEvent(params, displayMethod) {
    var internalDisplayMethod = displayMethod || displayEvents;
    params['contextPath'] = getTargetContextPath();
    // Automation requests
    var NXRequestEventsParams = {
        operationId: 'VEVENT.List',
        operationParams: params,
        entityType: 'documents',
        operationContext: {},
        operationDocumentProperties: "dublincore,vevent",
        operationCallback: operationExecutedCallback,
        displayMethod: internalDisplayMethod
    };

    doAutomationRequest(NXRequestEventsParams)
}

function createEvent(params, callback) {
    params = params || {};
    callback = callback || operationExecutedCallback;
    params['contextPath'] = getTargetContextPath();

    // Automation requests
    var NXRequestEventsParams = {
        operationId: 'VEVENT.Create',
        operationParams: params,
        operationContext: {},
        operationCallback: callback
    };

    doAutomationRequest(NXRequestEventsParams)
}

// execute automation request onload
gadgets.util.registerOnLoadHandler(function() {
    initAgenda();
    fetchEventWithFade(buildListOperationParams());
});
