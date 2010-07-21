function AwData() {

}

AwData._logger = log4javascript.getLogger();

// Throwable AwData errors
AwData.NoDataError = function(message) {
    this.name = "NoDataError";
    this.message = message;
}
AwData.NoDataError.prototype = new Error();


function EmaAwData() {
    this.currentData = null;
}

EmaAwData.prototype = new AwData();

// EMA data setters
EmaAwData.prototype.setData = function(jsonData) {
	// Preprocess the data to pull out the day into a Date
    jsonData.forEach(function(d) {
        var period = d.time.lastIndexOf('.');
        d.date = Date.parseDate(d.time.substring(0, period), "Y-m-d g:i:s").grabDate();
        
        // Check if the date was parsed correctly
        if (d.date == null) {
            if (AwData._logger.isErrorEnabled()) {
                AwData._logger.error("Date parsed incorrectly from: " + d.time);
            }
        }
    });
	
	// Save the data
	this.currentData = jsonData;
}


// EMA data getters

/*
 * Return data with basic filtering
 */
EmaAwData.prototype.getDataFiltered = function() {
	// Filter out RESPONSE_SKIPPED for now
    var filteredData = this.currentData.filter(function(dataPoint) {
        return ((dataPoint.response != "RESPONSE_SKIPPED"));
    });
    
    return filteredData;
}

/*
 * Return data with the passed promptId and groupId
 */
EmaAwData.prototype.getData = function(promptId, groupId) {
    // Filter out the needed data and remove RESPONSE_SKIPPED for now
    var filteredData = this.currentData.filter(function(dataPoint) {
        return ((promptId == dataPoint.prompt_id) && 
                (groupId == dataPoint.prompt_group_id) &&
                (dataPoint.response != "RESPONSE_SKIPPED"));
    });
    
    // Do some sanity checking on the filtered data
    // If no data found
    if (filteredData.length == 0) {
        throw new AwData.NoDataError("getData(): Found no data for promptId " + promptId + " and groupId " + groupId);
    }
    
    return filteredData;
}


/*
 * Return data specifically for a customized sleep graph.  This is functionality
 * that should eventually be moved to the server side, but we can do this here
 * for now.
 * 
 * Tons of magic numbers and hacks for now
 */
EmaAwData.prototype.getDataSleepTime = function() {
    var timeInBed = this.currentData.filter(function(dataPoint) {
        return ((dataPoint.prompt_id == 0) && 
                (dataPoint.prompt_group_id == 1));
    });
    
    var timeToFallAsleep = this.currentData.filter(function(dataPoint) {
        return ((dataPoint.prompt_id == 1) && 
                (dataPoint.prompt_group_id == 1));
    });
    
    var timeAwake = this.currentData.filter(function(dataPoint) {
        return ((dataPoint.prompt_id == 2) && 
                (dataPoint.prompt_group_id == 1));
    });
    
    var reportedHoursAsleep = this.currentData.filter(function(dataPoint) {
        return ((dataPoint.prompt_id == 3) && 
                (dataPoint.prompt_group_id == 1));
    });
    
    var reportedSleepQuality = this.currentData.filter(function(dataPoint) {
        return ((dataPoint.prompt_id == 4) && 
                (dataPoint.prompt_group_id == 1));
    });
    
    
    // Used to store all the data needed for the sleep graph
    var dataArray = [];
    var previousDay = null;
    
    // Run over each data point
    for (var i = 0; i < timeInBed.length; i += 1) {
        // Check to see if anything is RESPOSNE_SKIPPED, skip whole data point if so
        if (timeInBed[i].response == 'RESPONSE_SKIPPED' ||
            timeToFallAsleep[i].response == 'RESPONSE_SKIPPED' ||
            timeAwake[i].response == 'RESPONSE_SKIPPED' ||
            reportedHoursAsleep[i].response == 'RESPONSE_SKIPPED' ||
            reportedSleepQuality[i].response == 'RESPONSE_SKIPPED') {
            continue;
        }
        
        var curDay = timeInBed[i].date;
        
        // Make sure this is a new day of data
        if (curDay == previousDay) {
            continue;
        }
        else {
            previousDay = curDay;
        }
        
        // Create a new data point
        var dataPoint = {};
        dataPoint.date = curDay;
        dataPoint.timeInBed = Date.parseDate(timeInBed[i].response, "g:i").grabTime();
        
        // Check if "timeInBed" is yesterday (before midnight) or today (after midnight)
        if (dataPoint.timeInBed < new Date(0,0,0,15,0,0)) {
            dataPoint.timeInBed = dataPoint.timeInBed.incrementDay(1);
        }
        dataPoint.timeToFallAsleep = parseInt(timeToFallAsleep[i].response);
        
        // Increment day by 1 to move awake time to "today"
        dataPoint.timeAwake = Date.parseDate(timeAwake[i].response, "g:i").grabTime().incrementDay(1);
        
        dataPoint.reportedHoursAsleep = parseInt(reportedHoursAsleep[i].response);
        dataPoint.reportedSleepQuality = parseInt(reportedSleepQuality[i].response);
        
        // Push data point onto the data array
        dataArray.push(dataPoint);
    }
    
    // be sure we have some data
    if (dataArray.length == 0) {
        throw new AwData.NoDataError("getDataSleepTime(): Found no data.");   
    }
    
    return dataArray;
}

/*
 * Return data for the saliva data type.  This is an example of "meta tagging"
 * data that should be done on the server-side.
 */
EmaAwData.prototype.getDataSaliva = function() {
    var salivaSampleTime = this.currentData.filter(function(dataPoint) {
        return (dataPoint.prompt_id == 0 &&
                dataPoint.prompt_group_id == 0);
    });
    
    var salivaMetaData = this.currentData.filter(function(dataPoint) {
        return (dataPoint.prompt_id == 1 &&
                dataPoint.prompt_group_id == 0);
    });
    
    // be sure we have some data
    if (salivaSampleTime == null || salivaSampleTime.length == 0) {
        throw new AwData.NoDataError("getDataSaliva(): Found no data.");   
    }
    
    // Used to store all the data needed for the saliva graph
    var dataArray = [];
    
    // Run over each data point
    for (var i = 0; i < salivaSampleTime.length; i += 1) {
        // Check for RESPONSE_SKIPPED, skip whole data point if so
        if (salivaSampleTime[i].response == 'RESPONSE_SKIPPED' ||
            salivaMetaData[i].response == 'RESPONSE_SKIPPED') {
            continue;
        }
        
        // Create a new data point
        var dataPoint = {};
        dataPoint.date = salivaSampleTime[i].date;
        dataPoint.response = salivaSampleTime[i].response;
        dataPoint.metaData = salivaMetaData[i].response;
        
        dataArray.push(dataPoint);
    }
    
    // be sure we have some data
    if (dataArray.length == 0) {
        throw new AwData.NoDataError("getDataSleepTime(): Found no data.");   
    }
    
    return dataArray;
}


function SurveysPerDayAwData() {
	this.currentData = null;
}
SurveysPerDayAwData.prototype = new AwData();

SurveysPerDayAwData.prototype.setData = function(jsonData) {
    // Preprocess the data to pull out the day into a Date object
    jsonData.forEach(function(d) {
    	try {
    		if (d.hasOwnProperty("date")) {
    			d.date = Date.parseDate(d.date, "Y-m-d").grabDate();
    			//noDatesRemoved.push(d);
    		}
    	}
    	catch (err) {
    		if (AwData._logger.isErrorEnabled()) {
                AwData._logger.error("Date parsed incorrectly: " + d.date);
            }
    	}
    });	
	
    // Create an object to hold user_name -> data key/value pairs
    var userName = {};
    // Setup the object by adding each data point's user name to the keys.
    // Then, add the date into the user name
    // Finally, create an array of 5 representing the 5 group IDs that can be returned
    // (Super hard coded and weirdness to deal with format of returned server data)
    jsonData.forEach(function(d) {
    	// If the user does not exist yet, add it now
    	if (!(userName.hasOwnProperty(d.user))) {
    		userName[d.user] = new Object;
    	}
    	
    	// If this user has no data, stop here and move on to the next data point
    	if (!(d.hasOwnProperty("date"))) {
    		return;
    	}
    	
    	// If the date does not yet exist in the user, add it now
    	if (!(userName[d.user].hasOwnProperty(d.date))) {
    		userName[d.user][d.date] = new Array(0,0,0,0,0);
    	}
    	
    	// Finally, add the values
    	for (var group in d.groups) {
    		var groupId = d.groups[group].group_id;
    		var value = d.groups[group].value;
    		userName[d.user][d.date][groupId - 1] = value;
    	}
    });

    
	// Now, separate everything out into an array of users, each with an array of date/group_id pairs
	var separatedData = [];
	for (var user in userName) {
		var userObject = {};
		userObject.user = user;
		userObject.data = [];
		
		for (var date in userName[user]) {
			for (var i = 0; i < 5; i++) {
				var newDataPoint = {};
				newDataPoint.date = date;
				newDataPoint.groupId = i;
				newDataPoint.response = userName[user][date][i];
				newDataPoint.dayCount = i;
				newDataPoint.totalDayCount = 5;
				
				// Append new data point to the separated data
				userObject.data.push(newDataPoint);
			}
		}
		
		separatedData.push(userObject);
	}
	
	this.currentData = separatedData;
}



function HoursSinceLastUpdateAwData() {
	this.currentData = null;
}
HoursSinceLastUpdateAwData.prototype = new AwData();

HoursSinceLastUpdateAwData.prototype.setData = function(jsonData) {
	// Run through data, remove any 0s, assume no data returned
	var removedZeros = [];
	jsonData.forEach(function(d) {
		if (d.value != 0) {
			removedZeros.push(d);
		}
	});
	
	this.currentData = removedZeros;
}


function HoursSinceLastSurveyAwData() {
	this.currentData = null;
}
HoursSinceLastSurveyAwData.prototype = new AwData();

HoursSinceLastSurveyAwData.prototype.setData = function(jsonData) {
	this.currentData = jsonData;
}



function LocationUpdatesAwData() {
	this.currentData = null;
}
LocationUpdatesAwData.prototype = new AwData();

LocationUpdatesAwData.prototype.setData = function(jsonData) {
	this.currentData = jsonData;
}


/*
 * MobilityPerDayAwData - Holds data to describe the amount of each activity
 * completed per day.  Translate into an array of data points, each with an array
 * of activities per day.  Normalize to 1.
 */
function MobilityPerDayAwData() {
	this.currentData = null;
}
MobilityPerDayAwData.prototype = new AwData();

MobilityPerDayAwData.prototype.setData = function(jsonData) {
    // Pre-process the data to pull out the day into a Date object
    jsonData.forEach(function(d) {
    	try {
    		if (d.hasOwnProperty("date")) {
    			d.date = Date.parseDate(d.date, "Y-m-d").grabDate();
    		}
    	}
    	catch (err) {
    		if (AwData._logger.isErrorEnabled()) {
                AwData._logger.error("Date parsed incorrectly: " + d.date);
            }
    	}
    });	
    
    // Create an object to hold user_name -> data key/value pairs
    var userName = {};
    // Setup the object by adding each data point's user name to the keys.
    // Then, add the date into the user name
    // Finally, create an array of 5 representing the 5 modes that can be returned
    // (Super hard coded and weirdness to deal with format of returned server data)
    jsonData.forEach(function(d) {
    	// If the user does not exist yet, add it now
    	if (!(userName.hasOwnProperty(d.user))) {
    		userName[d.user] = new Object;
    	}
    	
    	// If this user has no data, stop here and move on to the next data point
    	if (!(d.hasOwnProperty("date"))) {
    		return;
    	}
    	
    	// If the date does not yet exist in the user, add it now
    	if (!(userName[d.user].hasOwnProperty(d.date))) {
    		userName[d.user][d.date] = new Array(0,0,0,0,0);
    	}
    	
    	// Finally, add the values
    	d.modes.forEach(function(mode) {
    		var modeIndex = mobilityModes.findIndex(mode.mode);
			userName[d.user][d.date][modeIndex] += mode.value;
			
		});
    });   
    

    // Now, separate everything out into an array of users, each with an 
    // array of objects with a date/array pair
	var separatedData = [];
	for (var user in userName) {
		var userObject = {};
		userObject.user = user;
		userObject.data = new Array([], [], [], [] ,[]);
		
		for (var date in userName[user]) {
			for (var i = 0; i < userName[user][date].length; i += 1) {
				var newDataPoint = {};
				newDataPoint.date = date;
				newDataPoint.data = userName[user][date][i];
				// So we can color by index in the graphs later
				newDataPoint.index = i;
				userObject.data[i].push(newDataPoint)
			}
		}
		
		separatedData.push(userObject);
	}
	
	this.currentData = separatedData;    
}

