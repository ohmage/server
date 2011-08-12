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
import org.ohmage.service.AuditServices;

/**
 * Handler for all incoming HTTP requests.
 * 
 * @author John Jenkins
 */
@MultipartConfig(location="/tmp/", fileSizeThreshold=1024*1024, maxFileSize=1024*1024*5, maxRequestSize=1024*1024*5*5)
public class RequestServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(RequestServlet.class);
	
	private static final String PASSWORD_OMITTED = "omitted";
	
	private static final String KEY_DEVICE_ID = "device_id";
	
	private static final long serialVersionUID = 1L;
	
	private Request request;
	
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
				final RequestType requestType,
				final String uri,
				final Map<String, String[]> parameterMap,
				final Map<String, String[]> headerMap,
				final long receivedTimestamp, 
				final long respondTimestamp) {
			
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
				// Retrieve the client parameter should one exist. Otherwise,
				// null is used.
				String[] clientValues = parameterMap.get(InputKeys.CLIENT);
				String client = null;
				if((clientValues != null) && (clientValues.length > 0)) {
					client = clientValues[0];
				}
				
				// Replace any obvious passwords with a placeholder.
				String[] passwords;
				String[] passwordsOmitted;
				
				// First, do it for all of the InputKeys.PASSWORD parameters.
				passwords = parameterMap.get(InputKeys.PASSWORD);
				if(passwords != null) {
					passwordsOmitted = new String[passwords.length];
					
					for(int i = 0; i < passwords.length; i++) {
						passwordsOmitted[i] = PASSWORD_OMITTED;
					}

					parameterMap.put(InputKeys.PASSWORD, passwordsOmitted);
				}
				
				// Then, do it for all of the InputKeys.NEW_PASSWORD 
				// parameters.
				passwords = parameterMap.get(InputKeys.NEW_PASSWORD);
				if(passwords != null) {
					passwordsOmitted = new String[passwords.length];
					
					for(int i = 0; i < passwords.length; i++) {
						passwordsOmitted[i] = PASSWORD_OMITTED;
					}

					parameterMap.put(InputKeys.NEW_PASSWORD, passwordsOmitted);
				}
				
				// Retrieve the device ID. If any number of device IDs exist,
				// the first one reported will be used.
				String deviceId = null;
				String[] deviceIds = parameterMap.get(KEY_DEVICE_ID);
				if((deviceIds != null) && (deviceIds.length > 0)) {
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
				}
				
				// Generate an 'extras' Map based on the HTTP headers.
				Map<String, String[]> extras = headerMap;
				
				// Get any extras from the request.
				if(request != null) {
					Map<String, String[]> requestExtras = request.getAuditInformation();
					if(requestExtras != null) {
						extras.putAll(requestExtras);
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
	 * Default constructor.
	 */
	public RequestServlet() {
		request = null;
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
		
		// Get the information from the httpRequest.
		
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
		
		// Retrieve the request's parameter map.
		Map<String, String[]> parameterMap = new HashMap<String, String[]>(httpRequest.getParameterMap());
		
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

		// Create a separate thread with the parameters and start that thread.
		AuditThread auditThread = new AuditThread(requestType, uri, parameterMap, extras, receivedTimestamp, respondedTimestamp);
		auditThread.start();
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

		long startTime = System.currentTimeMillis();
		
		Request request = RequestBuilder.buildRequest(httpRequest);
		
		if(! request.isFailed()) {
			request.service();
		}
		
		request.respond(httpRequest, httpResponse);
		
		LOGGER.info("total request milliseconds to service " + httpRequest.getRequestURI() + " = "+ (System.currentTimeMillis() - startTime));
	}
}