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
 * Servlet implementation class RegisterCheck
 */
@WebServlet("/register")
@MultipartConfig
public class RegisterCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterCheck() {
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
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method not allowed.");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String username = request.getParameter("registerUsername");
		String name = request.getParameter("registerName");
		String surname = request.getParameter("registerSurname");
		String email = request.getParameter("registerEmail");
		String password = request.getParameter("registerPassword");
		String confirmPassword = request.getParameter("registerConfirmPassword");
		
		// Basic input validation (length of inputs, valid email, passwords match...)
		Map<String, String> errors = this.validateInputs(username, name, surname, email, password, confirmPassword);

		if (!errors.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append(new Gson().toJson(errors));
			return;
		}
		
		UserDAO uDAO = new UserDAO(this.connection);
		
		try {
			uDAO.register(username, email, password, name, surname);
		} catch (SQLException e) {
			if (e.getSQLState().equals("23000")) { // Duplicate entry (can only be username for this table)
				errors.put("username", "Username is not available, please choose another one.");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().append(new Gson().toJson(errors));
				return;
			}
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().append("Database failure.");
			return;
		}

		User user;
		try {
			user = uDAO.login(username, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		if (user == null) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		request.getSession().setAttribute("user", user);
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(new Gson().toJson(user));
	}
	
	private Map<String, String> validateInputs(String username, String name, String surname, String email, String password, String confirmPassword) {
		Map<String, String> errors = new HashMap<>();
		
		if (username.length() < 1 || username.length() > 45)
			errors.put("username", "Username must be at least 1 character long and shorter than 45 characters.");
		
		if (name.length() < 1 || name.length() > 45)
			errors.put("name", "Name must be at least 1 character long and shorter than 45 characters.");
		
		if (surname.length() < 1 || surname.length() > 45)
			errors.put("surname", "Surname must be at least 1 character long and shorter than 45 characters.");
		
		if (!this.isValidEmail(email))
			errors.put("email", "Invalid email format. Please insert a valid email.");

		if (password.length() < 4 || password.length() > 255)
			errors.put("password", "Password must be at least 4 characters long and shorter than 255 characters.");
		else if (!password.equals(confirmPassword))
			errors.put("confirmPassword", "The passwords do not match.");
		
		return errors;
	}
	
	private boolean isValidEmail(String email) {
		String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		return Pattern.compile(emailPattern).matcher(email).matches();
	}

}
