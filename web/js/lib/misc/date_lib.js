// Add a function to Date that will return a new Date
// object incremented by x days

// Set one day in milliseconds
Date.prototype.one_day = 1000*60*60*24;

Date.prototype.incrementDay = function(numDays) {
	return new Date(this.getTime() + this.one_day * numDays);
};

// Find the difference in days between this date and the
// passed in date
Date.prototype.difference_in_days = function(second_day) {
	return Math.ceil((second_day.getTime()-this.getTime())/(this.one_day));
}

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

// Return a date object of just the date (year/month/day)
Date.prototype.grabDate = function() {
	return new Date(this.getFullYear(), this.getMonth(), this.getDate(),0,0,0);
}

Date.prototype.grabTime = function() {
	return new Date(0,0,0,this.getHours(),this.getMinutes(),this.getSeconds());
}


