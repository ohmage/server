package org.ohmage.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VisualizationRequest;
import org.ohmage.util.CookieUtils;

/**
 * Generic writer for a visualization request.
 * 
 * @author John Jenkins
 */
public class VizResponseWriter extends AbstractResponseWriter {
	private static final Logger _logger = Logger.getLogger(VizResponseWriter.class);
	
	/**
	 * Builds a writer for this request with a default error response should
	 * the request fail.
	 * 
	 * @param errorResponse The error response to use should the request have
	 * 						failed and no other one is present.
	 */
	public VizResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes the image to the output stream or writes a JSON error message
	 * should the request have failed.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing visualization response.");
		
		// Creates the writer that will write the response, success or fail.
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
		
		// If the request hasn't failed, attempt to write the file to the
		// output stream. 
		String responseText = "";
		if(! awRequest.isFailedRequest()) {
			try {
				responseText = (String) awRequest.getToReturnValue(VisualizationRequest.VISUALIZATION_REQUEST_RESULT);
				
				// Setup the response headers.
				response.setContentType("image/png");
				response.setContentLength(responseText.length());
				
				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
			}
			catch(IllegalArgumentException e) {
				awRequest.setFailedRequest(true);
			}
		}
		
		// If the request ever failed, write an error message.
		if(awRequest.isFailedRequest()) {
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