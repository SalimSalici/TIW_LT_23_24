package it.polimi.tiw.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.formbeans.RegisterFormValidation;
import it.polimi.tiw.utils.DatabaseInitializer;
import it.polimi.tiw.utils.ThymeleafInitializer;

/**
 * Servlet implementation class Auth that handles authentication processes like login and registration.
 */
@WebServlet("/auth")
public class Auth extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;

	/**
	 * Initializes the servlet with necessary components like TemplateEngine and Database connection.
	 * @throws ServletException if an error occurs during initialization.
	 */
	public void init() throws ServletException {
		this.templateEngine = ThymeleafInitializer.initialize(this.getServletContext());
		this.connection = DatabaseInitializer.initialize(this.getServletContext());
	}

	/**
	 * Handles GET requests to display the authentication page.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = "auth";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("rememberMe"))
				ctx.setVariable("rememberMe", cookie.getValue());
		}
		
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * Handles POST requests to process authentication actions like login and registration.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String authAction = request.getParameter("authAction");
		if (authAction == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request.");
			return;
		}
		
		switch (authAction) {
			case "login":
				this.doLogin(request, response);
				break;
			case "register":
				this.doRegister(request, response);
				break;
			default:
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request.");
				break;
		}
	}
	
	/**
	 * Processes the login request.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	private void doLogin(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("loginUsername");
		String password = request.getParameter("loginPassword");
		
		// Check if username or password is missing
		if (username == null || password == null) {
			request.setAttribute("loginErrorMessage", "Missing parameters.");
		    this.doGet(request, response);
		    return;
		}
		
		User user;
		try {
			// Attempt to authenticate the user
			user = new UserDAO(this.connection).login(username, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking.");
			return;
		}
		
		// Check if user authentication was successful
		if (user == null) {
	        request.setAttribute("loginErrorMessage", "Wrong username or password.");
		    this.doGet(request, response);
		    return;
		}
		
		// Set cookie for "Remember username" feature
		Cookie rememberMeCookie = new Cookie("rememberMe", URLEncoder.encode(username, "UTF-8"));
		if (request.getParameter("rememberMe") != null) {
		    rememberMeCookie.setMaxAge(60 * 60 * 24 * 30);
		} else {
			rememberMeCookie.setMaxAge(0);
		}
		response.addCookie(rememberMeCookie);
		
		// Set user session and redirect to home page
		request.getSession().setAttribute("user", user);
		response.sendRedirect(request.getContextPath() + "/home");
	}
	
	/**
	 * Processes the registration request.
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException if a servlet-specific error occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	private void doRegister(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("registerUsername");
		String name = request.getParameter("registerName");
		String surname = request.getParameter("registerSurname");
		String email = request.getParameter("registerEmail");
		String password = request.getParameter("registerPassword");
		String confirmPassword = request.getParameter("registerConfirmPassword");
		
		// Check if any registration parameters are missing
		if (username == null || name == null || surname == null || email == null || password == null || confirmPassword == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing registration parameters.");
			return;
		}
		
		// Validate registration inputs
		RegisterFormValidation validation = this.validateInputs(username, name, surname, email, password, confirmPassword);

		// If validation fails, return to registration page with errors
		if (validation.hasErrors()) {
			request.setAttribute("registerValidation", validation);
			this.doGet(request, response);
			return;
		}
		
		UserDAO uDAO = new UserDAO(this.connection);
		
		try {
			// Attempt to register the user
			uDAO.register(username, email, password, name, surname);
		} catch (SQLException e) {
			if (e.getSQLState().equals("23000")) {
				validation.setUsernameError("Username is not available, please choose another one.");
				request.setAttribute("registerValidation", validation);
				this.doGet(request, response);
				return;
			}
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		

		User user;
		try {
			// Log in the newly registered user
			user = uDAO.login(username, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Check if user was successfully logged in
		if (user == null) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Database failure.");
			return;
		}
		
		// Set user session and redirect to home page
		request.getSession().setAttribute("user", user);
		response.sendRedirect(request.getContextPath() + "/home");
	}
	/**
	 * Validates the registration input fields.
	 * @param username The user's username.
	 * @param name The user's first name.
	 * @param surname The user's surname.
	 * @param email The user's email.
	 * @param password The user's password.
	 * @param confirmPassword The user's confirmed password.
	 * @return A RegisterFormValidation object containing any validation errors.
	 */
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
	
	/**
	 * Validates the format of an email address.
	 * @param email The email to validate.
	 * @return true if the email is valid, false otherwise.
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
