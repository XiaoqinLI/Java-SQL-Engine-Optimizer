
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
<form action="/peak" method="POST">
<%	
	// get the user
        String user = (String) session.getAttribute ("user");
        ArrayList <String> myList = (ArrayList <String>) request.getAttribute ("regions");
        if (user == null) {
%>
                <p>You are not logged in.</p>
<%
        } else {
%>
		<p>Hello, <%= user %>.</p>
		<p>Hello!  The time is now <%= new java.util.Date() %></p>
		<p>The regions in the database are:</p>
		<ol>
<%		
		//String regionFir = new String((String)myList.get(0));
		
		for (String s : myList) {
			out.println ("<li>" + s + "</li>");
		}
		out.println ("</ol>");
	}	
%>


Select Your Region? <INPUT TYPE=TEXT NAME="region" SIZE = 20 value= "<%= myList.get(0) %>"> <P> <INPUT TYPE=SUBMIT VALUE= SELECT>
</form>


</body>
</html>
