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
 * Servlet implementation class GroupDetails
 */
@WebServlet("/groupdetails")
@MultipartConfig
public class GroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private Gson gson;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupDetails() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws UnavailableException {
    	this.gson = new Gson();
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		int groupId = Integer.parseInt(request.getParameter("id"));
		GroupDAO gDAO = new GroupDAO(this.connection);
		
		Group group;
		List<User> users;
		try {
			group = gDAO.fetchGroupById(groupId);
			users = gDAO.fetchUsersOfGroup(groupId);
			group.setUserCount(users.size());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, e.getMessage());
//			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		JsonObject json = new JsonObject();
		json.add("group", this.gson.toJsonTree(group));
		json.add("users", this.gson.toJsonTree(users));
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(json.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
