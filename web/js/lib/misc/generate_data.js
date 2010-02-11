//LoadScriptJQuery('lib/misc/date_lib.js');

/*
 * Various functions to generate random data for the four EMA
 * question types.
 */

// Generate an array of random time based data, one per num_days
var generateTimeData = function(start_date, num_days) {
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
var generateTrueFalseArrayData = function(start_date, num_days, num_vals) {
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
var generateIntegerData = function(start_date, num_days, int_range) {
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
