package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ReturnKeys;

/**
 * Writes the response of the document creation.
 * 
 * @author John Jenkins
 */
public class DocumentCreationResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(DocumentCreationResponseWriter.class);
	
	/**
	 * Creates this writer with a default error response.
	 * 
	 * @param errorResponse The ErrorResponse to respond with should all else
	 * 						fail.
	 */
	public DocumentCreationResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	/**
	 * If the request is successful, it writes the documents identifier in the
	 * response and returns it. If creating the response fails, it will simply
	 * abort. If creating the response text fails, it will fall back to
	 * responding with a general error message.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing document creation response.");
		
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
		response.setContentType("application/json");
		
		String responseText = "";
		// If the request hasn't failed, try to build a response and if there
		// is an issue set it as failed.
		if(! awRequest.isFailedRequest()) {
			try {
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put(ReturnKeys.RESULT, ReturnKeys.SUCCESS);
			
				String documentId = (String) awRequest.getToReturnValue(ReturnKeys.DOCUMENT_ID);
				
				jsonResponse.put(ReturnKeys.DOCUMENT_ID, documentId);
				
				responseText = jsonResponse.toString();
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
