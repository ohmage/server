package edu.ucla.cens.awserver.jee.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Filter for protecting access to resources which require a logged-in user.
 * 
 * @author selsky
 */
public class AuthenticationFilter implements Filter {
	private static Logger _logger = Logger.getLogger(AuthenticationFilter.class);
	private String _loginRedirectUrl;
	private String _loginServletUrl;
	private String[] _ajaxUrls;
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public AuthenticationFilter() {
		
	}
	
	/**
	 * Does nothing.
	 */
	public void destroy() {
		
	}
	
	/**
	 * Initialize with properties from web.xml.
	 */
	public void init(FilterConfig config) throws ServletException {
		_loginRedirectUrl = config.getInitParameter("loginRedirectUrl");
		_loginServletUrl = config.getInitParameter("loginServletUrl");
		String ajaxUrls = config.getInitParameter("ajaxUrls");
		
		String filterName = config.getFilterName();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(_loginRedirectUrl)) {
			throw new ServletException("loginRedirectUrl init-param is required. Filter " + filterName + " cannot be initialized.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(_loginServletUrl)) {
			throw new ServletException("loginServletUrl init-param is required. Filter " + filterName + " cannot be initialized.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(ajaxUrls)) {
			throw new ServletException("ajaxUrls init-param is required. Filter " + filterName + " cannot be initialized.");
		}
		
		_ajaxUrls = ajaxUrls.split(",");
		Arrays.sort(_ajaxUrls);
	}
	
	/**
	 * Checks the HttpSession to determine whether the current user is logged in. If the user is logged in, does nothing. 
	 * If the user is not logged in and attempting to access a "non-login" resource, they are redirected to the login page.
	 * 
	 * The key for the attribute in the <code>HttpSession</code> that is checked is named <code>isLoggedIn</code>.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws ServletException, IOException {
	    
		// Check the HttpSession to see if a previous request already logged the current user in
		boolean isLoggedIn = false;
		User user = ((User) ((HttpServletRequest) request).getSession().getAttribute("user"));
		if(null != user) {
			isLoggedIn = user.isLoggedIn();
		}
		
		if(! isLoggedIn) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("request path: " + ((HttpServletRequest) request).getRequestURI());
			}
			
			// expired session, redirect to login page unless the user is attempting to login
			if(! ((HttpServletRequest) request).getRequestURI().startsWith(_loginServletUrl)
			    && ! ((HttpServletRequest) request).getRequestURI().startsWith(_loginRedirectUrl) ) { 
				
				// Check to see if the URL represents an AJAX call 
				if(-1 != Arrays.binarySearch(_ajaxUrls, ((HttpServletRequest) request).getRequestURI())) {
					
					Writer writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
					writer.write("{\"error_code\":\"0104\",\"error_text\":\"" + _loginRedirectUrl + "\"}");
					
				} else {
					
					_logger.info("redirecting user to login page for URL " + ((HttpServletRequest) request).getRequestURI());
					((HttpServletResponse) response).sendRedirect(_loginRedirectUrl);
				}
				
			} else {
				
				chain.doFilter(request, response);
			}
			
		} else { 
			// The user is logged in. Execute the rest of the filters in the chain and then whatever Servlet or JSP is bound 
			// to the current request URI
			chain.doFilter(request, response);
		}
	}
}
