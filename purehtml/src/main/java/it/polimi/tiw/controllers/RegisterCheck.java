package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.RegisterFormValidation;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.DatabaseInitializer;

/**
 * Servlet implementation class RegisterCheck
 */
@WebServlet("/register")
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
		String username = request.getParameter("registerUsername");
		String name = request.getParameter("registerName");
		String surname = request.getParameter("registerSurname");
		String email = request.getParameter("registerEmail");
		String password = request.getParameter("registerPassword");
		String confirmPassword = request.getParameter("registerConfirmPassword");
		
		// Basic input validation (length of inputs, valid email, passwords match...)
		RegisterFormValidation validation = this.validateInputs(username, name, surname, email, password, confirmPassword);

		if (validation.hasErrors()) {
			request.getSession().setAttribute("registerValidation", validation);
			response.sendRedirect(request.getContextPath() + "/auth");
			return;
		}
		
		UserDAO uDAO = new UserDAO(this.connection);
		
		try {
			uDAO.register(username, email, password, name, surname);
		} catch (SQLException e) {
			if (e.getSQLState().equals("23000")) { // Duplicate entry (can only be username for this table)
				validation.setUsernameError("Username is not available, please choose another one.");
				request.getSession().setAttribute("registerValidation", validation);
				response.sendRedirect(request.getContextPath() + "/auth");
				return;
			}
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
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
		response.sendRedirect(request.getContextPath() + "/home");
	}
	
	private RegisterFormValidation validateInputs(String username, String name, String surname, String email, String password, String confirmPassword) {
		RegisterFormValidation validation = new RegisterFormValidation(username, name, surname, email);
		
		if (username.length() < 1 || username.length() > 45)
			validation.setUsernameError("Username must be at least 1 character long and shorter than 45 characters.");
		
		if (name.length() < 1 || name.length() > 45)
			validation.setNameError("Name must be at least 1 character long and shorter than 45 characters.");
		
		if (surname.length() < 1 || surname.length() > 45)
			validation.setSurnameError("Surname must be at least 1 character long and shorter than 45 characters.");
		
		if (!this.isValidEmail(email))
			validation.setEmailError("Invalid email format. Please insert a valid email.");

		if (password.length() < 4 || password.length() > 255)
			validation.setPasswordError("Password must be at least 4 characters long and shorter than 255 characters.");
		else if (!password.equals(confirmPassword))
			validation.setConfirmPasswordError("The passwords do not match.");
		
		return validation;
	}
	
	private boolean isValidEmail(String email) {
		String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		return Pattern.compile(emailPattern).matcher(email).matches();
	}

}
