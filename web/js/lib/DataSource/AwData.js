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
    this.current_data = null;
}

EmaAwData.prototype = new AwData();

// EMA data setters
EmaAwData.prototype.set_data = function(json_data) {
	// Preprocess the data to pull out the day into a Date
    json_data.forEach(function(d) {
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
	this.current_data = json_data;
}


// EMA data getters


/*
 * Return data with the passed prompt_id and group_id
 */
EmaAwData.prototype.get_data = function(prompt_id, group_id) {
    // Filter out the needed data and remove RESPONSE_SKIPPED for now
    var filtered_data = this.current_data.filter(function(data_point) {
        return ((prompt_id == data_point.prompt_id) && 
                (group_id == data_point.prompt_group_id) &&
                (data_point.response != "RESPONSE_SKIPPED"));
    });
    
    // Do some sanity checking on the filtered data
    // If no data found
    if (filtered_data.length == 0) {
        throw new AwData.NoDataError("get_data(): Found no data for prompt_id " + prompt_id + " and group_id " + group_id);
    }
    
    return filtered_data;
}


/*
 * Return data specifically for a customized sleep graph.  This is functionality
 * that should eventually be moved to the server side, but we can do this here
 * for now.
 * 
 * Tons of magic numbers and hacks for now
 */
EmaAwData.prototype.get_data_sleep_time = function() {
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
        throw new AwData.NoDataError("get_data_sleep_time(): Found no data.");   
    }
    
    return data_array;
}

/*
 * Return data for the saliva data type.  This is an example of "meta tagging"
 * data that should be done on the server-side.
 */
EmaAwData.prototype.get_data_saliva = function() {
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
        throw new AwData.NoDataError("get_data_sleep_time(): Found no data.");   
    }
    
    return data_array;
}


function SurveysPerDayAwData() {
	this.current_data = null;
}
SurveysPerDayAwData.prototype = new AwData();

SurveysPerDayAwData.prototype.set_data = function(json_data) {
    // Preprocess the data to pull out the day into a Date object
	// Push into another array to remove users with NO dates
	//var noDatesRemoved = [];
    json_data.forEach(function(d) {
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
    var userName = new Object();
    // Setup the object by adding each data point's user name to the keys.
    // Then, add the date into the user name
    // Finally, create an array of 5 representing the 5 group IDs that can be returned
    // (Super hard coded and weirdness to deal with format of returned server data)
    json_data.forEach(function(d) {
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
    		var group_id = d.groups[group].group_id;
    		var value = d.groups[group].value;
    		userName[d.user][d.date][group_id - 1] = value;
    	}
    });

    
	// Now, separate everything out into an array of users, each with an array of date/group_id pairs
	var separated_data = [];
	for (var user in userName) {
		var userObject = new Object();
		userObject.user = user;
		userObject.data = [];
		
		for (var date in userName[user]) {
			for (var i = 0; i < 5; i++) {
				var newDataPoint = new Object();
				newDataPoint.date = date;
				newDataPoint.group_id = i;
				newDataPoint.response = userName[user][date][i];
				newDataPoint.day_count = i;
				newDataPoint.total_day_count = 5;
				
				// Append new data point to the separated data
				userObject.data.push(newDataPoint);
			}
		}
		
		separated_data.push(userObject);
	}
	
	this.current_data = separated_data;
}


function MobilitiyModesPerDayAwData() {
	this.current_data = null;
}
MobilitiyModesPerDayAwData.prototype = new AwData();

MobilitiyModesPerDayAwData.prototype.set_data = function(json_data) {
	this.current_data = json_data;
}


function HoursSinceLastUpdateAwData() {
	this.current_data = null;
}
HoursSinceLastUpdateAwData.prototype = new AwData();

HoursSinceLastUpdateAwData.prototype.set_data = function(json_data) {
	// Run through data, remove any 0s, assume no data returned
	var removedZeros = [];
	json_data.forEach(function(d) {
		if (d.value != 0) {
			removedZeros.push(d);
		}
	});
	
	this.current_data = removedZeros;
}


function HoursSinceLastSurveyAwData() {
	this.current_data = null;
}
HoursSinceLastSurveyAwData.prototype = new AwData();

HoursSinceLastSurveyAwData.prototype.set_data = function(json_data) {
	this.current_data = json_data;
}



function LocationUpdatesAwData() {
	this.current_data = null;
}
LocationUpdatesAwData.prototype = new AwData();

LocationUpdatesAwData.prototype.set_data = function(json_data) {
	this.current_data = json_data;
}

