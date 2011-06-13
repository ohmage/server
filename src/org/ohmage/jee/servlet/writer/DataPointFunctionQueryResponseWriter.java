/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.writer;

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
import org.ohmage.domain.DataPointFunctionQueryResult;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointFunctionQueryAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.DateUtils;


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
				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
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
