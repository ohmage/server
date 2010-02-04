package edu.ucla.cens.awserver.jee.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.ucla.cens.awserver.controller.Controller;
import edu.ucla.cens.awserver.controller.ControllerException;
import edu.ucla.cens.awserver.dao.EmaQueryDao.EmaQueryResult;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.jee.servlet.glue.AwRequestCreator;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
@SuppressWarnings("serial") 
public class EmaVizServlet extends HttpServlet {
	private static Logger _logger = Logger.getLogger(EmaVizServlet.class);
	private Controller _controller;
	private AwRequestCreator _awRequestCreator;
	
	/**
	 * Default no-arg constructor.
	 */
	public EmaVizServlet() {
		
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
		
		if(StringUtils.isEmptyOrWhitespaceOnly(controllerName)) {
			throw new ServletException("Invalid web.xml. Missing controllerName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(awRequestCreatorName)) {
			throw new ServletException("Invalid web.xml. Missing awRequestCreatorName init param. Servlet " + servletName +
					" cannot be initialized and put into service.");
		}
				
		// OK, now get the beans out of the Spring ApplicationContext
		// If the beans do not exist within the Spring configuration, Spring will throw a RuntimeException and initialization
		// of this Servlet will fail. (check catalina.out in addition to aw.log)
		ServletContext servletContext = config.getServletContext();
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		
		_controller = (Controller) applicationContext.getBean(controllerName);
		_awRequestCreator = (AwRequestCreator) applicationContext.getBean(awRequestCreatorName);
		
		_logger.info("Servlet " + servletName + " successfully put into service");
	}
	
	/**
	 * Services the user requests to the URLs bound to this Servlet as configured in web.xml. 
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException { // allow Tomcat to handle Servlet and IO Exceptions
		
		ServletOutputStream sos = null; 
		
		// Map data from the inbound request to our internal format
		AwRequest awRequest = _awRequestCreator.createFrom(request);
		
	    
		try {
			// Execute feature-specific logic
			_controller.execute(awRequest);
							
			if(! awRequest.isFailedRequest()) {
				// Convert the results to JSON for output.
				List<EmaQueryResult> results = (List<EmaQueryResult>) awRequest.getAttribute("emaQueryResults");
				JSONArray jsonArray = new JSONArray();
				try {
					
					for(EmaQueryResult result : results) {
						JSONObject entry = new JSONObject();	
						entry.put("response", new JSONObject(result.getJsonData()).get("response"));
						entry.put("time", result.getTimestamp());
						entry.put("timezone", result.getTimezone());
						jsonArray.put(entry);
					}
					
					sos = response.getOutputStream();
					sos.print(jsonArray.toString(4));
						
				} catch(JSONException jsone) {
					
					throw new ServletException(jsone);
				}	
			} else {
				
				sos = response.getOutputStream();
				sos.print(awRequest.getFailedRequestErrorMessage());
			}
		}
		
		catch(ControllerException ce) { 
			
			_logger.error("", ce); // make sure the stack trace gets into our app log
			throw ce; // re-throw and allow Tomcat to redirect to the configured error page. the stack trace will also end up
			          // in catalina.out
		} finally {
			
			if(null != sos) {
				sos.flush();
				sos.close();
			}
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
