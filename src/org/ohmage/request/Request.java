/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.request;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.jee.filter.GzipFilter;

/**
 * Superclass for all requests. Defines the basic requirements for a request.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public abstract class Request {
	private static final Logger LOGGER = Logger.getLogger(Request.class);
	
	/**
	 * The key to use when responding with a JSONObject about whether the 
	 * request was a success or failure.
	 */
	public static final String JSON_KEY_RESULT = "result";
	/**
	 * The value to use for the {@link #JSON_KEY_RESULT} when the request is
	 * successful.
	 */
	public static final String RESULT_SUCCESS = "success";
	/**
	 * The value to use for the {@link #JSON_KEY_RESULT} when the request has
	 * failed.
	 */
	public static final String RESULT_FAILURE = "failure";
	
	/**
	 * The key to use when responding with a JSONObject where the request is
	 * successful. The value associated with this key is the requested data.
	 */
	public static final String JSON_KEY_DATA = "data";
	/**
	 * The metadata associated with the response.
	 */
	public static final String JSON_KEY_METADATA = "metadata";
	/**
	 * The key to use when responding with a JSONOBject where the request has
	 * failed. The value associated with this key is the error code and error
	 * text describing why this request failed.
	 */
	public static final String JSON_KEY_ERRORS = "errors";

	/**
	 * The JSON key for the metadata that describes how many total results 
	 * exist that match the criteria as opposed to the number being
	 * returned, which may be different due to paging or aggregation.
	 */
	public static final String JSON_KEY_TOTAL_NUM_RESULTS = 
			"total_num_results";
	
	/**
	 * A hard-coded JSONObject which represents a successful result.
	 */
	public static final String RESPONSE_SUCCESS_JSON_TEXT =
		"{\"" + JSON_KEY_RESULT + "\":\"" + RESULT_SUCCESS + "\"}";
	
	/**
	 * A hard-coded JSONObject with which to respond in the event that the 
	 * JSONObject that is the response cannot be built. 
	 */
	public static final String RESPONSE_ERROR_JSON_TEXT = 
		"{\"" + JSON_KEY_RESULT + "\":\"" + RESULT_FAILURE + "\"," +
		"\"" + JSON_KEY_ERRORS + "\":[" +
			"{\"" + Annotator.JSON_KEY_CODE + "\":\"0103\"," +
			"\"" + Annotator.JSON_KEY_TEXT + "\":\"An error occurred while building the JSON response.\"}" +
		"]}";
	
	private static final String KEY_AUDIT_REQUESTER_INTERNET_ADDRESS = 
			"requester_inet_addr";
	
	/**
	 * The value our Android app uses when setting the client parameter for
	 * each request.
	 */
	public static final String ANDROID_CLIENT_NAME = "ohmage-android";
	
	private final Annotator annotator;
	private boolean failed;
	
	private final Map<String, String[]> parameters;
	private final String requesterInetAddr; 
	
	/**
	 * Initializes this request.
	 * 
	 * @param httpRequest An HttpServletRequest that was used to create this 
	 * 					  request. This may be null if no such request exists.
	 * 
	 * @param parameters The parameters for this request. If this is null, the
	 * 					 parameters are decoded from the HTTP request. 
	 * 					 Otherwise, the parameters in this map are used.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	@SuppressWarnings("unchecked")
	protected Request(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters)
			throws IOException, InvalidRequestException {
		
		annotator = new Annotator();
		failed = false;

		Map<String, String[]> tParameters = new HashMap<String, String[]>();
		String tRequesterInetAddr = null;
		try {
			if(httpRequest != null) {
				// Get the requester's IP address.
				tRequesterInetAddr = httpRequest.getRemoteAddr();
				
				// Get the parameters.
				if (parameters == null) {
					Object parametersObject = 
						httpRequest
							.getAttribute(GzipFilter.ATTRIBUTE_KEY_PARAMETERS);
					
					if(parametersObject instanceof Map) {
						// We make the assumption that we are the only one 
						// setting this value, so it must be a map.
						tParameters = (Map<String, String[]>) parametersObject;
					}
					else if(parametersObject == null) {
						throw new ValidationException(
							"The parameter map was never set which should have been done in the GZIP filter.");
					}
					else {
						throw new ValidationException(
							"The parameters object was not a map.");
					}
				}
				else {
					tParameters = parameters;
				}
				// HT iterates through the param map
				for (Map.Entry<String,String[]> entry : tParameters.entrySet()) {
				    String key = entry.getKey();
				    String[] value = entry.getValue();
				    LOGGER.debug("HT:" + key + " : " + Arrays.toString(value));
				}
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		
		this.parameters = tParameters;
		this.requesterInetAddr = tRequesterInetAddr;
	}
	
	/**
	 * @return Returns the parameters from the HTTP request.
	 */
	protected Map<String, String[]> getParameters() {
		return parameters;
	}

	/**
	 * Returns whether or not this request has failed.
	 * 
	 * @return Whether or not this request has failed.
	 */
	public boolean isFailed() {
		return failed;
	}
	
	/**
	 * The annotator describing why this request has failed, if it has. If it
	 * hasn't failed or it has and a reason wasn't given, then a generic 
	 * annotator will be returned.
	 * 
	 * @return The annotator describing why this request failed or the generic
	 * 		   annotator if this request hasn't failed or a reason was not
	 * 		   given.
	 */
	public Annotator getAnnotator() {
		return annotator;
	}
	
	/**
	 * Simply sets the request as failed without updating the error message.
	 */
	public void setFailed() {
		failed = true;
	}
	
	/**
	 * Marks that the request has failed and updates its response with the 
	 * given error code and error text.
	 * 
	 * @param errorCode A four-character error code related to the error text.
	 * 
	 * @param errorText The text to be returned to the user.
	 */
	public void setFailed(final ErrorCode errorCode, final String errorText) {
		annotator.update(errorCode, errorText);
		
		failed = true;
	}
	
	/**
	 * Returns a String representation of the failure message that would be
	 * returned to a user if this request has failed. All requests have a 
	 * default failure message, so this will always return some error message;
	 * however, if the request has not yet failed, this result is meaningless.
	 * 
	 * @return A String representation of the current failure message for this
	 * 		   request.
	 */
	public String getFailureMessage() {
		String result;
		try {
			// Use the annotator's message to build the response.
			JSONObject resultJson = new JSONObject();
			resultJson.put(JSON_KEY_RESULT, RESULT_FAILURE);
			
			// FIXME: We no longer have multiple error messages per failed
			// response, so we need to get rid of this unnecessary array.
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(annotator.toJsonObject());
			
			resultJson.put(JSON_KEY_ERRORS, jsonArray);
			result = resultJson.toString();
		}
		catch(JSONException e) {
			// If we can't even build the failure message, write a hand-
			// written message as the response.
			LOGGER.error("An error occurred while building the failure JSON response.", e);
			result = RESPONSE_ERROR_JSON_TEXT;
		}
		return result;
	}
	
	/**
	 * Returns an unmodifiable version of the parameter map.
	 * 
	 * @return An unmodifiable version of the parameter map.
	 */
	public Map<String, String[]> getParameterMap() {
		return Collections.unmodifiableMap(parameters);
	}
	
	/**
	 * Returns an array of all of the values from a parameter in the request.
	 * 
	 * @param parameterKey The key to use to lookup the parameter value.
	 * 
	 * @return An array of all values given for the parameter. The array may be
	 * 		   empty, but will never be null.
	 */
	protected String[] getParameterValues(String parameterKey) {
		if(parameterKey == null) {
			return new String[0];
		}
		
		String[] result = parameters.get(parameterKey);
		if(result == null) {
			result = new String[0];
		}
		return result;
	}
	
	/**
	 * Returns the first value for some key from the parameter list. If there
	 * are no values for a key, null is returned.
	 * 
	 * @param parameterKey The key to use to lookup a list of values, the first
	 * 					   of which will be returned.
	 * 
	 * @return Returns the first of a list of values for some key or null if no
	 * 		   values exist for the key.
	 */
	protected String getParameter(String parameterKey) {
		String[] values = getParameterValues(parameterKey);
		
		if(values.length == 0) {
			return null;
		}
		else {
			return values[0];
		}
	}
	
	/**
	 * Performs the operations for which this Request is responsible and 
	 * aggregates any resulting data. This should be container agnostic. The
	 * specific constructors should gather the required information to perform
	 * this service and any results set by this function should be not be 
	 * specific to any type of response generated by the container.
	 */
	public abstract void service();
	
	/**
	 * Gathers an request-specific data that should be logged in the audit.
	 */
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> auditInfo = new HashMap<String, String[]>();
		
		if(requesterInetAddr != null) {
			auditInfo.put(
					KEY_AUDIT_REQUESTER_INTERNET_ADDRESS, 
					new String[] { requesterInetAddr });
		}
		
		return auditInfo;
	}
		
	/**************************************************************************
	 *  Begin JEE Requirements
	 *************************************************************************/
	/**
	 * Writes a response to the request.
	 * 
	 * @param httpRequest The initial HTTP request.
	 * 
	 * @param httpResponse The HTTP response to this request.
	 */
	public abstract void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
	
	/**
	 * Writes the response that is a JSONObject. This is a helper function for
	 * when {@link #respond(HttpServletRequest, HttpServletResponse)} is called
	 * given that most responses are in some sort of JSON format. The response
	 * is modified only add a "success" message and then is sent, unless the
	 * request fails in which case a failure message is sent instead of the
	 * response. This means that the key {@link #JSON_KEY_RESULT} is added to
	 * this JSONObject and any previous value associated with that key is 
	 * removed.
	 *  
	 * @param httpRequest The initial HTTP request that we are processing.
	 * 
	 * @param httpResponse The response for this HTTP request.
	 * 
	 * @param jsonResponse An already-constructed JSONObject that contains the
	 * 					   'data' portion of the object.
	 */
	protected void respond(
			final HttpServletRequest httpRequest, 
			final HttpServletResponse httpResponse, 
			final JSONObject response) {
		
		// Create a writer for the HTTP response object.
		Writer writer = null;
		String responseText = "";
		
		try {
			writer = 
					new BufferedWriter(
							new OutputStreamWriter(
									getOutputStream(
											httpRequest, 
											httpResponse)));
			
			// Sets the HTTP headers to disable caching.
			expireResponse(httpResponse);
			
			httpResponse.setContentType("application/json");
			
			// If the response hasn't failed yet, attempt to create and write the
			// JSON response.
			if(! failed) {
				try {
					response.put(JSON_KEY_RESULT, RESULT_SUCCESS);
					
					responseText = response.toString();
				}
				catch(JSONException e) {
					// If anything fails, echo it in the logs and set the request
					// as failed.
					LOGGER.error("An error occurred while building the success JSON response.", e);
					failed = true;
				}
			}
			
			// If the request failed, either during the build or while the response
			// was being built, write a failure message.
			if(failed) {
				responseText = getFailureMessage();
			}
			
			writer.write(responseText);
		}
		// If the client hangs up, just print a warning.
		catch(ClientAbortException e) {
			LOGGER.info("The client hung up unexpectedly.", e);
		}
		catch(IOException e) {
			LOGGER.error("Unable to write response message. Aborting.", e);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				}
				catch(IOException e) {
					LOGGER.warn("Unable to close the writer.", e);
				}
			}
		}
	}
	
	/**
	 * <p>
	 * Retrieves a parameter from either parts or the servlet container's
	 * deserialization.
	 * </p>
	 * 
	 * <p>
	 * This supersedes {@link #getMultipartValue(HttpServletRequest, String)}.
	 * </p>
	 * 
	 * @param httpRequest
	 *        The HTTP request.
	 * 
	 * @param key
	 *        The parameter key.
	 * 
	 * @return The parameter if given otherwise null.
	 * 
	 * @throws ValidationException
	 *         There was a problem reading from the request.
	 */
	protected byte[] getParameter(
		final HttpServletRequest httpRequest,
		final String key)
		throws ValidationException {
		
		// First, attempt to decode it as a multipart/form-data post.
		try {
			// Get the part. If it isn't a multipart/form-data post, an
			// exception will be thrown. If it is and such a part does not
			// exist, return null.
			Part part = httpRequest.getPart(key);
			if(part == null) {
				return null;
			}
			
			// If the part exists, attempt to retrieve it.
			InputStream partInputStream = part.getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int amountRead;
			while((amountRead = partInputStream.read(chunk)) != -1) {
				outputStream.write(chunk, 0, amountRead);
			}
			
			// If the buffer is empty, return null. Otherwise, return the byte
			// array.
			if(outputStream.size() == 0) {
				return null;
			}
			else {
				return outputStream.toByteArray();
			}
		}
		// This will be thrown if it isn't a multipart/form-post, at which
		// point we can attempt to use the servlet container's deserialization
		// of the parameters.
		catch(ServletException e) {
			// Get the parameter.
			String result = httpRequest.getParameter(key);
			
			// If it doesn't exist, return null.
			if(result == null) {
				return null;
			}
			// Otherwise, return its bytes.
			else {
				return result.getBytes();
			}
		}
		// If we could not read a parameter, something more severe happened,
		// and we need to fail the request and throw an exception.
		catch(IOException e) {
			LOGGER
				.info(
					"There was an error reading the message from the input " +
						"stream.",
					e);
			setFailed();
			throw new ValidationException(e);
		}
	}
		
	/**
	 * Reads the HttpServletRequest for a key-value pair and returns the value
	 * where the key is equal to the given key.
	 * 
	 * @param httpRequest A "multipart/form-data" request that contains the 
	 * 					  parameter that has a key value 'key'.
	 * 
	 * @param key The key for the value we are after in the 'httpRequest'.
	 * 
	 * @return Returns null if there is no such key in the request or if, 
	 * 		   after reading the object, it has a length of 0. Otherwise, it
	 * 		   returns the value associated with the key as a byte array.
	 * 
	 * @throws ServletException Thrown if the 'httpRequest' is not a 
	 * 							"multipart/form-data" request.
	 * 
	 * @throws IOException Thrown if there is an error reading the value from
	 * 					   the request's input stream.
	 * 
	 * @throws IllegalStateException Thrown if the entire request is larger
	 * 								 than the maximum allowed size for a 
	 * 								 request or if the value of the requested
	 * 								 key is larger than the maximum allowed 
	 * 								 size for a single value.
	 */
	protected byte[] getMultipartValue(HttpServletRequest httpRequest, String key) throws ValidationException {
		try {
			Part part = httpRequest.getPart(key);
			if(part == null) {
				return null;
			}

			// Get the input stream.
			InputStream partInputStream = part.getInputStream();
			
			// Wrap the input stream in a GZIP de-compressor if it is GZIP'd.
			String contentType = part.getContentType();
			if((contentType != null) && contentType.contains("gzip")) {
				LOGGER.info("Part was GZIP'd: " + key);
				partInputStream = new GZIPInputStream(partInputStream);
			}
			
			// Parse the data.
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int amountRead;
			while((amountRead = partInputStream.read(chunk)) != -1) {
				outputStream.write(chunk, 0, amountRead);
			}
			
			if(outputStream.size() == 0) {
				return null;
			}
			else {
				return outputStream.toByteArray();
			}
		}
		catch(ServletException e) {
			LOGGER.error("This is not a multipart/form-data POST.", e);
			setFailed(ErrorCode.SYSTEM_GENERAL_ERROR, "This is not a multipart/form-data POST which is what we expect for the current API call.");
			throw new ValidationException(e);
		}
		catch(IOException e) {
			LOGGER
				.info("There was a problem with the zipping of the data.", e);
			throw
				new ValidationException(
					ErrorCode.SERVER_INVALID_GZIP_DATA,
					"The zipped data was not valid zip data.",
					e);
		}
	}
	
	/**
	 * Sets the response headers to disallow client caching.
	 */
	protected void expireResponse(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 5 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        
	     // This is done to allow client content to be served up from from 
	     // different domains than the server data e.g., when you want to run a
	     // client in a local sandbox, but retrieve data from a remote server
	     //response.setHeader("Access-Control-Allow-Origin","*");
	}
	
	/**
	 * There is functionality in Tomcat 6 to perform this action, but it is 
	 * also nice to have it controlled programmatically.
	 * 
	 * @return an OutputStream appropriate for the headers found in the 
	 * request.
	 */
	protected OutputStream getOutputStream(HttpServletRequest request, HttpServletResponse response) 
		throws IOException {
		
		OutputStream os = null; 
		
		// Determine if the response can be gzipped
		String encoding = request.getHeader("Accept-Encoding");
		if (encoding != null && encoding.indexOf("gzip") >= 0) {
            
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Returning a GZIPOutputStream");
			}
			
            response.setHeader("Content-Encoding","gzip");
            response.setHeader("Vary", "Accept-Encoding");
            os = new GZIPOutputStream(response.getOutputStream());
		
		} else {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Returning the default OutputStream");
			}
			
			os = response.getOutputStream();
		}
		
		return os;
	}
	/**************************************************************************
	 *  End JEE Requirements
	 *************************************************************************/
}