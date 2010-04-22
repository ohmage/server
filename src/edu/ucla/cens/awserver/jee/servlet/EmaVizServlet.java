package edu.ucla.cens.awserver.jee.servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.ucla.cens.awserver.controller.Controller;
import edu.ucla.cens.awserver.dao.EmaQueryDao.EmaQueryResult;
import edu.ucla.cens.awserver.jee.servlet.glue.AwRequestCreator;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Servlet for responding to requests for EMA visualization query execution.
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class EmaVizServlet extends AbstractAwHttpServlet {
	private static Logger _logger = Logger.getLogger(EmaVizServlet.class);
	private Controller _controller;
	private AwRequestCreator _awRequestCreator;
	private List<String> _parameterList;
	
	/**
	 * Default no-arg constructor.
	 */
	public EmaVizServlet() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"s","e"}));
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
	}
	
	/**
	 * Services the user requests to the URLs bound to this Servlet as configured in web.xml. 
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException { // allow Tomcat to handle Servlet and IO Exceptions
		
		// Top-level security validation
		if(! prevalidate(request)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // if some entity is doing strange stuff, just respond with a 404
			                                                      // in order not to give away too much about how the app works
			return;
		}
		
		// Map data from the inbound request to our internal format
		AwRequest awRequest = _awRequestCreator.createFrom(request);
		Writer writer = null;
	    
		try {
			// Execute feature-specific logic
			_controller.execute(awRequest);
							
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			expireResponse(response);
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				// Convert the results to JSON for output.
				List<?> results =  awRequest.getResultList();
				JSONArray jsonArray = new JSONArray();
					
				for(int i = 0; i < results.size(); i++) {
					EmaQueryResult result = (EmaQueryResult) results.get(i);
					JSONObject entry = new JSONObject();	
					entry.put("response", new JSONObject(result.getJsonData()).get("response"));
					entry.put("time", result.getTimestamp());
					entry.put("timezone", result.getTimezone());
					entry.put("prompt_id", result.getPromptConfigId());
					entry.put("prompt_group_id", result.getPromptGroupId());
					jsonArray.put(entry);
				}
				
				responseText = jsonArray.toString();
				
			} else {
				
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			
			_logger.info("finished processing and about to write output");
			
			// Write the ouptut
			writer.write(responseText);
		}
		
		catch(Exception e) { 
			
			_logger.error("an unrecoverable exception occurred while running an EMA query", e);
			writer.write("{\"code\":\"0103\",\"text\":\"" + e.getMessage() + "\"}");
			
		} finally {
			
			if(null != writer) {
				writer.flush();
				writer.close();
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
	
	/**
	 * Pre-validate to avoid situations where someone is sending purposefully malicious data 
	 */
	private boolean prevalidate(HttpServletRequest request) {
		Map<?,?> parameterMap = request.getParameterMap(); // String, String[]
		
		// Check for missing or extra parameters
		
		if(parameterMap.size() != 2) {
			_logger.warn("an incorrect number of parameters was found for an EMA viz query: " + parameterMap.size());
			return false;
		}
		
		// Check for duplicate parameters
		
		Iterator<?> iterator = parameterMap.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String[] valuesForKey = (String[]) parameterMap.get(key);
			
			if(valuesForKey.length != 1) {
				_logger.warn("an incorrect number of values (" + valuesForKey.length + ") was found for parameter " + key);
				return false;
			}
		}
		
		// Check for parameters with unknown names
		
		iterator = parameterMap.keySet().iterator(); // there is no way to reset the iterator so just obtain a new one
		
		while(iterator.hasNext()) {
			String name = (String) iterator.next();
			if(! _parameterList.contains(name)) {
			
				_logger.warn("an incorrect parameter name was found: " + name);
				return false;
			}
		}
		
		String s = (String) request.getParameter("s");
		String e = (String) request.getParameter("e");
		
		// Check for abnormal lengths (buffer overflow attack)
		// 50 is an arbitrary number for the length, but it would be very strange
		
		if(greaterThanLength("startDate", "s", s, 50) || greaterThanLength("endDate", "e", e, 50)) { 
			return false;
		}
		
		return true;
	}
	
}
