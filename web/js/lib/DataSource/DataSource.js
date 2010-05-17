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
function DataSourceJson() {
	//this.current_data = [];
	//this.callback = null;
	//this.error_status = 0;
	//this.url=url;
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

// AndWellness server API URLs
DataSourceJson.DATA_EMA = '/app/q/ema';
DataSourceJson.DATA_SURVEYS_PER_DAY = '/app/q/completed-surveys-per-day';
DataSourceJson.DATA_LOCATION_UPDATES = '/app/q/percent-successful-location-updates';



/*
 * Send a request for data to the server.  Takes in an dict of parameters
 * and a request URL.  The data will return to the specific receive function.
 */
DataSourceJson.request_data = function(data_type, params) {
    if (DataSourceJson._logger.isDebugEnabled()) {
        DataSourceJson._logger.debug("Grabbing data from URL: " + data_type);
    }

    // Send out the JSON request depending on the data type
	try {
	    switch (data_type) {
	    case DataSourceJson.DATA_EMA:
	        $.getJSON(data_type, params, DataSourceJson.receive_data_ema)
	    }		
	}
    catch (error) {
        throw new Error("populate_data(): Problem retrieving JSON from the server.");
    }
}

/*
 * Handles incoming EMA data from the server.  Create a new EmaAwData object and
 * pass to the dashboard.
 */
DataSourceJson.receive_data_ema = function(json_data, text_status) {                 
    var error = 0;
    
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received EMA data from server with status: " + text_status);
    }    
    
    // Did the request succeed?
    if (text_status != "success") {
        if (DataSourceJson._logger.isErrorEnabled()) {
            DataSourceJson._logger.error("Bad status from server: " + text_status);
        }
		return;
    }
    
    // Basic data validation
    error = DataSourceJson.validate_data(json_data);
    if (error > 0) {
        if (DataSourceJson._logger.isErrorEnabled()) {
            DataSourceJson._logger.error("Incoming EMA data failed validation with error: " + error);
        }
        return;
    }
    
    // Create the EMA data object
	var ema_data = AwDataCreator.create_ema_aw_data(json_data);
	
	// Pass the data to the dashboard
	dashBoard.pass_data(ema_data)
}



/*
 * Basic data validation.  Make sure data exists and is not an error.
 * Errors are usually either the server is down or the user has been
 * idle for too long and their session has expired.
 */
DataSourceJson.validate_data = function(json_data) {   
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



