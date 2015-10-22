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
package org.ohmage.request.user;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.UserSummary;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;

/**
 * <p>Gathers information about a user including their campaign creation 
 * privilege, the campaigns and classes with which they are associated, and the
 * union of their roles in all of those campaigns and classes.</p>
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
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>Username. If a username is provided, it will be used instead of 
 *         the requesting username. Only an admin can perform this operation. </td>
 *     <td>true</td>
 *   </tr>

 * </table>
 * 
 * @author John Jenkins
 * @author Hongsuda T. 
 */
public class UserInfoReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserInfoReadRequest.class);
	
	private String gUsername;
	private UserSummary result = null;
	
	/**
	 * Creates a new user info read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserInfoReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, false, TokenLocation.EITHER, null);

		String tUsername = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user info read request.");
	
			String[] t;
			try {
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Multiple usernames were given: " +
								InputKeys.USERNAME);
					}		
				else if(t.length == 1) {
					if(! StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tUsername = t[0];
					} 
				}
				// if username is not provided, assume username is the requesting user
				if (tUsername == null)
					tUsername = getUser().getUsername();
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		} 
		
		gUsername = tUsername;
		result = null;
		
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user info read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			
			// Need to be an admin to look at other user's info
			if (gUsername != getUser().getUsername()) {
				LOGGER.info("Checking that the user is an admin: " + getUser().getUsername());
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			}
			
			LOGGER.info("Gathering the information about the user " + gUsername);
			result = UserServices.instance().getUserSummary(gUsername);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user with either a success message and the information
	 * about the requesting user or a failure message with an explanation.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the user info read request.");
		
		JSONObject jsonResult = new JSONObject();
		
		if(result != null) {
			try {
				jsonResult.put(gUsername, result.toJsonObject());
			}
			catch(JSONException e) {
				LOGGER.error("There was an error building the JSONObject result.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, jsonResult);
	}
}
