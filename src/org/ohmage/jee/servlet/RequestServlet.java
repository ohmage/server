package org.ohmage.jee.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.RequestBuilder;
import org.ohmage.request.UserRequest;
import org.ohmage.service.AuditServices;

/**
 * Handler for all incoming HTTP requests.
 * 
 * @author John Jenkins
 */
// Note: maxFileSize refers to PUTs whereas maxRequestSize refers to the max 
// size of a multipart/form-data POST which doesn't differentiate between the
// different parameters. Therefore, there is no way through this call to limit
// individual file uploads via POST.
@MultipartConfig(location="/tmp/", maxFileSize=1024*1024*5, maxRequestSize=1024*1024*5*5, fileSizeThreshold=1024*1024*5*5 + 1)
public class RequestServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(RequestServlet.class);
	
	private static final int MAX_DATABASE_LENGTH = (1024 * 16) - 1;
	
	private static final String PASSWORD_OMITTED = "omitted";
	private static final String LONG_VALUE_OMITTED = "<<<Value exeeded " + MAX_DATABASE_LENGTH + " characters.>>>";
	private static final String ELLIPSE = "...";
	
	private static final String KEY_DEVICE_ID = "device_id";
	
	private static final String KEY_ATTRIBUTE = "_ohmage_request_";
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The different possible HTTP request types.
	 *  
	 * @author John Jenkins
	 */
	public static enum RequestType { POST, GET, OPTIONS, HEAD, PUT, DELETE, TRACE, UNKNOWN };
	
	/**
	 * This will simply insert the audit information as gathered by the 
	 * constructor into the database. This is being done in a thread to not
	 * delay the response to the client.
	 * 
	 * @author John Jenkins
	 */
	private final class AuditThread extends Thread {
		private final Request request;
		
		private final RequestType requestType;
		private final String uri;
		
		private final Map<String, String[]> parameterMap;
		private final Map<String, String[]> headerMap;

		private final long receivedTimestamp;
		private final long respondTimestamp;
		
		/**
		 * Creates an object to hold the information necessary to create an
		 * audit entry.
		 * 
		 * @param requestType The RequestType for the request being audited.
		 * 
		 * @param uri The URI of the request being audited.
		 * 
		 * @param parameterMap A map of parameter keys to all values given for
		 * 					   all of the parameters passed into this request.
		 * 
		 * @param headerMap A map of all header keys to all values given for
		 * 					all of the headers passed into this request.
		 * 
		 * @param receivedTimestamp The timestamp at which the request was 
		 * 							received by the same measure as 
		 * 							'respondTimestamp'.
		 * 
		 * @param respondTimestamp The timestamp at which the request was fully
		 * 						   responded to by the same measure as
		 * 						   'receivedTimestamp'.
		 */
		public AuditThread(
				final Request request,
				final RequestType requestType,
				final String uri,
				final Map<String, String[]> parameterMap,
				final Map<String, String[]> headerMap,
				final long receivedTimestamp, 
				final long respondTimestamp) {
			
			this.request = request;
			
			this.requestType = requestType;
			this.uri = uri;
			
			this.parameterMap = parameterMap;
			this.headerMap = headerMap;
			
			this.receivedTimestamp = receivedTimestamp;
			this.respondTimestamp = respondTimestamp;
		}
		
		/**
		 * Creates the entry in the audit table.
		 */
		@Override
		public void run() {
			try {
				// We remove any uploaded to data to avoid storing personal or
				// sensitive data in the audit table.
				parameterMap.remove(InputKeys.DATA);
				
				// Go through the parameters and remove all values that are
				// greater than 64kB because the database will reject it.
				for(String key : parameterMap.keySet()) {
					String[] values = parameterMap.get(key);
					
					// If it is a password or new_password, we mask it to avoid
					// accidentally storing any passwords in the database,
					// except in the user table.
					if(key.equals(InputKeys.PASSWORD) || 
							key.equals(InputKeys.NEW_PASSWORD)) {
						for(int i = 0; i < values.length; i++) {
							values[i] = PASSWORD_OMITTED;
						}
					}
					else {
						for(int i = 0; i < values.length; i++) {
							if(values[i].length() > MAX_DATABASE_LENGTH) {
								values[i] = LONG_VALUE_OMITTED;
							}
						}
					}
				}
				
				// Retrieve the device ID. If any number of device IDs exist,
				// the first one reported will be used.
				String deviceId = null;
				String[] deviceIds = parameterMap.get(KEY_DEVICE_ID);
				if((deviceIds != null) && (deviceIds.length == 1)) {
					deviceId = deviceIds[0];
				}
				
				// Create a result object based on whether or not the request
				// succeeded.
				String responseString = Request.RESPONSE_SUCCESS_JSON_TEXT;
				if(request == null) {
					responseString = Request.RESPONSE_ERROR_JSON_TEXT;
				}
				else if(request.isFailed()) {
					responseString = request.getFailureMessage();
					
					if(responseString.length() > MAX_DATABASE_LENGTH) {
						responseString = responseString.substring(0, MAX_DATABASE_LENGTH - 3) + ELLIPSE;
					}
				}
				
				// Generate an 'extras' Map based on the HTTP headers.
				Map<String, String[]> extras = headerMap;
				
				// Get any extras from the request.
				String client = null;
				if(request != null) {
					Map<String, String[]> requestExtras = request.getAuditInformation();
					if(requestExtras != null) {
						extras.putAll(requestExtras);
					}
					
					if(request instanceof UserRequest) {
						client = ((UserRequest) request).getClient();
					}
				}
				
				// Create the audit report.
				AuditServices.createAudit(requestType, uri, client, deviceId, responseString, parameterMap, extras, receivedTimestamp, respondTimestamp);
			}
			catch(IllegalArgumentException e) {
				LOGGER.error("Error while auditing the request.", e);
			}
			catch(IllegalStateException e) {
				LOGGER.error("Error while auditing the request.", e);
			}
			catch(ServiceException e) {
				LOGGER.error("Error while auditing the request.", e);
			}
		}
	}
	
	/**
	 * This injects itself between Tomcat and our request servicing components,
	 * so that we can audit all incoming requests.
	 */
	@Override
	protected final void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
		// Get the moment we received the request.
		long receivedTimestamp = System.currentTimeMillis();
		
		// Service the request by calling the appropriate getXXX() method.
		super.service(httpRequest, httpResponse);
		
		// Get the moment we have completed 
		long respondedTimestamp = System.currentTimeMillis();
		
		// Report how long the request took.
		LOGGER.info("Time to process '" + httpRequest.getRequestURI() + "':"+ (respondedTimestamp - receivedTimestamp));
		
		// Retrieve the type of request, GET, POST, etc.
		RequestType requestType;
		try {
			requestType = RequestType.valueOf(httpRequest.getMethod());
		}
		catch(IllegalArgumentException e) {
			requestType = RequestType.UNKNOWN;
		}
		
		// Retrieve the request's URI.
		String uri = httpRequest.getRequestURI();
		
		// Generate an 'extras' Map based on the HTTP headers.
		Map<String, String[]> extras = new HashMap<String, String[]>();
		Enumeration<String> headers = httpRequest.getHeaderNames();
		while(headers.hasMoreElements()) {
			String header = headers.nextElement();
			
			List<String> valueList = new LinkedList<String>();
			Enumeration<String> values = httpRequest.getHeaders(header);
			while(values.hasMoreElements()) {
				valueList.add(values.nextElement());
			}
			
			extras.put(header, valueList.toArray(new String[0]));
		}
		
		// This is the parameter map that will be taken from the request if
		// available; otherwise, it will be taken from the user request.
		Map<String, String[]> parameterMap;
		
		Object requestObject = httpRequest.getAttribute(KEY_ATTRIBUTE);
		Request request = null;
		if(requestObject != null) {
			request = (Request) requestObject;
			
			parameterMap = new HashMap<String, String[]>(request.getParameterMap());
		}
		else {
			// Retrieve the request's parameter map. We must make a copy 
			// because as soon as this function exists Tomcat will begin 
			// destroying the original parameter map.
			parameterMap = new HashMap<String, String[]>(httpRequest.getParameterMap());
		}

		// Create a separate thread with the parameters and start that thread.
		AuditThread auditThread = new AuditThread(request, requestType, uri, parameterMap, extras, receivedTimestamp, respondedTimestamp);
		auditThread.start();
	}
	
	/**
	 * Processes a GET request. Only certain APIs may make a GET request. This
	 * should be handled in a different way than hard-coding the allowed GET
	 * APIs. 
	 */
	@Override
	protected final void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		if(RequestBuilder.API_CONFIG_READ.equals(httpRequest.getRequestURI())) {
			processRequest(httpRequest, httpResponse);
		}
		else if(RequestBuilder.API_IMAGE_READ.equals(httpRequest.getRequestURI())) {
			processRequest(httpRequest, httpResponse);
		}
		else if(RequestBuilder.API_DOCUMENT_READ_CONTENTS.equals(httpRequest.getRequestURI())) {
			processRequest(httpRequest, httpResponse);
		}
		else if(httpRequest.getRequestURI().startsWith("/app/viz/")) {
			processRequest(httpRequest, httpResponse);
		}
		else {
			LOGGER.warn("GET attempted and denied.");
			try {
				httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
			catch(IOException e) {
				LOGGER.error("Error while attempting to respond.", e);
			}
			return;
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
	 * Rejects all PUT requests.
	 */
	@Override
	protected final void doPut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		try {
			httpResponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		catch(IOException e) {
			LOGGER.error("Error while attempting to respond.", e);
		}
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
		
		httpRequest.setAttribute(KEY_ATTRIBUTE, request);
	}
}