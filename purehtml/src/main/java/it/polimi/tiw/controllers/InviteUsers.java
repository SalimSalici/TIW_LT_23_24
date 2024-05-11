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

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.GroupDAO;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.formbeans.CreateGroupFormValidation;
import it.polimi.tiw.utils.DatabaseInitializer;
import it.polimi.tiw.utils.ThymeleafInitializer;

/**
 * Servlet implementation class InviteUsers that handles user invitations to groups.
 */
@WebServlet("/inviteusers")
public class InviteUsers extends HttpServlet {
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
	 * Handles the GET request by displaying the user invitation form.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		
		// Extract parameters from request and validate them
		String groupName = request.getParameter("groupName");
		int duration;
		int minMembers;
		int maxMembers;
		try {
			// Check if groupName is null and parse integer parameters
			if (groupName == null) throw new NullPointerException();
			duration = Integer.parseInt(request.getParameter("duration"));
			minMembers = Integer.parseInt(request.getParameter("minMembers"));
			maxMembers = Integer.parseInt(request.getParameter("maxMembers"));
		} catch (NumberFormatException | NullPointerException e) {
			// Send error if parameters are not correctly formatted or missing
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Form was not sent correctly.");
			return;
		}
		
		// Validate the group creation form
		CreateGroupFormValidation validation = new CreateGroupFormValidation(groupName, duration, minMembers, maxMembers);
		
		// Initialize GroupDAO to interact with the database
		GroupDAO gDAO = new GroupDAO(this.connection);
		User user = (User)request.getSession().getAttribute("user");
		try {
			// Check if the group name is already used by the user
			if (!gDAO.isGroupNameAvailableForUser(user.getId(), groupName))
				validation.addErrorMessage("You already have a group named '" + groupName + "'. Please choose another name.");
		} catch (SQLException e) {
			// Handle SQL exceptions
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
		}
		
		// Redirect if there are validation errors
		if (validation.hasErrors()) {
			request.getSession().setAttribute("createGroupValidation", validation);
		    response.sendRedirect(request.getContextPath() + "/home");
		    return;
		}
		
		// Fetch all users from the database
		List<User> users;
		try {
			users = new UserDAO(this.connection).fetchAllUsers();
		} catch (SQLException e) {
			// Handle SQL exceptions
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Set the path for the Thymeleaf template
		String path = "inviteusers";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("users", users);

		// Render the template with the list of users
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * Handles the POST request by processing the user invitations.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Retrieve form parameters
		String groupName = request.getParameter("groupName");
		int duration;
		int minMembers;
		int maxMembers;
		String[] usersToInviteStrings = request.getParameterValues("usersToInvite[]");
		int[] usersToInvite = new int[usersToInviteStrings.length];
		
		// Parse and validate form parameters
		try {
			if (groupName == null) throw new NullPointerException();
			duration = Integer.parseInt(request.getParameter("duration"));
			minMembers = Integer.parseInt(request.getParameter("minMembers"));
			maxMembers = Integer.parseInt(request.getParameter("maxMembers"));
			for (int i = 0; i < usersToInvite.length; i++)
				usersToInvite[i] = Integer.parseInt(usersToInviteStrings[i]);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Form was not sent correctly.");
			return;
		}
		
		// Calculate the number of invited users and validate against group size constraints
		int inviteCount = usersToInvite.length;
		boolean error = false;
		int shortage = minMembers - inviteCount;
		int excess = inviteCount - maxMembers;
		if (shortage > 0) {
			error = true;
			request.setAttribute("errorMessage", "Not enough users selected. Select at least " + shortage + " more.");
		} else if (excess > 0) {
			error = true;
			request.setAttribute("errorMessage", "Too many users selected. Deselect at least " + excess + " user(s).");
		}
		
		// Handle errors and retry logic
		if (error) {
			int attempt = Integer.parseInt(request.getParameter("attempt"));
			if (attempt == 3) {
				response.sendRedirect(request.getContextPath() + "/cancellation");
				return;
			}
			request.setAttribute("prevInvitedUsers", usersToInvite);
			request.setAttribute("attempt", attempt + 1);
			this.doGet(request, response);
			return;
		}
		
		// Create new group in the database
		User user = (User)request.getSession().getAttribute("user");
		GroupDAO gDAO = new GroupDAO(this.connection);
		try {
			gDAO.createNewGroup(groupName, user.getId(), duration, minMembers, maxMembers, usersToInvite);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Redirect to home page after successful group creation
		response.sendRedirect(request.getContextPath() + "/home");
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
