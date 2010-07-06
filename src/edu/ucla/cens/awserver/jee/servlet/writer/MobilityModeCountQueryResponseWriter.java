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

import edu.ucla.cens.awserver.domain.MobilityModeCountQueryResult;
import edu.ucla.cens.awserver.domain.UserDate;
import edu.ucla.cens.awserver.request.AwRequest;

public class MobilityModeCountQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(MobilityModeCountQueryResponseWriter.class);
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			expireResponse(response);
			response.setContentType("application/json");
			
			// Process query results and convert to JSON
			
			List<?> results = awRequest.getResultList();
			int size = results.size();
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("converting " + size + " results to JSON");
			}
			
			if(0 != size)  {
				JSONArray jsonArray = new JSONArray();
				
				UserDate currentUserDate = null;
				
				for(int i = 0; i < size; i++) {
					MobilityModeCountQueryResult result = (MobilityModeCountQueryResult) results.get(i);
					UserDate compareUserDate = new UserDate(result.getUserName(), result.getDate());
					
					if(! compareUserDate.equals(currentUserDate)) {
						
						currentUserDate = compareUserDate;
						
						JSONObject userObject = new JSONObject(); 
						userObject.put("user", result.getUserName());
						
						if(! result.isEmpty()) {
							
							userObject.put("date", result.getDate());
							
							JSONArray modesArray = new JSONArray();
							userObject.put("modes", modesArray);
							
							while(true) {
								
								JSONObject groupObject = new JSONObject();
								groupObject.put("mode", result.getMode());
								groupObject.put("value", result.getCount());
								modesArray.put(groupObject);
								
								i++;
								if(i == size) {
									break;
								}
								
								result = (MobilityModeCountQueryResult) results.get(i);
								compareUserDate = new UserDate(result.getUserName(), result.getDate());
								
								if(! compareUserDate.equals(currentUserDate)) { // make sure not to skip the first 
									                                            // result of the next group in the outer loop
									i--;
									break;
								}
							}
						}
						
						jsonArray.put(userObject);
					}
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
