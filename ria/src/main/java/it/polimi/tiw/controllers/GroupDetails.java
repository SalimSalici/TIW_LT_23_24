package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.GroupDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class GroupDetails to fetch the details of a group.
 */
@WebServlet("/groupdetails")
@MultipartConfig
public class GroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private Gson gson;
    
	/**
	 * Initializes the servlet by initializing the Gson object and the database connection.
	 * @throws UnavailableException if the database connection cannot be initialized.
	 */
	@Override
    public void init() throws UnavailableException {
    	this.gson = new Gson();
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * Handles GET requests. It fetches the details of a group and its members and returns them as a JSON object.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws ServletException if an error occurs while processing the request.
	 * @throws IOException if an error occurs while writing the response.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		
		// Get the group id from the request
		int groupId = Integer.parseInt(request.getParameter("id"));
		GroupDAO gDAO = new GroupDAO(this.connection);
		
		// Fetch the group and its users
		Group group;
		List<User> users;
		try {
			group = gDAO.fetchGroupById(groupId);
			users = gDAO.fetchUsersOfGroup(groupId);
			group.setUserCount(users.size());
		} catch (SQLException e) {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().append("Group not found.");
			return;
		}
		
		// Create a JSON object with the group and its users
		JsonObject json = new JsonObject();
		json.add("group", this.gson.toJsonTree(group));
		json.add("users", this.gson.toJsonTree(users));
		
		// Return the JSON object as the response
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(json.toString());
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
