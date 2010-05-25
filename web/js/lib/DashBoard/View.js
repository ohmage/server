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
}

ViewUpload.prototype = new View();

// Configure the html according the the user list received from
// time since last survey
ViewUpload.prototype.configure_html = function(json_config) {
	// First setup a static title
	$(this.divId).append('<div class="ViewUploadHeader"></div>');
	$(this.divId).find('.ViewUploadHeader').append('<span id="ViewUploadHeader1">Last Survey</span>')
		.append('<span id="ViewUploadHeader2">Last Location</span>')
		.append('<span id="ViewUploadHeader3">% Good Location</span>');
	
	var that = this;
    json_config.current_data.forEach(function(config) {
    	if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload: Setting up user: " + config.user);
        }
    	
    	// Setup a wrapper div to hold information about the user
    	$(that.divId).append('<div id="' + config.user.replace('.', '_') + '" class="StatDisplay"></div>');
		
		// Setup the title
    	var statTitleDivId = "StatTitle_" + config.user.replace('.', '_');
    	$(that.divId).find('div#' + config.user.replace('.', '_'))
    		.append('<div class="StatTitle" id="' + statTitleDivId + '"></div>');
    	
    	// Attach a user object to the div
    	var newUserInfo = new UserInfo(statTitleDivId, config.user);
    	$(that.divId).find('#' + statTitleDivId).data('userInfo', newUserInfo);
		
		// Setup the ProtoGraph to view the surveys per day data
        var div_id = 'ProtoGraph_' + config.user.replace('.', '_');
        $(that.divId).find('div#' + config.user.replace('.', '_'))
            .append('<div class="ProtoGraph" id="' + div_id + '"></div>');
			
        // Create a new ProtoGraph and add it to the div
		var graph_config = new Object();
		graph_config.type = ProtoGraph.graph_type.PROTO_GRAPH_ALL_INTEGER_TYPE;
		graph_config.text = "Surveys returned per day";
		graph_config.x_labels = group_list;
		
		var graph_width = $(that.divId).width();
        var new_graph = ProtoGraph.factory(graph_config, div_id, graph_width);
        $(that.divId).find('#' + div_id)
            .data('ProtoGraph', new_graph);
	
    });
	
	this.configured = true;
}


function UserInfo(divId, userName) {
	this.divId = '#' + divId;
	this.userName = userName;
	
	// Initialize with default values
	$(this.divId).append('<span class="UserName">' + userName + ':</span>')
	   .append('<span class="TimeSinceUserSurvey">No Data</span>')
	   .append('<span class="TimeSinceUserLocation">No Data</span>')
	   .append('<span class="PercentageGoodUploads">No Data</span>');
}

// Update the hours since the last survey for this user
UserInfo.prototype.update_hours_since_last_survey = function(value) {
	// Hack this in, if the value is 0 assume no data found
	if (value == 0) {
		$(this.divId + " > .TimeSinceUserSurvey").text("No Data");
	}
	else {
		$(this.divId + " > .TimeSinceUserSurvey").text(value.toFixed(1) + " hrs");
	}
}

// Update the time since the last good user GPS reading
UserInfo.prototype.update_time_since_user_location = function(value) {
	// Hack this in, if the value is 0 assume no data found
	if (value == 0) {
		$(this.divId + " > .TimeSinceUserLocation").text("No Data");
	}
	else {
		$(this.divId + " > .TimeSinceUserLocation").text(value.toFixed(1) + " hrs");
	}
}

// Update the percentage of good GPS readings
UserInfo.prototype.update_percentage_good_uploads = function(value) {
    $(this.divId + " > .PercentageGoodUploads").text((value * 100) + "%");
}

// Load newly received data
ViewUpload.prototype.load_data = function(json_data) {
	// Check the incoming data type
	if (json_data instanceof HoursSinceLastSurveyAwData) {
		if (View._logger.isDebugEnabled()) {
            View._logger.debug("ViewUpload received data of type HouseSinceLastSurveyAwData.");
        }
	
		// Super hack, if first time we see this, configure the html, then ask for real data
		// FIX THIS, NOT A GOOD PLACE HERE
		if (this.configured == false) {
			this.configure_html(json_data);
			
			send_json_request(null);
			
			return;
		}
		
		// Load the new information into the div for each response
		var that = this;
		json_data.current_data.forEach(function(data) {
			// Grab the UserInfo object
			var userInfoDivId = "#StatTitle_" + data.user.replace('.', '_');
			var userInfo = $(that.divId).find(userInfoDivId).data('userInfo');
			if (userInfo != null)
				userInfo.update_hours_since_last_survey(data.value);
		});
	}
	
	if (json_data instanceof HoursSinceLastUpdateAwData) {
	    // Load the new information into the div for each response
        var that = this;
        json_data.current_data.forEach(function(data) {
            // Grab the UserInfo object
            var userInfoDivId = "#StatTitle_" + data.user.replace('.', '_');
            var userInfo = $(that.divId).find(userInfoDivId).data('userInfo');
            if (userInfo != null)
            	userInfo.update_time_since_user_location(data.value);
        });
	}
	
	if (json_data instanceof LocationUpdatesAwData) {
	    // Load the new information into the div for each response
        var that = this;
        json_data.current_data.forEach(function(data) {
            // Grab the UserInfo object
            var userInfoDivId = "#StatTitle_" + data.user.replace('.', '_');
            var userInfo = $(that.divId).find(userInfoDivId).data('userInfo');
            if (userInfo != null)
            	userInfo.update_percentage_good_uploads(data.value);
        });
	}
	
	// Load the new SurveysPerDayAwData information
	if (json_data instanceof SurveysPerDayAwData) {
		var that = this;
		// The data should be separated by user
		for (var user in json_data.current_data) {
			// Graph the ProtoGraph object
			var protoGraphDivId = "#ProtoGraph_" + user.replace('.', '_');
			var protoGraph = $(that.divId).find(protoGraphDivId).data('ProtoGraph');
			// If the graph exists, load the new data
			if (protoGraph != null) {
	            if (View._logger.isDebugEnabled()) {
	                View._logger.debug("Sending new data to ProtoGraph: " + protoGraphDivId);
	            }
	            
				protoGraph.apply_data(json_data.current_data[user], startDate, numDays);
				// Render the graph with the new data
				protoGraph.render();
				
	            // If there is no data for the user, hide the graph
	            if (json_data.current_data[user].length == 0) {
	            	$(that.divId).find(protoGraphDivId).hide();
	            }
	            // else show the graph
	            else {
	            	$(that.divId).find(protoGraphDivId).show();
	            }
			}		
		}
	}
}

// Show the loading graphic when loading new data
ViewUpload.prototype.loading = function(enable) {
	
}