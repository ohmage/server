package edu.ucla.cens.awserver.jee.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.ucla.cens.awserver.controller.Controller;
import edu.ucla.cens.awserver.controller.ControllerException;
import edu.ucla.cens.awserver.jee.servlet.glue.AwRequestCreator;
import edu.ucla.cens.awserver.jee.servlet.validator.HttpServletRequestValidator;
import edu.ucla.cens.awserver.jee.servlet.writer.ResponseWriter;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Servlet for responding to requests for JSON data. The data Writer is configurable, so this class may also be used for emitting
 * data that is formatted in other ways (like XML or HTML).
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class AwDataServlet extends HttpServlet {
	private static Logger _logger = Logger.getLogger(AwDataServlet.class);
	
	private AwRequestCreator _awRequestCreator;
	private Controller _controller;
	private HttpServletRequestValidator _httpServletRequestValidator;
	private ResponseWriter _responseWriter;
	
	/**
	 * Default no-arg constructor.
	 */
	public AwDataServlet() {
	
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
		String httpServletRequestValidatorName = config.getInitParameter("httpServletRequestValidatorName");
		String responseWriterName = config.getInitParameter("responseWriterName");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(controllerName)) {
			throw new ServletException("Invalid web.xml. Missing controllerName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequestCreatorName)) {
			throw new ServletException("Invalid web.xml. Missing awRequestCreatorName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(responseWriterName)) {
			throw new ServletException("Invalid web.xml. Missing responseWriterName init param. Servlet " + 
					servletName + " cannot be initialized and put into service.");
		}

				
		// OK, now get the beans out of the Spring ApplicationContext
		// If the beans do not exist within the Spring configuration, Spring will throw a RuntimeException and initialization
		// of this Servlet will fail. (check catalina.out in addition to aw.log)
		ServletContext servletContext = config.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		
		_controller = (Controller) applicationContext.getBean(controllerName);
		_awRequestCreator = (AwRequestCreator) applicationContext.getBean(awRequestCreatorName);
		
		if(null != httpServletRequestValidatorName) {
			_httpServletRequestValidator = (HttpServletRequestValidator) applicationContext.getBean(httpServletRequestValidatorName);
		}
		_responseWriter = (ResponseWriter) applicationContext.getBean(responseWriterName);
		
	}
	
	/**
	 * Services the user requests to the URLs bound to this Servlet as configured in web.xml. 
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException { // allow Tomcat to handle Servlet and IO Exceptions
		
		AwRequest awRequest = null;
		
		try {
			
			// Top-level security validation (if configured with a validator)
			if(null != _httpServletRequestValidator && ! _httpServletRequestValidator.validate(request)) {
				
				response.sendError(HttpServletResponse.SC_NOT_FOUND); // if some entity is doing strange stuff, just respond with a 404
				                                                      // in order not to give away too much about how the app works
				return;
			}
			
			// Map data from the inbound request to our internal format
			awRequest = _awRequestCreator.createFrom(request);
			
		} catch(IllegalStateException ise) {
			
			_logger.error("caught IllegalStateException", ise);
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // return a 404 in order to avoid giant stack traces returned to
			                                                      // the client in the case of throwing a ServletException
			return;
		}
		
		try {
			
			// Execute feature-specific logic
			_controller.execute(awRequest);
			
		} catch (ControllerException ce) {
			
			_logger.error("caught ControllerException", ce);
			
			if(! awRequest.isFailedRequest()) { // this is bad because it means an error occurred and the code didn't mark up 
				                                // the awRequest correctly
				_logger.warn("caught a ControllerException where the awRequest was not marked as failed");
				awRequest.setFailedRequest(true);
			}
		}
		
		// Invalidate the session
		request.getSession().invalidate();
		
		// Write the output
		_responseWriter.write(request, response, awRequest);
	}
	
	/**
	 * Dispatches to processRequest().
	 */
	@Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		
		if("/app/image/read".equals(req.getRequestURI())) {
			processRequest(req, resp);
		}
		else {
			_logger.warn("GET attempted and denied.");
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Dispatches to processRequest().
	 */
	@Override protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		
		processRequest(req, resp);
	
	}
}
