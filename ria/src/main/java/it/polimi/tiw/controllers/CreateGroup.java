package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.GroupDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class CreateGroup that handles requests for creating a new group.
 */
@WebServlet("/creategroup")
@MultipartConfig
public class CreateGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    
    /**
     * Initializes the servlet by initializing the database connection.
     * @throws UnavailableException if the database connection cannot be initialized.
     */
    @Override
    public void init() throws UnavailableException {
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * Handles POST requests. It validates the input data, checks if the group name is available for the user,
	 * and if everything is correct, it creates a new group.
	 * If the database connection fails, it returns a 502 error.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws ServletException if an error occurs while processing the request.
	 * @throws IOException if an error occurs while writing the response.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		
		// Get the input parameters
		String groupName = request.getParameter("groupName");
		int duration;
		int minMembers;
		int maxMembers;
		String[] usersToInviteStrings = request.getParameterValues("usersToInvite[]");
		int inviteCount = usersToInviteStrings.length;
		int[] usersToInvite = new int[inviteCount];
		try {
			// Validate the input parameters
			if (groupName == null) throw new NullPointerException();
			duration = Integer.parseInt(request.getParameter("duration"));
			minMembers = Integer.parseInt(request.getParameter("minMembers"));
			maxMembers = Integer.parseInt(request.getParameter("maxMembers"));
			for (int i = 0; i < usersToInvite.length; i++)
				usersToInvite[i] = Integer.parseInt(usersToInviteStrings[i]);
		} catch (NumberFormatException | NullPointerException e) {
			// If the input parameters are not valid, return a bad request response
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String jsonString = new Gson().toJson(List.of("Form was not sent correctly."));
			response.getWriter().append(jsonString);
			return;
		}
		
		// Validate the input parameters
		List<String> errors = this.validateInputs(groupName, duration, minMembers, maxMembers, inviteCount);
		
		// Check if the group name is available for the user
		GroupDAO gDAO = new GroupDAO(this.connection);
		User user = (User)request.getSession().getAttribute("user");
		try {
			if (!gDAO.isGroupNameAvailableForUser(user.getId(), groupName))
				errors.add("You already have a group named '" + groupName + "'. Please choose another name.");
		} catch (SQLException e) {
			// If the database connection fails, return a bad gateway response
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append("Database failure.");
			return;
		}
		
		// If there are any errors, return a bad request response
		if (!errors.isEmpty()) {
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append(new Gson().toJson(errors));
		    return;
		}

		// Validation passed, create a new group
		try {
			gDAO.createNewGroup(groupName, user.getId(), duration, minMembers, maxMembers, usersToInvite);
		} catch (SQLException e) {
			// If the database connection fails, return a bad gateway response
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append("Database failure.");
			return;
		}
		
		// If everything is successful, return an OK response
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append("Group created");
	}
	
	/**
	 * Validates the input data. It checks if the group name is within the correct length,
	 * if the minimum and maximum members are at least 1, and if the maximum members are not less than the minimum members.
	 * It also checks if the number of invited users is within the correct range.
	 * @param groupName the name of the group.
	 * @param duration the duration of the group.
	 * @param minMembers the minimum number of members.
	 * @param maxMembers the maximum number of members.
	 * @param inviteCount the number of invited users.
	 * @return a list of errors, if any.
	 */
	private List<String> validateInputs(String groupName, int duration, int minMembers, int maxMembers, int inviteCount) {
		List<String> errors = new LinkedList<>();
		
		if (groupName.length() < 1 || groupName.length() > 45)
			errors.add("Group name must be within 1 and 100 characters long.");
		
		if (minMembers < 1)
			errors.add("Minimum amount of members must be at least 1.");
		if (maxMembers < 1)
			errors.add("Maxmum amount of members must be at least 1.");
		
		if (maxMembers < minMembers)
			errors.add("Maximum amount of members cannot be less than minimum amount of members.");
		else {
			int shortage = minMembers - inviteCount;
			int excess = inviteCount - maxMembers;
			
			if (shortage > 0)
				errors.add("Not enough users selected. Select at least " + shortage + " more.");
			else if (excess > 0)
				errors.add("Too many users selected. Deselect at least " + excess + " user(s).");
		}		 
		
		return errors;
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
