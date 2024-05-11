package it.polimi.tiw.controllers;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.utils.ThymeleafInitializer;

/**
 * Servlet implementation class Error
 */
@WebServlet("/error")
public class Error extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	/**
	 * Initializes the servlet with necessary components like TemplateEngine.
	 * 
	 * @throws ServletException if an error occurs during initialization.
	 */
	@Override
	public void init() {
		this.templateEngine = ThymeleafInitializer.initialize(this.getServletContext());
	}

	/**
	 * Handles GET requests to the servlet.
	 * @param request The HTTP request object.
	 * @param response The HTTP response object.
	 * @throws ServletException if an error occurs during processing.
	 * @throws IOException if an error occurs during processing.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		String path = "error";
//		ServletContext servletContext = getServletContext();
//		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
//		templateEngine.process(path, ctx, response.getWriter());
		
		// Retrieve error code and message set by sendError()
	    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
	    String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

	    // Set defaults in case attributes are not set
	    if (statusCode == null) {
	        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	        errorMessage = "Something went wrong";
	    }

	    // Use the error code and message as needed in your error page
	    ServletContext servletContext = getServletContext();
	    final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
	    ctx.setVariable("statusCode", statusCode);
	    ctx.setVariable("errorMessage", errorMessage);

	    String path = "error";
	    templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
