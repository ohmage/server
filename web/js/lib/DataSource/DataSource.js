/*
 * DataSourceJson - Handles grabbing data according to the defined survey.
 * Takes in the survey definition, then can be told to grab data for
 * a certain time period, and return data according to type keys
 * (prompt_id, group_id).
 * 
 * Can be set to either generate data randomly, or to grab data from
 * a remote JSON source.
 */

// Generic DataSource constuctor
function DataSourceJson(url) {
	this.current_data = [];
	this.callback = null;
	this.error_status = 0;
	this.url=url;
}

// Constants
DataSourceJson._logger = log4javascript.getLogger();

// Server error codes
DataSourceJson.SUCCESS = 0;
DataSourceJson.BAD_STATUS = 1;
DataSourceJson.NO_DATA = 2;
DataSourceJson.MALFORMED_DATA = 3;
DataSourceJson.SESSION_EXPIRED = 4;

// Throwable DataSourceJson errors
DataSourceJson.NoDataError = function(message) {
    this.name = "NoDataError";
    this.message = message;
}
DataSourceJson.NoDataError.prototype = new Error();
































/*
 * Return data with the passed prompt_id and group_id
 */
DataSourceJson.prototype.retrieve_data = function(prompt_id, group_id) {
    // Filter out the needed data and remove RESPONSE_SKIPPED for now
    var filtered_data = this.current_data.filter(function(data_point) {
        return ((prompt_id == data_point.prompt_id) && 
                (group_id == data_point.prompt_group_id) &&
                (data_point.response != "RESPONSE_SKIPPED"));
    });
    
    // Do some sanity checking on the filtered data
    // If no data found
    if (filtered_data.length == 0) {
        throw new DataSourceJson.NoDataError("retrieve_data(): Found no data for prompt_id " + prompt_id + " and group_id " + group_id);
    }
    
    return filtered_data;
}

/*
 * Return data for the saliva data type.  This is an example of "meta tagging"
 * data that should be done on the server-side.
 */
DataSourceJson.prototype.retreive_data_saliva = function() {
    var saliva_sample_time = this.current_data.filter(function(data_point) {
        return (data_point.prompt_id == 0 &&
                data_point.prompt_group_id == 0);
    });
    
    var saliva_meta_data = this.current_data.filter(function(data_point) {
        return (data_point.prompt_id == 1 &&
                data_point.prompt_group_id == 0);
    });
    
    // be sure we have some data
    if (saliva_sample_time == null || saliva_sample_time.length == 0) {
        throw new DataSourceJson.NoDataError("retreive_data_saliva(): Found no data.");   
    }
    
    // Used to store all the data needed for the saliva graph
    var data_array = [];
    
    // Run over each data point
    for (var i = 0; i < saliva_sample_time.length; i += 1) {
        // Check for RESPONSE_SKIPPED, skip whole data point if so
        if (saliva_sample_time[i].response == 'RESPONSE_SKIPPED' ||
            saliva_meta_data[i].response == 'RESPONSE_SKIPPED') {
            continue;
        }
        
        // Create a new data point
        var data_point = new Object();
        data_point.date = saliva_sample_time[i].date;
        data_point.response = saliva_sample_time[i].response;
        data_point.meta_data = saliva_meta_data[i].response;
        
        data_array.push(data_point);
    }
    
    // be sure we have some data
    if (data_array.length == 0) {
        throw new DataSourceJson.NoDataError("retrieve_data_sleep_time(): Found no data.");   
    }
    
    return data_array;
}

/*
 * Return data specifically for a customized sleep graph.  This is functionality
 * that should eventually be moved to the server side, but we can do this here
 * for now.
 * 
 * Tons of magic numbers and hacks for now
 */
DataSourceJson.prototype.retrieve_data_sleep_time = function() {
    var time_in_bed = this.current_data.filter(function(data_point) {
        return ((data_point.prompt_id == 0) && 
                (data_point.prompt_group_id == 1));
    });
    
    var time_to_fall_asleep = this.current_data.filter(function(data_point) {
        return ((data_point.prompt_id == 1) && 
                (data_point.prompt_group_id == 1));
    });
    
    var time_awake = this.current_data.filter(function(data_point) {
        return ((data_point.prompt_id == 2) && 
                (data_point.prompt_group_id == 1));
    });
    
    var reported_hours_asleep = this.current_data.filter(function(data_point) {
        return ((data_point.prompt_id == 3) && 
                (data_point.prompt_group_id == 1));
    });
    
    var reported_sleep_quality = this.current_data.filter(function(data_point) {
        return ((data_point.prompt_id == 4) && 
                (data_point.prompt_group_id == 1));
    });
    
    
    // Used to store all the data needed for the sleep graph
    var data_array = [];
    var previous_day = null;
    
    // Run over each data point
    for (var i = 0; i < time_in_bed.length; i += 1) {
        // Check to see if anything is RESPOSNE_SKIPPED, skip whole data point if so
        if (time_in_bed[i].response == 'RESPONSE_SKIPPED' ||
            time_to_fall_asleep[i].response == 'RESPONSE_SKIPPED' ||
            time_awake[i].response == 'RESPONSE_SKIPPED' ||
            reported_hours_asleep[i].response == 'RESPONSE_SKIPPED' ||
            reported_sleep_quality[i].response == 'RESPONSE_SKIPPED') {
            continue;
        }
        
        var cur_day = time_in_bed[i].date;
        
        // Make sure this is a new day of data
        if (cur_day == previous_day) {
            continue;
        }
        else {
            previous_day = cur_day;
        }
        
        // Create a new data point
        var data_point = new Object();
        data_point.date = cur_day;
        data_point.time_in_bed = Date.parseDate(time_in_bed[i].response, "g:i").grabTime();
        
        // Check if "time_in_bed" is yesterday (before midnight) or today (after midnight)
        if (data_point.time_in_bed < new Date(0,0,0,15,0,0)) {
            data_point.time_in_bed = data_point.time_in_bed.incrementDay(1);
        }
        data_point.time_to_fall_asleep = parseInt(time_to_fall_asleep[i].response);
        
        // Increment day by 1 to move awake time to "today"
        data_point.time_awake = Date.parseDate(time_awake[i].response, "g:i").grabTime().incrementDay(1);
        
        data_point.reported_hours_asleep = parseInt(reported_hours_asleep[i].response);
        data_point.reported_sleep_quality = parseInt(reported_sleep_quality[i].response);
        
        // Push data point onto the data array
        data_array.push(data_point);
    }
    
    // be sure we have some data
    if (data_array.length == 0) {
        throw new DataSourceJson.NoDataError("retrieve_data_sleep_time(): Found no data.");   
    }
    
    return data_array;
}


/*
 * Call to populate the data store with new data from start_date to end_date.
 * Pass in a call back function to handle any possible errors.
 */
DataSourceJson.prototype.populate_data = function(start_date, end_date, callback) {
    // Make sure the data types are correct
    if (!(typeof start_date == "string") || 
        !(typeof end_date == "string") || 
        callback == null) {
        throw TypeError("populate_data(): Incorrect argument type passed.");
    }
    
    // Save the callback to call when data is retrieved from the server
    this.callback = callback;
    
    // Send out the JSON request
    var _url = this.url + "?s=" + start_date + "&e=" + end_date;
    if (DataSourceJson._logger.isDebugEnabled()) {
        DataSourceJson._logger.debug("Grabbing data from URL: " + _url);
    }
    try {
        // Use jQuery proxy to enforce context when the asynchronous callback
        // comes back from the server (or else we would lose the this variable)
        $.getJSON(_url, jQuery.proxy( this.populate_data_callback, this ));
    }
    catch (error) {
        throw new Error("populate_data(): Problem retrieving JSON from the server.");
    }
}

/*
 * The callback to handle incoming JSON data from the server.
 * Send any errors to the callback function.
 */
DataSourceJson.prototype.populate_data_callback = function(json_data, text_status) {
    // Clear the old data
    this.current_data = [];                    
    var error = 0;
    
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received JSON data from server with status: " + text_status);
    }    
    
    // Did the request succeed?
    if (text_status != "success") {
        if (DataSourceJson._logger.isErrorEnabled()) {
            DataSourceJson._logger.error("Bad status from server: " + text_status);
        }
        return DataSourceJson.BAD_STATUS;
    }
    
    // Make sure the data makes sense
    error = this.validate_data(json_data);
    if (error > 0) {
        this.callback(error);
        return;
    }
    
    // Do basic data filtering
	this.preprocess_data(json_data);
	
	// Save the data for later retrieval
	this.current_data = json_data;
	
	this.callback(error);
}

/*
 * Make sure the data makes sense and is not an error
 */
DataSourceJson.prototype.validate_data = function(json_data) {   
    // Make sure we found JSON
    if (json_data == null) {
        if (DataSourceJson._logger.isWarnEnabled()) {
            DataSourceJson._logger.warn("Bad response from server!");
        }
        
        return DataSourceJson.MALFORMED_DATA;
    }
    
    // Run through possible error codes from server
    
    // 0104 is session expired, redirect to the passed URL
    if (json_data.code != null && json_data.code == "0104") {
        if (DataSourceJson._logger.isInfoEnabled()) {
            DataSourceJson._logger.info("Session expired, redirecting to: " + json_data.text);
        }
        
        // Should handle this at a higher layer, not here
        window.location = json_data.text;
        
        return DataSourceJson.SESSION_EXPIRED;
    }
    
    // Make sure we have an array of data points
    if (!json_data instanceof Array || json_data.length == 0) {
        if (DataSourceJson._logger.isWarnEnabled()) {
            DataSourceJson._logger.warn("No data found from server!");
        }

        return DataSourceJson.NO_DATA;
    }
    
    // If we made it through all the checks, success
    return DataSourceJson.SUCCESS;
}

/*
 * Any pre-processing of the data will get done here.
 */
DataSourceJson.prototype.preprocess_data = function(json_data) {
    // Pull out the day into a Date for each data point
    json_data.forEach(function(d) {
        var period = d.time.lastIndexOf('.');
        d.date = Date.parseDate(d.time.substring(0, period), "Y-m-d g:i:s").grabDate();
        
        // Check if the date was parsed correctly
        if (d.date == null) {
            if (DataSourceJson._logger.isErrorEnabled()) {
                DataSourceJson._logger.error("Date parsed incorrectly from: " + d.time);
            }
        }
    });
}

