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
 * Servlet implementation class RequestGroupCreation
 */
@WebServlet("/creategroup")
@MultipartConfig
public class CreateGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateGroup() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws UnavailableException {
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String groupName = request.getParameter("groupName");
		int duration;
		int minMembers;
		int maxMembers;
		String[] usersToInviteStrings = request.getParameterValues("usersToInvite[]");
		int inviteCount = usersToInviteStrings.length;
		int[] usersToInvite = new int[inviteCount];
		try {
			if (groupName == null) throw new NullPointerException();
			duration = Integer.parseInt(request.getParameter("duration"));
			minMembers = Integer.parseInt(request.getParameter("minMembers"));
			maxMembers = Integer.parseInt(request.getParameter("maxMembers"));
			for (int i = 0; i < usersToInvite.length; i++)
				usersToInvite[i] = Integer.parseInt(usersToInviteStrings[i]);
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String jsonString = new Gson().toJson(List.of("Form was not sent correctly."));
			response.getWriter().append(jsonString);
			return;
		}
		
		List<String> errors = this.validateInputs(groupName, duration, minMembers, maxMembers, inviteCount);
		
		GroupDAO gDAO = new GroupDAO(this.connection);
		User user = (User)request.getSession().getAttribute("user");
		try {
			if (!gDAO.isGroupNameAvailableForUser(user.getId(), groupName))
				errors.add("You already have a group named '" + groupName + "'. Please choose another name.");
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append("Database failure.");
			return;
		}
		
		
		
		if (!errors.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append(new Gson().toJson(errors));
		    return;
		}

		// Validation passed
		try {
			gDAO.createNewGroup(groupName, user.getId(), duration, minMembers, maxMembers, usersToInvite);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append("Database failure.");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append("Group created");
	}
	
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

}
