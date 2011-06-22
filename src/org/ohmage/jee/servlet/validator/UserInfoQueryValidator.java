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
package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Validator for inbound queries about the requesting user's information.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryValidator extends AbstractGzipHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(UserInfoQueryValidator.class);

	/**
	 * Default constructor that sets up the viable parameters for this query.
	 */
	public UserInfoQueryValidator() {
		// Do nothing.
	}
	
	/**
	 * Validates that none of the parameters is not too long and that the list
	 * of usernames contains at least one username. 
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is missing or invalid. 
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Get the authentication / session token from the header.
		String token;
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
			
			if(token == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		else {
			token = tokens.get(0);
		}
		
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(token == null) {
			throw new MissingAuthTokenException("The required authentication / session token is missing.");
		}
		else if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36) || (token.length() < 36)) {
			_logger.warn("Incorrectly sized " + InputKeys.AUTH_TOKEN);
			return false;
		}
		else if(client == null) {
			_logger.warn("Missing " + InputKeys.CLIENT);
			return false;
		}
		
		return true;
	}
}