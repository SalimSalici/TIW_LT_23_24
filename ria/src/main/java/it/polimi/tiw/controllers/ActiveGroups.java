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
 * This servlet is used to handle requests related to active groups.
 * It fetches the active groups of the current user and returns them as a JSON array.
 */
@WebServlet("/activegroups")
@MultipartConfig
public class ActiveGroups extends HttpServlet {
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
	 * Handles GET requests. It fetches the active groups of the current user and returns them as a JSON array.
	 * If the database connection fails, it returns a 502 error.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws ServletException if an error occurs while processing the request.
	 * @throws IOException if an error occurs while writing the response.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		
		User user = (User)request.getSession().getAttribute("user");
		
		UserDAO uDAO = new UserDAO(this.connection);
		List<Group> groups;
		try {
			groups = uDAO.fetchGroupsWithUser(user.getId());
		} catch (SQLException e) {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append("Database failure.");
			return;
		}
		
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(new Gson().toJson(groups));
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
