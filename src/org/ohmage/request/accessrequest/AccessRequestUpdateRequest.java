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
package org.ohmage.request.accessrequest;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.service.AccessRequestServices;
import org.ohmage.validator.UserValidators;
import org.ohmage.domain.AccessRequest;

/**
 * <p>Creates a new AccessRequestUpdateRequest. </p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USER_ACCESS_REQUEST_ID}</td>
 *     <td>The uuid of the user setup request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#EMAIL_ADDRESS}</td>
 *     <td>The requester's email address.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ACCESS_REQUEST_CONTENT}</td>
 *     <td>The content of the user setup request e.g. project name, objectives, etc.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Hongsuda T.
 */
public class AccessRequestUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AccessRequestUpdateRequest.class);
	
	private final String emailAddress;
	private final JSONObject requestContent; 	
	private final String uuid;
	private final String requestType;
	private final String requestStatus;

	
	/**
	 * Creates a user creation request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required and
	 * 					  optional parameters for creating this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public AccessRequestUpdateRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);

		String tEmailAddress = null;
		JSONObject tRequestContent = null; 
		String tUuid = null;
		String tRequestType = null;
		String tRequestStatus = null;

		if(! isFailed()) {
			LOGGER.info("Creating an AccessRequestUpdateRequest.");
		
			try {
				String[] t;
				
				// request's uuid
				t = getParameterValues(InputKeys.USER_ACCESS_REQUEST_ID);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_ACCESS_REQUEST_INVALID_PRAMETER,
							"Multiple request_id parameters were given: " +
								InputKeys.USER_ACCESS_REQUEST_ID);
				}
				else if(t.length == 1) {
					tUuid = AccessRequest.validateRequestId(t[0]);
				}
				else {
					throw new ValidationException(
							ErrorCode.USER_ACCESS_REQUEST_INVALID_PRAMETER,
							"Missing parameter: " +
								InputKeys.USER_ACCESS_REQUEST_ID);
				}

				// notify email address (optional)
				t = getParameterValues(InputKeys.EMAIL_ADDRESS);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_EMAIL_ADDRESS,
							"Multiple email address parameters were given: " +
								InputKeys.EMAIL_ADDRESS);
				}
				else if(t.length == 1) {
					tEmailAddress = UserValidators.validateEmailAddress(t[0]);
				}
				
				// request content (optional)
				t = getParameterValues(InputKeys.USER_ACCESS_REQUEST_CONTENT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_ACCESS_REQUEST_INVALID_PRAMETER,
							"Multiple request content were given: " +
								InputKeys.USER_ACCESS_REQUEST_CONTENT);
				}
				else if(t.length == 1) {
					tRequestContent = AccessRequest.validateRequestContent(t[0]);
				}

				// type (optional)
				t = getParameterValues(InputKeys.USER_ACCESS_REQUEST_TYPE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_ACCESS_REQUEST_INVALID_PRAMETER,
							"Multiple request status were given: " +
								InputKeys.USER_ACCESS_REQUEST_TYPE);
				}
				else if(t.length == 1) {
					tRequestType = AccessRequest.validateRequestType(t[0]);
				}
				
				// status (optional)
				t = getParameterValues(InputKeys.USER_ACCESS_REQUEST_STATUS);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_ACCESS_REQUEST_INVALID_PRAMETER,
							"Multiple request status were given: " +
								InputKeys.USER_ACCESS_REQUEST_STATUS);
				}
				else if(t.length == 1) {
					tRequestStatus = AccessRequest.validateRequestStatus(t[0]);
				}
				
				// one or more of the following parameters need to be present to update request
				if ((tEmailAddress == null) && (tRequestContent == null) 
						&& (tRequestStatus == null && (tRequestType == null))){
					throw new ValidationException(
							ErrorCode.USER_ACCESS_REQUEST_INVALID_PRAMETER,
							"Require one or more parameters to update the request : " +
							InputKeys.EMAIL_ADDRESS + ", " + 
							InputKeys.USER_ACCESS_REQUEST_CONTENT + ", " +
							InputKeys.USER_ACCESS_REQUEST_STATUS);					
				}
			
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		uuid = tUuid;
		emailAddress = tEmailAddress;
		requestContent = tRequestContent;
		requestType = tRequestType;
		requestStatus = tRequestStatus;
	}

	
	/**
	 * Services this request if an existing user is making the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the AccessRequestUpdateRequest.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			
			LOGGER.info("Update the AccessRequest.");
			AccessRequestServices.instance().updateAccessRequest(this.getUser().getUsername(), uuid, emailAddress, 
					requestContent, requestType, requestStatus);
			
			
			// TODO: if successful, send an email notification
			
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success or failure and a message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
}