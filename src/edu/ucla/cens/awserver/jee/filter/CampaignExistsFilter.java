package edu.ucla.cens.awserver.jee.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.ucla.cens.awserver.controller.Controller;
import edu.ucla.cens.awserver.controller.ControllerException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.datatransfer.AwRequestImpl;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Filter for determining if a user or device is attempting to access a subdomain that maps onto a campaign.
 *
 * @author selsky
 */
public class CampaignExistsFilter implements Filter {
	private static Logger _logger = Logger.getLogger(CampaignExistsFilter.class);
	private Controller _controller;
	
	
	/**
	 * Default no-arg constructor.
	 */
	public CampaignExistsFilter() {
		
	}
	
	/**
	 * Destroys instance variables.
	 */
	public void destroy() {
		_controller = null;
	}
	
	/**
	 * Looks for a Controller name (Spring bean id) in the FilterConfig and attempts to retrieve the Controller out of the Spring
	 * ApplicationContext.
	 * 
	 * @throws ServletException if an init-param named controllerName cannot be found in the FilterConfig
	 */
	public void init(FilterConfig config) throws ServletException {
		String filterName = config.getFilterName();
		String controllerName = config.getInitParameter("controllerName");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(controllerName)) {
			throw new ServletException("Invalid web.xml. Missing controllerName init param. Filter " + filterName +
					" cannot be initialized and put into service.");
		}
		
		ServletContext servletContext = config.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		_controller = (Controller) applicationContext.getBean(controllerName);
		
		_logger.info(filterName + " successfully put into service");
	}
	
	/**
	 * Checks that a user is hitting a valid request subdomain.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws ServletException, IOException {
		
		AwRequest awRequest = new AwRequestImpl();
		String url = ((HttpServletRequest) request).getRequestURL().toString();
		String uri = ((HttpServletRequest) request).getRequestURI();
		String subdomain = StringUtils.retrieveSubdomainFromUrlString(url);
		awRequest.setAttribute("subdomain", subdomain);
		
		try {
		
			_controller.execute(awRequest);
			
			if(Boolean.valueOf((String) awRequest.getAttribute("campaignExistsForSubdomain"))) {
				
				chain.doFilter(request, response);
				
			} else { // a subdomain that is not bound to a campaign was found
				
				if(uri.startsWith("/app/sensor")) { // a phone or device is attempting access
					
					String json = "{\"errors\":[{\"error_code\":\"0100\",\"error_text\":\"subdomain does not exist\"}]}"; // TODO - move to config file
					ServletOutputStream outputStream = response.getOutputStream();
					outputStream.print(json);
					outputStream.flush();
					
				} else { // assume it's a browser. Tomcat will return a custom 404 page if configured to do so.
					
					((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}	
		}
		catch(ControllerException ce) {
			_logger.error("", ce); // make sure the stack trace gets into our app log
			throw ce; // re-throw and allow Tomcat to redirect to the configured error page. the stack trace will also end up
			          // in catalina.out
		}
	}
}
