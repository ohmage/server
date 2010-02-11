// Add a function to Date that will return a new Date
// object incremented by x days
Date.prototype.incrementDay = function(numDays) {
    // Create a new copy of the date
    var dateCopy = new Date(this.getTime());
    // Increment the day
    dateCopy.setDate(dateCopy.getDate() + numDays);
    
    return dateCopy;
};

// Returns the month and day of the current date as a string
Date.prototype.toStringMonthAndDay = function() {
    // Map month number to three digit abbreviation of month name
    var months = new Array(12);
    months[0] = 'Jan';
    months[1] = 'Feb';
    months[2] = 'Mar';
    months[3] = 'Apr';
    months[4] = 'May';
    months[5] = 'Jun';
    months[6] = 'Jul';
    months[7] = 'Aug';
    months[8] = 'Sep';
    months[9] = 'Oct';
    months[10] = 'Nov';
    months[11] = 'Dec';
    
    // return month name plus day
    return months[this.getMonth()] + " " + this.getDate();
}

// Returns an object with the date param set to the date, and the time
// param set to the time.  Generally used for graphing with the day on 
// one axis and the time of day on the other.
Date.prototype.splitDateTime = function() {
	var datetime = new Object();
	
	datetime.time = new Date(0,0,0,this.getHours(),this.getMinutes(),this.getSeconds());
	datetime.date = new Date(this.getFullYear(), this.getMonth(), this.getDate(),0,0,0);

	return datetime;
}

// Return a date object of just the date (year/month/day)
Date.prototype.grabDate = function() {
	return new Date(this.getFullYear(), this.getMonth(), this.getDate(),0,0,0);
}
