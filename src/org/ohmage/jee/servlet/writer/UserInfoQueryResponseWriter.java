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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.UserInfoQueryAwRequest;


/**
 * Basic response writer that will write success and the user's info if the 
 * request was successful or an informative message if one is available. In
 * the worst case, it will simply output a generic failure message that this
 * object was built with.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(UserInfoQueryResponseWriter.class);

	/**
	 * Basic constructor that sets up the parent's error response object.
	 * 
	 * @param errorResponse
	 */
	public UserInfoQueryResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	/**
	 * Writes the response to the request.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing the user info request response.");

		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client.
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
		}
		catch(IOException e) {
			_logger.error("Failed to create writer.", e);
			return;
		}
		
		// Set the HTTP headers to disable caching
		expireResponse(response);
		
		// Set the content of the response to JSON.
		response.setContentType("application/json");
		
		// Build the response
		String responseText = "";
		if(! awRequest.isFailedRequest()) {
			try {
				JSONObject responseJson = new JSONObject();
				
				responseJson.put("result", "success");
				responseJson.put("data", awRequest.getToReturnValue(UserInfoQueryAwRequest.RESULT));
				
				responseText = responseJson.toString();
			}
			catch(IllegalArgumentException e) {
				_logger.error("There is no result object to return.");
				awRequest.setFailedRequest(true);
			}
			catch(ClassCastException e) {
				_logger.error("The resulting object is not the expected type of object: " + awRequest.getToProcessValue(UserInfoQueryAwRequest.RESULT).getClass());
				awRequest.setFailedRequest(true);
			}
			catch(JSONException e) {
				_logger.error("Failed to create response.", e);
				awRequest.setFailedRequest(true);
			}
		} 

		if(awRequest.isFailedRequest()) {
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			} else {
				responseText = generalJsonErrorMessage();
			}
		}
		
		try {
			writer.write(responseText);
		}
		catch(IOException e) {
			_logger.error("Failed to write response.", e);
			return;
		}
		
		try {
			writer.flush();
			writer.close();
			writer = null;
		}
		catch(IOException e) {
			_logger.error("Caught IOException when attempting to free resources.", e);
		}
	}
}
