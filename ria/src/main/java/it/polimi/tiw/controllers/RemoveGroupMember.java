package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.GroupDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class RemoveGroupMember that handles the removal of a member from a group.
 */
@WebServlet("/removegroupmember")
@MultipartConfig
public class RemoveGroupMember extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    
	/**
	 * Initializes the servlet by initializing the database connection.
	 * @throws UnavailableException if the servlet is unable to handle the request
	 */
	@Override
    public void init() throws UnavailableException {
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * Handles the POST request by removing a member from a group.
	 * @param request the HttpServletRequest object that contains the request the client has made of the servlet
	 * @param response the HttpServletResponse object that contains the response the servlet sends to the client
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 * @throws IOException if an input or output exception occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		int groupId = Integer.parseInt(request.getParameter("groupId"));
		int memberIdToRemove = Integer.parseInt(request.getParameter("memberId"));
		
		GroupDAO gDAO = new GroupDAO(this.connection);
		Group group;
		try {
			group = gDAO.fetchGroupById(groupId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append(e.getMessage());
			response.getWriter().append("Database failure.");
			return;
		}
		
		if (group == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().append("Group not found.");
			return;
		}
		
		User user = (User)request.getSession().getAttribute("user");
		if (user.getId() != group.getUserId()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().append("You are not the owner of the group.");
			return;
		}
		
		if (group.getUserCount() - 1 < group.getMinUsers()) {
			System.out.println(group.getUserCount());
			System.out.println(group.getMinUsers());
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			response.getWriter().append("Group cannot have less than the current amount of members.");
			return;
		}
		
		boolean removed;
		try {
			removed = gDAO.removeMemberFromGroup(groupId, memberIdToRemove);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append(e.getMessage());
			response.getWriter().append("Database failure.");
			return;
		}
		
		if (removed) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().append("Member removed successfully.");
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append("Member/group association not found.");
		}
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
