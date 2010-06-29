/* Various functionality added to the Array type */

/* Return the sum of all values in an array */
Array.prototype.sum = function() {
	for(var i=0,sum=0;i<this.length;sum+=this[i++]);
	return sum;
}

/* Return the average of all values in an array */
Array.prototype.average = function() {
	return this.sum() / parseFloat(this.length);
}

/* Return the max of all values in an array */
Array.prototype.max = function() {
	var cur_max = this[0];
	for (var i = 0; i < this.length; i++) {
		if (this[i] > cur_max) {
			cur_max = this[i];
		}
	}
	
	return cur_max;
}

/* Return the min of all values in an array */
Array.prototype.min = function () {
	var cur_min = this[0];
	for (var i = 0; i < this.length; i++) {
		if (this[i] < cur_min) {
			cur_min = this[i];
		}
	}
	
	return cur_min;
}

/* Find the first index of the passed value in the array */
Array.prototype.find_index = function(value) {
	var ctr = "";
	for (var i=0; i < this.length; i++) {
		// use === to check for Matches. ie., identical (===), ;
		if (this[i] == value) {
			return i;
		}
	}
	return ctr;
};