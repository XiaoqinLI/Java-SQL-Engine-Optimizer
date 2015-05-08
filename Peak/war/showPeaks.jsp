
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
        	String regionName = (String) request.getAttribute("regionSelected");
        	
        	int count  = myList.size();
        	if (count > 0){
%>
				<form action="/climber" method="POST">
				<p>Hello, <%= user %>. The time is now <%= new java.util.Date() %></p>
				<p> <%= count %> peaks found in region <%= regionName %>, they are: </p>
				<ol>
<%
				ArrayList <Integer> peakElev = (ArrayList <Integer>) request.getAttribute ("elev");
				ArrayList <Integer> peakDiff = (ArrayList <Integer>) request.getAttribute ("diff");
				ArrayList <String> peakMap = (ArrayList <String>) request.getAttribute ("map");
				
				int pos = 0;
				
				for (String currPeak : myList) {
					out.println ("<li>" + currPeak + " ("+ peakElev.get(pos)+ " ft, " + peakDiff.get(pos) +  
								" diff, " + "map is "+ peakMap.get(pos)+ ")" + "</li>");
					pos++;
				}
				out.println ("</ol>");			
%>
				What peak do you want to see the climbers for? <INPUT TYPE=TEXT NAME="peakInput" SIZE=20 value = "<%= myList.get(0) %>">
				<P><INPUT TYPE=SUBMIT VALUE= "Check for Climbers of Peak">
				</form>
				<form action="/region" method="GET">
					<INPUT TYPE=SUBMIT VALUE= "Back" onclick="history.nback()">
				<form>
<%
			}
			else{
%>
				<form action="/region" method="GET">
					<p>Hello, <%= user %>.The time is now <%= new java.util.Date() %></p>
					<p>There was an error, region <%= regionName %> was not found </p>
					<P><INPUT TYPE=SUBMIT VALUE= "Back" onclick="history.nback()">
				</form>		
<%			
			}
		}
%>



</body>
</html>
