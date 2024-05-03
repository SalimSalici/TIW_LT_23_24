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
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginCheck() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {
		this.connection = DatabaseInitializer.initialize(this.getServletContext());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method not allowed.");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("loginUsername");
		String password = request.getParameter("loginPassword");
		
		User user;
		try {
			user = new UserDAO(this.connection).login(username, password);
		} catch (SQLException e) {
			// throw new ServletException(e);
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking.");
			return;
		}
		
		if (user == null) {
	        /* // Chose the redirect approach over forward approach to have more consistency with url 
	         *    displayed in the top browser bar
			request.setAttribute("errorMessage", "Wrong username or password"); // Error message
	        request.getRequestDispatcher("/Auth").forward(request, response); // Forwarding back to the login page
			*/
			
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
			response.getWriter().append("Wrong username or password.");
		    return;
		}
		
		request.getSession().setAttribute("user", user);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().append(new Gson().toJson(user));
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}

}
