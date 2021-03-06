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
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;

/**
 * <p>Updates a user's own password. The user must give their username and
 * current password in order to call this API and can only change their own
 * password.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#NEW_PASSWORD}</td>
 *     <td>The user's new plaintext password.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the user whose password you are attempting to 
 *       change.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserChangePasswordRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(UserChangePasswordRequest.class);
	
	private final String newPassword;
	private final String username;
	private final boolean validSession;
	
	private String hashedPassword;
	
	/**
	 * Creates a password change request.
	 * 
	 * @param httpRequest The HttpServletRequest with all of the parameters for
	 * 					  this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserChangePasswordRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, true, null, null);
		
		String tNewPassword = null;
		String tUsername = null;
		boolean tValidSession = false;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user change password request.");
			
			String[] t;
			try {
				t = getParameterValues(InputKeys.NEW_PASSWORD);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PASSWORD, 
							"The new password is missing: " + 
								InputKeys.NEW_PASSWORD);
				}
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PASSWORD, 
							"Multiple new password parameters were given.");
				}
				else {
					tNewPassword =
						UserValidators.validatePlaintextPassword(t[0]);

					if(tNewPassword == null) {
						throw new ValidationException(
								ErrorCode.USER_INVALID_PASSWORD, 
								"The new password is missing: " + 
									InputKeys.NEW_PASSWORD);
					}
				}
				
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Multiple usernames were given: " +
								InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					tUsername = UserValidators.validateUsername(t[0]);
				}
				
				try {
					// If a user is returned, then the sesion is valid.
					// Otherwise, there is not token so there is no session.
					tValidSession = 
						retrieveToken(httpRequest, TokenLocation.EITHER) !=
							null;
				}
				// The token is unknown, so the session is no longer valid.
				catch(ValidationException e) {
					tValidSession = false;
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		newPassword = tNewPassword;
		username = tUsername;
		validSession = tValidSession;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the change password request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_ALLOWED)) {
			// If it's a valid session, change the error code.
			if(validSession) {
				setFailed(
					ErrorCode.AUTHENTICATION_FAILED_BUT_NEW_ERROR_CODE_BECAUSE_WHY_NOT,
					getAnnotator().getErrorText());
			}
			
			// Exit either way.
			return;
		}
		
		try {
			if((username != null) && (! username.equals(getUser().getUsername()))) {
				LOGGER
					.info(
						"The requester is attempting to change another " +
							"user's password: " +
							username);
				LOGGER
					.info(
						"Verfying that the requesting user is allowed to " +
							"change the other user's password.");
				UserServices
					.instance()
					.verifyUserCanChangeOtherUsersPassword(
						getUser().getUsername(),
						username);
			}
			
			LOGGER.info("Updating the user's password.");
			if(username == null) {
				hashedPassword =
					UserServices
						.instance()
						.updatePassword(getUser().getUsername(), newPassword);
			}
			else {
				hashedPassword =
					UserServices
						.instance()
						.updatePassword(username, newPassword);
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success if the user's password was successfully changed.
	 * Otherwise, a failure message and description are returned.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the change password request.");
		
		super
			.respond(
				httpRequest,
				httpResponse,
				"hashed_password",
				hashedPassword);
	}
}
