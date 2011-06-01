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


/**
 * Writes the response to the request. It is built with a default failure
 * response but will attempt to use a more detailed one if available.
 * 
 * @author John Jenkins
 */
public class ClassUpdateResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(ClassUpdateResponseWriter.class);

	/**
	 * Basic constructor that sets up the default error response.
	 * 
	 * @param errorResponse The error response to signal if no other error
	 * 						response is available.
	 */
	public ClassUpdateResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	/**
	 * Writes the response. If successful, it will write the success message.
	 * If it is a failure, it will check to see if a more specific message is
	 * available. If so it will use that; otherwise it will use the one it got
	 * from its constructor.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		_logger.info("Writing class update response.");
		
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
