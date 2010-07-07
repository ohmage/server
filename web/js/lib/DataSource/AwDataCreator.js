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

EmaAwDataCreator.prototype.createFrom = function(jsonData) {
	var awData = new EmaAwData();
	awData.setData(jsonData);
	return awData;
}


function SurveysPerDayAwDataCreator() {
	
}
SurveysPerDayAwDataCreator.prototype = new AwDataCreator();

SurveysPerDayAwDataCreator.prototype.createFrom = function(jsonData) {
	var awData = new SurveysPerDayAwData();
	awData.setData(jsonData);
	return awData;
}


function MobilityPerDayAwDataCreator() {
	
}
MobilityPerDayAwDataCreatorprototype = new AwDataCreator();

MobilityPerDayAwDataCreator.prototype.createFrom = function(jsonData) {
	var awData = new MobilityPerDayAwData();
	awData.setData(jsonData);
	return awData;
}



function HoursSinceLastUpdateAwDataCreator() {
	
}
HoursSinceLastUpdateAwDataCreator.prototype = new AwDataCreator();

HoursSinceLastUpdateAwDataCreator.prototype.createFrom = function(jsonData) {
	var awData = new HoursSinceLastUpdateAwData();
	awData.setData(jsonData);
	return awData;
}



function HoursSinceLastSurveyAwDataCreator() {
	
}
HoursSinceLastSurveyAwDataCreator.prototype = new AwDataCreator();

HoursSinceLastSurveyAwDataCreator.prototype.createFrom = function(jsonData) {
	var awData = new HoursSinceLastSurveyAwData();
	awData.setData(jsonData);
	return awData;
}


function LocationUpdatesAwDataCreator() {
	
}
LocationUpdatesAwDataCreator.prototype = new AwDataCreator();

LocationUpdatesAwDataCreator.prototype.createFrom = function(jsonData) {
	var awData = new LocationUpdatesAwData();
	awData.setData(jsonData);
	return awData;
}