<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="edu.ucla.cens.awserver.domain.User" %>
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
    
    
    <!-- CSS includes for various formatting -->
    <link href="/css/zp-compressed.css" type="text/css" media="screen, print" rel="stylesheet" />
    <link href="/css/zp-print.css" type="text/css" media="print" rel="stylesheet" />
    <link href="http://andwellness.cens.ucla.edu/favicon.ico" rel="shortcut icon" type="image/x-icon">
    <link type="text/css" href="/css/jquery-ui-1.7.2.custom.css" rel="stylesheet" />
    <link type="text/css" href="/css/jquery-validity.css" rel="stylesheet" />
    <link type="text/css" href="/css/tabs.css" rel="stylesheet" />
    <!-- Custom CSS for the "dashboard" setup -->
    <link type="text/css" href="/css/dashboard.css" rel="stylesheet" />
    <!-- dateinput styling -->
	<link rel="stylesheet" type="text/css" href="/css/dateinput.css"/>
    

    <!-- A large number of javascript includes, will reduce -->
    <!-- Main jQuery library -->
    <script type="text/javascript" src="/js/thirdparty/jquery/jquery-1.4.2.min.js"></script>
    <!-- jQuery UI toolkit for tabs -->
    <script type="text/javascript" src="/js/thirdparty/jquery/jquery.tools.min.js"></script>
    <!-- jQuery UI for Datepicker -->
    <script type="text/javascript" src="/js/thirdparty/jquery/jquery-ui-1.7.2.custom.min.js"></script>
    <!-- log4java like logging -->
    <script type="text/javascript" src="/js/lib/misc/log4javascript.js"></script>
    <!-- Protovis graphing library with hacked in IE support -->
    <script type="text/javascript" src="/js/thirdparty/Protovis/protovis-d3.2.js"></script>
    <!-- Useful additions to Javascript objects -->
    <script type="text/javascript" src="/js/lib/misc/array_lib.js"></script>
    <script type="text/javascript" src="/js/lib/misc/date-functions.js"></script>
    <!-- Various validators to validate user input and server responses -->
    <script type="text/javascript" src="/js/thirdparty/jquery/jquery.validity.min.js"></script>
    <script type="text/javascript" src="/js/lib/validator/DateValidator.js"></script>
    <!-- Contains the query response visualization types -->
    <script type="text/javascript" src="/js/response_list.js"></script>
    <!-- Generates the different graph types using Protovis -->
    <script type="text/javascript" src="/js/lib/DashBoard/DashBoard.js"></script>
    <script type="text/javascript" src="/js/lib/DataSource/DataSource.js"></script>
    <script type="text/javascript" src="/js/lib/DataSource/AwData.js"></script>
    <script type="text/javascript" src="/js/lib/DataSource/AwDataCreator.js"></script>
    <script type="text/javascript" src="/js/lib/Graph/ProtoGraph.js"></script>
    <script type="text/javascript" src="/js/lib/DashBoard/StatDisplay.js"></script>
    <script type="text/javascript" src="/js/lib/DashBoard/View.js"></script>
    <!-- Simple date formatting functions -->
    <script type="text/javascript" src="/js/thirdparty/misc/date.format.js"></script>
	
    <!--[if IE]>
	<link href="/css/zp-ie.css" type="text/css" media="screen" rel="stylesheet" />
	<![endif]-->
   
    
   
    <!-- Main page javascript.  Instantiates the dashboard and datasource.  Does
         basic form validation. -->
    <script type="text/javascript">
	
    // Holds the currently requested start date and number of days
    var startDate = new Date();
    var numDays = 0;

    // Holds the current page's DashBoard setup
    var dashBoard = null;
	
	// Grab the logged in user name from the jsp session
	var userName = "<c:out value="${sessionScope.user.userName}"></c:out>";
		
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

        // Setup the datepickers for the date input box
        // old datepicker
        //$("#startDate").datepicker({dateFormat: 'yy-mm-dd'});
 
 		var today = new Date().incrementDay(-13);
        $(":date").dateinput({
        	format: 'yyyy-mm-dd',	// the format displayed for the user
        	selectors: true,        // whether month/year dropdowns are shown
        })
        // Initially set to 13 days ago
        .data("dateinput").setValue(today);
        
        // Override the default submit function for the form
        $("#grabDateForm").submit(send_json_request);

        // Setup the dash board with the campaign configuration JSON
        dashBoard = new DashBoard();
        dashBoard.set_user_name(userName);
        dashBoard.initialize();
		
        // Initialize the page by grabbing config information from server
        send_json_request_init();
        //send_json_request(null);
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

        // All of the validator methods have been called
        // End the validation session
        var result = $.validity.end();
        
        // Return whether it's okay to proceed with the request
        return result.valid;
    }

    /*
     * Ask for enough info to initialize the webpage
     */
    function send_json_request_init() {
    	// Switch on the loading graphic
        dashBoard.loading(true);

		// This will initialize the main user upload page
		DataSourceJson.request_data(DataSourceJson.DATA_HOURS_SINCE_LAST_SURVEY);
    }

    /*
     * Grab the form inputs, validate, and send a request to the server for data.
     */
    function send_json_request(data) {
        // Grab the URL from the form
        //var url = $("#grabDateForm").attr("action");
        var start_date = $("#startDate").val();
        var num_days = $("#numDays").val();

        // Validate inputs
        if (!validateDateFormInputs()) {
            if (log.isWarnEnabled()) {
                log.warn("Validation failed!");
            }
            return false;	    	 
        }
		
        // Switch on the loading graphic
        dashBoard.loading(true);
		
        // Set global start and number of days
        startDate = Date.parseDate(start_date, "Y-m-d");
        numDays = parseInt(num_days);

        var end_date = startDate.incrementDay(numDays).dateFormat("Y-m-d");
        
        if (log.isInfoEnabled()) {
            log.info("Grabbing data from " + start_date + " to " + end_date);
        }

        // Setup params
        var params = {
        	    's': start_date,
        	    'e': end_date
        };
       

		// Grab hours since last survey information
		DataSourceJson.request_data(DataSourceJson.DATA_HOURS_SINCE_LAST_SURVEY);

		// Grab percentage good location updates
		DataSourceJson.request_data(DataSourceJson.DATA_LOCATION_UPDATES);
		
		// Grab hours since last location update
		DataSourceJson.request_data(DataSourceJson.DATA_HOURS_SINCE_LAST_UPDATE);

		// Grab number of completed surveys per day from server
		DataSourceJson.request_data(DataSourceJson.DATA_SURVEYS_PER_DAY, params);

        // Grab EMA data from the server 
        DataSourceJson.request_data(DataSourceJson.DATA_EMA, params);
        
		// Grab number of mobilities from the survey per day
		// NOT YET IMPLEMENTED ON SERVER
		//DataSourceJson.request_data(DataSourceJson.DATA_MOBILITY_MODE_PER_DAY, params);
		
        
        // Return false to cancel the usual submit functionality
        return false;
    }

	
    </script>
	
  </head>
  <body>
  <!-- Wrap the entire page in a custom div, maybe can use body instead -->
  <div id="wrapper" class="f">
  
  <!-- Dashboard banner -->
  <div id="banner">

  </div>
  
  <div id="controls">
 	Choose a time period:

    <form method="post" action="/app/q/ema" id="grabDateForm">
      <label for="startDate" class="label">Start Date:</label>
      <!--<input id="startDate" type="text"/> -->
      <input id="startDate" type="date" />
      <label for="numDays" class="label">Length:</label>
      <select id="numDays">
	    <option value="7">1 week</option>
	    <option selected="selected" value="14">2 weeks</option>
	    <option value="21">3 weeks</option>
	    <option value="28">4 weeks</option>
      </select>
      <button type="submit" id="submit">Go</button>                
    </form>
  </div>
  
  <!-- Main body of the dashboard -->
  <div id="main">

  </div>
  
  <!-- Dashboard footer -->
  <div id="footer">
    Question? Comment? Problem? Email us at andwellness-info@cens.ucla.edu.
  </div>
  
  </div>
  </body>
</html>
