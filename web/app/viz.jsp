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
    

	<!-- A large number of javascript includes, will reduce -->
	<!-- Main jQuery library -->
    <script type="text/javascript" src="/js/lib/jquery/jquery-1.3.2.min.js"></script>
    <!-- Protovis graphing library with hacked in IE support -->
	<script type="text/javascript" src="/js/lib/Protovis/protovis-d3.1-ie.js"></script>
	<!-- Useful additions to Javascript objects -->
	<script type="text/javascript" src="/js/lib/misc/array_lib.js"></script>
	<script type="text/javascript" src="/js/lib/misc/date-functions.js"></script>
	<script type="text/javascript" src="/js/lib/misc/date_lib.js"></script>
	<!-- Generates the different graph types -->
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
		
	</style>
    
	
	
	<script type="text/javascript">
	
	// Globals for now for testing
	var startDate = new Date(2010,1,16,0,0,0);
	var numDays = 12;
	var curGroupId = -1;
	var graphList = [];	
	var dataSource = DataSource.factory(DataSource.data_source_type.REMOTE_JSON);
		
	// Called when document is done loading
    $(function() {
		// Hide the graphs to start
		$('#graph_insert').hide();
		
		// Loop over the questions in the response list
		response_list.forEach(handleResponse);
		
		// Over-ride the default submit for the form to grab data
		$("#grabDateForm").submit(send_json_request);
	});
	
	// Create a JSON request to the sever using jQuery
	function send_json_request(data) {
		// Grab the URL from the form
		var url = $("#grabDateForm").attr("action");
		var start_date = $("#startDate").val();
		var end_date = $("#endDate").val();
		
		// Set global start date
		startDate = Date.parseDate(start_date, "Y-m-d");
		numDays = startDate.difference_in_days(Date.parseDate(end_date, "Y-m-d"));
		
		url += "?s=" + start_date + "&e=" + end_date;     
        
        $.getJSON(url, populate_graphs_with_json);
		
		return false;
	}
	
	function populate_graphs_with_json(json_data, text_status) {
		// Pull out the day into a Date for each data point
	    json_data.forEach(function(d) {
			var period = d.time.lastIndexOf('.');
	        d.date = Date.parseDate(d.time.substring(0,period), "Y-m-d g:i:s").grabDate();
	    });
		
		// Give the new data to the graphs
		var data = json_data;
		graphList.forEach(function(graph) {
			var prompt_id = graph.prompt_id;
			var group_id = graph.group_id;
			
			var new_data = data.filter(function(data_point) {
		        return ((prompt_id == data_point.prompt_id) && (group_id == data_point.prompt_group_id));
		    });
			
			// Apply data to the graph
            graph.apply_data(new_data, 
                             startDate, 
                             numDays);
                             
			// Show the graphs
			$('#graph_insert').show();
							 
            // Re-render graphs with the new data
            graph.render();
		});
	}
	
	// Handle a response by figuring out the response type and creating
	// and appending a new graph to the DOM structure
	function handleResponse(graph_description) {
		// If we move to a new group of questions, print out a header
		if (curGroupId != graph_description.group_id) {
			$('#graph_insert').append('<h1>' + group_list[graph_description.group_id] + '</h1>');
			curGroupId = graph_description.group_id;
		}
		
	    // Create a unique div_id for each Graph
        var new_div_id = 'ProtovisGraph_' + graph_description.group_id + '_' + graph_description.prompt_id;
        // Append a new div on to the DOM for the new graph     
        $('#graph_insert').append('<div id="' + new_div_id + '"></div>');    
		
		// Create a new graph object using the graph information
		var new_graph = ProtoGraph.factory(graph_description, new_div_id);

        // Set group and prompt ID on each graph for later retrieval
		new_graph.prompt_id = graph_description.prompt_id;
		new_graph.group_id = graph_description.group_id;

		// Render the blank graph
		new_graph.render();
		
		// Add graph to the global list for now
		graphList.push(new_graph);
	}
	
    </script>
	

	
  </head>
  <body>
  
  <div class="zp-wrapper">
    <div class="zp-90 content">
	  <div class="padding">
	    <h1>EMA Visualizations for <c:out value="${sessionScope.userName}"></c:out>.</h1>
  	    
  	     <form method="post" action="/app/viz" id="grabDateForm">
		   <fieldset>

		     <div class="form-item">
			   <label for="startDate">Start Date:</label>
			   <input tabindex="1" id="startDate" type="text" name="s" />
			 </div>	
			 <div class="form-item">
			   <label for="endDate">End Date:</label>
			   <input tabindex="1" id="endDate" type="text" name="e" />
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
  
  
  <!-- The graphs will be anchored onto this div -->
  <div class="zp-wrapper">
  	<div class="zp-100 content">
      <div id="graph_insert"></div>  	
	</div>
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
