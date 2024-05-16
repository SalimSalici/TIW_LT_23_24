package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class CheckLogin
 */
@WebServlet("/login")
@MultipartConfig
public class LoginCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	/**
	 * Initializes the servlet by initializing the database connection.
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 */
	@Override
	public void init() throws ServletException {
		this.connection = DatabaseInitializer.initialize(this.getServletContext());
	}

	/**
	 * Handles the POST request by checking the user's credentials and setting the session attribute.
	 * @param request the HttpServletRequest object that contains the request the client has made of the servlet
	 * @param response the HttpServletResponse object that contains the response the servlet sends to the client
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 * @throws IOException if an input or output exception occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");

		String username = request.getParameter("loginUsername");
		String password = request.getParameter("loginPassword");

		if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append("Missing parameters.");
		    return;
		}
		
		User user;
		try {
			user = new UserDAO(this.connection).login(username, password);
		} catch (SQLException e) {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().append("Failure in database credential checking.");
			return;
		}
		
		if (user == null) {
			// Login failed for wrong credentials
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
			response.getWriter().append("Wrong username or password.");
		    return;
		}
		
		request.getSession().setAttribute("user", user);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().append(new Gson().toJson(user));
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
