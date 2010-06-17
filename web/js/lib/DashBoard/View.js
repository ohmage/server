/*
 * View handles the views for the dash board.  Each defined View subclass
 * needs to implement the interface functions to update the banner, controls, main
 * and footer div of the dash board.
 */

// View constructor
function View() {

}

// Logger for the View
View._logger = log4javascript.getLogger();

// check_datatype - Check if this View can handle the incoming data type.
// Return true if we can, false if we cannot
View.prototype.check_datatype = function(awData) {
	throw new Error('View.check_datatype is not defined!');
}

// configure_html - Read in JSON describing each specific view.  Build
// the banner, controls, main and footer.  Assume no data has yet been loaded.
View.prototype.configure_html = function(json_config) {
	throw new Error('View.configure_html is not defined!');
}

// load_data - Loads in a new DataSource with which to display data
View.prototype.load_data = function(data_source) {
	throw new Error('View.load_data is not defined!');
}

// loading - Pass true/false to enable/disable the View's loading graphic
View.prototype.loading = function(enable) {
	throw new Error('View.loading is not defined!')
}


/*
 * ViewGraph - Create a View to show graphs of user data
 */
function ViewGraph(divId) {
	this.divId = '#' + divId;
	
	// Setup basic View information
	this.tabName = "EMA Graphs"
}

ViewGraph.prototype = new View();

// ViewGraph.configure_html - Give JSON that describes the surveys and prompts.
ViewGraph.prototype.configure_html = function(json_config) {
	// Setup the tabs and panes
	$(this.divId).append('<ul class="tabs"></ul> ')
	 		    .append('<div class="panes"></div>');
	
	// First setup the main panel
    var cur_group = -1;
    
    // Loop over each graph type
    var that = this;
    json_config.forEach(function(config) {
        // If we are in a new group, add a new pane to the tabs
        if (cur_group != config.group_id) {
            // Grab new group name from the group_list
            var new_group_name = group_list[config.group_id];
            // Translate the name into something that works as an html reference
            var new_group_name_ref = new_group_name.toLowerCase().replace(' ', '_');
            
            if (View._logger.isDebugEnabled()) {
                View._logger.debug("Creating group name: " + new_group_name + " with ref: " + new_group_name_ref);
            }
            
            // Setup the tabs with the prompt groups
            $(that.divId).find('.tabs').append('<li><a href="' + new_group_name_ref + '">' + new_group_name + '</a></li>');
            $(that.divId).find('.panes').append('<div id="group_' + config.group_id + '"></div>');
            
            cur_group = config.group_id;
        }
        
        // Now append a new div into the panes for our new graph
        $(that.divId).find('.panes > #group_' + cur_group).append('<div class="ProtoGraph" id="prompt_' + config.prompt_id + '"></div>');
    
        // Create a unique div ID for Protovis to know where to attach the graph
        var div_id = 'ProtoGraph_' + cur_group + '_' + config.prompt_id;
        
        // Put the graph title and another div for the graph itself into this div
        $(that.divId).find('#group_' + cur_group + ' > #prompt_' + config.prompt_id)
            .append('<span class="graph_title">' + config.text + '</span>')
            .append('<div id="' + div_id + '"></div>');
        
        // Finally create a new graph and add it to the div
        // Make the graph have the width of the tab panes
        var new_graph = ProtoGraph.factory(config, div_id, $('div.panes').width());
        $(that.divId).find('#' + div_id)
            .data('graph', new_graph)
            .data('prompt_id', config.prompt_id)
            .data('group_id', cur_group)
            .data('hidden', false);
    });
    
    // setup ul.tabs to work as tabs for each div directly under div.panes 
    $(this.divId).find("ul.tabs").tabs(this.divId + " > div.panes > div");
    
    // Hide all the graphs for now
    $(this.divId).find('div.ProtoGraph').hide();
    
    // Append a loading div in the pane
    $(this.divId).find('div.panes > div').append('<div class="loading"></div>');
}


ViewGraph.prototype.load_data = function(aw_data) {
	// Make sure the data is of type EmaAwData
	if (!(aw_data instanceof EmaAwData)) {
		return;
	}
	
    // Iterate over every ProtoGraph class
    var that = this;
    $(this.divId).find('div.ProtoGraph > div').each(function(index) {
        // Grab the graph object attached to this div
        var graph = $(this).data('graph');
        var prompt_id = $(this).data('prompt_id');
        var group_id = $(this).data('group_id');
        
        // Time the rendering of the graph
        if (View._logger.isDebugEnabled()) {
        	View._logger.debug("Rendering graph with prompt_id " + prompt_id + " group_id " + group_id);
            var start_render_time = new Date().getTime();
        }
        
        // Grab data for the specified prompt/group
        try {
            // Hack in custom graphs here
            if (graph instanceof ProtoGraphCustomSleepType) {
                var new_data = aw_data.get_data_sleep_time();
            }
            else if (graph instanceof ProtoGraphMultiTimeType) {
                var new_data = aw_data.get_data_saliva();
            }
            // No custom data processing
            else {
                var new_data = aw_data.get_data(prompt_id, group_id);
            }
            
            
        }
        catch (error) {
            if (error instanceof DataSourceJson.NoDataError) {
                if (View._logger.isInfoEnabled()) {
                	View._logger.info(error.message);
                }
                
                // Replace graph with no data found warning
                if ($(this).data('hidden') == false) {
                    that.replace_with_no_data($(this));
                    $(this).data('hidden', true);
                }
            }
            
            return;
        }
        
        if (View._logger.isDebugEnabled()) {
        	View._logger.debug("Found " + new_data.length + " data points");
        }
        
        // If the graph was hidden due to no data found, un-hide
        if ($(this).data('hidden') == true) {
            that.replace_with_graph($(this));
            $(this).data('hidden', false);
        }
        
        // Apply data to the graph
        graph.apply_data(new_data, 
                         startDate, 
                         numDays);
        
        // Re-render graph with the new data
        graph.render();
        
        
        if (View._logger.isDebugEnabled()) {
            var time_to_render = new Date().getTime() - start_render_time;           
            View._logger.debug("Time to render graph: " + time_to_render + " ms");
        }     
    });
}

ViewGraph.prototype.loading = function(enable) {
    if (enable) {
        // Hide the graphs while loading
    	$(this.divId).find("div.ProtoGraph").hide();
        // Show the loading graphic in the displayed pane
    	$(this.divId).find('div.panes .loading').show();
    }
    else {
        // Hide all the loading divs in the panes
    	$(this.divId).find('div.panes .loading').hide();
        // And reshow the graphs
    	$(this.divId).find("div.ProtoGraph").show();
    }
}

// Hide the passed div and add a no data found
ViewGraph.prototype.replace_with_no_data = function(div_to_replace) {
    div_to_replace.after("<span>No data found</span>");
    div_to_replace.hide();
}

// Show the passed div and remove the next sibling
ViewGraph.prototype.replace_with_graph = function(div_to_show) {
    div_to_show.next().remove();
    div_to_show.show();
}


/*
 * ViewUpload - Setup and view upload statistics for a user or researcher
 */
function ViewUpload(divId) {
	this.divId = '#' + divId;
	
	this.tabName = "Upload Stats";
	this.configured = false;
	
	this.graphsEnabled = false;
}

ViewUpload.prototype = new View();

// Check if we can handle the incoming AwData
ViewUpload.prototype.check_datatype = function(awData) {
	var goodData = false;
	
	if (awData instanceof HoursSinceLastSurveyAwData ||
		awData instanceof HoursSinceLastUpdateAwData ||
		awData instanceof LocationUpdatesAwData ||
		awData instanceof SurveysPerDayAwData) {
		goodData = true;
	}
	else {
		if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload.check_datatype: Cannot load datatype.");
        }
	}
	
	return goodData;
}

// Configure the html according the the user list received from
// time since last survey
ViewUpload.prototype.configure_html = function(json_config) {
	// First setup a static title
	// texts for the tooltips
	var lastSurveyTooltip = 'Displays the time since the user last completed a survey in hours.';
	var lastLocationTooltip = 'Displays the time since the user last had a good GPS fix in hours.';
	var goodLocationTooltip = 'Displays the percent of good GPS data in the last 24 hours.';
	
	$(this.divId).append('<div class="ViewUploadHeader"></div>');
	$(this.divId).find('.ViewUploadHeader')
	    .append('<span id="Name" class="DisplayColumnHeader">Name:</span>')
	    .append('<span id="LastSurvey" class="DisplayColumnHeader" title="' + lastSurveyTooltip + '">Last Survey</span>')
	    .append('<span id="LastLocation" class="DisplayColumnHeader" title="' + lastLocationTooltip + '">Last Location</span>')
	    .append('<span id="GoodLocation" class="DisplayColumnHeader" title="' + goodLocationTooltip + '">% Good Location</span>')
	    .append('<span id="enableAllGraphs">Show all</span>');
	
	// Setup the tooltips using the jQuery tooltip plugin
	//$(".ViewUploadHeader span[title]").tooltip();
	
	// Attach a function to the headers to sort when clicked
	$(this.divId).find('#Name').click(jQuery.proxy(this.sort_by_name_click, this));
	$(this.divId).find('#LastSurvey').click(jQuery.proxy(this.sort_by_last_survey_click, this));
	$(this.divId).find('#LastLocation').click(jQuery.proxy(this.sort_by_last_location_click, this));
	$(this.divId).find('#GoodLocation').click(jQuery.proxy(this.sort_by_good_location_click, this));
	
	// Attach a function to handle a click on enableAllGraphs
	$(this.divId).find('#enableAllGraphs').click(jQuery.proxy(this.enable_all_graphs, this));
	
	// Setup each user in the configuration
	var that = this;
    json_config.current_data.forEach(function(config) {
    	if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload: Setting up user: " + config.user);
        }
    	
    	// Setup a wrapper div to hold information about the user
    	var statDisplayDivId = 'StatDisplay_' + config.user.replace('.', '_');
    	$(that.divId).append('<div id="' + statDisplayDivId + '" class="StatDisplay"></div>');
    	
    	// Create a new StatDisplay and attach to the div
    	var newStatDisplay = new StatDisplay(statDisplayDivId, config.user);
    	$(that.divId).find('#' + statDisplayDivId).data('StatDisplay', newStatDisplay);
    });
    
    // Now sort everything by name to start
    this.sort_column('Name');
}

// Load newly received data
ViewUpload.prototype.load_data = function(json_data) {
	// Make sure we can handle the incoming data type, silently do nothing and
	// return if we cannot
	if (this.check_datatype(json_data) == false) {
		return;
	}
	
	// Super hack, if first time we see this, configure the html, then ask for real data
	// FIX THIS, NOT A GOOD PLACE HERE
	if (json_data instanceof HoursSinceLastSurveyAwData && this.configured == false) {
		if (View._logger.isDebugEnabled()) {
            View._logger.debug("ViewUpload received configuration data.");
        }
	
		this.configure_html(json_data);
		this.configured = true;
		
		// Ask for real data, also not a good place to do this
		send_json_request(null);
		
		return;
	}
	
	// Each data point is labeled with its user name.  Find the div, grab the StatDisplay, and
	// load the data.
	var that = this;
	json_data.current_data.forEach(function(data) {
		// Grab the corresponding StatDisplay and load the new data
		var statDisplayDivId = "#StatDisplay_" + data.user.replace('.', '_');
		var statDisplay = $(that.divId).find(statDisplayDivId).data('StatDisplay');
		
		// Call various loading functions depending on the type of data
		if (statDisplay != null) {
			if (json_data instanceof HoursSinceLastSurveyAwData) {
				statDisplay.update_hours_since_last_survey(data.value);
			}
			
			if (json_data instanceof HoursSinceLastUpdateAwData) {
				statDisplay.update_time_since_user_location(data.value);
			}
			
			if (json_data instanceof LocationUpdatesAwData) {
				// Translate from ratio into percentage
				statDisplay.update_percentage_good_uploads(data.value * 100);
			}
			
			if (json_data instanceof SurveysPerDayAwData) {
				statDisplay.update_surveys_per_day(data);
			}
		}
		else {
			if (View._logger.isErrorEnabled()) {
                View._logger.error("Could not find a StatDisplay for user: " + data.user);
            }
		}		
	});
}

// Functions to attach to any dom object to sort by various columns
ViewUpload.prototype.sort_by_name_click = function() {
	this.sort_column("Name");
}

ViewUpload.prototype.sort_by_last_survey_click = function() {
	this.sort_column("LastSurvey");
}

ViewUpload.prototype.sort_by_last_location_click = function() {
	this.sort_column("LastLocation");
}

ViewUpload.prototype.sort_by_good_location_click = function() {
	this.sort_column("GoodLocation");
}

// Sort any column based on it's name
// This function also needs each DIV in the column to have the same
// attribute name with the value to sort by
ViewUpload.prototype.sort_column = function(columnName) {
	var sortOrder = "";
	// Decide whether to sort ascending or descending
	if ($(this.divId + ' #' + columnName).hasClass("desc")) {
		$(this.divId + ' #' + columnName).removeClass("desc").addClass("asc");
		sortOrder = "desc";
	}
	else {
		$(this.divId + ' #' + columnName).removeClass("asc").addClass("desc");
		sortOrder = "asc";
	}
	
	// Use the tiny sort jquery plugin to sort by selector and attribute
	$(this.divId + ' .StatDisplay').tsort("",{order:sortOrder,attr:columnName});	
}

ViewUpload.prototype.enable_all_graphs = function() {
	// Switch the graphsEnabled/graphsDisabled
	if (this.graphsEnabled) {
	   this.graphsEnabled = false;
	   $(this.divId).find('#enableAllGraphs').text('Show all');
	}
	else {
		this.graphsEnabled = true;
		$(this.divId).find('#enableAllGraphs').text('Hide all');
	}
	
	var that = this;
	$(this.divId).find('.StatDisplay').each(function() {
		// Grab the stat display graph attached
		var statDisplay = $(this).data('StatDisplay');
		// Enable or disable the graphs
		statDisplay.enable_graphs(that.graphsEnabled);
	});
}

// Show the loading graphic when loading new data
ViewUpload.prototype.loading = function(enable) {
	
}


/*
 * ViewSurveyMap - Shows a map of all the surveys taken
 */
function ViewSurveyMap(divId) {
	this.divId = '#' + divId;
	
	this.tabName = "Survey map";
	this.configured = false;
	
	this.graphsEnabled = false;
}

ViewSurveyMap.prototype = new View();

//check_datatype - Check if this View can handle the incoming data type.
//Return true if we can, false if we cannot
ViewSurveyMap.prototype.check_datatype = function(awData) {
	var goodData = false;
	
	if (awData instanceof EmaAwData) {
		goodData = true;
	}
	else {
		if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload.check_datatype: Cannot load datatype.");
        }
	}
	
	return goodData;
}

//configure_html - Read in JSON describing each specific view.  Build
//the banner, controls, main and footer.  Assume no data has yet been loaded.
ViewSurveyMap.prototype.configure_html = function(json_config) {
	// Add an img tag for the incoming image
	$(this.divId).append('<img></img>');
}

//load_data - Loads in a new DataSource with which to display data
ViewSurveyMap.prototype.load_data = function(json_data) {
	// Make sure we can handle the incoming data type, silently do nothing and
	// return if we cannot
	if (this.check_datatype(json_data) == false) {
		return;
	}
	
	var googleApiUrl = 'http://maps.google.com/maps/api/staticmap?';
	
	// Setup the list of params to send to the google API
	var params = {size:'512x512',
				  maptype:'roadmap',
				  sensor:'true',
				  markers:[]};
	
	// Now run through the passed data, adding any durvey lat/lon to the map
	data_count = 0;
	json_data.get_data_filtered().forEach(function(data_point) {
		// Check to be sure either lat or long is not null
		if (data_point.latitude == 0 || data_point.longitude == 0) {
			return;
		}
		
		// Check if the lat/long is already in the markers list
		found_string = false;
		string_to_find = data_point.latitude + ',' + data_point.longitude;
		params.markers.forEach(function(marker) {
			// Check for the string
			if (marker.indexOf(string_to_find) > -1) {
				found_string = true;
			}
		});
			
		// If we found the string already, move on
		if (found_string == true) {
			return;
		}
		
		// Add point to param list
		params.markers.push('label:' + data_count + '|' + data_point.latitude + 
							',' + data_point.longitude);
		data_count += 1;
	});
	
	
	// Pull out the params into URL encoding, use the traditional param encoding
	var param_string = decodeURIComponent($.param(params, true));
	
	// Replace the img with the new image
	$(this.divId).find('img').attr('src', googleApiUrl + param_string);
	//$(this.divId).find('img').attr('src', 'http://maps.google.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=13&size=512x512&maptype=roadmap&markers=color:blue|label:S|40.702147,-74.015794&markers=color:green|label:G|40.711614,-74.012318&markers=color:red|color:red|label:C|40.718217,-73.998284&sensor=false');
	
}

//loading - Pass true/false to enable/disable the View's loading graphic
ViewSurveyMap.prototype.loading = function(enable) {
	
}
