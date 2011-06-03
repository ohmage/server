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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;


public class PasswordChangeValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(PasswordChangeValidator.class);
	
	/**
	 * Default constructor.
	 */
	public PasswordChangeValidator() {
		// Do nothing.
	}
	
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		String username = httpRequest.getParameter(InputKeys.USERNAME);
		String password = httpRequest.getParameter(InputKeys.PASSWORD);
		String newPassword = httpRequest.getParameter(InputKeys.NEW_PASSWORD);
		
		if(client == null) {
			throw new MissingAuthTokenException("The required authentication / session token is missing.");
		}
		else if(username == null) {
			// Missing username.
			return false;
		}
		else if(password == null) {
			// Missing password.
			return false;
		}
		else if(greaterThanLength(InputKeys.USERNAME, InputKeys.USERNAME, username, 15)) {
			// Username isn't the right length.
			return false;
		}
		else if(greaterThanLength(InputKeys.PASSWORD, InputKeys.PASSWORD, password, 100)) {
			// Password isn't the right length.
			return false;
		}
		else if(newPassword == null) {
			// Missing required parameter.
			return false;
		}
		else if(greaterThanLength(InputKeys.NEW_PASSWORD, InputKeys.NEW_PASSWORD, newPassword, 15)) {
			// The new password isn't the right length.
			_logger.warn("Attempting to update to a new password that is of an incorrect length: " + newPassword.length());
			return false;
		}
		
		return true;
	}

}
