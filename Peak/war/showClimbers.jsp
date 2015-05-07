
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.rdbms.AppEngineDriver" %>
<%@ page import="java.sql.*" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<body>

<%	
	// get the user
    String user = (String) session.getAttribute ("user");
    if (user == null) {
%>
            <p>You are not logged in.</p>
<%
    }
    else{
    
	     String peakName = (String) request.getAttribute("peak");
	     ArrayList <String> myList = (ArrayList <String>) request.getAttribute ("climber");
	     int count = myList.size();  
	     if (count > 0){     
%>
			<form action="/region" method="GET">
			<p>Hello, <%= user %>. The time is now <%= new java.util.Date() %></p>	
			<p>The climbers who climbed <%= peakName %> are: </p> 	  
			<ol>
<%		
			ArrayList <String> climberDate = (ArrayList <String>) request.getAttribute ("date");
			int pos  = 0;
			for (String currName : myList) {
				out.println ("<li>" + currName + " on "+ climberDate.get(pos)  + "</li>");
				pos++;
			}
			out.println ("</ol>");
%>		

			<P><INPUT TYPE = SUBMIT VALUE = "Back to Start">
			</form>
<%
		}
		else{
%>
			<form action="/region" method="GET">
				<p>Hello, <%= user %>.The time is now <%= new java.util.Date() %></p>
				<p>There was an error, peak <%= peakName %> was not found </p>
				<P><INPUT TYPE=SUBMIT VALUE= "Back" onclick="history.nback()">
			</form>		
<%
		}
	}
%>



</body>
</html>
