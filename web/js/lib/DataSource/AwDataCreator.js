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


function SurveysPerDayAwDataCreator() {
	
}
SurveysPerDayAwDataCreator.prototype = new AwDataCreator();

SurveysPerDayAwDataCreator.prototype.create_from = function(json_data) {
	var awData = new SurveysPerDayAwData();
	awData.set_data(json_data);
	return awData;
}


function MobilityPerDayAwDataCreator() {
	
}
MobilityPerDayAwDataCreatorprototype = new AwDataCreator();

MobilityPerDayAwDataCreator.prototype.create_from = function(json_data) {
	var awData = new MobilityPerDayAwData();
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



function HoursSinceLastSurveyAwDataCreator() {
	
}
HoursSinceLastSurveyAwDataCreator.prototype = new AwDataCreator();

HoursSinceLastSurveyAwDataCreator.prototype.create_from = function(json_data) {
	var awData = new HoursSinceLastSurveyAwData();
	awData.set_data(json_data);
	return awData;
}


function LocationUpdatesAwDataCreator() {
	
}
LocationUpdatesAwDataCreator.prototype = new AwDataCreator();

LocationUpdatesAwDataCreator.prototype.create_from = function(json_data) {
	var awData = new LocationUpdatesAwData();
	awData.set_data(json_data);
	return awData;
}