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
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Writer for the responses from a campaign update request.
 * 
 * @author John Jenkins
 */
public class CampaignUpdateResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(CampaignUpdateResponseWriter.class);
	
	/**
	 * Writer for a campaign update request.
	 * 
	 * @param errorResponse Default response if an unknown error occurrs.
	 */
	public CampaignUpdateResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}

	/**
	 * Writes the request for the response.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing campaign update response.");
		
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
		// The response type will need to be text/html instead of application/
		// json because, when uploading a file, browsers require this response.
		response.setContentType("text/html");
		
		String responseText;
		if(awRequest.isFailedRequest()) {
			if(awRequest.getFailedRequestErrorMessage() != null) {
				responseText = awRequest.getFailedRequestErrorMessage();
			}
			else {
				responseText = generalJsonErrorMessage();
			}
		}
		else {
			CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
			responseText = generalJsonSuccessMessage();
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
