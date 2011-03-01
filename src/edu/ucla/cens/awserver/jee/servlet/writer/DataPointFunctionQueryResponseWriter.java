package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.DataPointFunctionQueryResult;
import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointFunctionQueryAwRequest;
import edu.ucla.cens.awserver.util.DateUtils;

/**
 * @author selsky
 */
public class DataPointFunctionQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(DataPointFunctionQueryResponseWriter.class);
	
	public DataPointFunctionQueryResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		DataPointFunctionQueryAwRequest req = (DataPointFunctionQueryAwRequest) awRequest;
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			expireResponse(response);
			response.setContentType("application/json");
			JSONObject rootObject = new JSONObject();
			
			// Convert the results to JSON for output. 
			if(! awRequest.isFailedRequest()) {
				rootObject.put("result", "success");
				rootObject.put("label", req.getMetadata().getLabel());
				rootObject.put("unit", req.getMetadata().getUnit());
				rootObject.put("type", req.getMetadata().getType());
				
				@SuppressWarnings("unchecked")
				List<DataPointFunctionQueryResult> results =  (List<DataPointFunctionQueryResult>) req.getResultList();
				
				_logger.info("found " + results.size() + " results");
							
				generateUtcTimestamps(results);
				
				JSONArray resultArray = new JSONArray();
				
				for(DataPointFunctionQueryResult result : results) {
					JSONObject dataObject = new JSONObject();
					dataObject.put("value", result.getValue());
					dataObject.put("timestamp", result.getTimestamp());
					dataObject.put("timezone", result.getTimezone());
					dataObject.put("utc_timestamp", result.getUtcTimestamp());
					dataObject.put("location_status", result.getLocationStatus());
					dataObject.put("location", null == result.getLocation() ? null : new JSONObject(result.getLocation()));
					resultArray.put(dataObject);
				}
				
				rootObject.put("data", resultArray);
				responseText = rootObject.toString();
				
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
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality (Java 7 will have 
			                 // comma-separated catch clauses) 
			
			_logger.error("an unrecoverable exception occurred while running an data point query", e);
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
	
	//TODO -- move to base class. A base class for results that contain UTC timestamps is also necessary
	private void generateUtcTimestamps(List<DataPointFunctionQueryResult> results) {
		for(DataPointFunctionQueryResult result : results) {
			result.setUtcTimestamp(DateUtils.timestampStringToUtc(result.getTimestamp(), result.getTimezone()));
		}
	}
}
