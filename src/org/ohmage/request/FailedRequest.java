/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.exception.InvalidRequestException;

/**
 * This class represents a failed request. It will simply set the request as 
 * failed and return a HTTP error code to the requester.
 * 
 * @author John Jenkins
 */
public class FailedRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(FailedRequest.class);
	
	/**
	 * Default constructor. Sets the request as failed.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public FailedRequest() throws IOException, InvalidRequestException {
		super(null);
		
		setFailed();
	}

	/**
	 * This should never be called as the only thing in the FailRequest's 
	 * constructor is to set the request as failed.
	 */
	@Override
	public void service() {
		// Do nothing.
	}
	
	/**
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		return new HashMap<String, String[]>();
	}

	/**
	 * Returns a HTTP 404 error.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		try {
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		catch(IOException e) {
			LOGGER.error("Error while attempting to respond.", e);
		}
	}
}
