/*
 * StatDisplay - Handles the overall display of user upload information.
 * Setup with a divId and a user name.  Will add a UserInfo and a 
 * number of ProtoGraphs to display any passed information.  Can be used to
 * sort by any information about the user.
 */

function StatDisplay(divId, userName) {
	this.divId = '#' + divId;
	this.userName = userName.replace('.', '_');

	// Initialize default data
	this.hoursSinceLastSurvey = 0.0;
	this.hoursSinceLastLocationUpdate = 0.0
	this.percentageGoodLocationUpdate = 0;
	
	// Initialize the div here
}

// Load new data into the StatDisplay.  Can be any AwData that the
// StatDisplay can handle.
//
// Input: AwData data - Data to load into the display.
StatDisplay.prototype.load_data = function(data) {
	// Handle various types of Stat data
}

// Show or hide the graphs for the user
StatDisplay.prototype.enable_graphs = function(enable) {
	if (enable == true) {
		$(this.divId).find('.ProtoGraph').show();
	}
	else {
		$(this.divId).find('.ProtoGraph').hide();
	}
}



