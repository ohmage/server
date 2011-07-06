package org.ohmage.jee.servlet;

import java.io.IOException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.request.Request;
import org.ohmage.request.RequestBuilder;

/**
 * Handler for all incoming HTTP requests.
 * 
 * @author John Jenkins
 */
@MultipartConfig(location="/tmp/", fileSizeThreshold=1024*1024, maxFileSize=1024*1024*5, maxRequestSize=1024*1024*5*5)
public class RequestServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(RequestServlet.class);
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor.
	 */
	public RequestServlet() {
		// Do nothing.
	}
	
	/**
	 * Processes a GET request. Only certain APIs may make a GET request. This
	 * should be handled in a different way than hard-coding the allowed GET
	 * APIs. 
	 */
	@Override
	protected final void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		if("/app/image/read".equals(httpRequest.getRequestURI())) {
			processRequest(httpRequest, httpResponse);
		}
		else if("/app/document/read/contents".equals(httpRequest.getRequestURI())) {
			processRequest(httpRequest, httpResponse);
		}
		else {
			LOGGER.warn("GET attempted and denied.");
			try {
				httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
			catch(IOException e) {
				LOGGER.error("Error while attempting to respond.", e);
				return;
			}
		}
	}
	
	/**
	 * Processes a POST request. All APIs may use a POST.
	 */
	@Override
	protected final void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		processRequest(httpRequest, httpResponse);
	}
	
	/**
	 * Builds a Request object. If building the object didn't fail, it will
	 * service the request. Finally, the request will respond.
	 * 
	 * @param httpRequest The HTTP request that is to be built, serviced, and
	 * 					  responded.
	 * 
	 * @param httpResponse The HTTP response that will be sent back to the user
	 * 					   once the request has been processed.
	 */
	protected void processRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		Request request = RequestBuilder.buildRequest(httpRequest);
		
		if(! request.isFailed()) {
			request.service();
		}
		
		request.respond(httpRequest, httpResponse);
	}
}