/* 
 * ProtoGraph - The basic protovis graph type.  Meant as a prototype
 * for question type specific graph types.  More generally, pass in
 * the DOM ID or the object to graphize, the data to graph, and a few
 * various tweaks.
 */ 

 
// ProtoGraph constructor:
//   divId: The ID of the html element to graphize
//   title: The title to display for the graph
//   graphWidth: Total width of the graph in pixels
function ProtoGraph(divId, title, graphWidth) {
    this.divId = divId;
    this.title = title;
	
    // Default values before data exists
    this.leftXLabel = "Undef";
    this.rightXLabel = "Undef";
    this.topYLabel = "Undef";
    this.bottomylabel = "Undef";
    this.numDays = 0;
    this.data = [];
    this.hasData = false;
    this.xScale = null;
    this.yScale = null;
    this.hasAverageLine = false;
    this.hasDayDemarcations = false;
    this.width = graphWidth - ProtoGraph.LEFT_MARGIN - ProtoGraph.RIGHT_MARGIN;
	
    // Create a protovis Panel and attach to the div ID
    this.vis = new pv.Panel()
        .width(this.width)
        .height(ProtoGraph.HEIGHT)
        .left(ProtoGraph.LEFT_MARGIN)
        .bottom(ProtoGraph.BOTTOM_MARGIN)
        .top(ProtoGraph.TOP_MARGIN)
        .right(ProtoGraph.RIGHT_MARGIN)
        .canvas(this.divId);
		
    // Add a line to the bottom of the graph
    this.vis.add(pv.Rule)
        .bottom(0);
    // Add a line to the left of the graph.
    this.vis.add(pv.Rule)
        .left(0);
		
    // Add X labels to the graph, use closures to refer to
    // this.leftXLabel and this.rightXLabel so that later
    // we can simply change those values and re-render the graph
    // to change the X labels, instead of deleting and adding new
    // pv.Labels
    var that = this;
    this.vis.add(pv.Label)
        .bottom(0)
        .left(0)
        .textAlign('left')
        .textBaseline('top')
        .text(function() {
            return that.leftXLabel;
        })
        .font(ProtoGraph.LABEL_STYLE);
    this.vis.add(pv.Label)
        .bottom(0)
        .right(0)
        .textAlign('right')
        .textBaseline('top')
        .text(function() {
            return that.rightXLabel;
        })
        .font(ProtoGraph.LABEL_STYLE);
}

// ProtoGraph constants, applies to all protographs
ProtoGraph.graph_type = {
    "PROTO_GRAPH_SINGLE_TIME_TYPE":0,
    "PROTO_GRAPH_TRUE_FALSE_ARRAY_TYPE":1,
    "PROTO_GRAPH_INTEGER_TYPE":2,
    "PROTO_GRAPH_YES_NO_TYPE":3,
    "PROTO_GRAPH_MULTI_TIME_TYPE":4,
    "PROTO_GRAPH_CUSTOM_SLEEP_TYPE":5,
	"PROTO_GRAPH_ALL_INTEGER_TYPE":6,
	"PROTO_GRAPH_STACKED_BAR_TYPE":7
};

ProtoGraph.HEIGHT = 120;
//ProtoGraph.WIDTH = 600;
ProtoGraph.LEFT_MARGIN = 75;
ProtoGraph.BOTTOM_MARGIN = 20;
ProtoGraph.TOP_MARGIN = 5;
ProtoGraph.RIGHT_MARGIN = 150;
ProtoGraph.LABEL_STYLE = '10px Arial, sans-serif';
ProtoGraph.BAR_WIDTH = 4/5;
ProtoGraph.DEFAULT_COLOR = '#1f89b0';
ProtoGraph.TICK_HEIGHT = 5;

// TrueFalse constants
ProtoGraph.CATEGORY_HEIGHT = 40;
ProtoGraph.TRUE_COLOR = 'green';
ProtoGraph.FALSE_COLOR = 'red';
ProtoGraph.DISTANCE_FROM_CENTER = .25;

// Color constants for multiple responses per day
ProtoGraph.DAY_COLOR = ['#1f89b0',
                        '#00a6da',
                        '#05c5c8',
                        '#19823f',
                        '#6e943f',
                        '#a1a536',
                        '#cfb82e',
						'#1f89b0',
						'#00a6da'];

// Static logger for ProtoGraph
ProtoGraph._logger = log4javascript.getLogger();

/*
 * ProtoGraph factory to create a ProtoGraph based on JSON
 * describing the graph type.  Pass in a JSON object with
 * the defined graph description, an initialized ProtoGraph
 * subtype will be returned.
 */
ProtoGraph.factory = function(graph_description, divId, graphWidth) {
    // Switch among all the graph types
    if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_SINGLE_TIME_TYPE) {
        var newGraph = new ProtoGraphSingleTimeType(divId, graph_description.text, graphWidth);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_TRUE_FALSE_ARRAY_TYPE) {
        var newGraph = new ProtoGraphTrueFalseArrayType(divId, graph_description.text, graphWidth, graph_description.yLabels);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_INTEGER_TYPE) {
        var newGraph = new ProtoGraphIntegerType(divId, graph_description.text, graphWidth, graph_description.yLabels);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_YES_NO_TYPE) {
        var newGraph = new ProtoGraphYesNoType(divId, graph_description.text, graphWidth);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_MULTI_TIME_TYPE) {
        var newGraph = new ProtoGraphMultiTimeType(divId, graph_description.text, graphWidth);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_CUSTOM_SLEEP_TYPE) {
        var newGraph = new ProtoGraphCustomSleepType(divId, graph_description.text, graphWidth, graph_description.sleepLabels);
    }
	else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_ALL_INTEGER_TYPE) {
        var newGraph = new ProtoGraphAllIntegerType(divId, graph_description.text, graphWidth, graph_description.x_labels);
    }
	else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_STACKED_BAR_TYPE) {
        var newGraph = new ProtoGraphStackedBarType(divId, graph_description.text, graphWidth, graph_description.barLabels);
    }
    else {
        throw new TypeError("ProtoGraph.factory(): Unknown graph type in JSON.");
    }
	
    return newGraph;
}


/*
 * Functions common to all ProtoGraphs
 */ 

// Accessors
ProtoGraph.prototype.getDivId = function() {
    return this.divId;
}

// Check if the graph has no data
ProtoGraph.prototype.isEmpty = function() {
	if (this.data.length == 0) {
		return true;
	}
	
	return false;
}

// Helper function to replace X labels with day values, given
// the data types normally used to pass data to a graph
ProtoGraph.prototype.replaceXLabels = function(startDate, numDays) {
    this.leftXLabel = startDate.toStringMonthAndDay();
    this.rightXLabel = startDate.incrementDay(numDays - 1).toStringMonthAndDay();	
}

// Helper function to replace Y labels with day values
ProtoGraph.prototype.replaceYLabels = function(bottomylabel, topYLabel) {
    this.bottomylabel = bottomylabel;
    this.topYLabel = topYLabel;
}

// Add an average line to the graph
//
// Input:  average - The float height for the average array
//         yScale - pv.scale to scale the average to the graph
//         averageLabel - The average label to use
ProtoGraph.prototype.addAverageLine = function(average, yScale, averageLabel) {
    // Update the data for the average line, these will be propagated
    // to already instantiated average lines through closures
    this.average = average;
    this.averageLineLabel = averageLabel;
    this.averageLineScale = pv.Scale.linear(0, this.numDays).range(0, this.width);

    // If the average line has not yet been created, create it now
    // Else, do nothing as we have already updated the average data
    if (this.hasAverageLine == false) {
        // Add the line to the graph
        var that = this;
        this.vis.add(pv.Line)
            // Calculate an array of average values of length numDays+1
            .data(function() {
                averageLine = [];
                for (var i = 0; i < that.numDays+1; i++) {
                    averageLine.push(that.average);
                }
                return averageLine;
            })
            .bottom(function(d) {
                return yScale(d);
            })
            .left(function() {
                return that.averageLineScale(this.index);
            })
            .strokeStyle("lightgray")
            .strokeDasharray('10,5')
            .lineWidth(1);
    
        // Add an average label to the graph
        this.vis.add(pv.Label)
            .right(0)
            .bottom(function() {
                return yScale(that.average);
            })
            .textAlign('left')
            .textBaseline('middle')
            .text(function(){
                return that.averageLineLabel;
            })
            .font(ProtoGraph.LABEL_STYLE);
            
        this.hasAverageLine = true;
    } 
}

// Add day demarcations to the bottom of the graph.
//
// numTicks - The number of ticks to show.
// margin - Insert a margin into the xScale, defaults to 0
ProtoGraph.prototype.addDayDemarcations = function(numTicks, margin) {
    // Default margin to 0
    if (arguments.length == 1) {
        var margin = 0;  
    }
    
    // Need to add 2 ticks, the first and last ones.  These will not be shown
    this.tickArray = pv.range(numTicks + 2);
    this.xScaleTicks = pv.Scale.linear(this.tickArray).range(margin, this.width - margin);
        
    // Only create the pv.Rule once, just update the Rule in subsequent calls
    if (this.hasDayDemarcations == false) {
        var that = this;
        // Add ticks between the days using the day array as alignment
        that.vis.add(pv.Rule)
            .data(function(d) {
                return that.tickArray;
            })
            .left(function(d) {
                return that.xScaleTicks(d);
            })
            .bottom(0)
            // Do not show the first or last marks
            .height(function() {
                if ((this.index == 0) || (this.index == that.tickArray.length - 1)) {
                    return 0;
                }
                else {
                    return ProtoGraph.TICK_HEIGHT;
                }
            })
            .strokeStyle('black');
        
        this.hasDayDemarcations = true;
    }
}

/*
 * preprocessAddDayCounts() - Add the total number of data points per day,
 * and which this current data point is, to every data point
 */
ProtoGraph.prototype.preprocessAddDayCounts = function(data) {
    // Initialize the counting variables
    var curDay = new Date(0,0,0,0,0,0);
    var curDayCount = 1;
    var totalCountPerDay = new Object();
	
    // First pass over the data to count the number of points per day
    data.forEach(function(d) {
        // Check if this is a new day
        if (!d.date.equals(curDay)) {
            // Reset the counting vars
            curDay = d.date;
            curDayCount = 1;
        }
        else {
            curDayCount += 1;
        }
	    
        d.dayCount = curDayCount;
        // Save the current day count for the second pass
        totalCountPerDay[curDay] = curDayCount;
    });
	
    // Second pass to set total number of data points per day
    data.forEach(function(d) {
        d.totalDayCount = totalCountPerDay[d.date];
    });
}

/*
 * render() - Render any new changes to the underlying graph 
 * to the screen.
 */
ProtoGraph.prototype.render = function() {
    this.vis.render();
}


/*
 * ProtoGraphIntegerType - A subtype of the ProtoGraph class to 
 * visualize integer response data.
 */

// ProtoGraphIntegerType constructor
// divId - ID of the div element on which to create the graph
// title - The title of the graph
// yLabels - How to label the graph
function ProtoGraphIntegerType(divId, title, graphWidth, yLabels) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    // new properties
    this.yLabels = yLabels;
    this.minVal = 0;  // Integer ranges always start at 0
    this.maxVal = this.yLabels.length - 1;
    this.yScale = pv.Scale.linear(this.minVal,this.maxVal).range(0, ProtoGraph.HEIGHT);

    // The Y labels never change, add them now
    var that = this;
    this.vis.add(pv.Label)
        .data(this.yLabels)
        .left(0)
        .bottom(function() {
            return that.yScale(this.index);
        })
        .textAlign('right')
        .textBaseline('middle');
}

// Inherit methods from ProtoGraph
ProtoGraphIntegerType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in integer data.  For now
// assumes the data one integer response per day.  Draws a bar graph
// along with an average line.
ProtoGraphIntegerType.prototype.loadData = function(data, startDate, numDays) {
    // Copy the new information
    this.data = data;
    this.numDays = numDays;
	
    // Replace the x labels with possible new labels
    this.replaceXLabels(startDate, numDays);

    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.numDays; i += 1) {
        var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
    }
	
    // Setup the X scale now
    this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
	
    // Process the data as necessary
    this.preprocessAddDayCounts(this.data);
    
    // If there is no data yet, setup the display
    if (this.hasData == false) {
        // Add a bar for each response
        var that = this;
        this.vis.add(pv.Bar)
            // Make this a closure to automatically update data
            .data(function() {
				return that.data;
            })
            .width(function(d) {
                // Shrink the bar width by the total number of responses per day
                // Subtract one to be sure there is a space between bars
                return that.xScale.range().band / d.totalDayCount - 1;
            })
            .height(function(d) {
                return that.yScale(d.response) + 1;
            })
            .bottom(1)
            .left(function(d) {
                // Shift the bar left by which response per day this is
                return that.xScale(d.date) + that.xScale.range().band * ((d.dayCount - 1) / d.totalDayCount);
            })
            .fillStyle(function(d) {
                //return ProtoGraph.DAY_COLOR[d.dayCount];
                // Always use the same color for now
                return ProtoGraph.DAY_COLOR[0];
            });
			
        this.hasData = true;
    }
		  
    // Overlay the average line
    // Make an array of average values to correctly graph the line
    var average = 0;
    for (var i = 0; i < this.data.length; i++) {
        average += this.data[i].response;
    }
    average /= this.data.length;
    // Add the average line and label
    this.addAverageLine(average, this.yScale, average.toFixed(1));
	
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}




/*
 * ProtoGraphSingleTimeType - A subtype of the ProtoGraph class to 
 * visualize time based response data.
 */

// ProtoGraphSingleTimeType constructor
function ProtoGraphSingleTimeType(divId, title, graphWidth) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    // Add the Y labels now
    this.vis.add(pv.Label)
        .bottom(0)
        .left(0)
        .textAlign('right')
        .textBaseline('bottom')
        .text('00:01')
        .font(ProtoGraph.LABEL_STYLE);
        
    this.vis.add(pv.Label)
        .top(0)
        .left(0)
        .textAlign('right')
        .textBaseline('top')
        .text('23:59')
        .font(ProtoGraph.LABEL_STYLE);
		
    // Setup the Y scale
    this.yScale = pv.Scale.linear(new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59))
                       .range(0, ProtoGraph.HEIGHT);
}

// Inherit methods from ProtoGraph
ProtoGraphSingleTimeType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in time data.  For now
// assumes the data one time response per day.  Draws a scatter graph
// along with an average line.
ProtoGraphSingleTimeType.prototype.loadData = function(data, startDate, numDays) {
    // Copy the new information
    this.data = data;
    this.numDays = numDays;
    
    // Replace the x labels
    this.replaceXLabels(startDate, numDays);
    
	// Split the data into categories using Scale.ordinal
	dayArray = [];
	for (var i = 0; i < this.numDays; i += 1) {
	    var nextDay = startDate.incrementDay(i);
		dayArray.push(nextDay);
	}
	
	// Setup the X scale now
	this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
	
	// If there is no data yet setup the graph
	if (this.hasData == false) {
        // Need "that" to access "this" inside the closures
		var that = this;
		
		// Add the line plot
		this.vis.add(pv.Line)
		  .data(function() {
		    return that.data;
		  })
		  .left(function(d) {
		    // Shift the dot right by half a band to center it in the day
		    var date_position = that.xScale(d.date);
		      
            var position = that.xScale(d.date) + that.xScale.range().band / 2;
            return position;
		  })
		  .bottom(function(d){
			 return that.yScale(Date.parseDate(d.response, "g:i").grabTime());
		  })	
		  // Add dots on the line
		.add(pv.Dot).fillStyle(that.defaultColor).size(3);
		
		this.hasData = true;
	}
		
	// Average the data values for the average line
	var totalTimeInMinutes = 0;
	for (var i = 0; i < this.data.length; i++) {
		var time = Date.parseDate(this.data[i].response, "g:i").grabTime();
		totalTimeInMinutes += time.getHours() * 60;
		totalTimeInMinutes += time.getMinutes();
	}
	totalTimeInMinutes /= this.data.length;
	average = new Date(0,0,0,totalTimeInMinutes / 60, totalTimeInMinutes % 60);
	// Add the average line and label
	this.addAverageLine(average, this.yScale, average.toStringHourAndMinute());
	
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}





/*
 * ProtoGraphTrueFalseArrayType - A subtype of the ProtoGraph class to 
 * visualize arrays of true/false based response data.
 */

// ProtoGraphTrueFalseArrayType constructor
function ProtoGraphTrueFalseArrayType(divId, title, graphWidth, yLabels) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

	// An array to label the y axis with question types
	this.yLabels = yLabels;
	
	// Instead of the regular graph height, calculate height based on the 
	// number of categories
	this.height = ProtoGraph.CATEGORY_HEIGHT * this.yLabels.length;
	// Set new height in graph
	this.vis.height(this.height);

    // Create a horizontal line to separate true from false, also throw
    // labels in for good measure
	this.yScale = pv.Scale.ordinal(this.yLabels).split(0, this.height);
    this.vis.add(pv.Rule)
        .data(this.yLabels)
        .bottom(this.yScale)
        .strokeStyle('black')
        .anchor('right')
      .add(pv.Label)
        .textAlign('left')
        .textBaseline('middle')
        .font(ProtoGraph.LABEL_STYLE);
}

// Inherit methods from ProtoGraph
ProtoGraphTrueFalseArrayType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in arrays of true/false
// data.  Time is along the x axis, the questions are along the y axis.
// A box going down is false, a box going up is true.
ProtoGraphTrueFalseArrayType.prototype.loadData = function(data, startDate, numDays) {
	this.data = data;
	this.numDays = numDays;
	
	// Replace the x labels
    this.replaceXLabels(startDate, numDays);
    
	// Split the data into categories using Scale.ordinal
	var dayArray = [];
	for (var i = 0; i < this.numDays; i += 1) {
	    var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
	}
	this.dayArray = dayArray;
	this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);

	// Also create a linear scale to do day demarcations
    var range = this.xScale.range();
    var margin = range[0] / 2;
    this.tickArray = pv.range(numDays + 1);
    this.xScaleTicks = pv.Scale.linear(this.tickArray).range(margin, this.width - margin);

    // Preprocess the data to count the number of days
    this.preprocessAddDayCounts(this.data);
	
    // Pull out the response arrays for graphing
	this.transformedData = [];
	var that = this;
	this.data.forEach(function(dataPoint) {
        for(var i = 0; i < dataPoint.response.length; i++) {
			// Make a new data point for each response in the true/false array
			var newDataPoint = new Object();
			newDataPoint.date = dataPoint.date;
			// Need to remember which question this response is for
			newDataPoint.questionId = i;
			// Save true if the response is 't', false otherwise
			if (dataPoint.response[i] == 't') {
				newDataPoint.response = true;
			}
			else if (dataPoint.response[i] == 'f') {
				newDataPoint.response = false;
			}
			else {
				ProtoGraph._logger.error('ProtoGraphTrueFalseArrayType: Bad response ' + dataPoint.response[i] + ' in data for day ' + dataPoint.date);
				break;
			}
			// Save the data point count from preprocessing
			newDataPoint.dayCount = dataPoint.dayCount;
			newDataPoint.totalDayCount = dataPoint.totalDayCount;
			
            that.transformedData.push(newDataPoint);    
        }
	});

	// If we have no data yet, create the graph
	if (this.hasData == false) {
		// Create the bars.  If the response is 't', the bar will go up
		// to represent true.  If the response is 'f' the bar will go
		// down to represent false.
		
		// The height of each bar should be proportional to the overall
		// graph height and the number of questions in the graph
		var barHeight = this.height / this.yLabels.length / 2.5;
		var that = this;
		this.vis.add(pv.Bar)
		.data(function() {
            return that.transformedData;
		})
		.width(function(d) {
            // Shrink the bar width by the total number of responses per day,
		    // plus a bit more to add a space between bars
            return that.xScale.range().band / d.totalDayCount - 2;
		})
		.height(barHeight)	
		// Move bar down if a negative response
		.bottom(function(d) {
			if (d.response) {
				return that.yScale(d.questionId) + 1;
			}
			else {
				return that.yScale(d.questionId) - barHeight - 1;
			}
		})
		.left(function(d) {
            // Shift the bar left by which response per day this is
            return that.xScale(d.date) + that.xScale.range().band * ((d.dayCount - 1) / d.totalDayCount);
		})	// Color based on a negative or positive response
		.fillStyle(function(d) {
			return (d.response) ? ProtoGraph.TRUE_COLOR : ProtoGraph.FALSE_COLOR;
		});
		

		// Create day demarcations, one for each category
		this.yLabels.forEach(function(label, index) {
		    that.vis.add(pv.Rule)
                .data(function(d) {
                    return that.tickArray;
                })
                .left(function(d) {
                    // Shift left just a bit to center between days
                    return that.xScaleTicks(d);
                })
                .bottom(function() {
                    // Move down a bit to line up
                    return that.yScale(index) - ProtoGraph.TICK_HEIGHT;
                })
                // Do not show the first or last mark
                .height(function() {
                    if ((this.index == 0) || (this.index == that.tickArray.length - 1)) {
                        return 0;
                    }
                    // Since the tick goes both up AND down, double the height
                    else {
                        return ProtoGraph.TICK_HEIGHT * 2;
                    }
                })
                .strokeStyle('black');
		});
		
		this.hasData = true;
	};
}




/*
 * ProtoGraphYesNoType - A subtype of the ProtoGraph class to 
 * visualize arrays of true/false based response data.
 */

// ProtoGraphYesNoType constructor
function ProtoGraphYesNoType(divId, title, graphWidth) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    this.yScale = pv.Scale.linear(0,1).range(0, ProtoGraph.HEIGHT);
    // Create a horizontal line to separate true from false
    this.vis.add(pv.Rule)
        .data(this.yScale.range())
        .bottom(this.yScale(.5))
        .strokeStyle('black');

    // Y labels
    this.vis.add(pv.Label)
        .bottom(this.yScale((1 - ProtoGraph.DISTANCE_FROM_CENTER)))
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text('Yes')
        .font(ProtoGraph.LABEL_STYLE)
        
    this.vis.add(pv.Label)
        .bottom(this.yScale(ProtoGraph.DISTANCE_FROM_CENTER))
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text('No')
        .font(ProtoGraph.LABEL_STYLE)
}

// Inherit methods from ProtoGraph
ProtoGraphYesNoType.prototype = new ProtoGraph();

ProtoGraphYesNoType.prototype.loadData = function(data, startDate, numDays) {
    this.data = data;
	this.numDays = numDays;

    // Replace the x labels
    this.replaceXLabels(startDate, numDays);

	// Split the data into categories using Scale.ordinal
	var dayArray = [];
	for (var i = 0; i < this.numDays; i += 1) {
	    var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
	}
	this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
	
    // Preprocess the data to count the number of days
    this.preprocessAddDayCounts(this.data);
	
	// If no data yet, build the graph
	if (this.hasData == false) {
		// Save this so we can access the constants in the anonymous
		// functions below
		var that = this;

		// Create a circle for each response.  True responses are moved
		// up distanceFromCenter amount and colored trueColor.  False
		// responses are moved down and colored falseColor
		this.vis.add(pv.Dot)
		  .data(function() {
		  	return that.data;
		  })
		  .bottom(function(d) {
			// if a true response, move the dot up
			if (d.response) {
				return that.yScale(1 - ProtoGraph.DISTANCE_FROM_CENTER);
			}
			// if false move the dot down
			else {
				return that.yScale(ProtoGraph.DISTANCE_FROM_CENTER);
			}
		})
		// Shift the dot right by which response per day it is
		.left(function(d) {
		    var position = that.xScale(d.date) + that.xScale.range().band * ((d.dayCount - 1) / d.totalDayCount);
		    // Shift the dot further right to align with the "center" of the band
		    position += that.xScale.range().band / d.totalDayCount / 2;
		    return position;
		})
		.strokeStyle(function(d) {
			// If true response color true, else color false
			return d.response ? ProtoGraph.TRUE_COLOR : ProtoGraph.FALSE_COLOR;
		});
		
		this.hasData = true;
	}
		
	// Add an average line
	var average = 0;
	this.data.forEach(function(d) {
		average += d.response;
	});
	average /= this.data.length;
	
	averageYScale = pv.Scale.linear(0,1).range(ProtoGraph.HEIGHT * ProtoGraph.DISTANCE_FROM_CENTER, 
												 ProtoGraph.HEIGHT * (1 - ProtoGraph.DISTANCE_FROM_CENTER));
	this.addAverageLine(average, averageYScale, average.toFixed(2));
	
	// splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}


/*
 * ProtoGraphMultiTimeType - A subtype of the ProtoGraph class to 
 * visualize time based response data, when expecting multiple responses
 * per day.
 */

// ProtoGraphMultiTimeType constructor
function ProtoGraphMultiTimeType(divId, title, graphWidth) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    // Add the Y labels now
    this.vis.add(pv.Label)
        .bottom(0)
        .left(0)
        .textAlign('right')
        .textBaseline('bottom')
        .text('00:01')
        .font(ProtoGraph.LABEL_STYLE)
        
    this.vis.add(pv.Label)
        .top(0)
        .left(0)
        .textAlign('right')
        .textBaseline('top')
        .text('23:59')
        .font(ProtoGraph.LABEL_STYLE)
        
    // Setup the Y scale
    this.yScale = pv.Scale.linear(new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)).range(0, ProtoGraph.HEIGHT);
}

// Inherit methods from ProtoGraph
ProtoGraphMultiTimeType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in time data.  For now
// assumes the data one time response per day.  Draws a scatter graph
// along with an average line.
ProtoGraphMultiTimeType.prototype.loadData = function(data, startDate, numDays) {
    // Copy the new information
    this.data = data;
    this.numDays = numDays;
    
    // Replace the x labels
    this.replaceXLabels(startDate, numDays);
    
    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.numDays; i += 1) {
        var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
    }
    
    // Setup the X scale now
    this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
    
    // Preprocess the data to count the number of days
    this.preprocessAddDayCounts(this.data);
    
    // If there is no data yet setup the graph
    if (this.hasData == false) {
        // Need "that" to access "this" inside the closures
        var that = this;
        
        // Save the root panel to a local var for easier access, and
        // add an index variable for later reference
        this.panel = this.vis.add(pv.Panel)
            .def("i", -1);
        
        // Add the line plot
        this.panel.add(pv.Dot)
          .data(function() {
            return that.data;
          })
          .left(function(d) {
             return that.xScale(d.date) + that.xScale.range().band / 2;
          })
          .bottom(function(d) {
             return that.yScale(Date.parseDate(d.response, "g:i").grabTime());
          })
          .strokeStyle(function(d) {
              // first check if the mouse is over this dot
              if (that.panel.i() == this.index) {
                  return "black";
              }

              var metaData = d.metaData;
              var colorToReturn = ProtoGraph.DAY_COLOR[0]; 
              // If any of the meta data responses are true, make the dot red
              metaData.forEach(function(d) {
                  if (d == 't')
                      colorToReturn = "red";
              });
              return colorToReturn;
          })
          .lineWidth(2)
          .size(5)
          // Event catcher to see if the mouse is in this dot
          .event("mouseover", function() {
              return that.panel.i(this.index);
          })
          // Simple event catcher to see if the mouse leaves this dot
          .event("mouseout", function() {
              return that.panel.i(-1);
          });
        
        // Add a legend for the dot colors
        this.panel.add(pv.Dot)
            .right(-5)
            .bottom(10)
            .lineWidth(2)
            .size(5)
            .strokeStyle("red")
        // Add a label for this dot
        .anchor("right")
            .add(pv.Label)
            .text(": > 0 responses true.");
        
        this.panel.add(pv.Dot)
            .right(-5)
            .bottom(20)
            .lineWidth(2)
            .size(5)
            .strokeStyle(ProtoGraph.DAY_COLOR[0])
        // Add a label for this dot
        .anchor("right")
            .add(pv.Label)
            .text(": 0 responses true.");
        
        // Mouse over legend to display metaData
        this.panel.add(pv.Label)
            .top(10)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    var curDay = that.data[that.panel.i()].date.toStringMonthAndDay();
                    var curTime = Date.parseDate(that.data[that.panel.i()].response, "g:i").format('h:MM tt');
                    
                    return curDay + ", " + curTime;
                }
            });
        
        this.panel.add(pv.Label)
            .top(25)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    var text = "Brushed? "
                    
                    if (that.data[that.panel.i()].metaData[0] == 't')
                        text += "Yes";
                    else
                        text += "No";
                    
                    return text;
                }
            });
        
        this.panel.add(pv.Label)
            .top(35)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    var text = "Ate? "
                    
                    if (that.data[that.panel.i()].metaData[1] == 't')
                        text += "Yes";
                    else
                        text += "No";
                    
                    return text;
                }
            });
        
        this.panel.add(pv.Label)
            .top(45)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    var text = "Drank? "
                    
                    if (that.data[that.panel.i()].metaData[2] == 't')
                        text += "Yes";
                    else
                        text += "No";
                    
                    return text;
                }
            });
        
        this.hasData = true;
    }
    
    
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}

/*
 * Custom type to combine multiple sleep responses into one graph
 */
function ProtoGraphCustomSleepType(divId, title, graphWidth, sleepLabels) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    this.sleepLabels = sleepLabels;
}

// Inherit methods from ProtoGraph
ProtoGraphCustomSleepType.prototype = new ProtoGraph();

ProtoGraphCustomSleepType.prototype.loadData = function(data, startDate, numDays) {
    // Copy the new information for later usage by the graphs
    this.data = data;
    this.numDays = numDays;
    this.startDate = startDate;
 
    // Replace the x labels
    this.replaceXLabels(startDate, numDays);
 
    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.numDays; i += 1) {
        var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
    }
 
    // Setup the X scale now
    this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);

    // Create a map from X position in the X scale to the corresponding day in the data array
    this.xScaleMap = [];
    var that = this;
    dayArray.forEach(function(dataPoint) {
        var locationInData = -1;
        // Run through every day in the data to see if we find a match
        for (var i = 0; i < that.data.length; i += 1) {
            // Check if the days are the same
            if (that.data[i].date.differenceInDays(dataPoint) == 0) {
                locationInData = i;
            }
        }
        // Now set the found location (-1 if not found)
        that.xScaleMap.push(locationInData);
    });
    
    // Find the earliest in bed and latest awake point for the Y scale and labels
    var earliestTimeInBed = new Date(0,0,2,0,0,0);
    var latestTimeAwake = new Date(0,0,0,0,0,0);
 
    this.data.forEach(function(dataPoint) {
        if (dataPoint.timeInBed < earliestTimeInBed) {
            earliestTimeInBed = dataPoint.timeInBed;
        }
     
        if (dataPoint.timeAwake > latestTimeAwake) {
            latestTimeAwake = dataPoint.timeAwake;
        }
    });

    // Give an hour margin on top and bottom to make graph look nicer
    earliestTimeInBed = earliestTimeInBed.incrementHour(-1).roundDownToHour();
    latestTimeAwake = latestTimeAwake.incrementHour(1).roundUpToHour();
 
    // Change the height of the graph to match the number of hours in the scale, that is
    // find the difference in hours and multiple by the number of pixels per hour
    var newGraphHeight = earliestTimeInBed.differenceInHours(latestTimeAwake) * 20;
    this.vis.height(newGraphHeight);
    
    if (ProtoGraph._logger.isDebugEnabled()) {
        ProtoGraph._logger.debug("Setting new graph height to: " + newGraphHeight);
    }
    
    // Setup the y scale: earliest time in bed should be on the top
    this.yScale = pv.Scale.linear(earliestTimeInBed, latestTimeAwake).range(newGraphHeight, 0);
    
    // Setup a linear X scale to assist mapping the mouse position to day index
    this.xScaleLinear = pv.Scale.linear(0, this.numDays).range(0, this.width);

    
    
    // Setup the plots if there is no data yet
    if (this.hasData == false) {
        // Need "that" to access "this" inside the closures
        var that = this;  
        
        // Save the root panel to a local var for easier access, and
        // add an index variable for later reference
        this.panel = this.vis.add(pv.Panel)
            .def("i", -1);
     
        // Plot time in bed
        this.panel.add(pv.Line)
            .data(function() {
                return that.data;
            })
            .left(function(d) {
                // Shift the dot right by half a band to center it in the day
                var date_position = that.xScale(d.date);
           
                var position = that.xScale(d.date) + that.xScale.range().band / 2;
                return position;
            })
            .bottom(function(d) {
                return that.yScale(d.timeInBed);
            })    
            .strokeStyle(ProtoGraph.DEFAULT_COLOR)
            // Add dots on the line
            .add(pv.Dot)
            .fillStyle(ProtoGraph.DEFAULT_COLOR)
            .strokeStyle(ProtoGraph.DEFAULT_COLOR)
            .size(3)
            // Add a dashed line connecting time in bed to time awake
            .anchor("center")
            .add(pv.Rule)
            .height(function(d) {
                return that.yScale(d.timeInBed) -
                       that.yScale(d.timeAwake);
            })
            .strokeStyle("lightgray")
            .strokeDasharray('10,5')
            .lineWidth(1)
            // Add a line and dot connecting time awakes
            .anchor("bottom")
            .add(pv.Line)
            .add(pv.Dot)
            .fillStyle(ProtoGraph.DEFAULT_COLOR)
            .strokeStyle(ProtoGraph.DEFAULT_COLOR)
            .size(3);
     
        // Add in 2 dots and a dashed line for the mouseover
        this.panel.add(pv.Dot)
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .left(function() {
                // Shift the dot right by half a band to center it in the day
                var date_position = that.xScale(that.data[that.panel.i()].date);
                
                var position = that.xScale(that.data[that.panel.i()].date) + that.xScale.range().band / 2;
                return position;
            })
            .bottom(function() {
                return that.yScale(that.data[that.panel.i()].timeInBed);
            })
            .fillStyle("red")
            .strokeStyle("red")
            .size(3)
            // Add a dashed line connecting time in bed to time awake
            .anchor("center")
            .add(pv.Rule)
            .height(function() {
                return that.yScale(that.data[that.panel.i()].timeInBed) -
                       that.yScale(that.data[that.panel.i()].timeAwake);
            })
            .strokeStyle("black")
            .strokeDasharray('10,5')
            .lineWidth(1)
            // Add a line and dot connecting time awakes
            .anchor("bottom")
            .add(pv.Dot)
            .fillStyle("red")
            .strokeStyle("red")
            .size(3);
        
        // Add Y ticks and labels
        this.panel.add(pv.Rule)
            .data(function() {
                return that.yScale.ticks();
            })
            .top(function(d) {
                return that.panel.height() - that.yScale(d);
            })
            .left(0)
            .width(5)
            .lineWidth(1)
            .add(pv.Label)
                .textAlign("right")
                .textBaseline("middle")
                .text(function(d) {
                    return d.format('h:MM tt');
                });

     
        // Add in a legend to display the currently selected day
        this.panel.add(pv.Label)
            .top(10)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    return that.data[that.panel.i()].date.toStringMonthAndDay();
                }
            });
         
        // Display time in bed
        this.panel.add(pv.Label)
            .top(20)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    return "Time to bed: " + that.data[that.panel.i()].timeInBed.format('h:MM tt');
                }
            });
         
     
        // Display time to fall asleep
        this.panel.add(pv.Label)
            .top(30)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    return "Fell asleep in: " + that.sleepLabels[that.data[that.panel.i()].timeToFallAsleep];
                }
            });
         
        // Display time awake
        this.panel.add(pv.Label)
            .top(40)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    return "Woke up at: " + that.data[that.panel.i()].timeAwake.format('h:MM tt');
                }
            });
     
        // Display a static separation of prompt responses and calculated data
        this.panel.add(pv.Label)
            .top(55)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text("Calculated metrics:");
        
        // Display time asleep
        this.panel.add(pv.Label)
            .top(65)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    // Calculate time asleep by taking the time in bed, adding time to fall asleep,
                    // and finding time to timeAwake
                 
                    // Time to sleep in milliseconds
                    var time_to_sleep = that.data[that.panel.i()].timeToFallAsleep * 10 * 60 * 1000;
                    var millisecondsAsleep = that.data[that.panel.i()].timeAwake.getTime() -
                                              that.data[that.panel.i()].timeInBed.getTime() -
                                              time_to_sleep;
                 
                    // Calculate a time string based on this
                    var hours = Math.floor(millisecondsAsleep / 1000 / 60 / 60);
                    var minutes = Math.floor(millisecondsAsleep / 1000 / 60 - hours * 60);
                    
                    // Add a leading zero is minutes < 10 for formatting
                    if (minutes < 10) {
                        minutes = "0" + minutes;
                    }
                    
                    // Return the label text
                    return "Total time asleep: " + hours + ":" + minutes;
                }
            });
         
        
        // Update the index based on the mouse location
        this.panel.add(pv.Bar)
            .fillStyle("rgba(0,0,0,.001)")
            .event("mouseout", function() {
                return that.panel.i(-1);
            })
            .event("mousemove", function() {
                // Grab the current x position of the mouse
                var mouse_pos = that.panel.mouse().x;
                // Find the day index under the mouse pointer
                var dayIndex = Math.floor(that.xScaleLinear.invert(mouse_pos));
                // Now map to the location in the data_array
                var dataIndex = that.xScaleMap[dayIndex];
             
                // If the index has changed, update the graph
                if (that.panel.i() != dataIndex) {
                    return that.panel.i(dataIndex);
                }
            });
     
        // If we add new data, only update the data, do not recreate the
        // graph marks
        this.hasData = true;
    }
 
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}



/*
 * ProtoGraphAllIntegerType - A subtype of the ProtoGraph class to 
 * visualize integer responses wuth no defined range or labels.
 */

// ProtoGraphAllIntegerType constructor
// divId - ID of the div element on which to create the graph
// title - The title of the graph
// x_labels - Labels for the different bars on the x axis
function ProtoGraphAllIntegerType(divId, title, graphWidth, x_labels) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    // new properties
    this.minVal = 0;  // Integer ranges always start at 0
    this.maxVal = 6;
    this.yScale = pv.Scale.linear(this.minVal,this.maxVal).range(0, ProtoGraph.HEIGHT);
    this.x_labels = x_labels;
    
    // The Y labels never change, add them now
    var that = this;
    this.vis.add(pv.Label)
        .data(this.yScale.ticks())
        .left(0)
        .bottom(function(d) {
            return that.yScale(d);
        })
        .visible(function(d) {
        	return (d % 2) == 0;
        })
        .textAlign('right')
        .textBaseline('middle');
    
}

// Inherit methods from ProtoGraph
ProtoGraphAllIntegerType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in integer data.  For now
// assumes the data one integer response per day.  Draws a bar graph
// along with an average line.
ProtoGraphAllIntegerType.prototype.loadData = function(data, startDate, numDays) {
    // Copy the new information
    this.data = data;
    this.numDays = numDays;
    
    // Replace the x labels with possible new labels
    this.replaceXLabels(startDate, numDays);

    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.numDays; i += 1) {
        var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
    }
    
    // Setup the X scale now
    this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
    
    // Process the data as necessary
	// THIS SHOULD NOW BE DONE IN AwData.create_from!
    //this.preprocessAddDayCounts(this.data);
    
    // If there is no data yet, setup the display
    if (this.hasData == false) {
        // Add a bar for each response
        var that = this;
        this.vis.add(pv.Bar)
            // Make this a closure to automatically update data
            .data(function() {
                return that.data;
            })
            .width(function(d) {
                // Shrink the bar width by the total number of responses per day
                // Subtract one to be sure there is a space between bars
                return that.xScale.range().band / d.totalDayCount - 1;
            })
            .height(function(d) {
                return that.yScale(d.response) + 1;
            })
            .bottom(1)
            .left(function(d) {
                // Shift the bar left by which response per day this is
                return that.xScale(d.date) + that.xScale.range().band * ((d.dayCount) / d.totalDayCount);
            })
            .fillStyle(function(d) {
                return ProtoGraph.DAY_COLOR[d.dayCount];
                // Always use the same color for now
                //return ProtoGraph.DAY_COLOR[0];
            });
        
        // Add a legend if x_labels exist
        if (this.x_labels != null) {
        	// Use i to count what index we are at when we iterate
        	var i = 0;
        	var that = this;
        	this.x_labels.forEach(function(label) {
        		// Add a color box to show what color this is
        		that.vis.add(pv.Bar)
        			.right(-5)
        			.top(i*10)
        			.height(5)
        			.width(5)
        			.strokeStyle(ProtoGraph.DAY_COLOR[i])
        			.fillStyle(ProtoGraph.DAY_COLOR[i])
        		.anchor("right")
        			.add(pv.Label)
        			.text(": " + label)
        			.textAlign('left')
        			.textBaseline('middle');
        		
        		// Move to next label
        		i += 1;
        	});
        }
            
        this.hasData = true;
    }
    
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}


/*
 * ProtoGraphStackedBarType - Show a stack of bars, one stack per day.  The X axis is
 * days, the y axis ranges from 0 to 1.  Each bar represents a percentage of something
 * per day.  The data should an array of objects, each object should have a data key that is an
 * array of one or more data points.  The points should not add up to over 1 (they are normalized).
 * Each object's data array should have the same number of points.
 * 
 * Input:
 *   divId - The divId on which to display the graph
 *   title - The title of the graph
 *   graphWidth - The width of the protovis graph
 *   barLabels - Labels for each of the passed data points
 */

function ProtoGraphStackedBarType(divId, title, graphWidth, barLabels) {
    // Inherit properties
    ProtoGraph.call(this, divId, title, graphWidth);

    // new properties
    this.minVal = 0;
    this.maxVal = 1;
    this.yScale = pv.Scale.linear(this.minVal,this.maxVal).range(0, ProtoGraph.HEIGHT);
    this.barLabels = barLabels;
    
    // Y labels
    var that = this;
    this.vis.add(pv.Label)
        .bottom(0)
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text(function() {
        	return that.minVal;
        })
        .font(ProtoGraph.LABEL_STYLE)
        
    this.vis.add(pv.Label)
        .top(0)
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text(function() {
        	return that.maxVal;
        })
        .font(ProtoGraph.LABEL_STYLE)
}
ProtoGraphStackedBarType.prototype = new ProtoGraph();

ProtoGraphStackedBarType.prototype.loadData = function(data, startDate, numDays) {
	// Copy the new information
    this.data = data;
    this.numDays = numDays;
    
    // Replace the x labels with possible new labels
    this.replaceXLabels(startDate, numDays);

    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.numDays; i += 1) {
        var nextDay = startDate.incrementDay(i);
        dayArray.push(nextDay);
    }
    
    // Setup the X scale now
    this.xScale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
    
    // Setup the new max value.  This requires running through the data, collapsing
    // the arrays into a single array, then finding the max of that.
    var newMax = 0;
    for (var i = 0; i < this.data[0].length; i++) {
    	var testMax = 0;
    	for (var j = 0; j < this.data.length; j++) {
    		testMax += this.data[j][i].data;
    	}
    	// Now see if this is a new max
    	if (testMax > newMax) {
    		newMax = testMax;
    	}
    }

    this.maxVal = newMax;
    this.yScale = pv.Scale.linear(this.minVal,this.maxVal).range(0, ProtoGraph.HEIGHT);
    
    
    // If there is no data yet, setup the display
    if (this.hasData == false) {
    	var that = this;
    	
    	/* The stack layout. */
    	this.vis.add(pv.Layout.Stack)
    	    .layers(function() {
    	    	return that.data;
    	    })
    	    .x(function(d) {
    	    	return that.xScale(d.date);
    	    })
    	    .y(function(d) {
    	    	return that.yScale(d.data);
    	    })
    	.layer.add(pv.Bar)
    	  	.width(function(d) {
    	  		return that.xScale.range().band;
    	  	})
    	  	.fillStyle(function(d) {
                return ProtoGraph.DAY_COLOR[d.index];
            });
    	
    	// Add a legend if barLabels exist
        if (this.barLabels != null) {
        	// Use i to count what index we are at when we iterate
        	var i = 0;
        	var that = this;
        	this.barLabels.forEach(function(label) {
        		// Add a color box to show what color this is
        		that.vis.add(pv.Bar)
        			.right(-5)
        			// Reverse the legend order to match the order of bars
        			.top((that.barLabels.length - i - 1) * 10)
        			.height(5)
        			.width(5)
        			.strokeStyle(ProtoGraph.DAY_COLOR[i])
        			.fillStyle(ProtoGraph.DAY_COLOR[i])
        		.anchor("right")
        			.add(pv.Label)
        			.text(": " + label)
        			.textAlign('left')
        			.textBaseline('middle');
        		
        		// Move to next label
        		i += 1;
        	});
        }
        
    	
    	
    	this.hasData = true;
    }
    
    
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.xScale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.addDayDemarcations(numDays - 1, margin);
}