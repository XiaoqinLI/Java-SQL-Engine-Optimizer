
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
        } else {
%>
		<p>Hello, <%= user %>.</p>
		<p>The regions in the database are:</p>
		<ol>
<%
		ArrayList <String> myList = (ArrayList <String>) request.getAttribute ("regions");
		for (String s : myList) {
			out.println ("<li>" + s + "</li>");
		}
		out.println ("</ol>");
	}	
%>
</body>
</html>
