<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isErrorPage="true" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>AndWellness - 500 Server Error</title>
    
    <link href="/css/zp-compressed.css" type="text/css" media="screen, print" rel="stylesheet" />
    <link href="http://andwellness.cens.ucla.edu/favicon.ico" rel="shortcut icon" type="image/x-icon">
    
    <!--[if IE]>
	<link href="/css/zp-ie.css" type="text/css" media="screen" rel="stylesheet" />
	<![endif]-->
    
    <style type="text/css">
		
		.content .padding {
			padding: 50px 50px 50px 50px;
		}
		.top-padding {
		    padding: 50px 0px 0px 0px;
		}
		.errorText {
		   color: #FF0000;
		   font-family: Courier;
		   margin: 0 0 0 0; 
		}
		.h {
		  font-size: 36px;   	
	      line-height: 36px;
	      font-weight: normal;
		}
		.m {
		  font-size: 18px;   	
	      line-height: 20px;
	      font-weight: normal;
		}
	</style>
  </head>
  
  <body>
  
  <div class="zp-wrapper">
    <div class="zp-100 content">
	  <div class="padding">
  		<p class="h">Server Error</p>
        <p class="m">Sorry, an error occurred while processing your request. If you'd like to copy and 
        paste the red text below into an email and send it to andwellness-info@cens.ucla.edu along with a description of what you 
        were attempting to do, the AndWellness team would greatly appreciate it!</p>
        
        <p class="errorText"><% out.println(exception.getMessage() == null ? "" : exception.getMessage()); %></p>
  		<p class="errorText"><% exception.printStackTrace(new java.io.PrintWriter(out)); %></p>
        </div> 
    </div>
  </div>
    
  </body>
</html>
