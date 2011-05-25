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
import edu.ucla.cens.awserver.request.DocumentReadAwRequest;
import edu.ucla.cens.awserver.request.ReturnKeys;

public class DocumentReadResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(DocumentReadResponseWriter.class);
	
	/**
	 * Creates a response object for a document read request.
	 * 
	 * @param errorResponse The response if the request failed and no better
	 * 						response exists.
	 */
	public DocumentReadResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * 
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing document read response.");
		
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
		if(! awRequest.isFailedRequest()) {
			try {
				JSONObject responseJson = new JSONObject();
				responseJson.put(ReturnKeys.RESULT, ReturnKeys.SUCCESS);
				
				JSONObject result = (JSONObject) awRequest.getToReturnValue(DocumentReadAwRequest.KEY_DOCUMENT_INFORMATION);
				responseJson.put(ReturnKeys.DATA, result);
				
				responseText = responseJson.toString();
			}
			catch(JSONException e) {
				_logger.error("Error building JSON response.", e);
				awRequest.setFailedRequest(true);
			}
			catch(IllegalArgumentException e) {
				_logger.error("Missing document information in request.", e);
				awRequest.setFailedRequest(true);
			}
		}
		
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
