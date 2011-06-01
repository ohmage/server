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
import org.ohmage.domain.ChunkedMobilityQueryResult;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.domain.MobilityQueryResult;
import org.ohmage.request.AwRequest;
import org.ohmage.util.DateUtils;


/**
 * @author selsky
 */
public class MobilityQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(MobilityQueryResponseWriter.class);
	
	public MobilityQueryResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
			response.setContentType("application/json");
			JSONObject rootObject = new JSONObject();
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				
				rootObject.put("result", "success");
				
				@SuppressWarnings("unchecked")
				List<MobilityQueryResult> results =  (List<MobilityQueryResult>) awRequest.getResultList();
				generateUtcTimestamps(results);
				
				JSONArray resultArray = new JSONArray();
				
				for(int i = 0; i < results.size(); i++) {
					JSONObject entry = new JSONObject();
					MobilityQueryResult result = results.get(i);
					
					entry.put("v", result.getValue());
					if(result instanceof ChunkedMobilityQueryResult) {
						entry.put("d", ((ChunkedMobilityQueryResult)result).getDuration());
					}
					
					entry.put("ts", result.getTimestamp());
					entry.put("tz", result.getTimezone());
					entry.put("ls", result.getLocationStatus());
					if(! "unavailable".equals(result.getLocationStatus())) {
						JSONObject location = new JSONObject(result.getLocation());
						Object ts = location.remove("timestamp");
						Object la = location.remove("latitude");
						Object lo = location.remove("longitude");
						Object ac = location.remove("accuracy");
						Object pr = location.remove("provider");
						location.put("ts", ts);
						location.put("la", JSONObject.stringToValue(la.toString()));
						location.put("lo", JSONObject.stringToValue(lo.toString()));
						location.put("ac", JSONObject.stringToValue(ac.toString()));
						location.put("pr", pr);
						entry.put("l", location);
					}
					resultArray.put(entry);
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
	
	//TODO -- move to base class. A base class for Results that contain UTC timestamps is also necessary
	private void generateUtcTimestamps(List<MobilityQueryResult> results) {
		for(MobilityQueryResult result : results) {
			result.setUtcTimestamp(DateUtils.timestampStringToUtc(result.getTimestamp(), result.getTimezone()));
		}
	}
}
