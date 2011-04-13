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
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Writer for the response to a campaign creation request.
 * 
 * @author John Jenkins
 */
public class CampaignCreationResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(CampaignCreationResponseWriter.class);
	
	/**
	 * Sets up the writer with an error response if it fails.
	 * 
	 * @param errorResponse The response if there is a failure.
	 */
	public CampaignCreationResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes out the response to the request. If there was a problem, it will
	 * write out an error message. If everything succeeded, it will write out
	 * the new campaign's identifier.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing campaign creation response.");
		
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
		
		// This is called first. If it has failed in the past, we will just
		// skip this part. If it hasn't failed before, we will begin writing
		// the response, but if we fail along the way, we will switch it to a
		// failed request and handle the response below.
		if(! awRequest.isFailedRequest()) {
			JSONObject jsonResponse = new JSONObject();
			
			try {
				CampaignCreationAwRequest ccAwRequest = (CampaignCreationAwRequest) awRequest;
				
				jsonResponse.put("result", "success");
				jsonResponse.put("id", ccAwRequest.getCampaignId());
			}
			catch(ClassCastException e) {
				_logger.error("Using the CampaignCreationResponseWriter for a non-CampaignCreationAwRequest object. Wires crossed in the Spring XML config?");
				awRequest.setFailedRequest(true);
			}
			catch(JSONException e) {
				_logger.error("There was a problem building the response JSONObject.", e);
				awRequest.setFailedRequest(true);
			}
		}
		
		// This is called after it is checked as a way to catch failed
		// operations durring the actual writing of a response.
		if(awRequest.isFailedRequest()) {
			String responseText;
			
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			else {
				responseText = generalJsonErrorMessage();
			}
			
			try {
				writer.write(responseText); 
			}
			catch(IOException e) {
				_logger.error("Unable to write failed response message. Aborting.", e);
				return;
			}
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
