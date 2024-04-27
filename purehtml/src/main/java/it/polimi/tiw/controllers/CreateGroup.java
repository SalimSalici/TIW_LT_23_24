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

import it.polimi.tiw.beans.CreateGroupFormValidation;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.GroupDAO;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.DatabaseInitializer;
import it.polimi.tiw.utils.ThymeleafInitializer;

/**
 * Servlet implementation class CreateGroup
 */
@WebServlet("/creategroup")
public class CreateGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateGroup() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws UnavailableException {
    	this.templateEngine = ThymeleafInitializer.initialize(this.getServletContext());
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method not allowed.");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String groupName = request.getParameter("groupName");
		int duration = Integer.parseInt(request.getParameter("duration"));
		int minMembers = Integer.parseInt(request.getParameter("minMembers"));
		int maxMembers = Integer.parseInt(request.getParameter("maxMembers"));
		
		CreateGroupFormValidation validation = this.validateForm(groupName, duration, minMembers, maxMembers);
		
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
		
		List<User> users;
		try {
			users = new UserDAO(this.connection).fetchAllUsers();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		String path = "creategroup";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("users", users);

		templateEngine.process(path, ctx, response.getWriter());
	}
	
	private CreateGroupFormValidation validateForm(String groupName, int duration, int minMembers, int maxMembers) {
		CreateGroupFormValidation validation = new CreateGroupFormValidation(groupName, duration, minMembers, maxMembers);
		
		if (groupName.length() < 1 || groupName.length() > 100)
			validation.addErrorMessage("Group name must be within 1 and 100 characters long.");
		
		if (duration < 1)
			validation.addErrorMessage("Duration must be at least 1 day.");
		
		if (minMembers < 1)
			validation.addErrorMessage("Minimum amount of members must be at least 1.");
		
		if (maxMembers < 1)
			validation.addErrorMessage("Maximum amount of members must be at least 1.");
		
		if (maxMembers < minMembers)
			validation.addErrorMessage("Maximum amount of members cannot be less than minimum amount of members.");
		
		return validation;
	}

}
