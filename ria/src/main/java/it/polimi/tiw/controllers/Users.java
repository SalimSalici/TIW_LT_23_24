package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class Users that fetches all the users from the database.
 */
@WebServlet("/users")
public class Users extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private Gson gson;

	/**
	 * Initializes the servlet by initializing the Gson object and the database connection.
	 * @throws UnavailableException if the servlet is unable to handle the request
	 */
	public void init() throws UnavailableException {
    	this.gson = new Gson();
    	this.connection = DatabaseInitializer.initialize(this.getServletContext());
    }

	/**
	 * Handles the GET request by fetching all the users from the database and returning them as a JSON array.
	 * @param request the HttpServletRequest object that contains the request the client has made of the servlet
	 * @param response the HttpServletResponse object that contains the response the servlet sends to the client
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 * @throws IOException if an input or output exception occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		List<User> users;
		try {
			users = new UserDAO(this.connection).fetchAllUsers();
		} catch (SQLException e) {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().write("Database failure.");
			return;
		}
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(this.gson.toJson(users));
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
