/* 
 * ProtoGraph - The basic protovis graph type.  Meant as a prototype
 * for question type specific graph types.  More generally, pass in
 * the DOM ID or the object to graphize, the data to graph, and a few
 * various tweaks.
 */ 
 
// Libraries that add functionality to the Array type
//LoadScript('lib/misc/array_lib.js');
// Added Date functionality
//LoadScriptJQuery('lib/misc/date_lib.js');
 
// ProtoGraph constructor:
//   div_id: The ID of the html element to graphize
//   title: The title to display for the graph
function ProtoGraph(div_id, title) {
	this.div_id = div_id;
	this.title = title;
	
	// Default values before data exists
	this.left_x_label = "Undef";
	this.right_x_label = "Undef";
	this.num_days = 0;
	this.data = [];
	this.has_data = false;
	this.x_scale = null;
	this.y_scale = null;
	this.has_average_line = false;

	/*
	 * Do basic graph panel setup, common to any type of graph
	 */
	
	// Build the div structure for the graph at the passed div_id
    this.build_div_structure();
	
	// Create a protovis Panel and attach to the div ID
	this.vis = new pv.Panel()
		.width(this.width)
		.height(this.height)
		.left(this.leftMargin)
		.bottom(this.bottomMargin)
		.top(this.topMargin)
		.right(this.rightMargin)
		.canvas(this.div_id);
		
	// Add a line to the bottom of the graph
	this.vis.add(pv.Rule)
		.bottom(0);
	// Add a line to the left of the graph.
	this.vis.add(pv.Rule)
		.left(0);
		
	// Add X labels to the graph, use closures to refer to
	// this.left_x_label and this.right_x_label so that later
	// we can simply change those values and rerender the graph
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
        .font(this.labelStyle);
    this.vis.add(pv.Label)
        .bottom(0)
        .right(0)
        .textAlign('right')
        .textBaseline('top')
        .text(function() {
			return that.right_x_label;
		})
        .font(this.labelStyle);
}

// ProtoGraph constants, applies to all protographs
ProtoGraph.graph_type = {
    "PROTO_GRAPH_TIME_ONCE_TYPE":0,
    "PROTO_GRAPH_TRUE_FALSE_ARRAY_TYPE":1,
    "PROTO_GRAPH_INTEGER_TYPE":2,
    "PROTO_GRAPH_YES_NO_TYPE":3,
	"PROTO_GRAPH_TIME_MULTI_TYPE":4,
}

ProtoGraph.prototype.height = 120;
ProtoGraph.prototype.width = 600;
ProtoGraph.prototype.leftMargin = 70;
ProtoGraph.prototype.bottomMargin = 20;
ProtoGraph.prototype.topMargin = 5;
ProtoGraph.prototype.rightMargin = 250;
ProtoGraph.prototype.labelStyle = '13px sans-serif bolder';
ProtoGraph.prototype.barWidth = 4/5;
ProtoGraph.prototype.defaultColor = '#1f77b4';

// TrueFalse constants
ProtoGraph.prototype.categoryHeight = 40;
ProtoGraph.prototype.trueColor = 'green';
ProtoGraph.prototype.falseColor = 'red';
ProtoGraph.prototype.distanceFromCenter = .25;

/*
 * ProtoGraph factory to create a ProtoGraph based on JSON
 * describing the graph type.  Pass in a JSON object with
 * the defined graph description, an initialized ProtoGraph
 * subtype will be returned.
 */
ProtoGraph.factory = function(graph_description, div_id) {
	// Swith among all the graph types, don't know if this is the best
	// method to do this but here goes anyway
	
	if (graph_description.type == 0) {
		// Pass in the div_id and the title
		var new_graph = new ProtoGraphTimeType(div_id, graph_description.text);
	}
	if (graph_description.type == 1) {
        // Pass in the div_id and the title
        var new_graph = new ProtoGraphTrueFalseArrayType(div_id, graph_description.text, graph_description.y_labels);
    }
	if (graph_description.type == 2) {
        // Pass in the div_id and the title
        var new_graph = new ProtoGraphIntegerType(div_id, graph_description.text, graph_description.y_labels);
    }
	if (graph_description.type == 3) {
        // Pass in the div_id and the title
        var new_graph = new ProtoGraphYesNoType(div_id, graph_description.text);
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
    this.right_x_label = start_date.incrementDay(num_days).toStringMonthAndDay();	
}


// build_div_structure - Builds a div structure through jQuery on
// the root HTML page at the passed div ID
ProtoGraph.prototype.build_div_structure = function() {
	$('#' + this.div_id).replaceWith("<h3>" + this.title + "</h3>\n\
            <span id=\"" + this.div_id + "\">Loading...</span>");
}

// Add an average line to the graph
//
// Input:  average - The float height for the average array
//         y_scale - pv.scale to scale the average to the graph
//         average_label - The average label to use
ProtoGraph.prototype.add_average_line = function(average, y_scale, average_label) {
    // Update the data for the average line, these will be propogated
	// to already instantiated average lines through closures
	this.average = average;
    this.average_line_label = average_label;
    this.average_line_scale = pv.Scale.linear(0,this.num_days).range(0, this.width);

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
	        .font(this.labelStyle)
			
		this.has_average_line = true;
	} 
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
function ProtoGraphIntegerType(div_id, title, y_labels) {
	// Inherit properties
	ProtoGraph.call(this, div_id, title);

	// new properties
	this.y_labels = y_labels;
	this.min_val = 0;  // Integer ranges always start at 0
	this.max_val = this.y_labels.length - 1;
	this.y_scale = pv.Scale.linear(this.min_val,this.max_val).range(0, this.height);

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
		dayArray.push(start_date.incrementDay(i));
	}
	// Setup the X scale now
    this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, this.barWidth);
	
	// If there is no data yet, setup the display
	if (this.has_data == false) {
	    // Add a bar for each response
        var that = this;
	    this.vis.add(pv.Bar)
		    // Make this a closure to automatically update data
	        .data(function() {
				return that.data;
			})
	        .width(function(){
				return that.x_scale.range().band;
			})
	        .height(function(d) {
	            return that.y_scale(d.response) + 1;
	        })
	        .bottom(1)
	        .left(function(d) {
	            return that.x_scale(d.date);
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
}




/*
 * ProtoGraphTimeType - A subtype of the ProtoGraph class to 
 * visualize time based response data.
 */

// ProtoGraphTimeType constructor
function ProtoGraphTimeType(div_id, title, data, start_date, num_days) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title);

    // Add the Y labels now
	this.vis.add(pv.Label)
        .bottom(0)
        .left(0)
        .textAlign('right')
        .textBaseline('bottom')
        .text('00:01')
        .font(this.labelStyle)
        
    this.vis.add(pv.Label)
        .top(0)
        .left(0)
        .textAlign('right')
        .textBaseline('top')
        .text('23:59')
        .font(this.labelStyle)
		
	// Setup the Y scale
	this.y_scale = pv.Scale.linear(new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)).range(0, this.height);
}

// Inherit methods from ProtoGraph
ProtoGraphTimeType.prototype = new ProtoGraph();

// Draws a sparkline graph using the passed in time data.  For now
// assumes the data one time response per day.  Draws a scatter graph
// along with an average line.
ProtoGraphTimeType.prototype.apply_data = function(data, start_date, num_days) {
    // Copy the new information
    this.data = data;
    this.num_days = num_days;
    
    // Replace the x labels
    this.replace_x_labels(start_date, num_days);
    
	// Split the data into categories using Scale.ordinal
	var dayArray = [];
	for (var i = 0; i < this.num_days; i += 1) {
		dayArray.push(start_date.incrementDay(i));
	}
	
	// Setup the X scale now
	this.x_scale = pv.Scale.ordinal(dayArray).split(0, this.width);
	
	// If there is no data yet setup the graph
	if (this.has_data == false) {
        // Need "that" to access "this" inside the closures
		var that = this;
		
		// Add the line plot
		this.vis.add(pv.Line)
		  .data(function() {
		  	 // Separate the data by time and date
		    datetime = [];
		    that.data.forEach(function(d) {
				var dt = new Object();
				dt.date = d.date;
                dt.time = Date.parseDate(d.response, "g:i").grabTime();
				
		        datetime.push(dt);
		    });
			
			return datetime;
		  })
		  .left(function(d) {
			 return that.x_scale(d.date);
		  })
		  .bottom(function(d){
			 return that.y_scale(d.time);
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
	this.add_average_line(average, this.y_scale, average.getHours() + ':' + average.getMinutes());
}





/*
 * ProtoGraphTrueFalseArrayType - A subtype of the ProtoGraph class to 
 * visualize arrays of true/false based response data.
 */

// ProtoGraphTrueFalseArrayType constructor
function ProtoGraphTrueFalseArrayType(div_id, title, y_labels) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title);

	// An array to label the y axis with question types
	this.y_labels = y_labels;
	
	// Instead of the regular graph height, calculate height based on the 
	// number of categories
	this.height = this.categoryHeight * this.y_labels.length;
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
        .font(this.labelStyle);
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
		dayArray.push(start_date.incrementDay(i));
	}
	this.x_scale = pv.Scale.ordinal(dayArray).splitBanded(0, this.width, this.barWidth);

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
			// Separate the data into a 2d array, with each row of the array being
		    // the day label, the question label, and whether the response is true or false
		    transformed_data = [];
		    that.data.forEach(function(data_point) {
		        for(var i = 0; i < data_point.response.length; i++) {
		            transformed_data.push([data_point.date, i, data_point.response[i]]);    
		        }
		    });
			return transformed_data;
		})
		.width(function() {
			return that.x_scale.range().band;
		})
		.height(barHeight)	
		// Move bar down if a negative response
		.bottom(function(d) {
			if (d[2] == 't') {
				return that.y_scale(d[1]) + 1;
			}
			else if (d[2] == 'f') {
				return that.y_scale(d[1]) - barHeight - 1;
			}
			else {
				throw new Error('ProtoGraphTrueFalseArrayType: Bad resposne in data.');
			}
		})
		  .left(function(d) {
			return that.x_scale(d[0]);
		})	// Color based on a negative or positive response
		.fillStyle(function(d) {
			return (d[2] == 't') ? that.trueColor : that.falseColor;
		});
		
		this.has_data = true;
	};
}




/*
 * ProtoGraphYesNoType - A subtype of the ProtoGraph class to 
 * visualize arrays of true/false based response data.
 */

// ProtoGraphYesNoType constructor
function ProtoGraphYesNoType(div_id, title) {
    // Inherit properties
    ProtoGraph.call(this, div_id, title);

    this.y_scale = pv.Scale.linear(0,1).range(0, this.height);
    // Create a horizontal line to separate true from false
    this.vis.add(pv.Rule)
        .data(this.y_scale.range())
        .bottom(this.y_scale(.5))
        .strokeStyle('black');

    // Y labels
    this.vis.add(pv.Label)
        .bottom(this.y_scale((1 - this.distanceFromCenter)))
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text('Yes')
        .font(this.labelStyle)
        
    this.vis.add(pv.Label)
        .bottom(this.y_scale(this.distanceFromCenter))
        .left(0)
        .textAlign('right')
        .textBaseline('middle')
        .text('No')
        .font(this.labelStyle)
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
		dayArray.push(start_date.incrementDay(i));
	}
	this.x_scale = pv.Scale.ordinal(dayArray).split(0, this.width);
	
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
				return that.y_scale(1 - that.distanceFromCenter);
			}
			// if false move the dot down
			else {
				return that.y_scale(that.distanceFromCenter);
			}
		})
		.left(function(d) {
			return that.x_scale(d.date);
		})
		.strokeStyle(function(d) {
			// If true response color true, else color false
			return d.response ? that.trueColor : that.falseColor;
		});
		
		this.has_data = true;
	}
		
	// Add an average line
	var average = 0;
	this.data.forEach(function(d) {
		average += d.response;
	});
	average /= this.data.length;
	
	average_y_scale = pv.Scale.linear(0,1).range(this.height * this.distanceFromCenter, 
												 this.height * (1 - this.distanceFromCenter));
	this.add_average_line(average, average_y_scale, average.toFixed(2));
}