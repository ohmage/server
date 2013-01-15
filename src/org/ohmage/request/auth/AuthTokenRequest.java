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
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.UserBin;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.validator.ServerValidators;

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
	
	private final URL redirect;
	
	/**
	 * Creates a new authentication token request with a preset map of 
	 * parameters.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The map of parameters to use.
	 * 
	 * @param callClientRequester Use the name "requester" in place of 
	 * 							  "client".
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AuthTokenRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final boolean callClientRequester,
			final String redirect)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, true, null, parameters, callClientRequester);
		
		URL tRedirect = null;
		
		if(! isFailed()) {
			LOGGER.info("Building an authentication token request.");
			
			try {
				if(redirect != null) {
					tRedirect = ServerValidators.validateRedirect(redirect);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		this.redirect = tRedirect;
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
	public AuthTokenRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, true, null, null);
		
		URL tRedirect = null;
		
		if(! isFailed()) {
			LOGGER.info("Building an authentication token request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.REDIRECT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_REDIRECT,
							"Multiple redirects were given: " +
								InputKeys.REDIRECT);
				}
				else if(t.length == 1) {
					LOGGER.debug("Redirect: " + t[0]);
					
					tRedirect = ServerValidators.validateRedirect(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		redirect = tRedirect;
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
		
		try {
			UserBin.addUser(getUser());
		}
		catch(DomainException e) {
			e.logException(LOGGER);
			e.failRequest(this);
		}
	}

	/**
	 * Replies to the request with no data and instantiates the token in the
	 * request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the authentication token request.");
		
		if((! isFailed()) && (redirect != null)) {
			respond(httpRequest, httpResponse, redirect);
		}
		else {
			respond(
				httpRequest, 
				httpResponse, 
				KEY_AUTH_TOKEN,
				(getUser() == null) ? null : getUser().getToken());
		}
	}
}
