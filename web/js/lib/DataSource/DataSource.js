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
DataSourceJson.DATA_MOBILITY_MODE_PER_DAY = '/app/q/mobility-modes-per-day';
DataSourceJson.DATA_LOCATION_UPDATES = '/app/q/percent-successful-location-updates';
DataSourceJson.DATA_HOURS_SINCE_LAST_UPDATE = '/app/q/hours-since-last-update';
DataSourceJson.DATA_HOURS_SINCE_LAST_SURVEY = '/app/q/hours-since-last-survey';



/*
 * Send a request for data to the server.  Takes in an dict of parameters
 * and a request URL.  The data will return to the specific receive function.
 */
DataSourceJson.request_data = function(data_type, params) {
    if (DataSourceJson._logger.isDebugEnabled()) {
        DataSourceJson._logger.debug("Grabbing data type: " + data_type);
    }

    // Send out the JSON request depending on the data type

	    switch (data_type) {
	    case DataSourceJson.DATA_EMA:
	        $.getJSON(data_type, params, DataSourceJson.receive_data_ema);
	        break;
	    case DataSourceJson.DATA_SURVEYS_PER_DAY:
	        $.getJSON(data_type, params, DataSourceJson.receive_data_surveys_per_day);
	        break;
	    case DataSourceJson.DATA_MOBILITY_MODE_PER_DAY:
	    	$.getJSON(data_type, params, DataSourceJson.receive_mobility_mode_per_day);
	    	break;
	    case DataSourceJson.DATA_LOCATION_UPDATES :
	        $.getJSON(data_type, params, DataSourceJson.receive_data_location_updates);
	        break;
	    case DataSourceJson.DATA_HOURS_SINCE_LAST_UPDATE:
	    	$.getJSON(data_type, DataSourceJson.receive_data_hours_since_last_update);
	    	break;
	    case DataSourceJson.DATA_HOURS_SINCE_LAST_SURVEY:
	        $.getJSON(data_type, DataSourceJson.receive_data_hours_since_last_survey);
	        break;
	    default:
	    	throw new Error("Unknown data type: " + data_type);
	    }
	

}

/*
 * Create an EmaDataCreator and pass into the standard incoming data function
 */
DataSourceJson.receive_data_ema = function(json_data, text_status) {    
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received EMA data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new EmaAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receive_data(json_data, text_status, awDataCreator);
}


DataSourceJson.receive_data_surveys_per_day = function(json_data, text_status) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received surveys per day data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new SurveysPerDayAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receive_data(json_data, text_status, awDataCreator);	
}

DataSourceJson.receive_mobility_mode_per_day = function(json_data, text_status) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received mobilities per day data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new MobilityPerDayAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receive_data(json_data, text_status, awDataCreator);	
}



/*
 * Create an HoursSinceLastUpdateAwDataCreator and pass into the standard incoming data function
 */
DataSourceJson.receive_data_hours_since_last_update = function(json_data, text_status) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received hours since last update data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new HoursSinceLastUpdateAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receive_data(json_data, text_status, awDataCreator);	
}

DataSourceJson.receive_data_hours_since_last_survey = function(json_data, text_status) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received hours since last survey data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new HoursSinceLastSurveyAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receive_data(json_data, text_status, awDataCreator);		
}

DataSourceJson.receive_data_location_updates = function(json_data, text_status) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received percentage good location updates in past day data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new LocationUpdatesAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receive_data(json_data, text_status, awDataCreator);		
}


/*
 * All incoming data will come through this function.  Perform basic data validation,
 * create an AwData object, and pass to the dashboard.
 */
DataSourceJson.receive_data = function(json_data, text_status, aw_data_creator) {                 
    var error = 0;

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
            DataSourceJson._logger.error("Incoming data failed validation with error: " + error);
        }
        return;
    }
    
    // Create the EMA data object
	var awData = aw_data_creator.create_from(json_data);
	
	// Pass the data to the dashboard
	dashBoard.pass_data(awData)
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



