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
import org.ohmage.request.DocumentReadAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.ReturnKeys;
import org.ohmage.util.CookieUtils;


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
				
				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
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
