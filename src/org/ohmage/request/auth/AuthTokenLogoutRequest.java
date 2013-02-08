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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.UserBin;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;

/**
 * <p>Removes the user's authentication token from the bin.</p>
 * 
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
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
public class AuthTokenLogoutRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthTokenLogoutRequest.class);
	
	/**
	 * Creates a request for deleting the user's authentication token from the
	 * bin.
	 * 
	 * @param httpRequest The HTTP request containing the parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AuthTokenLogoutRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.EITHER, null);
		
		LOGGER.info("Creating a logout request.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the logout request.");

		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		UserBin.expireUser(getUser().getToken());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the logout request.");
		
		if(getUser() != null) {
			final String token = getUser().getToken(); 
			
			if(token != null) {
				
				Cookie authTokenCookie = new Cookie(InputKeys.AUTH_TOKEN, token);
				authTokenCookie.setHttpOnly(false);
				authTokenCookie.setMaxAge(0);
				authTokenCookie.setPath("/");
				httpResponse.addCookie(authTokenCookie);
				
			}
			UserBin.expireUser(token);
		}
		
		JSONObject response = new JSONObject();
		try {
			
			response.put(JSON_KEY_METADATA, JSONObject.NULL);
			response.put(JSON_KEY_DATA, JSONObject.NULL);
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the response.", e);
			setFailed();
			
		}
		
		super.respond(httpRequest, httpResponse, response);
	}
}
