function nuxeoAgendaLoadEvents(from, to, callback) {

	var params = {
		"documentSchemas" : "vevent,dublincore"
	};
	op = jQuery().automation('Document.PageProvider', params);

	var temp = {};
	jQuery.extend(temp, params);
	temp.lang = currentUserLang;

	// build default operation for Document
	temp.providerName = "agenda_list_events_with_date_query";
	
	temp.queryParams = from.toISOString() + "," + to.toISOString();
	
	// temp.providerName = params.pageProviderName;
	temp.page = "0";
	temp.pageSize = "500";

	temp.id = "Document.PageProvider";
	
	if (typeof currentConversationId != 'undefined') {
		// Give needed info to restore Seam context
		temp.conversationId = currentConversationId;
	}
	op.addParameters(temp);
    op.setHeaders({
      "X-NXenrichers.document": "documentURL"
    });
	
	op.execute(function(data, textStatus, xhr) {
		var eventsArray = Array();
        evts = data.entries;
        for (i = 0; i < evts.length; i++) {
                var dtStart = moment(evts[i].properties["vevent:dtstart"]).toDate();
                var dtEnd = moment(evts[i].properties["vevent:dtend"]).toDate();
                evt = {"title":evts[i].title,"start":dtStart,"end":dtEnd,"allDay":false,"url":evts[i].contextParameters.documentURL};
                eventsArray.push(evt);
        }
        callback(eventsArray);
	});
}

jQuery(document).ready(function() {

	jQuery('#calendar').fullCalendar({
		header : {
			left : 'prev,next today',
			center : 'title',
			right : 'month,agendaWeek,agendaDay'
		},
		editable : true,
		events : nuxeoAgendaLoadEvents
	});

});
