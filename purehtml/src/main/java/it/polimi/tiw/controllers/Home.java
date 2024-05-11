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
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.DatabaseInitializer;
import it.polimi.tiw.utils.ThymeleafInitializer;

/**
 * Servlet implementation class Home that handles the main page display after user login.
 */
@WebServlet("/home")
public class Home extends HttpServlet {
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
     * Handles the GET request by fetching user-specific data and rendering the home page.
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Retrieve user from session
		User user = (User)request.getSession().getAttribute("user");
		
		// DAO for accessing user data
		UserDAO uDAO = new UserDAO(this.connection);
		List<Group> ownedGroups;
		List<Group> otherGroups;
		try {
			// Fetch groups owned by the user and groups the user is part of
			ownedGroups = uDAO.fetchGroupsOwnedBy(user.getId());
			otherGroups = uDAO.fetchGroupsWithUser(user.getId());
		} catch (SQLException e) {
			// Handle SQL exceptions by sending a server error response
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Define the path for the Thymeleaf template
		String path = "home";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("ownedGroups", ownedGroups);
		ctx.setVariable("otherGroups", otherGroups);
		
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
