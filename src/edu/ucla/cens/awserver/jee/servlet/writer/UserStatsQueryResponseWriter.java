package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.domain.UserStatsQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserStatsQueryAwRequest;

/**
 * @author selsky
 */
public class UserStatsQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(UserStatsQueryResponseWriter.class);
	
	public UserStatsQueryResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(((UserStatsQueryAwRequest) awRequest).getUserStatsQueryResult());
			}
			
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				JSONObject jsonObject = new JSONObject().put("result", "success");
				UserStatsQueryResult result = ((UserStatsQueryAwRequest) awRequest).getUserStatsQueryResult();
				JSONArray outerArray = new JSONArray();
				
				if(null == result.getMostRecentSurveyUploadTime()) {
					outerArray.put(new JSONArray().put("Hours Since Last Survey Upload").put("null"));
				} else {
					outerArray.put(new JSONArray().put("Hours Since Last Survey Upload").put(
						((System.currentTimeMillis() - result.getMostRecentSurveyUploadTime()) / 1000d / 60d / 60d) ));
				}
				
				if(null == result.getMostRecentMobilityUploadTime()) {
					outerArray.put(new JSONArray().put("Hours Since Last Mobility Upload").put("null"));
				} else {
					outerArray.put(new JSONArray().put("Hours Since Last Mobility Upload").put(
						((System.currentTimeMillis() - result.getMostRecentMobilityUploadTime()) / 1000d / 60d / 60d) ));
				}
				
				if(null == result.getSurveyLocationUpdatesPercentage()) {
					outerArray.put(new JSONArray().put("Percent Successful Survey Location Updates").put("null"));
				} else {
					outerArray.put(new JSONArray().put("Percent Successful Survey Location Updates").put(result.getSurveyLocationUpdatesPercentage()));
				}
				
				if(null == result.getMobilityLocationUpdatesPercentage()) {
					outerArray.put(new JSONArray().put("Percent Successful Mobility Location Updates").put("null"));
				} else {
					outerArray.put(new JSONArray().put("Percent Successful Mobility Location Updates").put(result.getMobilityLocationUpdatesPercentage()));
				}
				
				jsonObject.put("stats", outerArray);
				responseText = jsonObject.toString();
				
			} else {
				
				if(null != awRequest.getFailedRequestErrorMessage()) {
					responseText = awRequest.getFailedRequestErrorMessage();
				} else {
					responseText = generalJsonErrorMessage();
				}
			}
			
			_logger.info("about to write output");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality
			
			_logger.error("an unrecoverable exception occurred while generating a response", e);
			
			try {
				
				writer.write(generalJsonErrorMessage());
				
			} catch (Exception ee) {
				
				_logger.error("caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} finally {
			
			if(null != writer) {
				
				try {
					
					writer.flush();
					writer.close();
					writer = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
		}
	}

}
