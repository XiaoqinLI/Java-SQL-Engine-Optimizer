package peaks;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.rdbms.AppEngineDriver;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

@SuppressWarnings("serial")
public class RegionsServlet extends HttpServlet {
	private String url;
	public void doGet (HttpServletRequest req, HttpServletResponse resp)throws IOException {
		
		resp.setContentType("text/plain");
		// make sure that the user has been authenticated
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		// if not, send them to the login page
		if (user == null) {
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));		
		} 
		// if so, then add them to the session
		else {
			req.getSession ().setAttribute ("user", user.getEmail ());
		}
		
		// this is how we will talk to the database
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

			// execute a query that will obtain all of the peaks
			String query = "select distinct REGION from PEAK";
			PreparedStatement statement = connection.prepareStatement (query);
			ResultSet resultSet = statement.executeQuery ();

			// store all of the peaks into a list
			ArrayList <String> myList = new ArrayList <String> ();
			while (resultSet.next ()) {
				myList.add (resultSet.getString (1));
			}			
			// close the SQL connection
			connection.close ();

			// augment the request by adding the list of regions to it
			req.setAttribute ("regions", myList);
			
			// For debug only
			// System.out.println("Results:" + myList);

			// Forward the request to the "showregions.jsp" page for display
			ServletContext servletContext = getServletContext();
			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher ("/showregions.jsp");
			requestDispatcher.forward (req, resp);

		} catch (SQLException e) {
			e.printStackTrace();
			resp.getWriter().println(e.getMessage());
		} catch (ServletException e) {
			resp.getWriter().println(e.getMessage());

			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			resp.getWriter().println(e.getMessage());
			e.printStackTrace();
		}		
		
	}
}
