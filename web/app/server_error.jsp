<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
		.footer {
		   color: #BEBEBE;
		}
		.errorText {
		   color: #FF0000;
		   font-family: Courier;
		   margin: 0 0 0 0; 
		}
	</style>
  </head>
  
  <body>
  
  <div class="zp-wrapper">
    <div class="zp-100 content">
	  <div class="padding">
  		<h1>AndWellness - 500 Server Error.</h1>
        <p>We have encountered an error while attempting to process your request.</p>
        
        <p class="errorText"><c:out value="${sessionScope.exception.message}"></c:out></p>
        <c:forEach items="${sessionScope.exception.stackTrace}" var="st">
  			<p class="errorText"><c:out value="${st}"></c:out></p>
        </c:forEach>
        
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
