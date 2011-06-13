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

public class DocumentUpdateResponseWriter extends AbstractResponseWriter {
private static Logger _logger = Logger.getLogger(DocumentUpdateResponseWriter.class);
	
	/**
	 * Writer for a campaign update request.
	 * 
	 * @param errorResponse Default response if an unknown error occurrs.
	 */
	public DocumentUpdateResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes the request for the response.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing document update response.");
		
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
		// The response type will need to be text/html instead of application/
		// json because, when uploading a file, browsers require this response.
		response.setContentType("text/html");
		
		String responseText;
		if(awRequest.isFailedRequest()) {
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			else {
				responseText = generalJsonErrorMessage();
			}
		}
		else {
			responseText = generalJsonSuccessMessage();
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