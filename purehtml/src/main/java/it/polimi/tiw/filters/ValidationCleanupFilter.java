package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class AuthValidationCleanupFilter to clean up the createGroupValidation attribute
 * from the session after the it has been used by the Home servlet
 */
@WebFilter({"/home"})
public class ValidationCleanupFilter extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, response);

		// After processing the request

		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			// Get the session if it exists, do not create if it doesn't
			HttpSession session = req.getSession(false);
			if (session != null) {
				session.removeAttribute("createGroupValidation");
			}
		}
	}
}
