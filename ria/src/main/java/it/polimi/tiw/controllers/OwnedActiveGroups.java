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

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class OwnedGroups that fetches the active groups owned by the user
 */
@WebServlet("/ownedactivegroups")
@MultipartConfig
public class OwnedActiveGroups extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
    
	/**
	 * Initializes the servlet by initializing the database connection.
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 */
	@Override
    public void init() throws UnavailableException {
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * Handles the GET request by fetching the active groups owned by the user.
	 * @param request the HttpServletRequest object that contains the request the client has made of the servlet
	 * @param response the HttpServletResponse object that contains the response the servlet sends to the client
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 * @throws IOException if an input or output exception occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		
		User user = (User)request.getSession().getAttribute("user");
		
		UserDAO uDAO = new UserDAO(this.connection);
		List<Group> ownedGroups;
		try {
			ownedGroups = uDAO.fetchGroupsOwnedBy(user.getId());
		} catch (SQLException e) {
			response.setContentType("text/plain");
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(new Gson().toJson(ownedGroups));
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
