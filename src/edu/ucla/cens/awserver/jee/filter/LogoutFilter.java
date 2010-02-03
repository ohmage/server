package edu.ucla.cens.awserver.jee.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutFilter implements Filter {
	
	/**
	 * Default no-arg constructor.
	 */
	public LogoutFilter() {
		
	}
	
	/**
	 * Does nothing.
	 */
	public void destroy() {
		
	}
	
	/**
	 * Does nothing.
	 */
	public void init(FilterConfig config) throws ServletException {
		
	}
	
	/**
	 * Destroys the user's HttpSession and redirect them to the login page.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws ServletException, IOException {	
		
		HttpSession session = ((HttpServletRequest) request).getSession(); 
		String subdomain = (String) session.getAttribute("subdomain");
		String serverName = (String) session.getAttribute("serverName"); 
		session.invalidate();
		((HttpServletResponse) response).sendRedirect("http://" + subdomain + "." + serverName + ".cens.ucla.edu/app/login.jsp");
		
	}
}
