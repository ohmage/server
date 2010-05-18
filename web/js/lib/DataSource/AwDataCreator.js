function AwDataCreator() {
	
}

AwDataCreator.prototype.createFrom = function() {
	throw new Error('createFrom() not yet implemented!');
}


/*
 * EmaAwDataCreator - Create an EmaAwData object from EMA data from the server.
 */
function EmaAwDataCreator() {
	
}
EmaAwDataCreator.prototype = new AwDataCreator();

EmaAwDataCreator.prototype.create_from = function(json_data) {
	var awData = new EmaAwData();
	awData.set_data(json_data);
	return awData;
}


function HoursSinceLastUpdateAwDataCreator() {
	
}
HoursSinceLastUpdateAwDataCreator.prototype = new AwDataCreator();

HoursSinceLastUpdateAwDataCreator.prototype.create_from = function(json_data) {
	var awData = new HoursSinceLastUpdateAwData();
	awData.set_data(json_data);
	return awData;
}