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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

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
	
	private static final String KEY_CONTENT_ENCODING = "Content-Encoding";
	private static final String VALUE_GZIP = "gzip";
	
	private static final int CHUNK_SIZE = 4096;
	
	private static final String PARAMETER_SEPARATOR = "&";
	private static final String PARAMETER_VALUE_SEPARATOR = "=";
	
	private final Annotator annotator;
	private boolean failed;
	
	private final Map<String, String[]> parameters;
	
	/**
	 * Default constructor. Creates a new, generic annotator for this object.
	 * 
	 * @param httpRequest An HttpServletRequest that was used to create this 
	 * 					  request. This may be null if no such request exists.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	protected Request(
			final HttpServletRequest httpRequest)
			throws IOException, InvalidRequestException {
		
		annotator = new Annotator();
		failed = false;

		parameters = getParameters(httpRequest);
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
	public abstract Map<String, String[]> getAuditInformation();
		
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
			httpResponse.setContentType("text/html");
			
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
		catch(IOException e) {
			LOGGER.error("Unable to write response message. Aborting.", e);
		}
		finally {
			if(writer != null) {
				try {
					writer.flush();
				}
				catch(IOException e) {
					LOGGER.warn("Unable to flush the writer.", e);
				}
				
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
	 * Retrieves the parameter map from the request and returns it.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the desired 
	 * 					  parameter map.
	 * 
	 * @return Returns a map of keys to an array of values for all of the
	 * 		   parameters contained in the request. This may return an empty 
	 * 		   map, but it will never return null.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	private Map<String, String[]> getParameters(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		if(httpRequest == null) {
			return Collections.emptyMap();
		}
		
		// This is a hack to validate whether or not the size limits have been
		// exceeded. If this isn't done and the parameters violate the size
		// limits, Tomcat will silently fail by returning an empty parameter
		// map even if all parameters are valid except one. It will not throw
		// an exception or give any indication that something has failed.
		try {
			httpRequest.getParts();
		}
		catch(ServletException e) {
			// This simply means that it is not a multipart/form-post request.
		}
		catch(IllegalStateException e) {
			String errorText;
			
			Throwable cause = e.getCause();
			if(cause instanceof FileSizeLimitExceededException) {
				errorText = 
						((FileSizeLimitExceededException) cause).getMessage();
			}
			else if(cause instanceof SizeLimitExceededException) {
				errorText = ((SizeLimitExceededException) cause).getMessage();
			}
			else {
				errorText = 
						"A parameter and/or the entire request is too large.";
			}
			
			setFailed(ErrorCode.SYSTEM_REQUEST_TOO_LARGE, errorText);
		} 
		catch(IOException e) {
			// This appears to happen when it is a POST request but there 
			// aren't any attached files; however, nothing has actually failed.
			// Given that this is simply a check to see if the size limit has
			// been exceeded and not to actually retrieve or validate any data,
			// this is being allowed to pass through.
			/*
			setFailed(
					ErrorCode.SYSTEM_GENERAL_ERROR, 
					"Error reading the request's parameters.");
			*/
		}
		
		Map<String, String[]> result = null;
		Enumeration<String> contentEncodingHeaders = 
				httpRequest.getHeaders(KEY_CONTENT_ENCODING);
		
		// Look for a GZIP content encoding header.
		while(contentEncodingHeaders.hasMoreElements()) {
			// If one is found, gunzip the request.
			if(VALUE_GZIP.equals(contentEncodingHeaders.nextElement())) {
				result = gunzipRequest(httpRequest);
				break;
			}
		}
		
		// If the parameter map has not yet been decoded, use the container's
		// parameter map retrieval.
		if(result == null) {
			result = httpRequest.getParameterMap();
		}
		
		return result;
	}
	
	/**
	 * Retrieves the parameter map from a request that has had its contents
	 * GZIP'd. 
	 * 
	 * @param httpRequest A HttpServletRequest whose contents are GZIP'd as
	 * 					  indicated by a "Content-Encoding" header.
	 * 
	 * @return Returns a map of keys to a list of values for all of the 
	 * 		   parameters passed to the server. This may return an empty map,
	 * 		   but it will never return null.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	private Map<String, String[]> gunzipRequest(
			final HttpServletRequest httpRequest)
			throws IOException, InvalidRequestException {
		
		// Get the request's InputStream.
		InputStream requestInputStream;
		try {
			requestInputStream = httpRequest.getInputStream();
		}
		catch(IOException e) {
			LOGGER.info("Could not connect to the request's input stream.", e);
			throw e;
		}
		
		// Pass it through the GZIP input stream.
		GZIPInputStream gzipInputStream;
		try {
			gzipInputStream = new GZIPInputStream(requestInputStream);
		}
		catch(IOException e) {
			try {
				requestInputStream.close();
			}
			catch(IOException requestIs) {
				LOGGER.error(
					"Could not close the request's input stream.", 
					requestIs);
				throw requestIs;
			}
			
			throw new InvalidRequestException(
				HttpServletResponse.SC_BAD_REQUEST,
				"The content was not a valid GZIP stream.");
		}
		
		// Retrieve the parameter list as a string.
		String parameterString;
		try {
			// This will build the parameter string.
			StringBuilder builder = new StringBuilder();
			
			// These will store the information for the current chunk.
			byte[] chunk = new byte[CHUNK_SIZE];
			int readLen = 0;
			
			while((readLen = gzipInputStream.read(chunk)) != -1) {
				builder.append(new String(chunk, 0, readLen));
			}
			
			parameterString = builder.toString();
		}
		catch(IOException e) {
			LOGGER.info(
				"The stream was cut off before reading was finished.",
				e);
			throw e;
		}
		finally {
			try {
				gzipInputStream.close();
				gzipInputStream = null;
			}
			catch(IOException e) {
				LOGGER.info("Error closing the GZIP input stream.", e);
			}

			try {
				requestInputStream.close();
				requestInputStream = null;
			}
			catch(IOException e) {
				LOGGER.info("Error closing the request's input stream.", e);
			}
		}
		
		// Create the resulting object so that we will never return null.
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		
		// If the parameters string is not empty, parse it for the parameters.
		if(! StringUtils.isEmptyOrWhitespaceOnly(parameterString)) {
			Map<String, List<String>> parameters = new HashMap<String, List<String>>();
			
			// First, split all of the parameters apart.
			String[] keyValuePairs = parameterString.split(PARAMETER_SEPARATOR);
			
			// For each of the pairs, split their key and value and store them.
			for(String keyValuePair : keyValuePairs) {
				// If the pair is empty or null, ignore it.
				if(StringUtils.isEmptyOrWhitespaceOnly(keyValuePair.trim())) {
					continue;
				}
				
				// Split the key from the value.
				String[] splitPair = keyValuePair.split(PARAMETER_VALUE_SEPARATOR);
				
				// If there isn't exactly one key to one value, then there is a
				// problem, and we need to abort.
				if(splitPair.length <= 1) {
					String errorText =
						"One of the parameter's 'pairs' did not contain a '" + 
							PARAMETER_VALUE_SEPARATOR + 
							"': " + 
							keyValuePair;
					
					LOGGER.error(errorText);
					throw new InvalidRequestException(
						HttpServletResponse.SC_BAD_REQUEST, 
						errorText);
				}
				else if(splitPair.length > 2) {
					String errorText =
						"One of the parameter's 'pairs' contained multiple '" + 
							PARAMETER_VALUE_SEPARATOR + 
							"'s: " + 
							keyValuePair;
					
					LOGGER.error(errorText);
					throw new InvalidRequestException(
						HttpServletResponse.SC_BAD_REQUEST, 
						errorText);
				}
				
				// The key is the first part of the pair.
				String key = StringUtils.urlDecode(splitPair[0]);
				
				// The first or next value for the key is the second part of 
				// the pair.
				List<String> values = parameters.get(key);
				if(values == null) {
					values = new LinkedList<String>();
					parameters.put(key, values);
				}
				values.add(StringUtils.urlDecode(splitPair[1]));
			}
			
			// Now that we have all of the pairs, convert it into the 
			// appropriate map.
			for(String key : parameters.keySet()) {
				parameterMap.put(key, parameters.get(key).toArray(new String[0]));
			}
		}
		
		return parameterMap;
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
			
			InputStream partInputStream = part.getInputStream();
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
			LOGGER.error("There was an error reading the message from the input stream.", e);
			setFailed();
			throw new ValidationException(e);
		}
	}
	
	/**
	 * Sets the response headers to disallow client caching.
	 */
	protected void expireResponse(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 5 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
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
