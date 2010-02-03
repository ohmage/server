<%@ page contentType="text/html; charset=UTF-8" %>
<%@page import="edu.ucla.cens.awserver.domain.User"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Data Visualizations</title>
    
    <link href="/css/zp-compressed.css" type="text/css" media="screen, print" rel="stylesheet" />
	<link href="/css/zp-print.css" type="text/css" media="print" rel="stylesheet" />
    
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
    
  </head>
  <body>
  
  <div class="zp-wrapper">
    <div class="zp-90 content">
	  <div class="padding">
	    <h1>Welcome <c:out value="${sessionScope.userName}"></c:out>! </h1>
  	    <h2>I am the future home of the viz dashboard.</h2>
      </div>
    </div>
    <div class="zp-10 content">
	  <div class="padding">
  	    <p><a href="http://<c:out value="${sessionScope.subdomain}"></c:out>.<c:out value="${sessionScope.serverName}"></c:out>.cens.ucla.edu/logout">Logout</a></p>
      </div>
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
