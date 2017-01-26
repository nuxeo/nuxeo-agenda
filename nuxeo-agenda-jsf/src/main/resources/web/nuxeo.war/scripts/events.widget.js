var nuxeo = nuxeo || {};

nuxeo.agenda = nuxeo.agenda || {};
nuxeo.agenda.widgets = nuxeo.agenda.widgets || {};

// Events widget namespace
nuxeo.agenda.widgets.events = (function(nx) {
  // Vars

  var divContent, divTools;
  var dateFormat = "YYYY-MM-DD HH:mm";
  var formTemplate = '<table>{{#rows}}<tr>{{#columns}}<td>'
      + '<span class="{{labelClass}}">{{label}}</span></td><td colspan="{{colspan}}">'
      + '<input type="{{input}}" name="{{name}}" class="{{inputClass}}" />' + '</td>{{/columns}}</tr>{{/rows}}</table>';

  // Functions

  function buildUrl(entry) {
    return entry.contextParameters.documentURL;
  }

  function initAgenda() {
    moment.lang('en'); // set 'en' first, to prevent from DE as default.
    moment.lang(nuxeo.agenda.messages.lang);

    divContent = jQuery(nuxeo.agenda.selectors.widget);
    divTools = jQuery(nuxeo.agenda.selectors.widgetTools);
    initContextPanel(divTools);
  }

  function displayCalendar() {
    divContent.fadeOut(300, function() {
      divContent.empty();
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

            for (var i = 0; i < entries.length; i++) {
              var entry = entries[i];
              var dtStart = moment(entry.properties["vevent:dtstart"]);
              var dtEnd = moment(entry.properties["vevent:dtend"]);
              var allDay = dtEnd.diff(dtStart, 'days') > 0;

              events.push({
                title: entry.properties["dc:title"],
                start: moment(entry.properties["vevent:dtstart"]).toDate(),
                end: moment(entry.properties["vevent:dtend"]).toDate(),
                allDay: allDay,
                url: buildUrl(entry)
              });
            }

            callback(events);
          });
        }
      }, jQuery.fullCalendar.regional[nuxeo.agenda.messages.lang]);

      divContent.fadeIn(300, function() {
        divContent.html('<div id="calendar" />').fullCalendar(params);
      });
    });
  }

  function initCreateEvent(e) {
    e.preventDefault();
    divContent.fadeOut(300, function() {
      divContent.empty();
      var form = jQuery("<form />").addClass('creationForm').submit(function(eventObject) {
        eventObject.preventDefault();
        var that = jQuery(this);

        // check required fields
        var isFormValid = true;
        jQuery('.required').parents("td").next().find(":last").each(function() {
          if (!jQuery(this).val()) {
            jQuery(this).addClass("warning");
            isFormValid = false;
          } else {
            jQuery(this).removeClass("warning");
          }
        });

        var dtStart = that.find("[name='dtStart']")[0];
        var dtEnd = that.find("[name='dtEnd']")[0];
        if (isFormValid && dtEnd.value) {
          jQuery(dtEnd).removeClass("warning");

          var start = moment(dtStart.value, dateFormat);
          var end = moment(dtEnd.value, dateFormat);
          if (end.diff(start) < 0) {
            isFormValid = false;
            jQuery(dtEnd).addClass("warning");
          }
        }

        if (!isFormValid) {
          return false;
        }

        // build params
        var formParams = jQuery(this).serializeArray();
        var params = {};
        for ( var index in formParams) {
          var field = formParams[index];
          if (field.value) {
            if (field.name.match(/^dt/i)) {
              field.value = moment(field.value, dateFormat).toDate();
            }

            params[field.name] = field.value;
          }
        }

        createEvent(params, createEventCallback);
        return false;
      });

      jQuery("<h3>" + nuxeo.agenda.messages['label.vevent.create'] + "</h3>").appendTo(form);

      form.html(Mustache.render(formTemplate, nuxeo.agenda.createEventLayout));

      form.appendTo(divContent);

      divContent.fadeIn(300, function() {
        var args = jQuery.extend(jQuery.datepicker.regional[nuxeo.agenda.messages.lang],
            jQuery.timepicker.regional[nuxeo.agenda.messages.lang], {
              dateFormat: 'yy-mm-dd',
              touchonly: false,
              stepMinute: 5
            });
        divContent.find(".inputDate").datetimepicker(args);
      });
    });
  }

  function initContextPanel(node) {
    // Create calendar div
    var all = jQuery("<div />").addClass("floatR");
    jQuery("<a />").addClass("button smallButton").attr('href', '#').click(function(e) {
      e.preventDefault();
      var state = jQuery("#filters").toggle().css("display");
      jQuery(this).children().toggle();
      if (state == 'none') {
        displayCalendar();
      } else {
        jQuery("#filters .selected").click();
      }
    }).html(
        "<span>» " + nuxeo.agenda.messages['command.vevent.calendar'] + '</span><span style="display:none;">« '
            + nuxeo.agenda.messages['command.vevent.list'] + '</span>').appendTo(all);
    node.append(all);

    // Create filter div
    var parag = jQuery('<div id="filters"/>');
    var values = [ "incoming", "month", "week", "today" ];
    var clickHandler = function(value) {
      return function(event) {
        fetchEventWithFade(buildListOperationParams(value));
        var target = jQuery(event.target);
        target.parent().children().each(function() {
          jQuery(this).removeClass("selected");
        });
        jQuery(event.target).addClass('selected');
        return false;
      }
    };

    for (var i = 0; i < values.length; i++) {
      var entry = values[i];
      if (i != 0) {
        parag.append("<span>&nbsp;/&nbsp;</span>");
      }
      var currNode = jQuery("<a/>").attr('href', '#').click(clickHandler(entry)).html(
        nuxeo.agenda.messages['command.vevent.' + entry]);
      if (i == 0) {
        currNode.addClass('selected');
      }
      parag.append(currNode);
    }

    node.append(parag);
    // clear both
    node.append(jQuery('<div class="clear" />'));
  }

  function buildListOperationParams(period) {
    var dtStart = moment().sod(), dtEnd;

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
    };
  }

  function fillTables(table, entries) {
    table.empty();
    var now = moment();

    var headings = ['&nbsp;', '&nbsp;', nuxeo.agenda.messages['label.vevent.startDate'],
      nuxeo.agenda.messages['label.vevent.endDate'], nuxeo.agenda.messages['label.vevent.place']];

    var $tr = jQuery('<tr/>');
    headings.forEach(function(heading) {
      $tr.append('<th>' + heading + '</th>');
    });
    table.append($tr);
    
    for (var i = 0; i < entries.length; i++) {
      var entry = entries[i];
      var dtStart = moment(entry.properties["vevent:dtstart"]);
      var dtEnd = moment(entry.properties["vevent:dtend"]);
      var currState = 'incoming';

      if (now.diff(dtStart) >= 0) {
        if (now.diff(dtEnd) >= 0) {
          currState = 'done';
        } else {
          currState = 'inProgress';
        }
      }

      var tr = jQuery("<tr/>").addClass(currState);
      tr.append('<td><img src="' + nuxeo.agenda.clientBaseUrl + 'icons/agenda.png" alt="'
        + nuxeo.agenda.messages['VEVENT'] + '" title="' + nuxeo.agenda.messages['VEVENT'] + '" /></td>');
      tr.append('<td><a class="boldLabel" target="_top" href="' + buildUrl(entry) + '">' + entry.properties["dc:title"]
        + "</a></td>");
      tr.append("<td>" + dtStart.calendar() + "</td>");
      tr.append("<td>" + dtEnd.calendar() + "</td>");

      var location = entry.properties["vevent:location"];
      if (location == 'null') {
        location = '';
      }

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
    var charly = jQuery("#" + nodeId), ret;
    if (charly.length <= 0) {
      charly = creationMethod(nodeId);
    }
    return charly;
  }

  function displayEvents(entries, nxParams) {
    divContent.empty();
    // Fill between banner
    var banner = findOrCreate('betweenBanner', mkBanner);
    if (nxParams.dtStart) {
      var pattern = "ddd LL";
      var dtStart = moment(nxParams.dtStart).format(pattern);
      var dtEnd = moment(nxParams.dtEnd).format(pattern);

      banner.html(nuxeo.agenda.messages['label.vevent.between'] + " " + dtStart + " "
          + nuxeo.agenda.messages['label.vevent.between.and'] + " " + dtEnd);
    } else {
      banner.html(nuxeo.agenda.messages['label.vevent.incoming']);
    }

    if (!entries || entries.length <= 0) {
      jQuery('<p>' + nuxeo.agenda.messages['label.no.vevents'] + '</p>').appendTo(divContent);
    } else {
      // Fill Results Table
      var tableResults = findOrCreate("agenda", mkTable);
      fillTables(tableResults, entries);
    }
    divContent.fadeIn(300);
  }

  function createEventCallback(response, params) {
    jQuery("#waitMessage").empty();
    jQuery("#errorMessage").empty();
    jQuery('#debugInfo').empty();

    if (response.rc >= 500) {
      // Server error
      console.log('Server error when creating event');
      console.debug(response);
    } else {
      fetchEventWithFade(buildListOperationParams());
    }
  }

  function fetchEventWithFade(params, displayMethod) {
    divContent.fadeOut(300, function() {
      fetchEvent(params, displayMethod);
    });
  }

  function fetchEvent(params, displayMethod) {
    params['documentLinkBuilder'] = '';
    params['contextPath'] = nuxeo.agenda.contextDocumentPath;

    /*
     * Convert in string. The jquery automation performed a JSON.stringify on the params object - so date are not well
     * formatted.
     */
    if (params.dtStart) {
      params.dtStart = params.dtStart.toISOString();
      params.dtEnd = params.dtEnd.toISOString();
    }

    var op = jQuery().automation('VEVENT.List', {
      "documentSchemas": "vevent,dublincore"
    });
    op.addParameters(params);
    op.setHeaders({
      "X-NXenrichers.document": "documentURL"
    });
    op.execute(function(data, textStatus, xhr) {
      var internalDisplayMethod = displayMethod || displayEvents;
      internalDisplayMethod(data.entries, params);
    });
  }

  function createEvent(params, callback) {
    params['contextPath'] = nuxeo.agenda.contextDocumentPath;

    if (params.dtStart) {
      params.dtStart = params.dtStart.toISOString();
      params.dtEnd = params.dtEnd.toISOString();
    }

    var op = jQuery().automation('VEVENT.Create');
    op.addParameters(params);

    op.execute(function(data, textStatus, xhr) {
      createEventCallback(data, params);
    });
  }

  nx.initWidget = function() {
    initAgenda();
    fetchEventWithFade(buildListOperationParams());
  }

  return nx;
})(nuxeo.agenda.widgets.events || {});
