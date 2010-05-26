/*
 * UserInfo - Used to setup and display user upload information.  Will add
 * display information into the divId passed during initialization.  Handles
 * the display of time since last survey, time since last GPS, and percent
 * good location updates.  Can be setup with thresholds to highlight the
 * information is various ways.
 */

function UserInfo(divId, userName) {
	this.divId = '#' + divId;
	this.userName = userName;
	
	// Initialize the div with No Data
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