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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.AccessRequestServices;
import org.ohmage.domain.AccessRequest;


/**
 * <p>Creates a new userSetupRequest. </p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USER_SETUP_REQUEST_ID_LIST}</td>
 *     <td>A list of user setup request UUIDs to be used to limit the requests 
 *     whose ids are in the list. </td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_SETUP_REQUEST_USER_LIST}</td>
 *     <td>A list of usernames to be used to limit the requests to 
 * 	              only requests with owners that are in the list.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_SETUP_REQUEST_EMAIL_ADDRESS_SEARCH}</td>
 *     <td>A list of search tokens to be used to limit the results to only those 
 *     requests with email addresses contain those tokens.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_SETUP_REQUEST_CONTENT_SEARCH}</td>
 *     <td> A list of search tokens to be used to limit the results to only those 
 *     requests with content contain those tokens.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_SETUP_REQUEST_STATUS}</td>
 *     <td>The status of the requests to search for.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>The datetime limiting the results to only those requests whose 
 *         creation time is later than this parameter.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>The datetime limiting the results to only those requests whose 
 *          creation time is prior to this parameter. </td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Hongsuda T.
 */
public class AccessRequestReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AccessRequestReadRequest.class);
	
	private final Collection<String> requestIdList;
	private final Collection<String> userList;
	private final Collection<String> emailAddressSearchTokens;
	private final Collection<String> contentSearchTokens;
	private final String requestType;
	private final String requestStatus;
	private final DateTime startDate;
	private final DateTime endDate;
	private Collection<AccessRequest> results = null;

	
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
	public AccessRequestReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);

		
		Collection<String> tRequestIdList = null;
		Collection<String> tUserList = null;
		Collection<String> tEmailAddressSearchTokens = null;
		Collection<String> tContentSearchTokens = null;
		String tRequestType = null;
		String tRequestStatus = null;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user creation request.");
		
			try {
				String[] t;
				
				// request's uuid (optional)
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_ID_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
							"Multiple request_id parameters were given: " +
								InputKeys.USER_SETUP_REQUEST_ID_LIST);
				}
				else if(t.length == 1) {
					tRequestIdList = AccessRequest.validateRequestIdList(t[0]);
				}

				// request's uuid (optional)
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_USER_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
							"Multiple request_id parameters were given: " +
								InputKeys.USER_SETUP_REQUEST_USER_LIST);
				}
				else if(t.length == 1) {
					tUserList = AccessRequest.validateUserList(t[0]);
				}

				// notify email address (optional)
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_EMAIL_ADDRESS_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_EMAIL_ADDRESS,
							"Multiple email address parameters were given: " +
								InputKeys.EMAIL_ADDRESS);
				}
				else if(t.length == 1) {
					tEmailAddressSearchTokens = AccessRequest.validateEmailAddressSearch(t[0]);
				}
				
				// request content (optional)
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_CONTENT_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
							"Multiple request content were given: " +
								InputKeys.USER_SETUP_REQUEST_CONTENT_SEARCH);
				}
				else if(t.length == 1) {
					tContentSearchTokens = AccessRequest.validateContentSearch(t[0]);
				}
				
				// request type (optional)
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_TYPE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
							"Multiple request type were given: " +
								InputKeys.USER_SETUP_REQUEST_TYPE);
				}
				else if(t.length == 1) {
					tRequestType = AccessRequest.validateRequestType(t[0]);
				}
				
				// status (optional)
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_STATUS);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
							"Multiple request status were given: " +
								InputKeys.USER_SETUP_REQUEST_STATUS);
				}
				else if(t.length == 1) {
					tRequestStatus = AccessRequest.validateRequestStatus(t[0]);
				}
				
				// Start Date (optional)
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple start dates were given: " + 
								InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = AccessRequest.validateDate(t[0]);
				}
				
				// End Date
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple end dates were given: " + 
								InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = AccessRequest.validateDate(t[0]);
				}
				
				// add number to skip and num to retrieve
				
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		requestIdList = tRequestIdList;
		userList = tUserList;
		emailAddressSearchTokens = tEmailAddressSearchTokens;
		contentSearchTokens = tContentSearchTokens;
		requestType = tRequestType;
		requestStatus = tRequestStatus;
		startDate = tStartDate;
		endDate = tEndDate;
	}

	
	/**
	 * Services this request if an existing user is making the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			
			LOGGER.info("Read the UserSetupRequest.");
			results = AccessRequestServices.instance().getUserSetupRequests(
					this.getUser().getUsername(), requestIdList, userList, emailAddressSearchTokens, 
					contentSearchTokens, requestType, requestStatus, startDate, endDate, null, null);
			
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
		
		JSONObject jsonResult = new JSONObject();
		
		for(AccessRequest request : results) {
			try {
				jsonResult.put(request.getRequestId(), request.toJsonObject());
			}
			catch(JSONException e) {
				LOGGER.error("Error building the result JSONObject.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, jsonResult);
	}
}