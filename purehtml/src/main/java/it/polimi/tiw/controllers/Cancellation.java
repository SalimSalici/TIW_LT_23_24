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
 * Servlet implementation class Cancellation that displays the cancellation page (after a user's third try of creating a group).
 */
@WebServlet("/cancellation")
public class Cancellation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
    
	/**
	 * Initializes the servlet with necessary components like TemplateEngine.
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
		String path = "cancellation";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}
}
