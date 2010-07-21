
/*
 * DashBoard handles the HTML and CSS setup of a users' dashboard.
 * The DashBoard reads in a local or remote JSON configuration, and
 * loads a dashboard based on the contents of the configuration.
 */

// DashBoard constructor
function DashBoard() {
	this.userName = ""; // The user name of the logged in user
	this.userRole = 0; // The role of the logged in user
	
	// List of views registered with the DashBoard.
	this.viewList = [];
	
	// Use these as globals for now
	this.startDate = new Date();
	this.numDays = 0;
}

// Logger for the dashboard
DashBoard._logger = log4javascript.getLogger();

// The various DashBoads views
DashBoard.view_type = {
    "VIEW_GRAPH":0,
    "VIEW_UPLOAD":1,
    "VIEW_INFO":2,
    "VIEW_MOBILITY":3
};

// Dashboard functions

DashBoard.prototype.setUserName = function(userName) {
	this.userName = userName;
}


// Setup the HTML and CSS according to the configuration JSON.
DashBoard.prototype.configureHtml = function(jsonConfig) {
	// Send the json_config to the view
	this.curView.configureHtml(jsonConfig);
}

// Load new data into the dashboard
DashBoard.prototype.passData = function(awData) {
	if (DashBoard._logger.isDebugEnabled()) {
        DashBoard._logger.debug("DashBoard.passData(): Passed data of type: "  + typeof awData);
    }

	// Send data to every View, they can decide whether or not they need it
	$.each(
			this.viewList,
			function( intIndex, objValue ) {
				objValue.loadData(awData);
				objValue.loading(false);
			}
	);
}

// Handles updating the state of the views.  Will pass the state change to all
// registered Views, which can either ignore it or take action.
DashBoard.prototype.updateState = function(stateChange) {
    
}

// Enable/disable the loading graphic
DashBoard.prototype.loading = function(enable) {
	$.each(
			this.viewList,
			function( intIndex, objValue ) {
				objValue.loading(enable);
			}
	);
}

/*
 * Initialize the dash board.  Separate this out eventually.  
 */
DashBoard.prototype.initialize = function() {
	$('#banner').append('<span class="h banner_text">Current Campaign: ABC Study</span><br>')
				.append('<ul class="tabs"></ul> ')
				.append('<span id="loggedInAs">Logged in as ' + this.userName + '</span>')
		        .append('<div id="logout"><a href="/app/logout">Logout</a></div>');
	
	// Hack in the tabs now
	$('#banner > ul.tabs').append('<li><a class="w2" href="ViewUpload">Upload Stats</a></li>')
	                      .append('<li><a href="ViewGraph">EMA Graphs</a></li>');
	
	$('#main').append('<div class="panes"></div>');
	$('#main > div.panes').append('<div id="ViewUpload"></div>')
						  .append('<div id="ViewGraph"></div>');
					      
	
	// Setup tabs to work with the panes
	$("#banner > ul.tabs").tabs("#main > div.panes > div");
	
	// Load viewList with necessary views
	var viewGraph = new ViewGraph('ViewGraph');
	viewGraph.configureHtml(responseList);
	this.viewList.push(viewGraph);
	
	var viewUpload = new ViewUpload('ViewUpload');
	this.viewList.push(viewUpload);
	
	//var viewSurveyMap = new ViewSurveyMap('ViewSurveyMap');
	//viewSurveyMap.configureHtml();
	//this.viewList.push(viewSurveyMap);
	
	
	// get handle to the api (must have been constructed before this call
	var api = $("#banner > ul.tabs").tabs();
	// Link tab clicks to fire GWT events
	api.onClick(function(event, index) {
	    if (index == 0) {
	        switchToUploadViewEvent();
	    }
	    if (index == 1) {
	        switchToGraphViewEvent();
	    }
	});

}


