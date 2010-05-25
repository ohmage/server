/* 
 * ProtoGraph - The basic protovis graph type.  Meant as a prototype
 * for question type specific graph types.  More generally, pass in
 * the DOM ID or the object to graphize, the data to graph, and a few
 * various tweaks.
 */ 

 
// ProtoGraph constructor:
//   div_id: The ID of the html element to graphize
//   title: The title to display for the graph
//   graph_width: Total width of the graph in pixels
function ProtoGraph(div_id, title, graph_width) {
    this.div_id = div_id;
    this.title = title;
	
    // Default values before data exists
    this.left_x_label = "Undef";
    this.right_x_label = "Undef";
    this.top_y_label = "Undef";
    this.bottom_y_label = "Undef";
    this.num_days = 0;
    this.data = [];
    this.has_data = false;
    this.x_scale = null;
    this.y_scale = null;
    this.has_average_line = false;
    this.has_day_demarcations = false;
    this.width = graph_width - ProtoGraph.LEFT_MARGIN - ProtoGraph.RIGHT_MARGIN;
	
    // Create a protovis Panel and attach to the div ID
    this.vis = new pv.Panel()
        .width(this.width)
        .height(ProtoGraph.HEIGHT)
        .left(ProtoGraph.LEFT_MARGIN)
        .bottom(ProtoGraph.BOTTOM_MARGIN)
        .top(ProtoGraph.TOP_MARGIN)
        .right(ProtoGraph.RIGHT_MARGIN)
        .canvas(this.div_id);
		
    // Add a line to the bottom of the graph
    this.vis.add(pv.Rule)
        .bottom(0);
    // Add a line to the left of the graph.
    this.vis.add(pv.Rule)
        .left(0);
		
    // Add X labels to the graph, use closures to refer to
    // this.left_x_label and this.right_x_label so that later
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
            return that.left_x_label;
        })
        .font(ProtoGraph.LABEL_STYLE);
    this.vis.add(pv.Label)
        .bottom(0)
        .right(0)
        .textAlign('right')
        .textBaseline('top')
        .text(function() {
            return that.right_x_label;
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
	"PROTO_GRAPH_ALL_INTEGER_TYPE":6
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
ProtoGraph.factory = function(graph_description, div_id, graph_width) {
    // Switch among all the graph types
    if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_SINGLE_TIME_TYPE) {
        var new_graph = new ProtoGraphSingleTimeType(div_id, graph_description.text, graph_width);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_TRUE_FALSE_ARRAY_TYPE) {
        var new_graph = new ProtoGraphTrueFalseArrayType(div_id, graph_description.text, graph_width, graph_description.y_labels);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_INTEGER_TYPE) {
        var new_graph = new ProtoGraphIntegerType(div_id, graph_description.text, graph_width, graph_description.y_labels);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_YES_NO_TYPE) {
        var new_graph = new ProtoGraphYesNoType(div_id, graph_description.text, graph_width);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_MULTI_TIME_TYPE) {
        var new_graph = new ProtoGraphMultiTimeType(div_id, graph_description.text, graph_width);
    }
    else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_CUSTOM_SLEEP_TYPE) {
        var new_graph = new ProtoGraphCustomSleepType(div_id, graph_description.text, graph_width, graph_description.sleep_labels);
    }
	else if (graph_description.type == ProtoGraph.graph_type.PROTO_GRAPH_ALL_INTEGER_TYPE) {
        var new_graph = new ProtoGraphAllIntegerType(div_id, graph_description.text, graph_width);
    }
    else {
        throw new TypeError("ProtoGraph.factory(): Unknown graph type in JSON.");
    }
	
    return new_graph;
}


/*
 * Functions common to all ProtoGraphs
 */ 

// Accessors
ProtoGraph.prototype.get_div_id = function() {
    return this.div_id;
}

// Helper function to replace X labels with day values, given
// the data types normally used to pass data to a graph
ProtoGraph.prototype.replace_x_labels = function(start_date, num_days) {
    this.left_x_label = start_date.toStringMonthAndDay();
    this.right_x_label = start_date.incrementDay(num_days - 1).toStringMonthAndDay();	
}

// Helper function to replace Y labels with day values
ProtoGraph.prototype.replace_y_labels = function(bottom_y_label, top_y_label) {
    this.bottom_y_label = bottom_y_label;
    this.top_y_label = top_y_label;
}

// Add an average line to the graph
//
// Input:  average - The float height for the average array
//         y_scale - pv.scale to scale the average to the graph
//         average_label - The average label to use
ProtoGraph.prototype.add_average_line = function(average, y_scale, average_label) {
    // Update the data for the average line, these will be propagated
    // to already instantiated average lines through closures
    this.average = average;
    this.average_line_label = average_label;
    this.average_line_scale = pv.Scale.linear(0, this.num_days).range(0, this.width);

    // If the average line has not yet been created, create it now
    // Else, do nothing as we have already updated the average data
    if (this.has_average_line == false) {
        // Add the line to the graph
        var that = this;
        this.vis.add(pv.Line)
            // Calculate an array of average values of length numDays+1
            .data(function() {
                averageLine = [];
                for (var i = 0; i < that.num_days+1; i++) {
                    averageLine.push(that.average);
                }
                return averageLine;
            })
            .bottom(function(d) {
                return y_scale(d);
            })
            .left(function() {
                return that.average_line_scale(this.index);
            })
            .strokeStyle("lightgray")
            .strokeDasharray('10,5')
            .lineWidth(1);
    
        // Add an average label to the graph
        this.vis.add(pv.Label)
            .right(0)
            .bottom(function() {
                return y_scale(that.average);
            })
            .textAlign('left')
            .textBaseline('middle')
            .text(function(){
                return that.average_line_label;
            })
            .font(ProtoGraph.LABEL_STYLE);
            
        this.has_average_line = true;
    } 
}

// Add day demarcations to the bottom of the graph.
//
// num_ticks - The number of ticks to show.
// margin - Insert a margin into the x_scale, defaults to 0
ProtoGraph.prototype.add_day_demarcations = function(num_ticks, margin) {
    // Default margin to 0
    if (arguments.length == 1) {
        var margin = 0;  
    }
    
    // Need to add 2 ticks, the first and last ones.  These will not be shown
    this.tick_array = pv.range(num_ticks + 2);
    this.x_scale_ticks = pv.Scale.linear(this.tick_array).range(margin, this.width - margin);
        
    // Only create the pv.Rule once, just update the Rule in subsequent calls
    if (this.has_day_demarcations == false) {
        var that = this;
        // Add ticks between the days using the day array as alignment
        that.vis.add(pv.Rule)
            .data(function(d) {
                return that.tick_array;
            })
            .left(function(d) {
                return that.x_scale_ticks(d);
            })
            .bottom(0)
            // Do not show the first or last marks
            .height(function() {
                if ((this.index == 0) || (this.index == that.tick_array.length - 1)) {
                    return 0;
                }
                else {
                    return ProtoGraph.TICK_HEIGHT;
                }
            })
            .strokeStyle('black');
        
        this.has_day_demarcations = true;
    }
}

/*
 * preprocess_add_day_counts() - Add the total number of data points per day,
 * and which this current data point is, to every data point
 */
ProtoGraph.prototype.preprocess_add_day_counts = function(_data) {
    // Initialize the counting variables
    var cur_day = new Date(0,0,0,0,0,0);
    var cur_day_count = 1;
    var total_count_per_day = new Object();
	
    // First pass over the data to count the number of points per day
    _data.forEach(function(d) {
        // Check if this is a new day
        if (!d.date.equals(cur_day)) {
            // Reset the counting vars
            cur_day = d.date;
            cur_day_count = 1;
        }
        else {
            cur_day_count += 1;
        }
	    
        d.day_count = cur_day_count;
        // Save the current day count for the second pass
        total_count_per_day[cur_day] = cur_day_count;
    });
	
    // Second pass to set total number of data points per day
    _data.forEach(function(d) {
        d.total_day_count = total_count_per_day[d.date];
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
// div_id - ID of the div element on which to create the graph
// title - The title of the graph
// y_labels - How to label the graph
function ProtoGraphIntegerType(div_id, title, graph_width, y_labels) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

    // new properties
    this.y_labels = y_labels;
    this.min_val = 0;  // Integer ranges always start at 0
    this.max_val = this.y_labels.length - 1;
    this.y_scale = pv.Scale.linear(this.min_val,this.max_val).range(0, ProtoGraph.HEIGHT);

    // The Y labels never change, add them now
    var that = this;
    this.vis.add(pv.Label)
        .data(this.y_labels)
        .left(0)
        .bottom(function() {
            return that.y_scale(this.index);
        })
        .textAlign('right')
        .textBaseline('middle');
}

// Inherit methods from ProtoGraph
ProtoGraphIntegerType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in integer data.  For now
// assumes the data one integer response per day.  Draws a bar graph
// along with an average line.
ProtoGraphIntegerType.prototype.apply_data = function(data, start_date, num_days) {
    // Copy the new information
    this.data = data;
    this.num_days = num_days;
	
    // Replace the x labels with possible new labels
    this.replace_x_labels(start_date, num_days);

    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.num_days; i += 1) {
        var next_day = start_date.incrementDay(i);
        dayArray.push(next_day);
    }
	
    // Setup the X scale now
    this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
	
    // Process the data as necessary
    this.preprocess_add_day_counts(this.data);
    
    // If there is no data yet, setup the display
    if (this.has_data == false) {
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
                return that.x_scale.range().band / d.total_day_count - 1;
            })
            .height(function(d) {
                return that.y_scale(d.response) + 1;
            })
            .bottom(1)
            .left(function(d) {
                // Shift the bar left by which response per day this is
                return that.x_scale(d.date) + that.x_scale.range().band * ((d.day_count - 1) / d.total_day_count);
            })
            .fillStyle(function(d) {
                //return ProtoGraph.DAY_COLOR[d.day_count];
                // Always use the same color for now
                return ProtoGraph.DAY_COLOR[0];
            });
			
        this.has_data = true;
    }
		  
    // Overlay the average line
    // Make an array of average values to correctly graph the line
    var average = 0;
    for (var i = 0; i < this.data.length; i++) {
        average += this.data[i].response;
    }
    average /= this.data.length;
    // Add the average line and label
    this.add_average_line(average, this.y_scale, average.toFixed(1));
	
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.add_day_demarcations(num_days - 1, margin);
}




/*
 * ProtoGraphSingleTimeType - A subtype of the ProtoGraph class to 
 * visualize time based response data.
 */

// ProtoGraphSingleTimeType constructor
function ProtoGraphSingleTimeType(div_id, title, graph_width) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

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
    this.y_scale = pv.Scale.linear(new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59))
                       .range(0, ProtoGraph.HEIGHT);
}

// Inherit methods from ProtoGraph
ProtoGraphSingleTimeType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in time data.  For now
// assumes the data one time response per day.  Draws a scatter graph
// along with an average line.
ProtoGraphSingleTimeType.prototype.apply_data = function(data, start_date, num_days) {
    // Copy the new information
    this.data = data;
    this.num_days = num_days;
    
    // Replace the x labels
    this.replace_x_labels(start_date, num_days);
    
	// Split the data into categories using Scale.ordinal
	dayArray = [];
	for (var i = 0; i < this.num_days; i += 1) {
	    var next_day = start_date.incrementDay(i);
		dayArray.push(next_day);
	}
	
	// Setup the X scale now
	this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
	
	// If there is no data yet setup the graph
	if (this.has_data == false) {
        // Need "that" to access "this" inside the closures
		var that = this;
		
		// Add the line plot
		this.vis.add(pv.Line)
		  .data(function() {
		    return that.data;
		  })
		  .left(function(d) {
		    // Shift the dot right by half a band to center it in the day
		    var date_position = that.x_scale(d.date);
		      
            var position = that.x_scale(d.date) + that.x_scale.range().band / 2;
            return position;
		  })
		  .bottom(function(d){
			 return that.y_scale(Date.parseDate(d.response, "g:i").grabTime());
		  })	
		  // Add dots on the line
		.add(pv.Dot).fillStyle(that.defaultColor).size(3);
		
		this.has_data = true;
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
	this.add_average_line(average, this.y_scale, average.toStringHourAndMinute());
	
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.add_day_demarcations(num_days - 1, margin);
}





/*
 * ProtoGraphTrueFalseArrayType - A subtype of the ProtoGraph class to 
 * visualize arrays of true/false based response data.
 */

// ProtoGraphTrueFalseArrayType constructor
function ProtoGraphTrueFalseArrayType(div_id, title, graph_width, y_labels) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

	// An array to label the y axis with question types
	this.y_labels = y_labels;
	
	// Instead of the regular graph height, calculate height based on the 
	// number of categories
	this.height = ProtoGraph.CATEGORY_HEIGHT * this.y_labels.length;
	// Set new height in graph
	this.vis.height(this.height);

    // Create a horizontal line to separate true from false, also throw
    // labels in for good measure
	this.y_scale = pv.Scale.ordinal(this.y_labels).split(0, this.height);
    this.vis.add(pv.Rule)
        .data(this.y_labels)
        .bottom(this.y_scale)
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
ProtoGraphTrueFalseArrayType.prototype.apply_data = function(data, start_date, num_days) {
	this.data = data;
	this.num_days = num_days;
	
	// Replace the x labels
    this.replace_x_labels(start_date, num_days);
    
	// Split the data into categories using Scale.ordinal
	var dayArray = [];
	for (var i = 0; i < this.num_days; i += 1) {
	    var next_day = start_date.incrementDay(i);
        dayArray.push(next_day);
	}
	this.dayArray = dayArray;
	this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);

	// Also create a linear scale to do day demarcations
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    this.tick_array = pv.range(num_days + 1);
    this.x_scale_ticks = pv.Scale.linear(this.tick_array).range(margin, this.width - margin);

    // Preprocess the data to count the number of days
    this.preprocess_add_day_counts(this.data);
	
    // Pull out the response arrays for graphing
	this.transformed_data = [];
	var that = this;
	this.data.forEach(function(data_point) {
        for(var i = 0; i < data_point.response.length; i++) {
			// Make a new data point for each response in the true/false array
			var new_data_point = new Object();
			new_data_point.date = data_point.date;
			// Need to remember which question this response is for
			new_data_point.question_id = i;
			// Save true if the response is 't', false otherwise
			if (data_point.response[i] == 't') {
				new_data_point.response = true;
			}
			else if (data_point.response[i] == 'f') {
				new_data_point.response = false;
			}
			else {
				ProtoGraph._logger.error('ProtoGraphTrueFalseArrayType: Bad response ' + data_point.response[i] + ' in data for day ' + data_point.date);
				break;
			}
			// Save the data point count from preprocessing
			new_data_point.day_count = data_point.day_count;
			new_data_point.total_day_count = data_point.total_day_count;
			
            that.transformed_data.push(new_data_point);    
        }
	});

	// If we have no data yet, create the graph
	if (this.has_data == false) {
		// Create the bars.  If the response is 't', the bar will go up
		// to represent true.  If the response is 'f' the bar will go
		// down to represent false.
		
		// The height of each bar should be proportional to the overall
		// graph height and the number of questions in the graph
		var barHeight = this.height / this.y_labels.length / 2.5;
		var that = this;
		this.vis.add(pv.Bar)
		.data(function() {
            return that.transformed_data;
		})
		.width(function(d) {
            // Shrink the bar width by the total number of responses per day,
		    // plus a bit more to add a space between bars
            return that.x_scale.range().band / d.total_day_count - 2;
		})
		.height(barHeight)	
		// Move bar down if a negative response
		.bottom(function(d) {
			if (d.response) {
				return that.y_scale(d.question_id) + 1;
			}
			else {
				return that.y_scale(d.question_id) - barHeight - 1;
			}
		})
		.left(function(d) {
            // Shift the bar left by which response per day this is
            return that.x_scale(d.date) + that.x_scale.range().band * ((d.day_count - 1) / d.total_day_count);
		})	// Color based on a negative or positive response
		.fillStyle(function(d) {
			return (d.response) ? ProtoGraph.TRUE_COLOR : ProtoGraph.FALSE_COLOR;
		});
		

		// Create day demarcations, one for each category
		this.y_labels.forEach(function(label, index) {
		    that.vis.add(pv.Rule)
                .data(function(d) {
                    return that.tick_array;
                })
                .left(function(d) {
                    // Shift left just a bit to center between days
                    return that.x_scale_ticks(d);
                })
                .bottom(function() {
                    // Move down a bit to line up
                    return that.y_scale(index) - ProtoGraph.TICK_HEIGHT;
                })
                // Do not show the first or last mark
                .height(function() {
                    if ((this.index == 0) || (this.index == that.tick_array.length - 1)) {
                        return 0;
                    }
                    // Since the tick goes both up AND down, double the height
                    else {
                        return ProtoGraph.TICK_HEIGHT * 2;
                    }
                })
                .strokeStyle('black');
		});
		
		this.has_data = true;
	};
}




/*
 * ProtoGraphYesNoType - A subtype of the ProtoGraph class to 
 * visualize arrays of true/false based response data.
 */

// ProtoGraphYesNoType constructor
function ProtoGraphYesNoType(div_id, title, graph_width) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

    this.y_scale = pv.Scale.linear(0,1).range(0, ProtoGraph.HEIGHT);
    // Create a horizontal line to separate true from false
    this.vis.add(pv.Rule)
        .data(this.y_scale.range())
        .bottom(this.y_scale(.5))
        .strokeStyle('black');

    // Y labels
    this.vis.add(pv.Label)
        .bottom(this.y_scale((1 - ProtoGraph.DISTANCE_FROM_CENTER)))
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text('Yes')
        .font(ProtoGraph.LABEL_STYLE)
        
    this.vis.add(pv.Label)
        .bottom(this.y_scale(ProtoGraph.DISTANCE_FROM_CENTER))
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text('No')
        .font(ProtoGraph.LABEL_STYLE)
}

// Inherit methods from ProtoGraph
ProtoGraphYesNoType.prototype = new ProtoGraph();

ProtoGraphYesNoType.prototype.apply_data = function(data, start_date, num_days) {
    this.data = data;
	this.num_days = num_days;

    // Replace the x labels
    this.replace_x_labels(start_date, num_days);

	// Split the data into categories using Scale.ordinal
	var dayArray = [];
	for (var i = 0; i < this.num_days; i += 1) {
	    var next_day = start_date.incrementDay(i);
        dayArray.push(next_day);
	}
	this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
	
    // Preprocess the data to count the number of days
    this.preprocess_add_day_counts(this.data);
	
	// If no data yet, build the graph
	if (this.has_data == false) {
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
				return that.y_scale(1 - ProtoGraph.DISTANCE_FROM_CENTER);
			}
			// if false move the dot down
			else {
				return that.y_scale(ProtoGraph.DISTANCE_FROM_CENTER);
			}
		})
		// Shift the dot right by which response per day it is
		.left(function(d) {
		    var position = that.x_scale(d.date) + that.x_scale.range().band * ((d.day_count - 1) / d.total_day_count);
		    // Shift the dot further right to align with the "center" of the band
		    position += that.x_scale.range().band / d.total_day_count / 2;
		    return position;
		})
		.strokeStyle(function(d) {
			// If true response color true, else color false
			return d.response ? ProtoGraph.TRUE_COLOR : ProtoGraph.FALSE_COLOR;
		});
		
		this.has_data = true;
	}
		
	// Add an average line
	var average = 0;
	this.data.forEach(function(d) {
		average += d.response;
	});
	average /= this.data.length;
	
	average_y_scale = pv.Scale.linear(0,1).range(ProtoGraph.HEIGHT * ProtoGraph.DISTANCE_FROM_CENTER, 
												 ProtoGraph.HEIGHT * (1 - ProtoGraph.DISTANCE_FROM_CENTER));
	this.add_average_line(average, average_y_scale, average.toFixed(2));
	
	// splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.add_day_demarcations(num_days - 1, margin);
}


/*
 * ProtoGraphMultiTimeType - A subtype of the ProtoGraph class to 
 * visualize time based response data, when expecting multiple responses
 * per day.
 */

// ProtoGraphMultiTimeType constructor
function ProtoGraphMultiTimeType(div_id, title, graph_width) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

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
    this.y_scale = pv.Scale.linear(new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)).range(0, ProtoGraph.HEIGHT);
}

// Inherit methods from ProtoGraph
ProtoGraphMultiTimeType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in time data.  For now
// assumes the data one time response per day.  Draws a scatter graph
// along with an average line.
ProtoGraphMultiTimeType.prototype.apply_data = function(data, start_date, num_days) {
    // Copy the new information
    this.data = data;
    this.num_days = num_days;
    
    // Replace the x labels
    this.replace_x_labels(start_date, num_days);
    
    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.num_days; i += 1) {
        var next_day = start_date.incrementDay(i);
        dayArray.push(next_day);
    }
    
    // Setup the X scale now
    this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
    
    // Preprocess the data to count the number of days
    this.preprocess_add_day_counts(this.data);
    
    // If there is no data yet setup the graph
    if (this.has_data == false) {
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
             return that.x_scale(d.date) + that.x_scale.range().band / 2;
          })
          .bottom(function(d) {
             return that.y_scale(Date.parseDate(d.response, "g:i").grabTime());
          })
          .strokeStyle(function(d) {
              // first check if the mouse is over this dot
              if (that.panel.i() == this.index) {
                  return "black";
              }

              var meta_data = d.meta_data;
              var color_to_return = ProtoGraph.DAY_COLOR[0]; 
              // If any of the meta data responses are true, make the dot red
              meta_data.forEach(function(d) {
                  if (d == 't')
                      color_to_return = "red";
              });
              return color_to_return;
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
        
        // Mouse over legend to display meta_data
        this.panel.add(pv.Label)
            .top(10)
            .right(0)
            .textBaseline("bottom")
            .visible(function() {
                return that.panel.i() >= 0;
            })
            .text(function() {
                if (that.panel.i() >= 0) {
                    var cur_day = that.data[that.panel.i()].date.toStringMonthAndDay();
                    var cur_time = Date.parseDate(that.data[that.panel.i()].response, "g:i").format('h:MM tt');
                    
                    return cur_day + ", " + cur_time;
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
                    
                    if (that.data[that.panel.i()].meta_data[0] == 't')
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
                    
                    if (that.data[that.panel.i()].meta_data[1] == 't')
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
                    
                    if (that.data[that.panel.i()].meta_data[2] == 't')
                        text += "Yes";
                    else
                        text += "No";
                    
                    return text;
                }
            });
        
        this.has_data = true;
    }
    
    
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.add_day_demarcations(num_days - 1, margin);
}

/*
 * Custom type to combine multiple sleep responses into one graph
 */
function ProtoGraphCustomSleepType(div_id, title, graph_width, sleep_labels) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

    this.sleep_labels = sleep_labels;
}

// Inherit methods from ProtoGraph
ProtoGraphCustomSleepType.prototype = new ProtoGraph();

ProtoGraphCustomSleepType.prototype.apply_data = function(data, start_date, num_days) {
    // Copy the new information for later usage by the graphs
    this.data = data;
    this.num_days = num_days;
    this.start_date = start_date;
 
    // Replace the x labels
    this.replace_x_labels(start_date, num_days);
 
    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.num_days; i += 1) {
        var next_day = start_date.incrementDay(i);
        dayArray.push(next_day);
    }
 
    // Setup the X scale now
    this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);

    // Create a map from X position in the X scale to the corresponding day in the data array
    this.x_scale_map = [];
    var that = this;
    dayArray.forEach(function(data_point) {
        var location_in_data = -1;
        // Run through every day in the data to see if we find a match
        for (var i = 0; i < that.data.length; i += 1) {
            // Check if the days are the same
            if (that.data[i].date.difference_in_days(data_point) == 0) {
                location_in_data = i;
            }
        }
        // Now set the found location (-1 if not found)
        that.x_scale_map.push(location_in_data);
    });
    
    // Find the earliest in bed and latest awake point for the Y scale and labels
    var earliest_time_in_bed = new Date(0,0,2,0,0,0);
    var latest_time_awake = new Date(0,0,0,0,0,0);
 
    this.data.forEach(function(data_point) {
        if (data_point.time_in_bed < earliest_time_in_bed) {
            earliest_time_in_bed = data_point.time_in_bed;
        }
     
        if (data_point.time_awake > latest_time_awake) {
            latest_time_awake = data_point.time_awake;
        }
    });

    // Give an hour margin on top and bottom to make graph look nicer
    earliest_time_in_bed = earliest_time_in_bed.incrementHour(-1).roundDownToHour();
    latest_time_awake = latest_time_awake.incrementHour(1).roundUpToHour();
 
    // Change the height of the graph to match the number of hours in the scale, that is
    // find the difference in hours and multiple by the number of pixels per hour
    var new_graph_height = earliest_time_in_bed.difference_in_hours(latest_time_awake) * 20;
    this.vis.height(new_graph_height);
    
    if (ProtoGraph._logger.isDebugEnabled()) {
        ProtoGraph._logger.debug("Setting new graph height to: " + new_graph_height);
    }
    
    // Setup the y scale: earliest time in bed should be on the top
    this.y_scale = pv.Scale.linear(earliest_time_in_bed, latest_time_awake).range(new_graph_height, 0);
    
    // Setup a linear X scale to assist mapping the mouse position to day index
    this.x_scale_linear = pv.Scale.linear(0, this.num_days).range(0, this.width);

    
    
    // Setup the plots if there is no data yet
    if (this.has_data == false) {
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
                var date_position = that.x_scale(d.date);
           
                var position = that.x_scale(d.date) + that.x_scale.range().band / 2;
                return position;
            })
            .bottom(function(d) {
                return that.y_scale(d.time_in_bed);
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
                return that.y_scale(d.time_in_bed) -
                       that.y_scale(d.time_awake);
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
                var date_position = that.x_scale(that.data[that.panel.i()].date);
                
                var position = that.x_scale(that.data[that.panel.i()].date) + that.x_scale.range().band / 2;
                return position;
            })
            .bottom(function() {
                return that.y_scale(that.data[that.panel.i()].time_in_bed);
            })
            .fillStyle("red")
            .strokeStyle("red")
            .size(3)
            // Add a dashed line connecting time in bed to time awake
            .anchor("center")
            .add(pv.Rule)
            .height(function() {
                return that.y_scale(that.data[that.panel.i()].time_in_bed) -
                       that.y_scale(that.data[that.panel.i()].time_awake);
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
                return that.y_scale.ticks();
            })
            .top(function(d) {
                return that.panel.height() - that.y_scale(d);
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
                    return "Time to bed: " + that.data[that.panel.i()].time_in_bed.format('h:MM tt');
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
                    return "Fell asleep in: " + that.sleep_labels[that.data[that.panel.i()].time_to_fall_asleep];
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
                    return "Woke up at: " + that.data[that.panel.i()].time_awake.format('h:MM tt');
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
                    // and finding time to time_awake
                 
                    // Time to sleep in milliseconds
                    var time_to_sleep = that.data[that.panel.i()].time_to_fall_asleep * 10 * 60 * 1000;
                    var milliseconds_asleep = that.data[that.panel.i()].time_awake.getTime() -
                                              that.data[that.panel.i()].time_in_bed.getTime() -
                                              time_to_sleep;
                 
                    // Calculate a time string based on this
                    var hours = Math.floor(milliseconds_asleep / 1000 / 60 / 60);
                    var minutes = Math.floor(milliseconds_asleep / 1000 / 60 - hours * 60);
                    
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
                var day_index = Math.floor(that.x_scale_linear.invert(mouse_pos));
                // Now map to the location in the data_array
                var data_index = that.x_scale_map[day_index];
             
                // If the index has changed, update the graph
                if (that.panel.i() != data_index) {
                    return that.panel.i(data_index);
                }
            });
     
        // If we add new data, only update the data, do not recreate the
        // graph marks
        this.has_data = true;
    }
 
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.add_day_demarcations(num_days - 1, margin);
}



/*
 * ProtoGraphAllIntegerType - A subtype of the ProtoGraph class to 
 * visualize integer responses wuth no defined range or labels.
 */

// ProtoGraphAllIntegerType constructor
// div_id - ID of the div element on which to create the graph
// title - The title of the graph
function ProtoGraphAllIntegerType(div_id, title, graph_width) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title, graph_width);

    // new properties
    this.min_val = 0;  // Integer ranges always start at 0
    this.max_val = 6;
    this.y_scale = pv.Scale.linear(this.min_val,this.max_val).range(0, ProtoGraph.HEIGHT);
    
    // The Y labels never change, add them now
    var that = this;
    this.vis.add(pv.Label)
        .data(this.y_scale.ticks())
        .left(0)
        .bottom(function(d) {
            return that.y_scale(d);
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
ProtoGraphAllIntegerType.prototype.apply_data = function(data, start_date, num_days) {
    // Copy the new information
    this.data = data;
    this.num_days = num_days;
    
    // Replace the x labels with possible new labels
    this.replace_x_labels(start_date, num_days);

    // Split the data into categories using Scale.ordinal
    var dayArray = [];
    for (var i = 0; i < this.num_days; i += 1) {
        var next_day = start_date.incrementDay(i);
        dayArray.push(next_day);
    }
    
    // Setup the X scale now
    this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, ProtoGraph.BAR_WIDTH);
    
    // Process the data as necessary
	// THIS SHOULD NOW BE DONE IN AwData.create_from!
    //this.preprocess_add_day_counts(this.data);
    
    // If there is no data yet, setup the display
    if (this.has_data == false) {
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
                return that.x_scale.range().band / d.total_day_count - 1;
            })
            .height(function(d) {
                return that.y_scale(d.response) + 1;
            })
            .bottom(1)
            .left(function(d) {
                // Shift the bar left by which response per day this is
                return that.x_scale(d.date) + that.x_scale.range().band * ((d.day_count) / d.total_day_count);
            })
            .fillStyle(function(d) {
                return ProtoGraph.DAY_COLOR[d.day_count];
                // Always use the same color for now
                //return ProtoGraph.DAY_COLOR[0];
            });
            
        this.has_data = true;
    }
    
    // splitBanded adds a margin in to the scale.  Find the margin
    // from the range
    var range = this.x_scale.range();
    var margin = range[0] / 2;
    // Only add ticks between days, so subtract one
    this.add_day_demarcations(num_days - 1, margin);
}