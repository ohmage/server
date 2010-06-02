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
	this.graphsEnabled = false;
	this.hoursSinceLastSurvey = 0.0;
	this.hoursSinceLastLocationUpdate = 0.0
	this.percentageGoodLocationUpdate = 0;
	this.surveysPerDayProtoGraph = null;
	
	// Add user name to the div for later sorting
	$(this.divId).attr('Name', this.userName.replace('.', '_'));
	
	// Add user upload information to the DIV
	$(this.divId).append('<div class="StatTitle"></div>');
	
	$(this.divId).find('.StatTitle').append('<span class="UserName">' + this.userName + ':</span>')
	   .append('<span class="TimeSinceUserSurvey">No Data</span>')
	   .append('<span class="TimeSinceUserLocation">No Data</span>')
	   .append('<span class="PercentageGoodUploads">No Data</span>')
	   // Button to show/hide the user's graphs
	   .append('<div class="GraphEnableButton"></div>');
	   
	// Attach a function to handle a button click
	$(this.divId).find('.GraphEnableButton').click(jQuery.proxy(this.button_clicked, this));
	
	// Add a ProtoGraph to display surveys per day to the div
	// Setup the ProtoGraph to view the surveys per day data
    var protoGraphDivId = 'ProtoGraph_' + this.userName.replace('.', '_');
    $(this.divId).append('<div class="ProtoGraph" id="' + protoGraphDivId + '"></div>');
	// Hide the div to start while loading
	$(this.divId).find('.ProtoGraph').hide();
		
    // Create a new ProtoGraph and add it to the div
	var graph_config = new Object();
	graph_config.type = ProtoGraph.graph_type.PROTO_GRAPH_ALL_INTEGER_TYPE;
	graph_config.text = "Surveys returned per day";
	// Grab the global "group_list" from response_list.js for now
	graph_config.x_labels = group_list;
	
	var graph_width = $(this.divId).width();
    var new_graph = ProtoGraph.factory(graph_config, protoGraphDivId, graph_width);
    this.surveysPerDayProtoGraph = new_graph;
    
    // Annoying hack, add default "column values"
    $(this.divId).attr("LastSurvey", 0.0);
    $(this.divId).attr("LastLocation", 0.0);
    $(this.divId).attr("GoodLocation", 0.0);
}

// Logger for the StatDisplay
StatDisplay._logger = log4javascript.getLogger();


// Update the hours since the last survey for this user
StatDisplay.prototype.update_hours_since_last_survey = function(value) {
	// Hack this in, if the value is 0 assume no data found
	if (value == 0) {
		$(this.divId + " .TimeSinceUserSurvey").text("No Data");
	}
	else {
		$(this.divId + " .TimeSinceUserSurvey").text(value.toFixed(1) + " hrs");
	}
	
	// Attach this value to the div for later sorting
	$(this.divId).attr("LastSurvey", value);
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
	
	// Attach this value to the div for later sorting
	$(this.divId).attr("LastLocation", value);
}

// Update the percentage of good GPS readings
StatDisplay.prototype.update_percentage_good_uploads = function(value) {
    $(this.divId + " .PercentageGoodUploads").text(value.toFixed(1) + "%");
    
 // Attach this value to the div for later sorting
	$(this.divId).attr("GoodLocation", value);
}

StatDisplay.prototype.update_surveys_per_day = function(data) {
	// Grab the global startDate and numDays for now, fix later
	this.surveysPerDayProtoGraph.apply_data(data.data, startDate, numDays);
	// Render the graph with the new data
	this.surveysPerDayProtoGraph.render();
	
	// Hide the div for now
	$(this.divId).find('.ProtoGraph').hide();
	
	// Update the graph/button state
	this.update_state();
}

// Whenever the graph status button is clicked, switch the enabled state
StatDisplay.prototype.button_clicked = function() {
	if (this.graphsEnabled) {
		this.enable_graphs(false);
	}
	else {
		this.enable_graphs(true);
	}
}

// update_state - logic to update the hide/show status of the graphs, 
// and the class of the dropdown button
StatDisplay.prototype.update_state = function() {
	// If the graph is empty, hide it and set the button disabled
	if (this.surveysPerDayProtoGraph.is_empty()) {
	   $(this.divId).find(".ProtoGraph").hide();
	   $(this.divId).find('.GraphEnableButton').removeClass('graphsEnabled graphsDisabled').addClass('disabled');
	   return;
	}
	
	// If the graph is not empty, and the graphs are NOT enabled, hide the graphs and
	// set the button to graphsDisabled
	if (!this.graphsEnabled) {
		$(this.divId).find(".ProtoGraph").slideUp('fast');
        $(this.divId).find('.GraphEnableButton').removeClass('graphsEnabled disabled').addClass('graphsDisabled');
		return;
	}
	
	// If the graph is not empty, and the graphs ARE enabled, show the graphs
	// and set the button to graphsEnabled
	if (this.graphsEnabled) {
		$(this.divId).find(".ProtoGraph").slideDown('fast');
		$(this.divId).find('.GraphEnableButton').removeClass('graphsDisabled disabled').addClass('graphsEnabled');
        return;
	}
	
}


// Show or hide the graphs for the user, also updates the button state
StatDisplay.prototype.enable_graphs = function(enable) {
	this.graphsEnabled = enable;
	
	// Update the graph and button state
	this.update_state();
}



