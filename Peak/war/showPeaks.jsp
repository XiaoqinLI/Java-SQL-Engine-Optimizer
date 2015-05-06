
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
        ArrayList <String> myList = (ArrayList <String>) request.getAttribute ("peaks");
        if (user == null) {
%>
                <p>You are not logged in.</p>
<%
        } 
        else {
        	String regionName = (String) request.getAttribute("regionSel");
        	
        	int count  = myList.size();
        	if (count > 0){
%>
				<form action="/climber" method="POST">
				<p>Hello, <%= user %>.</p>
				<p>Hello!  The time is now <%= new java.util.Date() %></p>
				<p>The number of Peaks in the database for region <%= regionName %> are <%= count %></p>
				<ol>
<%
				ArrayList <Integer> peakElev = (ArrayList <Integer>) request.getAttribute ("elev");
				ArrayList <Integer> peakDiff = (ArrayList <Integer>) request.getAttribute ("diff");
				ArrayList <String> peakMap = (ArrayList <String>) request.getAttribute ("map");
				
				int pos = 0;
				
				for (String s : myList) {
					out.println ("<li>" + s + "  (The elevation is: "+ peakElev.get(pos)+ " ft, difficulty level is: " + peakDiff.get(pos)+  
								"  and is located on Map: "+ peakMap.get(pos)+ " )" + "</li>");
					pos++;
				}
				out.println ("</ol>");			
%>
				Select Your Peak? <INPUT TYPE=TEXT NAME="peak" SIZE=20 value = "<%= myList.get(0) %>"><BR>
				<P><INPUT TYPE=SUBMIT VALUE= SELECT>
				</form>
<%
			}
			else{
%>
				
				<form action="/region" method="GET">
					<p>Hello, <%= user %>.</p>
					<p>Hello!  The time is now <%= new java.util.Date() %></p>
					<p>The number of Peaks in the database for region <%= regionName %> are <%= count %></p>
					<P><INPUT TYPE=SUBMIT VALUE= BACK>
				</form>		
<%			
			}
		}
%>



</body>
</html>
