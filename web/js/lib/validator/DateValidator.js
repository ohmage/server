/**
 * Date validator for entered dates with format YYYY-mm-dd.
 * 
 * @author hicks
 */
function DateValidator() {
}

// Only one possible date format
DateValidator._date_format = "Y-m-d";
// Setup a static logger
DateValidator._logger = log4javascript.getLogger();

/**
 * Uses the format to check the validity of the passed date.  Due to ECMA quirkiness, days
 * past the valid range (e.g., Feb 30) gets transformed in to the next month.  Also check
 * for this.
 * 
 * @return true if the date is valid for the format
 * @false otherwise
 */
DateValidator.validate = function(dateElement) {
	var stringDate = dateElement.value;
	
	if (_logger.isDebugEnabled()) {
		_logger.debug("DateValidator: Validating date string: " + stringDate);
	}

    // Make sure the field is not empty
	if (stringDate == null || stringDate == "") {
		if(_logger.isDebugEnabled()) {
            _logger.debug("DateValidator: Date is empty.")
        }
		
		return false;
	}
	
	// Make sure the date parses correctly
    try {
        var parsedDate = Date.parseDate(stringDate, DateValidator._date_format);
    }
    catch (error) {
        if(_logger.isDebugEnabled()) {
			_logger.debug("DateValidator: " + error)
        }
        
        return false;
    }
	
	// Make sure the parsed date is not NULL
	if (parsedDate == null) {
		if(_logger.isDebugEnabled()) {
            _logger.debug("DateValidator: Date did not parse correctly")
        }
		return false;
	}
	
	if(_logger.isDebugEnabled()) {
        _logger.debug("DateValidator: Parsed date is: " + parsedDate)
    }
    
    // Make sure the parsed date has the same month as the string date
    var reg = /^(\d+)\-(\d+)\-(\d+)$/;
    var matchArray = reg.exec(stringDate);
	
	// If the string month is different from the date month, reject
	if (parseInt(matchArray[2]) != parsedDate.getMonth() + 1) {
		if(_logger.isDebugEnabled()) {
	        _logger.debug("DateValidator: " + parseInt(matchArray[2]) + " does not equal " + parsedDate.getMonth() + 1);
	    }
    
		return false;
	}
    
    // All tests have passed
    return true;
}
