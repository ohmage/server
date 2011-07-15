package org.ohmage.request;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
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
	
	public static final String JSON_KEY_RESULT = "result";
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_FAILURE = "failure";
	
	public static final String JSON_KEY_DATA = "data";
	public static final String JSON_KEY_ERRORS = "errors";
	
	public static final String RESPONSE_ERROR_JSON_TEXT = 
		"{\"" + JSON_KEY_RESULT + "\":\"" + RESULT_FAILURE + "\"," +
		"\"" + JSON_KEY_ERRORS + "\":[" +
			"{\"" + Annotator.JSON_KEY_CODE + "\":\"0103\"," +
			"\"" + Annotator.JSON_KEY_TEXT + "\":\"An error occurred while building the JSON response.\"}" +
		"]}";
	
	protected final Annotator annotator;
	protected boolean failed;
	
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
	
	/**************************************************************************
	 *  Begin JEE Requirements
	 *************************************************************************/
	/**
	 * The service calls that ensure that all the parameters of the request are
	 * logically valid, not syntactically valid which is handled in the
	 * constructor, and stores the response.
	 */
	public abstract void service();
	
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
		Writer writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching.
		expireResponse(httpResponse);
		httpResponse.setContentType("application/json");
		
		String responseText = "";
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
			try {
				// Use the annotator's message to build the response.
				responseText = annotator.toJsonObject().toString();
			}
			catch(JSONException e) {
				// If we can't even build the failure message, write a hand-
				// written message as the response.
				LOGGER.error("An error occurred while building the failure JSON response.", e);
				responseText = RESPONSE_ERROR_JSON_TEXT;
			}
		}
		
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			LOGGER.error("Unable to write failed response message. Aborting.", e);
			return;
		}
		
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			LOGGER.error("Unable to flush or close the writer.", e);
			return;
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