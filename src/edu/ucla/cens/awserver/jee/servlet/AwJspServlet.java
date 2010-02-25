package edu.ucla.cens.awserver.jee.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.ucla.cens.awserver.controller.Controller;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.jee.servlet.glue.AwRequestCreator;
import edu.ucla.cens.awserver.jee.servlet.glue.HttpSessionModifier;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Dispatch and handle requests which ultimately result in a final redirect to a JSP for rendering. 
 * 
 * This Servlet has four init-params that need to be defined in web.xml. The parameters define objects used by the service() 
 * method when handling user requests.
 * 
 * <ol>
 *   <li><code>controllerName</code> -- The Spring Bean id of the Controller to use.
 *   <li><code>awRequestCreatorName</code> -- The Spring Bean id of the AwRequestCreator to use.
 *   <li><code>httpSessionModifierName</code> -- The Spring Bean id of the optional HttpSessionModifier to use. 
 *   <li><code>successfulRequestRedirectUrl</code> -- A relative (to WEB-INF) file URL to a JSP that will render the results of a 
 * successful request.  
 *   <li><code>failedRequestRedirectUrl</code> -- A relative (to WEB-INF) file URL to a JSP that will render the results of a failed
 * request.  
 * </ol>
 * 
 * @see Controller
 * @see AwRequest
 * @see AwRequestCreator
 * @see HttpSessionModifier
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class AwJspServlet extends AbstractAwHttpServlet {
	private static Logger _logger = Logger.getLogger(AwJspServlet.class);
	private Controller _controller;
	private String _successfulRequestRedirectUrl;
	private String _failedRequestRedirectUrl;
	private AwRequestCreator _awRequestCreator;
	private HttpSessionModifier _httpSessionModifier;
	
	/**
	 * Default no-arg constructor.
	 */
	public AwJspServlet() {
		
	}
		
	/**
	 * JavaEE-to-Spring glue code. When the web application starts up, the init method on all servlets is invoked by the Servlet 
	 * container (if load-on-startup for the Servlet > 0). In this method, names of Spring "beans" are pulled out of the 
	 * ServletConfig and the names are used to retrieve the beans out of the ApplicationContext. The basic design rule followed
	 * is that only Servlet.init methods contain Spring Framework glue code.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String servletName = config.getServletName();
		
		// Note that these names are actually Spring Bean ids, not FQCNs
		String controllerName = config.getInitParameter("controllerName");
		String awRequestCreatorName = config.getInitParameter("awRequestCreatorName");
		String httpSessionModifierName = config.getInitParameter("httpSessionModifierName");
		String successfulRequestRedirectUrl = config.getInitParameter("successfulRequestRedirectUrl");
		String failedRequestRedirectUrl = config.getInitParameter("failedRequestRedirectUrl");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(controllerName)) {
			throw new ServletException("Invalid web.xml. Missing controllerName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequestCreatorName)) {
			throw new ServletException("Invalid web.xml. Missing awRequestCreatorName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(httpSessionModifierName)) {
			_logger.info("Servet " + servletName + " configured without an AW to HTTP Request copier");
    	}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(successfulRequestRedirectUrl)) {
			throw new ServletException("Invalid web.xml. Missing redirectUrl init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
				
		_successfulRequestRedirectUrl = successfulRequestRedirectUrl;
		_failedRequestRedirectUrl = failedRequestRedirectUrl;
		
		// OK, now get the beans out of the Spring ApplicationContext
		// If the beans do not exist within the Spring configuration, Spring will throw a RuntimeException and initialization
		// of this Servlet will fail. (check catalina.out in addition to aw.log)
		ServletContext servletContext = config.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		
		_controller = (Controller) applicationContext.getBean(controllerName);
		_awRequestCreator = (AwRequestCreator) applicationContext.getBean(awRequestCreatorName);
		if(null != httpSessionModifierName) {
			_httpSessionModifier = (HttpSessionModifier) applicationContext.getBean(httpSessionModifierName);
		}	
	}
	
	/**
	 * Services the user requests to the URLs bound to this Servlet as configured in web.xml. 
	 * 
	 * Performs the following steps:
	 * <ol>
	 * <li>Maps HTTP request parameters into an AwRequest.
	 * <li>Passes the AwRequest to a Controller.
	 * <li>Places the results of the controller action into the HTTP Session if the instance variable HttpSessionModifier exists.
	 * <li>Redirects to a JSP for rendering of the feature results.
	 * </ol>
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException { // allow Tomcat to handle Servlet and IO Exceptions
		
		// Map data from the inbound request to our internal format
		AwRequest awRequest = _awRequestCreator.createFrom(request);
	    
		try {
			// Execute feature-specific logic
			_controller.execute(awRequest);
		    
			// Map data from the request into the HttpSession for later use or for rendering within a JSP
			if(null != _httpSessionModifier) {
				_httpSessionModifier.modifySession(awRequest, request.getSession());
			}
			
			// Redirect to JSP
			if(awRequest.isFailedRequest()) {

				response.sendRedirect(_failedRequestRedirectUrl);
				
			} else {
				
				response.sendRedirect(_successfulRequestRedirectUrl);
			}
		}
		
		catch(Throwable t) { 
			
			_logger.error("an unrecoverable error occurred", t); // make sure the stack trace gets into our app log
			throw new ServletException(t); // Re-throw and allow Tomcat to redirect to the configured error page. 
			                               // The stack trace will also end up in catalina.out
			
		}
	}
	
	/**
	 * Dispatches to processRequest().
	 */
	@Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		processRequest(req, resp);

	}

	/**
	 * Dispatches to processRequest().
	 */
	@Override protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
    
		processRequest(req, resp);
	
	}
	
}
