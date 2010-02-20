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

DataSourceJson.prototype.populate_data = function(start_date, end_date, url, callback) {
	this.callback = callback;
	
    url += "?s=" + start_date + "&e=" + end_date;		
		
    $.getJSON(url, this.handle_json_data);
}

DataSourceJson.prototype.handle_json_data = function(json_data, text_status) {
	// Do basic data filtering
	
	// Pull out the day into a Date for each data point
    json_data.forEach(function(d) {
        d.date = new Date(d.time).grabDate();
    });
	
	this.current_data = json_data;
	
	this.callback();
}

