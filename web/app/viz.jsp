<%@ page contentType="text/html; charset=UTF-8" %>
<%@page import="edu.ucla.cens.awserver.domain.User"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-cache" );
   response.setDateHeader( "Expires", 0 );
%>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	
	<!-- Force IE8 into IE7 mode for VML compatibility -->
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
	
    <title>Data Visualizations</title>
    
    <link href="/css/zp-compressed.css" type="text/css" media="screen, print" rel="stylesheet" />
	<link href="/css/zp-print.css" type="text/css" media="print" rel="stylesheet" />
	<link href="http://andwellness.cens.ucla.edu/favicon.ico" rel="shortcut icon" type="image/x-icon">
	<link type="text/css" href="/css/jquery-ui-1.7.2.custom.css" rel="stylesheet" />
	<link type="text/css" href="/css/jquery-validity.css" rel="stylesheet" />
	<link type="text/css" href="/css/tabs.css" rel="stylesheet" />
    

	<!-- A large number of javascript includes, will reduce -->
	<!-- Main jQuery library -->
	<!-- 
	   <script type="text/javascript" src="/js/lib/jquery/jquery-1.3.2.min.js"></script>
	-->
	<!-- jQuery UI toolkit, includes jQuery -->
	<script src="http://cdn.jquerytools.org/1.1.2/jquery.tools.min.js"></script>
	<!-- jQuery UI with Datepicker -->
	<script type="text/javascript" src="/js/lib/jquery/jquery-ui-1.7.2.custom.min.js"></script>
	<!-- Support logging -->
	<script type="text/javascript" src="/js/lib/misc/log4javascript.js"></script>
    <!-- Protovis graphing library with hacked in IE support -->
	<script type="text/javascript" src="/js/lib/Protovis/protovis-d3.1-ie.js"></script>
	<!-- Useful additions to Javascript objects -->
	<script type="text/javascript" src="/js/lib/misc/array_lib.js"></script>
	<script type="text/javascript" src="/js/lib/misc/date-functions.js"></script>
	<!-- Various validators to validate user input and server responses -->
	<script type="text/javascript" src="/js/lib/jquery/jquery.validity.min.js"></script>
	<script type="text/javascript" src="/js/lib/validator/DateValidator.js"></script>
	<!-- Generates the different graph types using Protovis -->
	<script type="text/javascript" src="/js/lib/DataSource/DataSource.js"></script>
	<script type="text/javascript" src="/js/lib/Graph/ProtoGraph.js"></script>
	<!-- Contains the query response visualization types -->
	<script type="text/javascript" src="/js/response_list.js"></script>
    
	
    <!--[if IE]>
	<link href="/css/zp-ie.css" type="text/css" media="screen" rel="stylesheet" />
	<![endif]-->
    
    <style type="text/css">
		
		.content .padding {
			padding: 50px 50px 50px 50px;
		}
		.footer {
		   color: #BEBEBE;
		}
		
		/* tab pane styling */
		div.panes div {
		    display:none;       
		    padding:15px 10px;
		    border:1px solid #999;
		    border-top:0;
		    width:830px;
		    font-size:14px;
		    background-color:#fff;
		}
		
		ul.tabs {
		    width:852px;
		}
		
		body {
		    padding:0px 10px;
		}

		
		
	</style>
    
	
	
	<script type="text/javascript">
	
	// Holds the currently requested start date and end date
	var startDate = new Date();
	var endDate = new Date();

	// Stores all instantiated graphs on the webpage, leave here for now
	var graphList = [];	
	
	// Handles retrieval and filtering of data
	//var dataSource = new DataSourceJson('/app/viz');
		
	// Main logger
	var log = log4javascript.getLogger();
	
	// Called when document is done loading
    $(function() {
		// Setup logging
        var popUpAppender = new log4javascript.PopUpAppender();
        popUpAppender.setThreshold(log4javascript.Level.DEBUG);
		var popUpLayout = new log4javascript.PatternLayout("%d{HH:mm:ss} %-5p - %m%n");
        popUpAppender.setLayout(popUpLayout);
        log.addAppender(popUpAppender);

        // Uncomment the line below to disable logging
        //log4javascript.setEnabled(false);

		// Setup the datepickers for the two date input forms
		$("#startDate").datepicker({dateFormat: 'yy-mm-dd'});
		$("#endDate").datepicker({dateFormat: 'yy-mm-dd'});

	    // Override the default submit function for the form
	    $("#grabDateForm").submit(send_json_request);

	    // Loop over the questions in the response list, setup the HTML for the graphs
        response_list.forEach(handleResponse);

        // setup ul.tabs to work as tabs for each div directly under div.panes 
        $("ul.tabs").tabs("div.panes > div").hide();
        $("div.panes").hide();
	});

	/*
	 * Uses the validity library to validate the date form inputs on this page.
	 * Call this from the submit override before sending a request to the server.
	 */
    function validateDateFormInputs() {
    	// Start validation
        $.validity.start();

        // Validate the startDate field
        $("#startDate")
            .require("A starting date is required")
            .assert(DateValidator.validate, "Date is invalid.");

        // Validate the endDate field
        $("#endDate")
            .require("An ending date is required")
            .assert(DateValidator.validate, "Date is invalid.");

        // All of the validator methods have been called
        // End the validation session
        var result = $.validity.end();
        
        // Return whether it's okay to proceed with the request
        return result.valid;
    }

    /*
     * Grab the form inputs, validate, and send a request to the server for data.
     */
	function send_json_request(data) {
		// Grab the URL from the form
		var url = $("#grabDateForm").attr("action");
		var start_date = $("#startDate").val();
		var end_date = $("#endDate").val();

	    // Validate inputs
	    if (!validateDateFormInputs()) {
	        if (log.isWarnEnabled()) {
		        log.warn("Validation failed!");
	        }
	        return false;	    	 
	    }
		
		// Set global start and end date
		startDate = Date.parseDate(start_date, "Y-m-d");
		endDate = Date.parseDate(end_date, "Y-m-d");

		if (log.isInfoEnabled()) {
		    log.info("Grabbing data from " + start_date + " to " + end_date);
		}

		// Form the URL and send out
		url += "?s=" + start_date + "&e=" + end_date;     
        $.getJSON(url, populate_graphs_with_json);

        if (log.isDebugEnabled()) {
            log.debug("Grabbing data from URL: " + url);
        }

		// Return false to cancel the usual submit functionality
		return false;
	}

	/*
	 * Iterate through each graph type, and add any data retruend from the server
	 * to the graph.  Validate incoming JSON and make sure there were no server errors.
	 */
	function populate_graphs_with_json(json_data, text_status) {
        if (log.isDebugEnabled()) {
            log.debug("Received JSON data from server with status: " + text_status);
        }		
		
	    // Validate incoming JSON data
	    if (text_status != "success") {
	        log.error("Bad status from server: " + text_status);
	    }

	    // Check validity of the JSON
	    // TODO: Make sure we received something that makes sense
	    
	    // Run through possible error codes from server
	    // TODO: Check for error codes in JSON
		
		// DATA PREPROCESSING, MOVE TO SERVER OR IN TO A JS CLASS
		
		// Pull out the day into a Date for each data point
	    json_data.forEach(function(d) {
			var period = d.time.lastIndexOf('.');
	        d.date = Date.parseDate(d.time.substring(0, period), "Y-m-d g:i:s").grabDate();
			
			// Check if the date was parsed correctly
			if (d.date == null) {
				if (log.isErrorEnabled()) {
					log.error("Date parsed incorrectly from: " + d.time);
				}
			}
		});		
		
		// Give the new data to the graphs
		var data = json_data;
		graphList.forEach(function(graph) {
			var prompt_id = graph.prompt_id;
			var group_id = graph.group_id;
			
			if (log.isDebugEnabled()) {
                log.debug("Rendering graph with prompt_id " + prompt_id + " group_id " + group_id);
				var start_render_time = new Date().getTime();
            }
			
			var new_data = data.filter(function(data_point) {
		        return ((prompt_id == data_point.prompt_id) && (group_id == data_point.prompt_group_id));
		    });
			
			// Apply data to the graph
            graph.apply_data(new_data, 
                             startDate, 
                             startDate.difference_in_days(endDate));
                             
			// Show the graphs
			$("ul.tabs").show();
			$("div.panes").show();
							 
            // Re-render graphs with the new data
            graph.render();
			
            if (log.isDebugEnabled()) {
				var time_to_render = new Date().getTime() - start_render_time;
				
                log.debug("Time to render graph: " + time_to_render + " ms");
            }				
		});
	}
	
	/*
	 * Pass a single object describing a data type.  Will create a Protovis graph for that type.
	 */ 
	function handleResponse(graph_description) {		
	    // Create a unique div_id for each Graph
        var new_div_id = 'ProtovisGraph_' + graph_description.group_id + '_' + graph_description.prompt_id;
        // Append a new div on to the DOM for the new graph     
        $('#' + graph_description.group_id).append('<div id="' + new_div_id + '"></div>');    
		
		// Create a new graph object using the graph information
		var new_graph = ProtoGraph.factory(graph_description, new_div_id);

        // Set group and prompt ID on each graph for later retrieval
		new_graph.prompt_id = graph_description.prompt_id;
		new_graph.group_id = graph_description.group_id;
		
		// Add graph to the global list for now
		graphList.push(new_graph);
	}
	
    </script>
	

	
  </head>
  <body>
  
  <div class="zp-wrapper">
    <div class="zp-90 content">
	  <div class="padding">
	    <h1>EMA Visualizations for <c:out value="${sessionScope.user.userName}"></c:out>.</h1>
  	    
  	     <form method="post" action="/app/viz" id="grabDateForm">
		   <fieldset>
		     <div class="form-item">
			   <label for="startDate">Start Date:</label>
			   <input tabindex="1" id="startDate" type="text" name="s"/>
			 </div>	
			 <div class="form-item">
			   <label for="endDate">End Date:</label>
			   <input tabindex="1" id="endDate" type="text" name="e"/>
			 </div>
		     <button type="submit">Send</button>
					
		   </fieldset>
		 </form>
		 
		 
		 
  	    
      </div>
    </div>
    <div class="zp-10 content">
	  <div class="padding">
  	    <p><a href="/app/logout">Logout</a></p>
      </div>
    </div>
  </div>

  <!-- Let's try to setup some tabs manually -->
  <ul class="tabs"> 
    <li><a href="#saliva">Saliva</a></li> 
    <li><a href="#sleep">Sleep</a></li> 
    <li><a href="#emotional_state">Emtional State</a></li> 
    <li><a href="#diary">Diary</a></li> 
  </ul> 
  
  <div class="panes"> 
    <div id="0"></div> 
    <div id="1"></div> 
    <div id="2"></div>
    <div id="3"></div>
  </div>
  
  
  <div class="zp-wrapper">
    <div class="zp-100 content">
      <div class="padding">
        <p class="footer">Question? Comment? Problem? Email us at andwellness-info@cens.ucla.edu.</p>
      </div>
    </div>
  </div>
  
  </body>
</html>
