package peaks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.rdbms.AppEngineDriver;
import com.google.appengine.api.utils.SystemProperty;

@SuppressWarnings("serial")
public class ClimbersServlet extends HttpServlet {
	private String url;
	public void doPost (HttpServletRequest req, HttpServletResponse resp)throws IOException {
		String peakSelected = req.getParameter("peakInput");

		// database connection
		Connection connection = null;
		// process the request by getting all of the peak names and adding them to the request
		try {
			// set up the connection
			DriverManager.registerDriver(new AppEngineDriver());
			if (SystemProperty.environment.value() ==
					SystemProperty.Environment.Value.Production) {
				// Load the class that provides the new "jdbc:google:mysql://" prefix.
				Class.forName("com.mysql.jdbc.GoogleDriver");
				url = "jdbc:google:mysql://animated-origin-93407:database/peak";
			} else {
				// Local MySQL instance to use during development.
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://localhost:3306/comp430";
			}
			// NOTE: this assumes that you have set a password for
			// root@localhost, and that's who you are connecting as!!
			connection = DriverManager.getConnection(url,"root","121314");

			// execute a query that will obtain all of the climbers that once climbed the chosen peak
			String query =	"Select P.NAME,C.WHEN_CLIMBED from CLIMBED C, PARTICIPATED P Where C.PEAK =  \'" + peakSelected +"\'" +
							"and  C.TRIP_ID = P.TRIP_ID " ;
			
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery ();

			// store all of the peaks into a list
			ArrayList <String> climberName = new ArrayList <String> ();
			ArrayList <String> climberDate = new ArrayList <String> ();
			
			while (resultSet.next ()) {
				
				//String temp = rs.getString (1);
				String name = new String((resultSet.getString (1)).substring(0, 1) + (((resultSet.getString (1))).substring(1)).toLowerCase());				
				climberName.add (name);
				climberDate.add( resultSet.getString(2));
			}			

			// close the connection
			connection.close ();

			// augment the request by adding the list of regions to it
			req.setAttribute ("climber", climberName);
			req.setAttribute("peak", peakSelected);
			req.setAttribute("date", climberDate);
			
			System.out.println("Result is " + climberName);

			// and forward the request to the "showregions.jsp" page for display
			ServletContext servletContext = getServletContext();
			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher ("/showClimbers.jsp");
			requestDispatcher.forward (req, resp);

		} catch (SQLException e) {
			resp.getWriter().println(e.getMessage());
			e.printStackTrace();
		} catch (ServletException e) {
			resp.getWriter().println(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			resp.getWriter().println(e.getMessage());
			e.printStackTrace();
		}		
	}
}
