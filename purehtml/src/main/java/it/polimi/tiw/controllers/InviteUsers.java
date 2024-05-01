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
 * Servlet implementation class CreateGroup
 */
@WebServlet("/inviteusers")
public class InviteUsers extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
    
    @Override
    public void init() throws UnavailableException {
    	this.templateEngine = ThymeleafInitializer.initialize(this.getServletContext());
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		
		// Extracting parameters from request and validating them
		String groupName = request.getParameter("groupName");
		int duration;
		int minMembers;
		int maxMembers;
		try {
			if (groupName == null) throw new NullPointerException();
			duration = Integer.parseInt(request.getParameter("duration"));
			minMembers = Integer.parseInt(request.getParameter("minMembers"));
			maxMembers = Integer.parseInt(request.getParameter("maxMembers"));
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Form was not sent correctly.");
			return;
		}
		
		CreateGroupFormValidation validation = new CreateGroupFormValidation(groupName, duration, minMembers, maxMembers);
		
		GroupDAO gDAO = new GroupDAO(this.connection);
		User user = (User)request.getSession().getAttribute("user");
		try {
			if (!gDAO.isGroupNameAvailableForUser(user.getId(), groupName))
				validation.addErrorMessage("You already have a group named '" + groupName + "'. Please choose another name.");
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
		}
		
		if (validation.hasErrors()) {
			request.getSession().setAttribute("createGroupValidation", validation);
		    response.sendRedirect(request.getContextPath() + "/home");
		    return;
		}
		
		// Fetch list of all users to be displayed
		List<User> users;
		try {
			users = new UserDAO(this.connection).fetchAllUsers();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		String path = "inviteusers";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("users", users);

		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String groupName = request.getParameter("groupName");
		int duration;
		int minMembers;
		int maxMembers;
		String[] usersToInviteStrings = request.getParameterValues("usersToInvite[]");
		int[] usersToInvite = new int[usersToInviteStrings.length];
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
		
		// Validation passed
		User user = (User)request.getSession().getAttribute("user");
		GroupDAO gDAO = new GroupDAO(this.connection);
		try {
			gDAO.createNewGroup(groupName, user.getId(), duration, minMembers, maxMembers, usersToInvite);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			// response.sendError(HttpServletResponse.SC_BAD_GATEWAY, e.getMessage());
			return;
		}
		
		response.sendRedirect(request.getContextPath() + "/home");
	}

}
