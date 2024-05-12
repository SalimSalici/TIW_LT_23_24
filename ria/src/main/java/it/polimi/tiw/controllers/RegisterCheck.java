package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
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
 * Servlet implementation class RegisterCheck that handles the registration of a new user.
 */
@WebServlet("/register")
@MultipartConfig
public class RegisterCheck extends HttpServlet {
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
	 * Handles the POST request by registering a new user and setting the session attribute.
	 * @param request the HttpServletRequest object that contains the request the client has made of the servlet
	 * @param response the HttpServletResponse object that contains the response the servlet sends to the client
	 * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
	 * @throws IOException if an input or output exception occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		
		// Get user input
		String username = request.getParameter("registerUsername");
		String name = request.getParameter("registerName");
		String surname = request.getParameter("registerSurname");
		String email = request.getParameter("registerEmail");
		String password = request.getParameter("registerPassword");
		String confirmPassword = request.getParameter("registerConfirmPassword");
		
		// Validate user input (also checks if the inputs are null)
		Map<String, String> errors = this.validateInputs(username, name, surname, email, password, confirmPassword);

		// If there are errors, return them
		if (!errors.isEmpty()) {
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append(new Gson().toJson(errors));
			return;
		}
		
		// Create a new UserDAO
		UserDAO uDAO = new UserDAO(this.connection);
		
		// Try to register the user
		try {
			uDAO.register(username, email, password, name, surname);
		} catch (SQLException e) {
			// If the username is already taken, return an error
			if (e.getSQLState().equals("23000")) { 
				errors.put("username", "Username is not available, please choose another one.");
				response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().append(new Gson().toJson(errors));
				return;
			}
			// If there is a database failure, return an error
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append("Database failure.");
			return;
		}

		// Try to login the user
		User user;
		try {
			user = uDAO.login(username, password);
		} catch (SQLException e) {
			response.setContentType("text/plain");
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// If the login fails, return an error
		if (user == null) {
			response.setContentType("text/plain");
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Set the user in the session and return the user
		request.getSession().setAttribute("user", user);

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(new Gson().toJson(user));
	}
	
	/**
	 * Validates the user input for registration.
	 * @param username The username of the user.
	 * @param name The name of the user.
	 * @param surname The surname of the user.
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @param confirmPassword The confirmation of the password.
	 * @return A map of errors, if any.
	 */
	private Map<String, String> validateInputs(String username, String name, String surname, String email, String password, String confirmPassword) {
		Map<String, String> errors = new HashMap<>();
		
		if (username == null || username.length() < 1 || username.length() > 45)
			errors.put("username", "Username must be at least 1 character long and shorter than 45 characters.");
		
		if (name == null || name.length() < 1 || name.length() > 45)
			errors.put("name", "Name must be at least 1 character long and shorter than 45 characters.");
		
		if (surname == null || surname.length() < 1 || surname.length() > 45)
			errors.put("surname", "Surname must be at least 1 character long and shorter than 45 characters.");
		
		if (email == null || !this.isValidEmail(email))
			errors.put("email", "Invalid email. Please insert a valid email.");

		if (password == null || password.length() < 4 || password.length() > 255)
			errors.put("password", "Password must be at least 4 characters long and shorter than 255 characters.");
		else if (!password.equals(confirmPassword))
			errors.put("confirmPassword", "The passwords do not match.");
		
		return errors;
	}
	
	/**
	 * Checks if the email is in a valid format.
	 * @param email The email to be checked.
	 * @return true if the email is valid, false if not.
	 */
	private boolean isValidEmail(String email) {
		String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		return Pattern.compile(emailPattern).matcher(email).matches();
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
