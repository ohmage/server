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

import edu.ucla.cens.awserver.domain.MostRecentActivityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Writer of JSON query output for the most recent activity query.
 * 
 * @author selsky
 */
public class MostRecentActivityQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(MostRecentActivityQueryResponseWriter.class);
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			expireResponse(response);
			response.setContentType("application/json");
			
			// Convert the results to JSON for output.
			
			// TODO - there should be a JSONBuilder so in this class the only thing that is done is writing the output
			
			List<?> results =  awRequest.getResultList();
		    int size = results.size();
		    
		    if(size > 0) { 
				JSONArray jsonArray = new JSONArray();
				
			    for(int i = 0; i < size; i++) {
			    	JSONObject jsonObject = new JSONObject();
			    	MostRecentActivityQueryResult result = (MostRecentActivityQueryResult) results.get(i);
			    	String type = result.getMaxFieldLabel();
			    	
			    	if("mobility".equals(type)) {
			    		
			    		jsonObject.put("user", result.getMobilityActivityQueryResult().getUserName());
			    		jsonObject.put("tz", result.getMobilityActivityQueryResult().getMobilityTimezone());
			    		
			    	} else if("prompt".equals(type)) {
			    		
			    		jsonObject.put("user", result.getPromptActivityQueryResult().getUserName());
			    		jsonObject.put("tz", result.getPromptActivityQueryResult().getPromptTimezone());
			    		
			    	} else {
			    		
			    		jsonObject.put("user", result.getUserName());
			    		jsonObject.put("tz", "");
			    	}
			    	
					jsonObject.put("value", result.getHoursSinceLastActivity());
					jsonArray.put(jsonObject);
			    }
				
				responseText = jsonArray.toString();
				
		    } else {
		    	
		    	responseText = "[]";
		    }
			
			_logger.info("about to write output");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality (Java 7 will have 
			                 // comma-separated catch clauses) 
			
			_logger.error("an unrecoverable exception occurred while running an successful location updates query", e);
			
			try {
				
				writer.write("{\"code\":\"0103\",\"text\":\"" + e.getMessage() + "\"}");
				
			} catch (IOException ioe) {
				
				_logger.error("caught IOException when attempting to write to HTTP output stream: " + ioe.getMessage());
			}
			
		} finally {
			
			if(null != writer) {
				
				try {
					
					writer.flush();
					writer.close();
					writer = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources: " + ioe.getMessage());
				}
			}
		}
	}
}
