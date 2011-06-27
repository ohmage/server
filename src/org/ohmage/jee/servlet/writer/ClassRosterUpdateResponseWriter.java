package org.ohmage.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassRosterUpdateRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.ReturnKeys;
import org.ohmage.util.CookieUtils;

/**
 * Writes the result of the class roster update request.
 * 
 * @author John Jenkins
 */
public class ClassRosterUpdateResponseWriter extends AbstractResponseWriter {
	private static final Logger _logger = Logger.getLogger(ClassRosterUpdateResponseWriter.class);
	
	/**
	 * Builds the writer for a class roster update request with a default 
	 * response should the request fail.
	 */
	public ClassRosterUpdateResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes the result of this request.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing class roster update response.");
		
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
		response.setContentType("text/html");
		
		String responseText = "";
		// If the request hasn't failed, try to build a response and if there
		// is an issue set it as failed.
		if(! awRequest.isFailedRequest()) {
			try {
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put(ReturnKeys.RESULT, ReturnKeys.SUCCESS);
				jsonResponse.put("warning_messages", awRequest.getToReturnValue(ClassRosterUpdateRequest.KEY_WARNING_MESSAGES));
				
				responseText = jsonResponse.toString();
				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
			}
			catch(IllegalArgumentException e) {
				_logger.error("Missing return value in request.", e);
				awRequest.setFailedRequest(true);
			}
			catch(JSONException e) {
				_logger.error("JSON Error while writing response.", e);
				awRequest.setFailedRequest(true);
			}
		}
		
		// This is checked a second time as a last-ditch effort that if
		// anything went wrong, we can just respond with some error here.
		if(awRequest.isFailedRequest()) {
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			else {
				responseText = generalJsonErrorMessage();
			}
		}
		
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
