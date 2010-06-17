
/*
 * DashBoard handles the HTML and CSS setup of a users' dashboard.
 * The DashBoard reads in a local or remote JSON configuration, and
 * loads a dashboard based on the contents of the configuration.
 */

// DashBoard consturctor
function DashBoard() {
	this.cur_view = null;
	this.userName = "";
	this.userRole = 0;
	
	// List of views registered with the DashBoard.
	this.view_list = [];
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

DashBoard.prototype.set_user_name = function(userName) {
	this.userName = userName;
}

// Switch dashboard view
DashBoard.prototype.switch_view = function(new_view) {
	// Check to be sure the new view exists
	//if (!new_view in DashBoard.view_type) {
	//	throw new Error(new_view + ' is not a defined view type.');
	//}
	/*
	switch (new_view) {
	case DashBoard.view_type.VIEW_GRAPH:
		this.cur_view = new ViewGraph('ViewGraph');
		break;
	case DashBoard.view_type.VIEW_UPLOAD:
		this.cur_view = new ViewUpload();
		break;
	}
	*/
}


// Unload a view and return the dashboard to a default state
DashBoard.prototype.unload_view = function() {
	// Implement this
	
	this.cur_view = null;
}

// Setup the HTML and CSS according to the configuration JSON.
DashBoard.prototype.configure_html = function(json_config) {
	// Be sure the View is loaded correctly
	this.check_view();
	
	// Send the json_config to the view
	this.cur_view.configure_html(json_config);
}

// Load new data into the dashboard
DashBoard.prototype.pass_data = function(aw_data) {
	if (DashBoard._logger.isDebugEnabled()) {
        DashBoard._logger.debug("DashBoard.pass_data(): Passed data of type: "  + typeof aw_data);
    }

	// Send data to every View, they can decide whether or not they need it
	$.each(
			this.view_list,
			function( intIndex, objValue ) {
				objValue.load_data(aw_data);
				objValue.loading(false);
			}
	);
}

// Enable/disable the loading graphic
DashBoard.prototype.loading = function(enable) {
	// Be sure the View is loaded correctly
	//this.check_view();
	
	// Enable loading on all views for now
	//for (view in this.view_list) {
	//	view.loading(enable);
	//}
	
	$.each(
			this.view_list,
			function( intIndex, objValue ) {
				objValue.loading(enable);
			}
	);
}

DashBoard.prototype.check_view = function() {
	if (this.cur_view == null) {
		throw new Error('DashBoard: A view is not loaded.');
	}
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
	                      .append('<li><a href="ViewGraph">EMA Graphs</a></li>')
	                      .append('<li><a href="ViewSurveyMap">Survey Map</a></li>');
	
	$('#main').append('<div class="panes"></div>');
	$('#main > div.panes').append('<div id="ViewUpload"></div>')
						  .append('<div id="ViewGraph"></div>')
						  .append('<div id="ViewSurveyMap"></div>');
					      
	
	// Setup tabs to work with the panes
	$("#banner > ul.tabs").tabs("#main > div.panes > div");
	
	// Load view_list with necessary views
	var viewGraph = new ViewGraph('ViewGraph');
	viewGraph.configure_html(response_list);
	this.view_list.push(viewGraph);
	
	var viewUpload = new ViewUpload('ViewUpload');
	this.view_list.push(viewUpload);
	
	var viewSurveyMap = new ViewSurveyMap('ViewSurveyMap');
	viewSurveyMap.configure_html();
	this.view_list.push(viewSurveyMap);
}


