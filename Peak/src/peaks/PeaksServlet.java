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
public class PeaksServlet extends HttpServlet {
	private String url;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
//		resp.setContentType("text/plain");
		String regionSelected =  req.getParameter("regionInput");
		
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
				url = "jdbc:google:mysql://peakclimbed:comp430/peak";
			} else {
				// Local MySQL instance to use during development.
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://localhost:3306/comp430";
			}
			// NOTE: this assumes that you have set a password for
			// root@localhost, and that's who you are connecting as!!
			connection = DriverManager.getConnection(url,"root","121314");

			// execute a query that will obtain all of the peaks
			String query = "Select NAME,ELEV,DIFF,MAP From PEAK Where REGION = \'" + regionSelected +"\'";
			PreparedStatement statement = connection.prepareStatement (query);
			ResultSet resultSet = statement.executeQuery ();

			// store all of the peaks' informations into lists
			ArrayList <String> peakName = new ArrayList <String> ();
			ArrayList <Integer> peakElev = new ArrayList <Integer> ();
			ArrayList <Integer> peakDiff = new ArrayList <Integer> ();
			ArrayList <String> peakMap = new ArrayList <String> ();		
			
			while (resultSet.next ()) {
				peakName.add (resultSet.getString (1));
				peakElev.add(resultSet.getInt(2));
				peakDiff.add(resultSet.getInt(3));
				peakMap.add(resultSet.getString(4));
			}					
			// close the SQL connection
			connection.close ();

			// augment the request by adding the list of regions to it
			req.setAttribute ("peaks", peakName);
			req.setAttribute("elev", peakElev);
			req.setAttribute("diff", peakDiff);
			req.setAttribute("map", peakMap);
			req.setAttribute("regionSelected", regionSelected);
			
			// For debug only
			//System.out.println("Results: " + peakName);

			// Forward the request to the "showregions.jsp" page for display
			ServletContext servletContext = getServletContext();
			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher ("/showPeaks.jsp");
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
