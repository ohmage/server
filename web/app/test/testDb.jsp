<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Simple DB Access Test - Prompt question text and the associated legend for each question</title>
    
    <link href="/css/zp-compressed.css" type="text/css" media="screen, print" rel="stylesheet" />
	<link href="/css/zp-print.css" type="text/css" media="print" rel="stylesheet" />
    
  </head>
  <body>
  <table>  
    <tr>
      <td>Row Num</td>
      <td>Legend</td>
      <td>Question</td>
    </tr>
    <c:forEach var="i" items="${testResultList}">
    <tr>
      <td>${i.rowNum}</td>
      <td>${i.legendText}</td>
      <td>${i.questionText}</td>
    </tr>
    </c:forEach>
  </table>
  </body>
</html>
