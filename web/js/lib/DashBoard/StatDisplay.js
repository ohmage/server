/*
 * StatDisplay - Handles the overall display of user upload information.
 * Setup with a divId and a user name.  Will add a UserInfo and a 
 * number of ProtoGraphs to display any passed information.  Can be used to
 * sort by any information about the user.
 */

function StatDisplay(divId, userName) {
	this.divId = '#' + divId;
	this.userName = userName;

	// Initialize default data
	this.graphsHidden = true;
	this.hoursSinceLastSurvey = 0.0;
	this.hoursSinceLastLocationUpdate = 0.0
	this.percentageGoodLocationUpdate = 0;
	this.surveysPerDayProtoGraph = null;
	
	// Add user upload information to the DIV
	$(this.divId).append('<div class="StatTitle"></div>');
	
	$(this.divId).find('.StatTitle').append('<span class="UserName">' + this.userName + ':</span>')
	   .append('<span class="TimeSinceUserSurvey">No Data</span>')
	   .append('<span class="TimeSinceUserLocation">No Data</span>')
	   .append('<span class="PercentageGoodUploads">No Data</span>')
	   // Button to show/hide the user's graphs
	   .append('<div class="GraphEnableButton"></div>');
	
	// Add a ProtoGraph to display surveys per day to the div
	// Setup the ProtoGraph to view the surveys per day data
    var protoGraphDivId = 'ProtoGraph_' + this.userName.replace('.', '_');
    $(this.divId).append('<div class="ProtoGraph" id="' + protoGraphDivId + '"></div>');
		
    // Create a new ProtoGraph and add it to the div
	var graph_config = new Object();
	graph_config.type = ProtoGraph.graph_type.PROTO_GRAPH_ALL_INTEGER_TYPE;
	graph_config.text = "Surveys returned per day";
	// Grab the global "group_list" from response_list.js for now
	graph_config.x_labels = group_list;
	
	var graph_width = $(this.divId).width();
    var new_graph = ProtoGraph.factory(graph_config, protoGraphDivId, graph_width);
    this.surveysPerDayProtoGraph = new_graph;
}

// Logger for the StatDisplay
StatDisplay._logger = log4javascript.getLogger();


// Load new data into the StatDisplay.  Can be any AwData that the
// StatDisplay can handle.
//
// Input: AwData data - Data to load into the display.
StatDisplay.prototype.load_data = function(data) {
	// Handle various types of AwData as necessary
	if (data instanceof HoursSinceLastSurveyAwData) {
		this.update_hours_since_last_survey(data.value);
	}
	
	if (data instanceof HoursSinceLastUpdateAwData) {
		this.update_time_since_user_location(data.value);
	}
	
	if (data instanceof LocationUpdatesAwData) {
		// Translate from ratio into percentage
		this.update_percentage_good_uploads(data.value * 100);
	}
	
	if (data instanceof SurveysPerDayAwData) {
		this.update_surveys_per_day(data);
	}
}

// Update the hours since the last survey for this user
StatDisplay.prototype.update_hours_since_last_survey = function(value) {
	// Hack this in, if the value is 0 assume no data found
	if (value == 0) {
		$(this.divId + " .TimeSinceUserSurvey").text("No Data");
	}
	else {
		$(this.divId + " .TimeSinceUserSurvey").text(value.toFixed(1) + " hrs");
	}
}

// Update the time since the last good user GPS reading
StatDisplay.prototype.update_time_since_user_location = function(value) {
	// Hack this in, if the value is 0 assume no data found
	if (value == 0) {
		$(this.divId + " .TimeSinceUserLocation").text("No Data");
	}
	else {
		$(this.divId + " .TimeSinceUserLocation").text(value.toFixed(1) + " hrs");
	}
}

// Update the percentage of good GPS readings
StatDisplay.prototype.update_percentage_good_uploads = function(value) {
    $(this.divId + " .PercentageGoodUploads").text(value + "%");
}

StatDisplay.prototype.update_surveys_per_day = function(data) {
	// Grab the global startDate and numDays for now, fix later
	this.surveysPerDayProtoGraph.apply_data(data.data, startDate, numDays);
	// Render the graph with the new data
	this.surveysPerDayProtoGraph.render();
	
    // If there is no data for the user, hide the graph
    if (data.data.length == 0) {
    	$(this.divId).find(".ProtoGraph").hide();
    }
    // else show the graph if they are not hidden
    else {
    	if (this.graphHidden = false)
    		$(this.divId).find(".ProtoGraph").show();
    }
}



// Show or hide the graphs for the user
StatDisplay.prototype.enable_graphs = function(enable) {
	if (enable == true) {
		this.graphsHidden = false;
		$(this.divId).find('.ProtoGraph').show();
	}
	else {
		this.graphsHidden = true;
		$(this.divId).find('.ProtoGraph').hide();
	}
}



