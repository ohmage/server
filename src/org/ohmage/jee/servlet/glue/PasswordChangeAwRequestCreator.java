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
package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.PasswordChangeAwRequest;


/**
 * Creates the request for changing a user's password.
 * 
 * @author John Jenkins
 */
public class PasswordChangeAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(PasswordChangeAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public PasswordChangeAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a new password change request where the credentials being used
	 * to authenticate this user must be the username and hashed password.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating a new request to handle a password change.");
		
		PasswordChangeAwRequest newRequest = new PasswordChangeAwRequest(request.getParameter(InputKeys.NEW_PASSWORD));
		
		String username = request.getParameter(InputKeys.USERNAME);
		String password = request.getParameter(InputKeys.PASSWORD);
		User user = new User();
		user.setUserName(username);
		user.setPassword(password);
		newRequest.setUser(user);
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return newRequest;
	}
}
