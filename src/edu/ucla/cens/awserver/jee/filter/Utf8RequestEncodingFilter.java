package edu.ucla.cens.awserver.jee.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Force UTF-8 character encoding on HTTP requests.
 * 
 * @author selsky
 */
public class Utf8RequestEncodingFilter implements Filter {
	
	/**
	 * Default no-arg constructor.
	 */
	public Utf8RequestEncodingFilter() {
		
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
	 * Sets the character encoding on the request to be UTF-8.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws ServletException, IOException {	
		
		request.setCharacterEncoding("UTF-8");
		chain.doFilter(request, response);
		
	}
}
