/*
 * DataSource - Handles grabbing data according to the defined survey.
 * Takes in the survey definition, then can be told to grab data for
 * a certain time period, and return data according to type keys
 * (prompt_id, group_id).
 * 
 * Can be set to either generate data randomly, or to grab data from
 * a remote JSON source.
 */

// DataSource constuctor
function DataSource() {
	current_data = [];
}

DataSource.data_source_type = {
	RANDOM_DATA:0,
	REMOTE_JSON:1,
}

// DataSource factory to create the correct DataSource type
// based on the delected data source
DataSource.factory = function(data_source_type) {
	if (data_source_type == this.data_source_type.RANDOM_DATA) {
		return new DataSourceRandomData();
	}
	if (data_source_type == this.data_source_type.REMOTE_JSON) {
        return new DataSourceRemoteJson();
    }
	else {
		throw new Error("DataSource.factory() - Data source undefined.");
	}
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
DataSource.prototype.populate_data = function(start_date, num_days) {
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

DataSourceRemoteJson.prototype.populate_data = function(start_date, num_days, remote_addr) {
	
}

/*
 * DataSourceRandomData - Generates random data on the fly as
 * a data source for the various graph types.
 */
function DataSourceRandomData() {
	// Inherit properties
    DataSource.call(this);
}

// Inherit methods
DataSourceRandomData.prototype = new DataSource();

DataSourceRandomData.prototype.populate_data = function(start_date, num_days, graph_types) {
	// Delete the current data, then grab new data
	this.current_data = [];
	
	// Run through every graph_type, populate the data store with random data
	var that = this;
	graph_types.forEach(function (graph) {
		if (graph.type == ProtoGraph.graph_type.PROTO_GRAPH_TIME_ONCE_TYPE) {
             var new_data = that.generateTimeData(start_date, num_days);
        }
        if (graph.type == ProtoGraph.graph_type.PROTO_GRAPH_TRUE_FALSE_ARRAY_TYPE) {
             var new_data = that.generateTrueFalseArrayData(start_date, num_days, graph.y_labels.length);
        }
        if (graph.type == ProtoGraph.graph_type.PROTO_GRAPH_INTEGER_TYPE) {
             var new_data = that.generateIntegerData(start_date, num_days, graph.y_labels.length);
        }
        if (graph.type == ProtoGraph.graph_type.PROTO_GRAPH_YES_NO_TYPE) {
             var new_data = that.generateIntegerData(start_date, num_days, 2);
        }
		
		// Splice the query_id and group_id into the new_data, for future lookups
		// This is a hack but imitates what is sent to us from an actual server
		new_data.forEach(function(data) {
			data.prompt_id = graph.prompt_id;
            data.group_id = graph.group_id;
		});
		
		
		// Concat the new data in to the current data
		that.current_data = that.current_data.concat(new_data);
	});
}

DataSourceRandomData.prototype.grab_data = function(prompt_id, group_id) {
	// Copy the arguments locally to be accessible from the closure below
	var group_id = group_id;
	var prompt_id = prompt_id;
	// Filter the data according to the query and group id
	return this.current_data.filter(function(data) {
		return ((prompt_id == data.prompt_id) && (group_id == data.group_id));
	});
}


// Generate an array of random time based data, one per num_days
DataSourceRandomData.prototype.generateTimeData = function(start_date, num_days) {
    var time_data = [];
    
    // Iterate num_days days
    for (var i = 0; i < num_days; i++) {
        var day = start_date.incrementDay(i);
        // Set a random hour, minute, second
        day.setHours(Math.floor(Math.random() * 24),
                     Math.floor(Math.random() * 60),
                     Math.floor(Math.random() * 60));
        time_data.push(day);
    }
    
    return time_data;
};
 
// Generate an array of objects that represent random true false values
DataSourceRandomData.prototype.generateTrueFalseArrayData = function(start_date, num_days, num_vals) {
    var true_false_data = [];
    
    // iterate num_days days
    for (var i = 0; i < num_days; i++) {
        var truefalse = new Object();
        truefalse.datetime = start_date.incrementDay(i);
        truefalse.response = [];
        // Iterate num_vals, append that many to the true false array
        for (var j = 0; j < num_vals; j++) {
            truefalse.response.push(Math.floor(Math.random() * 2));
        }
        // Append our new object into the array
        true_false_data.push(truefalse);
    }
    
    return true_false_data;
};

// Generate an array of integers
DataSourceRandomData.prototype.generateIntegerData = function(start_date, num_days, int_range) {
    var int_data = [];
    
    // Iterate over the number of days required
    for (var i = 0; i < num_days; i++) {
        var intdata = new Object();
        intdata.datetime = start_date.incrementDay(i);
        intdata.response = Math.floor(Math.random() * int_range);
        
        int_data.push(intdata);
    }
    
    return int_data;
}
