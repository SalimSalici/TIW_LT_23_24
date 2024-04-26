package it.polimi.tiw.test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/TestServlet")
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static int lol = 0;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		TestServlet.lol++;
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println("Hello this is a test " + TestServlet.lol);
		out.close();
	}
}