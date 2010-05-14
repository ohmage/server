/*
 * DashBoard handles the HTML and CSS setup of a users' dashboard.
 * The DashBoard reads in a local or remote JSON configuration, and
 * loads a dashboard based on the contents of the configuration.
 */

// DashBoard consturctor
function DashBoard() {
	this.cur_view = null;
}

// Logger for the dashboard
DashBoard._logger = log4javascript.getLogger();

// The various DashBoads views
DashBoard.view_type = {
    "VIEW_GRAPH":0,
    "VIEW_UPLOAD":1,
    "VIEW_INFO":2
};

// Dashboard functions

// Switch dashboard view
DashBoard.prototype.switch_view = function(new_view) {
	// Check to be sure the new view exists
	//if (!new_view in DashBoard.view_type) {
	//	throw new Error(new_view + ' is not a defined view type.');
	//}
	
	// Clear out the old view and insert the new
	this.unload_view();
	
	switch (new_view) {
	case DashBoard.view_type.VIEW_GRAPH:
		this.cur_view = new ViewGraph();
		break;
	}
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

// Load new data into the graphs and rerender
DashBoard.prototype.load_data = function(data_source) {
	// Be sure the View is loaded correctly
	this.check_view();
	
	// Send the data source to the View
	this.cur_view.load_data(data_source);
}

// Enable/disable the loading graphic
DashBoard.prototype.loading = function(enable) {
	// Be sure the View is loaded correctly
	this.check_view();
	
	// Tell teh current view the new loading status
	this.cur_view.loading(enable);
}

DashBoard.prototype.check_view = function() {
	if (this.cur_view == null) {
		throw new Error('DashBoard: A view is not loaded.');
	}
}