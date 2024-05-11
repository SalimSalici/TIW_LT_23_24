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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class UserLoggedFilter to redirect to login page if user is not logged in
 */
@WebFilter({"/home", "/inviteusers", "/creategroup", "/logout", "/cancellation", "/groupdetails"})
public class UserLoggedFilter extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpSession session = httpRequest.getSession();
        
		if (session.isNew() || session.getAttribute("user") == null) {
			((HttpServletResponse) response).sendRedirect(httpRequest.getContextPath() + "/auth");
			return;
		}
			
		chain.doFilter(request, response);
	}
}
