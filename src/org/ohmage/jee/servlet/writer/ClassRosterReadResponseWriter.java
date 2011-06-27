package org.ohmage.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassRosterReadRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

public class ClassRosterReadResponseWriter extends AbstractResponseWriter {
	private static final Logger _logger = Logger.getLogger(ClassRosterReadRequest.class);
	
	/**
	 * Builds the response writer for class roster read requests.
	 * 
	 * @param errorResponse A default response to return should a more-specific
	 * 						response not be available.
	 */
	public ClassRosterReadResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing class roster read response.");
		
		// Creates the writer that will write the response.
		Writer writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
		}
		catch(IOException e) {
			_logger.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(response);
		
		// Create the response text object.
		String responseText = "";
		
		// If the request hasn't failed, set the headers and 
		if(! awRequest.isFailedRequest()) {
			try {
				// Set the content type to something unknown and the content
				// disposition to an attachment so the browser will try to
				// download it.
				response.setContentType("ohmage/document");
				response.setHeader("Content-Disposition", "attachment; filename=roster.csv");
				
				// Get the resulting JSONObject.
				JSONObject result = (JSONObject) awRequest.getToReturnValue(ClassRosterReadRequest.KEY_RESULT);
				StringBuilder resultBuilder = new StringBuilder();
				
				// Get the keyset. I have no idea why they return a generic  
				// Iterator, but it is upsetting. 
				@SuppressWarnings("unchecked")
				Iterator<String> classIds = result.keys();
				while(classIds.hasNext()) {
					String classId = classIds.next();
					JSONObject currClassRoster = result.getJSONObject(classId);
					
					@SuppressWarnings("unchecked")
					Iterator<String> userIds = currClassRoster.keys();
					while(userIds.hasNext()) {
						String userId = userIds.next();
						String userRole = currClassRoster.getString(userId);
						
						resultBuilder.append(classId).append(",").append(userId).append(",").append(userRole).append("\n");
					}
				}
				
				// Set the response text with the CSV file contents.
				responseText = resultBuilder.toString();
				
				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
			}
			catch(IllegalArgumentException e) {
				_logger.error("The 'successful' request is missing the result object.", e);
				awRequest.setFailedRequest(true);
			}
			catch(ClassCastException e) {
				_logger.error("The request's result object is not of exepected type JSONObject.", e);
				awRequest.setFailedRequest(true);
			}
			catch(JSONException e) {
				_logger.error("There was an error retreiving class roster information from the request's result JSONObject.");
				awRequest.setFailedRequest(true);
			}
		}
		
		// If the request ever failed, write an error message.
		if(awRequest.isFailedRequest()) {
			response.setContentType("text/html");
			response.setContentType("application/json");
			
			// If a specific error message was annotated, use that. 
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			// Otherwise, just use the default one with which we were built.
			else {
				responseText = generalJsonErrorMessage();
			}
		}
			
		// Write the error response.
		try {
			writer.write(responseText); 
		}
		catch(IOException e) {
			_logger.error("Unable to write failed response message. Aborting.", e);
			return;
		}
		
		// Flush it and close.
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			_logger.error("Unable to flush or close the writer.", e);
		}
	}
}