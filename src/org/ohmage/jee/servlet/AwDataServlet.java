/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.controller.Controller;
import org.ohmage.controller.ControllerException;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.jee.servlet.glue.AwRequestCreator;
import org.ohmage.jee.servlet.validator.HttpServletRequestValidator;
import org.ohmage.jee.servlet.validator.MissingAuthTokenException;
import org.ohmage.jee.servlet.writer.AbstractResponseWriter;
import org.ohmage.jee.servlet.writer.ResponseWriter;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ResultListAwRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.json.FailedJsonRequestAnnotator;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


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
			try {
				if(null != _httpServletRequestValidator && ! _httpServletRequestValidator.validate(request)) {
					
					response.sendError(HttpServletResponse.SC_NOT_FOUND); // if some entity is doing strange stuff, just respond with a 404
					                                                      // in order not to give away too much about how the app works
					return;
				}
			}
			catch(MissingAuthTokenException e) {
				_logger.info("Error response: " + e.getMessage());
				
				ErrorResponse authenticationFailedError = new ErrorResponse();
				authenticationFailedError.setCode("0200");
				authenticationFailedError.setText("authentication failed");
				
				FailedJsonRequestAnnotator annotator = new FailedJsonRequestAnnotator(authenticationFailedError);
				
				writeAuthTokenMissingMessage(request, response, annotator);
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
		else if("/app/document/read/contents".equals(req.getRequestURI())) {
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
	
	private void writeAuthTokenMissingMessage(HttpServletRequest request, HttpServletResponse response, FailedJsonRequestAnnotator annotator) {
		_logger.info("Writing 'authentication token missing or invalid' response.");
		
		Writer writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(AbstractResponseWriter.getOutputStream(request, response)));
		}
		catch(IOException e) {
			_logger.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		AbstractResponseWriter.expireResponse(response);
		response.setContentType("application/json");
		
		AwRequest awRequest = new ResultListAwRequest();
		annotator.annotate(awRequest, "Responding with an authentication failed message.");
		String responseText = awRequest.getFailedRequestErrorMessage();
		
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			_logger.error("Unable to write failed response message. Aborting.", e);
			return;
		}
		
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			_logger.error("Unable to flush or close the writer.", e);
		}
	}
}
