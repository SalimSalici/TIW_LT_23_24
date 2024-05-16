package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.GroupDAO;
import it.polimi.tiw.utils.DatabaseInitializer;
import it.polimi.tiw.utils.ThymeleafInitializer;

/**
 * Servlet implementation class GroupDetails that handles the display of group details.
 */
@WebServlet("/groupdetails")
public class GroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;

	/**
	 * Initializes the servlet by setting up the template engine and database connection.
	 * @throws UnavailableException if initialization fails
	 */
	@Override
    public void init() throws UnavailableException {
    	this.templateEngine = ThymeleafInitializer.initialize(this.getServletContext());    
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * Handles the GET request by fetching group details and user list, then rendering the group details page.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Parse the group ID from the request
		int groupId;
		try {
			groupId = Integer.parseInt(request.getParameter("id"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter error.");
			return;
		}
		// Initialize GroupDAO with the current database connection
		GroupDAO gDAO = new GroupDAO(this.connection);
		
		Group group;
		List<User> users;
		try {
			// Fetch the group details and its associated users from the database
			group = gDAO.fetchGroupById(groupId);
			users = gDAO.fetchUsersOfGroup(groupId);
			// Set the user count for the group
			group.setUserCount(users.size());
		} catch (NullPointerException e) {
			// Handle the case in which a group with the given id doesn't exist
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Group not found.");
			return;
		} catch (SQLException e) {
			// Handle SQL exceptions by sending a server error response
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Define the path for the Thymeleaf template
		String path = "groupdetails";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		// Set variables for the group and its users to be accessible in the template
		ctx.setVariable("group", group);
		ctx.setVariable("groupUsers", users);
		
		// Process the template to render the page
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * Closes the database connection when the servlet is destroyed.
	 */
	@Override
	public void destroy() {
        try {
            if(this.connection != null )
                this.connection.close();
        } catch (SQLException e) {}
    }
}
