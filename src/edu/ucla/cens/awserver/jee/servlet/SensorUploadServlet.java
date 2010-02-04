package edu.ucla.cens.awserver.jee.servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.ucla.cens.awserver.controller.Controller;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.jee.servlet.glue.AwRequestCreator;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Servlet for processing sensor data uploads.
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class SensorUploadServlet extends AbstractAwHttpServlet {
	private static Logger _logger = Logger.getLogger(SensorUploadServlet.class);
	private Controller _controller;
	private AwRequestCreator _awRequestCreator;
	
	/**
	 * Default no-arg constructor.
	 */
	public SensorUploadServlet() {
		
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
		
		String awRequestCreatorName = config.getInitParameter("awRequestCreatorName");
		String controllerName = config.getInitParameter("controllerName");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequestCreatorName)) {
			throw new ServletException("Invalid web.xml. Missing awRequestCreatorName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(controllerName)) {
			throw new ServletException("Invalid web.xml. Missing controllerName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		
		
		// OK, now get the beans out of the Spring ApplicationContext
		// If the beans do not exist within the Spring configuration, Spring will throw a RuntimeException and initialization
		// of this Servlet will fail. (check catalina.out in addition to aw.log)
		ServletContext servletContext = config.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		
		_awRequestCreator = (AwRequestCreator) applicationContext.getBean(awRequestCreatorName);
		_controller = (Controller) applicationContext.getBean(controllerName);
 		
	}
	
	/**
	 * Dispatches to a Controller to perform sensor data upload. If the upload fails, an error message is persisted to the response.
	 * If the request is successful, allow Tomcat to simply return HTTP 200.
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException { 
		
		// Map data from the inbound request to our internal format
		AwRequest awRequest = _awRequestCreator.createFrom(request);
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
	    
		try {
			// Execute feature-specific logic
			_controller.execute(awRequest);
		    
			if(awRequest.isFailedRequest()) { 
				
				response.setContentType("application/json");
				writer.write(awRequest.getFailedRequestErrorMessage());
			} 
			// if the request is successful, just let Tomcat return a 200
		}
		
		catch(Throwable t) { 
			
			_logger.error("error occurred on sensor data upload", t);
			writer.write("{\"error_code\":\"0103\",\"error_text\":\"" + t.getMessage() + "\"}");
		}
		
		finally {
			
			if(null != writer) {
				writer.flush();
				writer.close();
			}
			
			request.getSession().invalidate(); // sensor data uploads only have state for the duration of a request
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
