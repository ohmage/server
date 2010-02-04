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

import org.apache.log4j.Logger;

/**
 * Filter for logging out a user from AW. 
 * 
 * @author selsky
 */
public class LogoutFilter implements Filter {
	private static Logger _logger = Logger.getLogger(LogoutFilter.class);
	
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
	 * Destroys the user's HttpSession and redirect them to the login page. If a non-logged in user is attempting to logout, the
	 * user is redirected to the index page. Does not invoke chain.doFilter(). 
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws ServletException, IOException {	
		
		HttpSession session = ((HttpServletRequest) request).getSession();
		
		if(session.isNew()) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("non-logged in user or bot hitting /app/logout. redirecting to index.html");
			}
			
			((HttpServletResponse) response).sendRedirect("/index.html");
			
		} else {
			
			session.invalidate();
			((HttpServletResponse) response).sendRedirect("/app/login.jsp");
			
		}
	}
}
