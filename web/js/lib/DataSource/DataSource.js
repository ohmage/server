/*
 * DataSourceJson - Handles grabbing data according to the defined survey.
 * Takes in the survey definition, then can be told to grab data for
 * a certain time period, and return data according to type keys
 * (prompt_id, group_id).
 * 
 * Can be set to either generate data randomly, or to grab data from
 * a remote JSON source.
 */

// DataSource constuctor
function DataSourceJson(url) {
	this.url = url;
	current_data = [];
}

/*
 * Interface functions, these must be implemented in any implementations
 * of a DataSource.
 */

// populate_data - Takes in a starting day the number of days of which to grab
// data.  Populates the data internally to be passed out as needed.  Any current
// data is dropped.  
//
// Inputs:
//   start_date: Must be of type Date, the day to start grabbing data
//   num_days: Integer, the number of days of data to grab
DataSourceJson.prototype.populate_data = function(start_date, num_days) {
	throw new Error("populate_data() has not been implemented.");
}

// grab_data - Grab data from internal storage.
DataSource.prototype.grab_data = function(prompt_id, group_id) {
	throw new Error("grab_data() has not been implemented.");
}

/*
 * DataSourceRemoteJson - Grabs JSON data from a remote server.
 */
function DataSourceRemoteJson() {
    // Inherit properties
    DataSource.call(this);	
}

// Inherit methods
DataSourceRemoteJson.prototype = new DataSource();

DataSourceRemoteJson.prototype.populate_data = function(start_date, end_date, url, callback) {
	this.callback = callback;
	
    url += "?s=" + start_date + "&e=" + end_date;		
		
    $.getJSON(url, this.handle_json_data);
}

DataSourceRemoteJson.prototype.handle_json_data = function(json_data, text_status) {
	// Do basic data filtering
	
	// Pull out the day into a Date for each data point
    json_data.forEach(function(d) {
        d.date = new Date(d.time).grabDate();
    });
	
	this.current_data = json_data;
	
	this.callback();
}

