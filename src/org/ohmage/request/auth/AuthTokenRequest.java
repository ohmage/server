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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.cache.UserBin;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest;

/**
 * <p>Uses the username and password parameters to create a request for an
 * authentication / session token.</p>
 * 
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username of the user that is attempting to login.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The password of the user that is attempting to login.</td>
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
public class AuthTokenRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthTokenRequest.class);
	
	public static final String KEY_AUTH_TOKEN = "token";
	
	/**
	 * Creates a new authentication token request with a preset map of 
	 * parameters.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The map of parameters to use.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AuthTokenRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, false, null, parameters);
		
		if(! isFailed()) {
			LOGGER.info("Building an authentication token request.");
		}
	}
	
	/**
	 * A request for an authentication token.
	 * 
	 * @param httpRequest The HTTP request containing the parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AuthTokenRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, true, null, null);
		
		LOGGER.info("Building an authentication token request.");
	}

	/**
	 * Authenticates the request with the username and password that were 
	 * passed in as parameters, and, if successful, adds the user to the bin 
	 * generating a token.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the authentication token request.");

		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		UserBin.addUser(getUser());
	}

	/**
	 * Replies to the request with no data and instantiates the token in the
	 * request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the authentication token request.");
		
		respond(httpRequest, httpResponse, KEY_AUTH_TOKEN, (getUser() == null) ? null : getUser().getToken());
	}
}
