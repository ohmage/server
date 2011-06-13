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
import org.ohmage.request.InputKeys;
import org.ohmage.request.ReturnKeys;
import org.ohmage.util.CookieUtils;


/**
 * Writes the response of the document creation.
 * 
 * @author John Jenkins
 */
public class DocumentCreationResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(DocumentCreationResponseWriter.class);
	
	/**
	 * Creates this writer with a default error response.
	 * 
	 * @param errorResponse The ErrorResponse to respond with should all else
	 * 						fail.
	 */
	public DocumentCreationResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	/**
	 * If the request is successful, it writes the documents identifier in the
	 * response and returns it. If creating the response fails, it will simply
	 * abort. If creating the response text fails, it will fall back to
	 * responding with a general error message.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing document creation response.");
		
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
		response.setContentType("text/html");
		
		String responseText = "";
		// If the request hasn't failed, try to build a response and if there
		// is an issue set it as failed.
		if(! awRequest.isFailedRequest()) {
			try {
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put(ReturnKeys.RESULT, ReturnKeys.SUCCESS);
			
				String documentId = (String) awRequest.getToReturnValue(ReturnKeys.DOCUMENT_ID);
				
				jsonResponse.put(ReturnKeys.DOCUMENT_ID, documentId);
				
				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
				responseText = jsonResponse.toString();
			}
			catch(IllegalArgumentException e) {
				_logger.error("Missing return value in request.", e);
				awRequest.setFailedRequest(true);
			}
			catch(JSONException e) {
				_logger.error("JSON Error while writing response.", e);
				awRequest.setFailedRequest(true);
			}
		}
		
		// This is checked a second time as a last-ditch effort that if
		// anything went wrong, we can just respond with some error here.
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
