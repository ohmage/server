package org.ohmage.request;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ValidationException;

/**
 * Superclass for all requests. Defines the basic requirements for a request.
 * 
 * @author John Jenkins
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
	 * The key to use when responding with a JSONOBject where the request has
	 * failed. The value associated with this key is the error code and error
	 * text describing why this request failed.
	 */
	public static final String JSON_KEY_ERRORS = "errors";
	
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
	
	private final Annotator annotator;
	private boolean failed;
	
	/**
	 * Default constructor. Creates a new, generic annotator for this object.
	 */
	protected Request() {
		annotator = new Annotator();
		failed = false;
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
	public void setFailed(String errorCode, String errorText) {
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
	 * given that most responses are in some sort of JSON format. 
	 *  
	 * @param httpRequest The initial HTTP request that we are processing.
	 * 
	 * @param httpResponse The response for this HTTP request.
	 * 
	 * @param jsonResponse An already-constructed JSONObject that contains the
	 * 					   'data' portion of the object.
	 */
	protected void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse, JSONObject jsonResponse) {
		respond(httpRequest, httpResponse, JSON_KEY_DATA, jsonResponse);
	}
	
	/**
	 * Writes the response that is a JSONObject. This is a helper function for
	 * when {@link #respond(HttpServletRequest, HttpServletResponse)} is called
	 * given that most responses are in some sort of JSON format. This creates
	 * a success/fail JSON response where, when the result is success, it will
	 * also include a second key-value pair which are the parameters to this
	 * function.
	 *  
	 * @param httpRequest The initial HTTP request that we are processing.
	 * 
	 * @param httpResponse The response for this HTTP request.
	 * 
	 * @param key The key to include along with {@link #JSON_KEY_RESULT}.
	 * 
	 * @param value The value to assign with the 'key'.
	 */
	protected void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String key, Object value) {
		// Create a writer for the HTTP response object.
		Writer writer = null;
		String responseText = "";
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
			
			// Sets the HTTP headers to disable caching.
			expireResponse(httpResponse);
			httpResponse.setContentType("application/json");
			
			// If the response hasn't failed yet, attempt to create and write the
			// JSON response.
			if(! failed) {
				try {
					JSONObject result = new JSONObject();
					
					result.put(JSON_KEY_RESULT, RESULT_SUCCESS);
					result.put(key, value);
					
					responseText = result.toString();
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
			try {
				if(writer != null) {
					writer.flush();
					writer.close();
					writer = null;
				}
			}
			catch(IOException e) {
				LOGGER.error("Unable to flush or close the writer.", e);
			}
		}
	}
	
	/**
	 * There is functionality in Tomcat 6 to perform this action, but it is also nice to have it controlled programmatically.
	 * 
	 * @return an OutputStream appropriate for the headers found in the request.
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
	
	/**
	 * Sets the response headers to disallow client caching.
	 */
	protected void expireResponse(HttpServletResponse response) {
		response.setHeader("Expires", "Fri, 5 May 1995 12:00:00 GMT");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        
        // this is done to allow client content to be served up from from different domains than the server data
        // e.g., when you want to run a client in a local sandbox, but retrieve data from a remote server
        response.setHeader("Access-Control-Allow-Origin","*");
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
			setFailed(ErrorCodes.SYSTEM_GENERAL_ERROR, "This is not a multipart/form-data POST which is what we expect for uploading campaign XMLs.");
			throw new ValidationException(e);
		}
		catch(IOException e) {
			LOGGER.error("There was an error reading the message from the input stream.", e);
			setFailed();
			throw new ValidationException(e);
		}
	}
	/**************************************************************************
	 *  End JEE Requirements
	 *************************************************************************/
}