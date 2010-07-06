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
DataSourceJson.requestData = function(dataType, params) {
    if (DataSourceJson._logger.isDebugEnabled()) {
        DataSourceJson._logger.debug("Grabbing data type: " + dataType);
    }

    // Send out the JSON request depending on the data type

	    switch (dataType) {
	    case DataSourceJson.DATA_EMA:
	        $.getJSON(dataType, params, DataSourceJson.receiveEmaData);
	        break;
	    case DataSourceJson.DATA_SURVEYS_PER_DAY:
	        $.getJSON(dataType, params, DataSourceJson.receiveDataSurveysPerDay);
	        break;
	    case DataSourceJson.DATA_MOBILITY_MODE_PER_DAY:
	    	$.getJSON(dataType, params, DataSourceJson.receiveMobilityModePerDay);
	    	break;
	    case DataSourceJson.DATA_LOCATION_UPDATES :
	        $.getJSON(dataType, params, DataSourceJson.receiveDataLocationUpdates);
	        break;
	    case DataSourceJson.DATA_HOURS_SINCE_LAST_UPDATE:
	    	$.getJSON(dataType, DataSourceJson.receiveDataHoursSinceLastUpdate);
	    	break;
	    case DataSourceJson.DATA_HOURS_SINCE_LAST_SURVEY:
	        $.getJSON(dataType, DataSourceJson.receiveDataHoursSinceLastSurvey);
	        break;
	    default:
	    	throw new Error("Unknown data type: " + dataType);
	    }
	

}

/*
 * Create an EmaDataCreator and pass into the standard incoming data function
 */
DataSourceJson.receiveEmaData = function(jsonData, textStatus) {    
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received EMA data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new EmaAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receiveData(jsonData, textStatus, awDataCreator);
}


DataSourceJson.receiveDataSurveysPerDay = function(jsonData, textStatus) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received surveys per day data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new SurveysPerDayAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receiveData(jsonData, textStatus, awDataCreator);	
}

DataSourceJson.receiveMobilityModePerDay = function(jsonData, textStatus) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received mobilities per day data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new MobilityPerDayAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receiveData(jsonData, textStatus, awDataCreator);	
}



/*
 * Create an HoursSinceLastUpdateAwDataCreator and pass into the standard incoming data function
 */
DataSourceJson.receiveDataHoursSinceLastUpdate = function(jsonData, textStatus) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received hours since last update data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new HoursSinceLastUpdateAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receiveData(jsonData, textStatus, awDataCreator);	
}

DataSourceJson.receiveDataHoursSinceLastSurvey = function(jsonData, textStatus) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received hours since last survey data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new HoursSinceLastSurveyAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receiveData(jsonData, textStatus, awDataCreator);		
}

DataSourceJson.receiveDataLocationUpdates = function(jsonData, textStatus) {
    if (DataSourceJson._logger.isInfoEnabled()) {
        DataSourceJson._logger.info("Received percentage good location updates in past day data from server.");
    }    
    
    // Create the EMA data object creator
	var awDataCreator = new LocationUpdatesAwDataCreator();
	
	// Pass the data to the dashboard
	DataSourceJson.receiveData(jsonData, textStatus, awDataCreator);		
}


/*
 * All incoming data will come through this function.  Perform basic data validation,
 * create an AwData object, and pass to the dashboard.
 */
DataSourceJson.receiveData = function(jsonData, textStatus, awDataCreator) {                 
    var error = 0;

    // Did the request succeed?
    if (textStatus != "success") {
        if (DataSourceJson._logger.isErrorEnabled()) {
            DataSourceJson._logger.error("Bad status from server: " + textStatus);
        }
		return;
    }
    
    // Basic data validation
    error = DataSourceJson.validate_data(jsonData);
    if (error > 0) {
        if (DataSourceJson._logger.isErrorEnabled()) {
            DataSourceJson._logger.error("Incoming data failed validation with error: " + error);
        }
        return;
    }
    
    // Create the EMA data object
	var awData = awDataCreator.createFrom(jsonData);
	
	// Pass the data to the dashboard
	dashBoard.passData(awData)
}

/*
 * Basic data validation.  Make sure data exists and is not an error.
 * Errors are usually either the server is down or the user has been
 * idle for too long and their session has expired.
 */
DataSourceJson.validate_data = function(jsonData) {   
    // Make sure we found JSON
    if (jsonData == null) {
        if (DataSourceJson._logger.isWarnEnabled()) {
            DataSourceJson._logger.warn("Bad response from server!");
        }
        
        return DataSourceJson.MALFORMED_DATA;
    }
    
    // Run through possible error codes from server
    
    // 0104 is session expired, redirect to the passed URL
    if (jsonData.code != null && jsonData.code == "0104") {
        if (DataSourceJson._logger.isInfoEnabled()) {
            DataSourceJson._logger.info("Session expired, redirecting to: " + jsonData.text);
        }
        
        // Should handle this at a higher layer, not here
        window.location = jsonData.text;
        
        return DataSourceJson.SESSION_EXPIRED;
    }
    
    // Make sure we have an array of data points
    if (!jsonData instanceof Array || jsonData.length == 0) {
        if (DataSourceJson._logger.isWarnEnabled()) {
            DataSourceJson._logger.warn("No data found from server!");
        }

        return DataSourceJson.NO_DATA;
    }
    
    // If we made it through all the checks, success
    return DataSourceJson.SUCCESS;
}



