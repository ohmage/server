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

import edu.ucla.cens.awserver.dao.EmaQueryDao.EmaQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class EmaQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(EmaQueryResponseWriter.class);
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			expireResponse(response);
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				// Convert the results to JSON for output.
				List<?> results =  awRequest.getResultList();
				JSONArray jsonArray = new JSONArray();
					
				for(int i = 0; i < results.size(); i++) {
					EmaQueryResult result = (EmaQueryResult) results.get(i);
					JSONObject entry = new JSONObject();	
					entry.put("response", new JSONObject(result.getJsonData()).get("response"));
					entry.put("time", result.getTimestamp());
					entry.put("timezone", result.getTimezone());
					entry.put("prompt_id", result.getPromptConfigId());
					entry.put("prompt_group_id", result.getPromptGroupId());
					jsonArray.put(entry);
				}
				
				responseText = jsonArray.toString();
				
			} else {
				
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			
			_logger.info("about to write output");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality (Java 7 will have 
			                 // comma-separated catch clauses) 
			
			_logger.error("an unrecoverable exception occurred while running an EMA query", e);
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
