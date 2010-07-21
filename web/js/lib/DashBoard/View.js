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

// checkDatatype - Check if this View can handle the incoming data type.
// Return true if we can, false if we cannot
View.prototype.checkDatatype = function(awData) {
	throw new Error('View.checkDatatype is not defined!');
}

// configureHtml - Read in JSON describing each specific view.  Build
// the banner, controls, main and footer.  Assume no data has yet been loaded.
View.prototype.configureHtml = function(config) {
	throw new Error('View.configureHtml is not defined!');
}

// loadData - Loads in a new DataSource with which to display data
//   Input: AwData dataSource - Any AwData object
View.prototype.loadData = function(dataSource) {
	throw new Error('View.loadData is not defined!');
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

// ViewGraph.configureHtml - Give JSON that describes the surveys and prompts.
ViewGraph.prototype.configureHtml = function(config) {
    // Add a title to display the user
    $(this.divId).append('<div id="ViewGraph_title"></div>')
    
	// Setup the tabs and panes
	$(this.divId).append('<ul class="tabs"></ul> ')
	 		     .append('<div class="panes"></div>');
	
	// First setup the main panel
    var curGroup = -1;
    
    // Loop over each graph type
    var that = this;
    config.forEach(function(config) {
        // If we are in a new group, add a new pane to the tabs
        if (curGroup != config.groupId) {
            // Grab new group name from the groupList
            var newGroupName = groupList[config.groupId];
            // Translate the name into something that works as an html reference
            var newGroupNameRef = newGroupName.toLowerCase().replace(' ', '_');
            
            if (View._logger.isDebugEnabled()) {
                View._logger.debug("Creating group name: " + newGroupName + " with ref: " + newGroupNameRef);
            }
            
            // Setup the tabs with the prompt groups
            $(that.divId).find('.tabs').append('<li><a href="' + newGroupNameRef + '">' + newGroupName + '</a></li>');
            $(that.divId).find('.panes').append('<div id="group_' + config.groupId + '"></div>');
            
            curGroup = config.groupId;
        }
        
        // Now append a new div into the panes for our new graph
        $(that.divId).find('.panes > #group_' + curGroup).append('<div class="ProtoGraph" id="prompt_' + config.promptId + '"></div>');
    
        // Create a unique div ID for Protovis to know where to attach the graph
        var divId = 'ProtoGraph_' + curGroup + '_' + config.promptId;
        
        // Put the graph title and another div for the graph itself into this div
        $(that.divId).find('#group_' + curGroup + ' > #prompt_' + config.promptId)
            .append('<span class="graph_title">' + config.text + '</span>')
            .append('<div id="' + divId + '"></div>');
        
        // Finally create a new graph and add it to the div
        // Make the graph have the width of the tab panes
        var newGraph = ProtoGraph.factory(config, divId, $('div.panes').width());
        $(that.divId).find('#' + divId)
            .data('graph', newGraph)
            .data('promptId', config.promptId)
            .data('groupId', curGroup)
            .data('hidden', false);
    });
    
    // setup ul.tabs to work as tabs for each div directly under div.panes 
    $(this.divId).find("ul.tabs").tabs(this.divId + " > div.panes > div");
    
    // Hide all the graphs for now
    $(this.divId).find('div.ProtoGraph').hide();
    
    // Append a loading div in the pane
    $(this.divId).find('div.panes > div').append('<div class="loading"></div>');
}


ViewGraph.prototype.loadData = function(dataSource) {
	// Make sure the data is of type EmaAwData
	if (!(dataSource instanceof EmaAwData)) {
		return;
	}
	
	// Update the title with the new userName
	$(this.divId).find("#ViewGraph_title").text("Showing data for " + dashBoard.userName + ":");
	
    // Iterate over every ProtoGraph class
    var that = this;
    $(this.divId).find('div.ProtoGraph > div').each(function(index) {
        // Grab the graph object attached to this div
        var graph = $(this).data('graph');
        var promptId = $(this).data('promptId');
        var groupId = $(this).data('groupId');
        
        // Time the rendering of the graph
        if (View._logger.isDebugEnabled()) {
        	View._logger.debug("Rendering graph with promptId " + promptId + " groupId " + groupId);
            var startRenderTime = new Date().getTime();
        }
        
        // Grab data for the specified prompt/group
        try {
            // Hack in custom graphs here
            if (graph instanceof ProtoGraphCustomSleepType) {
                var newData = dataSource.getDataSleepTime();
            }
            else if (graph instanceof ProtoGraphMultiTimeType) {
                var newData = dataSource.getDataSaliva();
            }
            // No custom data processing
            else {
                var newData = dataSource.getData(promptId, groupId);
            }
            
            
        }
        catch (error) {
            if (error instanceof AwData.NoDataError) {
                if (View._logger.isInfoEnabled()) {
                	View._logger.info(error.message);
                }
                
                // Replace graph with no data found warning
                if ($(this).data('hidden') == false) {
                    that.replaceWithNoData($(this));
                    $(this).data('hidden', true);
                }
            }
            
            return;
        }
        
        if (View._logger.isDebugEnabled()) {
        	View._logger.debug("Found " + newData.length + " data points");
        }
        
        // If the graph was hidden due to no data found, un-hide
        if ($(this).data('hidden') == true) {
            that.replaceWithGraph($(this));
            $(this).data('hidden', false);
        }
        
        // Apply data to the graph
        graph.loadData(newData, 
                       dashBoard.startDate, 
                       dashBoard.numDays);
        
        // Re-render graph with the new data
        graph.render();
        
        
        if (View._logger.isDebugEnabled()) {
            var timeToRender = new Date().getTime() - startRenderTime;           
            View._logger.debug("Time to render graph: " + timeToRender + " ms");
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
ViewGraph.prototype.replaceWithNoData = function(divToReplace) {
    divToReplace.after("<span><br>No data found</span>");
    divToReplace.hide();
}

// Show the passed div and remove the next sibling
ViewGraph.prototype.replaceWithGraph = function(divToShow) {
    divToShow.next().remove();
    divToShow.show();
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
ViewUpload.prototype.checkDatatype = function(dataSource) {
	var goodData = false;
	
	if (dataSource instanceof HoursSinceLastSurveyAwData ||
	    dataSource instanceof HoursSinceLastUpdateAwData ||
	    dataSource instanceof LocationUpdatesAwData ||
	    dataSource instanceof SurveysPerDayAwData ||
	    dataSource instanceof MobilityPerDayAwData) {
		goodData = true;
	}
	else {
		if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload.checkDatatype: Cannot load datatype.");
        }
	}
	
	return goodData;
}

// Configure the html according the the user list received from
// time since last survey
ViewUpload.prototype.configureHtml = function(config) {
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
	$(this.divId).find('#Name').click(jQuery.proxy(this.sortByNameClick, this));
	$(this.divId).find('#LastSurvey').click(jQuery.proxy(this.sortByLastSurveyClick, this));
	$(this.divId).find('#LastLocation').click(jQuery.proxy(this.sortByLastLocationClick, this));
	$(this.divId).find('#GoodLocation').click(jQuery.proxy(this.sortByGoodLocationClick, this));
	
	// Attach a function to handle a click on enableAllGraphs
	$(this.divId).find('#enableAllGraphs').click(jQuery.proxy(this.enableAllGraphs, this));
	
	// Setup each user in the configuration
	var that = this;
    config.currentData.forEach(function(userConfig) {
    	if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload: Setting up user: " + userConfig.user);
        }
    	
    	// Setup a wrapper div to hold information about the user
    	var statDisplayDivId = 'StatDisplay_' + userConfig.user.replace('.', '_');
    	$(that.divId).append('<div id="' + statDisplayDivId + '" class="StatDisplay"></div>');
    	
    	// Create a new StatDisplay and attach to the div
    	var newStatDisplay = new StatDisplay(statDisplayDivId, userConfig.user);
    	$(that.divId).find('#' + statDisplayDivId).data('StatDisplay', newStatDisplay);
    });
    
    // Now sort everything by name to start
    this.sortColumn('Name');
}

// Load newly received data
ViewUpload.prototype.loadData = function(dataSource) {
	// Make sure we can handle the incoming data type, silently do nothing and
	// return if we cannot
	if (this.checkDatatype(dataSource) == false) {
		return;
	}
	
	// Super hack, if first time we see this, configure the html, then ask for real data
	// FIX THIS, NOT A GOOD PLACE HERE
	if (dataSource instanceof HoursSinceLastSurveyAwData && this.configured == false) {
		if (View._logger.isDebugEnabled()) {
            View._logger.debug("ViewUpload received configuration data.");
        }
	
		this.configureHtml(dataSource);
		this.configured = true;
		
		// Ask for real data, also not a good place to do this
		sendJsonRequest(null);
		
		return;
	}
	
	/*
	// Loop over each StatDisplay, grab the associated user data, and pass to the correct
	// function based on its data type
	$(this.divId).find(".StatDisplay").each(function(statDiv) {
		var statDisplay; // StatDisplay object holding the divs associated object
		var userName; // The user name from the StatDisplay
		var userData; // Holds the AwData associated with the user name
		
		// Pull out the StatDisplay object
		statDisplay = statDiv.data('StatDisplay');
		// Find its user name
		userName = statDisplay.userName;
		// Find any data associated with the user
		userData = data.getUserData(userName);
		// Pass the data to the statDisplay
		statDisplay.loadData(userData);		
	});
	*/
	
	// Each data point is labeled with its user name.  Find the div, grab the StatDisplay, and
	// load the data.	
	var that = this;
	dataSource.currentData.forEach(function(data) {
		// Grab the corresponding StatDisplay and load the new data
		var statDisplayDivId = "#StatDisplay_" + data.user.replace('.', '_');
		var statDisplay = $(that.divId).find(statDisplayDivId).data('StatDisplay');
		
		// Call various loading functions depending on the type of data
		if (statDisplay != null) {
			if (dataSource instanceof HoursSinceLastSurveyAwData) {
				statDisplay.updateHoursSinceLastSurvey(data.value);
			}
			
			if (dataSource instanceof HoursSinceLastUpdateAwData) {
				statDisplay.updateTimeSinceUserLocation(data.value);
			}
			
			if (dataSource instanceof LocationUpdatesAwData) {
				// Translate from ratio into percentage
				statDisplay.updatePercentageGoodUploads(data.value * 100);
			}
			
			if (dataSource instanceof SurveysPerDayAwData) {
				statDisplay.updateSurveysPerDay(data);
			}
			
			if (dataSource instanceof MobilityPerDayAwData) {
				statDisplay.updateMobilityPerDay(data);
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
ViewUpload.prototype.sortByNameClick = function() {
	this.sortColumn("Name");
}

ViewUpload.prototype.sortByLastSurveyClick = function() {
	this.sortColumn("LastSurvey");
}

ViewUpload.prototype.sortByLastLocationClick = function() {
	this.sortColumn("LastLocation");
}

ViewUpload.prototype.sortByGoodLocationClick = function() {
	this.sortColumn("GoodLocation");
}

// Sort any column based on it's name
// This function also needs each DIV in the column to have the same
// attribute name with the value to sort by
ViewUpload.prototype.sortColumn = function(columnName) {
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

ViewUpload.prototype.enableAllGraphs = function() {
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
		statDisplay.enableGraphs(that.graphsEnabled);
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

//checkDatatype - Check if this View can handle the incoming data type.
//Return true if we can, false if we cannot
ViewSurveyMap.prototype.checkDatatype = function(dataSource) {
	var goodData = false;
	
	if (dataSource instanceof EmaAwData) {
		goodData = true;
	}
	else {
		if (View._logger.isDebugEnabled()) {        
            View._logger.debug("ViewUpload.checkDatatype: Cannot load datatype.");
        }
	}
	
	return goodData;
}

// configureHtml - Read in JSON describing each specific view.  Build
// the banner, controls, main and footer.  Assume no data has yet been loaded.
ViewSurveyMap.prototype.configureHtml = function(config) {
	// Add an img tag for the incoming image
	$(this.divId).append('<img></img>');
}

// loadData - Loads in a new DataSource with which to display data
ViewSurveyMap.prototype.loadData = function(dataSource) {
	// Make sure we can handle the incoming data type, silently do nothing and
	// return if we cannot
	if (this.checkDatatype(dataSource) == false) {
		return;
	}
	
	var googleApiUrl = 'http://maps.google.com/maps/api/staticmap?';
	
	// Setup the list of params to send to the google API
	var params = {size:'512x512',
				  maptype:'roadmap',
				  sensor:'true',
				  markers:[]};
	
	// Now run through the passed data, adding any durvey lat/lon to the map
	dataCount = 0;
	dataSource.getDataFiltered().forEach(function(dataPoint) {
		// Check to be sure either lat or long is not null
		if (dataPoint.latitude == 0 || dataPoint.longitude == 0) {
			return;
		}
		
		// Check if the lat/long is already in the markers list
		foundString = false;
		stringToFind = dataPoint.latitude + ',' + dataPoint.longitude;
		params.markers.forEach(function(marker) {
			// Check for the string
			if (marker.indexOf(stringToFind) > -1) {
				foundString = true;
			}
		});
			
		// If we found the string already, move on
		if (foundString == true) {
			return;
		}
		
		// Add point to param list
		params.markers.push('label:' + dataCount + '|' + dataPoint.latitude + 
							',' + dataPoint.longitude);
		dataCount += 1;
	});
	
	
	// Pull out the params into URL encoding, use the traditional param encoding
	var paramString = decodeURIComponent($.param(params, true));
	
	// Replace the img with the new image
	$(this.divId).find('img').attr('src', googleApiUrl + paramString);
}

//loading - Pass true/false to enable/disable the View's loading graphic
ViewSurveyMap.prototype.loading = function(enable) {
	
}
