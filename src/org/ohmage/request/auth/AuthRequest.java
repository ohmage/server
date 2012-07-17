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
package org.ohmage.request.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest;

/**
 * <p>An authentication request where the user's actual password is submitted
 * and, if correct, the hashed version of the password is returned. This hashed
 * password is what is used throughout the rest of the APIs.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username of the requester.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The plaintext password of the requester.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class AuthRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthRequest.class);

	public static final String KEY_HASHED_PASSWORD = "hashed_password";
	
	/**
	 * Creates this authentication request based on the information in the 
	 * httpRequest.
	 * 
	 * @param httpRequest A HttpServletRequest containing the parameters for
	 * 					  this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AuthRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, true, null, null);
		
		LOGGER.info("Building an username / password authentication request.");
	}

	/**
	 * Services the request. This simply authenticates the user.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the authentication request.");
		
		authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED);
	}

	/**
	 * Responds with either success and the user's hashed password or failure
	 * and an error message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the authentication request.");
		
		respond(httpRequest, httpResponse, KEY_HASHED_PASSWORD, (getUser() == null) ? null : getUser().getPassword());
	}
}
