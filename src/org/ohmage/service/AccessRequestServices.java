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
package org.ohmage.service;


import java.util.Collection;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.AccessRequest;
import org.ohmage.domain.AccessRequest.Status;
import org.ohmage.domain.AccessRequest.Type;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.impl.AccessRequestQueries;

/**
 * This class contains the services that pertain to UserSetupRequest.
 * 
 * @author Hongsuda T.
 */
public final class AccessRequestServices {
	private static final Logger LOGGER = Logger.getLogger(AccessRequestServices.class);

	private static AccessRequestServices instance;
	private AccessRequestQueries userSetupRequestQueries;

	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iClassQueries is null
	 */	
	private AccessRequestServices(AccessRequestQueries userSetupRequestQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this userSetupRequest already exists.");
		}
		
		this.userSetupRequestQueries = userSetupRequestQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static AccessRequestServices instance() {
		return instance;
	}
	
	/**
	 * Creates a new user setup request in the system.
	 * 
	 * @param requestId The unique identifier for the new request.
	 * 
	 * @param username The username associated with the request.
	 * 
	 * @param emailAddress The user's email address.
	 * 
	 * @param requestContent The request content
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void createAccessRequest(
			final String requestId, 
			final String username,
			final String emailAddress,
			final String requestType,
			final JSONObject requestContent) 
			throws ServiceException {
		
		String defaultStatus = AccessRequest.getDefaultStatus().toString();
		
		try {			
			// Check whether the request already exists
			checkRequestExistence(requestId, false);
			
			// Check whether there are other pending request from the same user.
			// There can only be one PENDING request per type. 
			if (userSetupRequestQueries.getRequestExists(username, requestType, defaultStatus))
				throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
						"There is already a pending request from the same user and type.");
			
			// user_setup request
			if (requestType.equals(AccessRequest.Type.USER_SETUP.toString())) {
			// Check whether the user already has the user setup privileges
			// (i.e. both class creation and user setup)
				if (userSetupRequestQueries.getUserSetupPrivilegesExist(username))
					throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
							"The user already has the privileges.");
			}
			
			// Create a request with pending status
			userSetupRequestQueries.createAccessRequest(requestId, username, emailAddress, 
					requestContent.toString(), requestType, defaultStatus);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if a request exists and compares that value to whether or not it
	 * should exist. If they don't match or there is an error, it will set the
	 * request as failed with an error message if the reason for failure is
	 * known and throw a ServiceException.<br />
	 * <br />
	 * Note: Passing in a value of null will always result in the class not
	 * existing.
	 * 
	 * @param requestId The request UUID to check for existence.
	 * 
	 * @param shouldExist Whether or not the class should already exist.
	 * 
	 * @throws ServiceException Thrown if there is an error, the class doesn't
	 * 							exist and it should, or the class does exist
	 * 							and it shouldn't.
	 */
	public void checkRequestExistence(final String requestId, 
			final boolean shouldExist) throws ServiceException {
		
		try {
			if((requestId != null) && userSetupRequestQueries.getRequestExists(requestId)) {
				if(! shouldExist) {
					throw new ServiceException(
							ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
							"The setup request already exists: " + requestId
						);
				}
			}
			else {
				if(shouldExist) {
					throw new ServiceException(
							ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
							"The setup request does not exist: " + requestId
						);
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	
	
	/**
	 * Searches for all requests that match the given parameters. 
	 *  
	 * @param requester The requester username. 
	 * 
	 * @param requestIds A list of user setup request UUIDs to be used to limit
	 *                   the requests whose ids are in the list. 
	 * 
	 * @param userIds A list of usernames to be used to limit the requests to 
	 * 	              only requests with owners that are in the list. 
	 * 
	 * @param emailAddressTokens A list of search tokens to be used to limit the 
	 *                           results to only those requests with email addresses 
	 *                           contain those tokens. 
	 *                            
	 * @param requestContentTokens A list of search tokens to be used to limit the 
	 *                           results to only those requests with content 
	 *                           contain those tokens. 
	 * 
	 * @param requestStatus The status of the requests to search for.  
	 * 	 
	 * @param fromDate The datetime limiting the results to only those requests whose 
	 *                 creation time is later than this parameter. 
	 *                 
	 * @param toDate The datetime limiting the results to only those requests whose 
	 *                 creation time is prior to this parameter. 
	 *
	 * @param numSkip Number of items to skipped in the result. 
	 * 
	 * @param numRetrieve Number of items in the result to be returned. 
	 * 
	 * @return A list of UserSetupRequest object. 
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Collection<AccessRequest> getAccessRequests(
			final String requester,
			final Collection<String> requestIds,
			final Collection<String> userIds,
			final Collection<String> emailAddressTokens,
			final Collection<String> requestContentTokens,
			final String requestType,
			final String requestStatus,
			final DateTime fromDate,
			final DateTime toDate,
			final Integer numSkip,
			final Integer numRetrieve)
			throws ServiceException {
		
		try {
			Collection<AccessRequest> requests = 
					userSetupRequestQueries
						.getAccessRequests(
								requester, 
								requestIds,
								userIds,
								emailAddressTokens,
								requestContentTokens,
								requestType,
								requestStatus,
								fromDate,
								toDate);
			
			return requests;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verify that the user can access the specific request. The admin can access any request.
	 * The owner can only access their requests.
	 *  
	 * @param requestId The request UUID.
	 *  
	 * @param username The username to check whether this person has access to the request. 
	 * 
	 * @throws ServiceException If the user doesn't have the right to access the request. 
	 * 
	 */
	public void verifyUserCanAccessRequest(final String requestId, final String username) throws ServiceException {	
		try {
			if (! userSetupRequestQueries.getUserCanAccessRequest(requestId, username)) {
				throw new ServiceException(
						ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
						"Access denied. The user can't access the user setup request: " + requestId
					);
			}
		} catch (DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verify that the user can access a list of user setup request. The admin can access any request.
	 * The owner can only access their requests.
	 *  
	 * @param requestIds A list of request UUIDs to check. 
	 *  
	 * @param username The username to check whether this person has access to the request. 
	 * 
	 * @throws ServiceException If the user doesn't have the right to access ALL requests in the list. 
	 * 
	 */
	public void verifyUserCanAccessRequests(final Collection<String> requestIds, final String username) throws ServiceException {	
		try {
			if (! userSetupRequestQueries.getUserCanAccessRequests(requestIds, username)) {
				throw new ServiceException(
						ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
						"Access denied. The user can't access the user setup request: " + requestIds
					);
			}
		} catch (DataAccessException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * update the user setup request in the system.
	 * 
	 * @param requestId The unique identifier for the existing request
	 * 					to be updated.
	 * 
	 * @param emailAddress The user's email address to be updated.
	 * 
	 * @param requestContent The request content to be updated.
	 * 
	 * @param requestStatus The new request status to be updated.
	 * @throws ServiceException Thrown if there is an error.
	 */
	// only admin can do this
	public void updateAccessRequest(
			final String username,
			final String requestId, 
			final String emailAddress, 
			final JSONObject requestContent, 
			final String requestType,
			final String requestStatus) throws ServiceException {

		Boolean updateUserPrivileges = null;
		String contentString = null;
		AccessRequest.Type finalType = null;
		try {
			
			// Check whether the request with the requestId exist
			// checkRequestExistence(requestId, true);
			
			// get the existing request 
			AccessRequest request = userSetupRequestQueries.getAccessRequest(username,requestId); 
			if (request == null) {
				throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
						"Request doesn't exist.");
			} else {
				finalType = request.getType();
			}

			if (requestType == null) 
				finalType = request.getType();
			else finalType = AccessRequest.Type.getValue(requestType); 
			
			if (finalType.equals(Type.USER_SETUP)) {
				// if status has changed
				if ((requestStatus != null) &&
					(! request.getStatus().toString().equals(requestStatus)))
				{
					// Only an admin can update the status.
					LOGGER.info("Checking that the user is an admin to change the user setup status.");
					UserServices.instance().verifyUserIsAdmin(username);
					
					if (requestStatus.equals(Status.APPROVED.toString())){
						updateUserPrivileges  = true;
					} else if (requestStatus.equals(Status.REJECTED.toString())){
						updateUserPrivileges = false;
					} else { // pending : 
						// check whether it already exists
						if (userSetupRequestQueries.getRequestExists(username, requestType, requestStatus)) {
							throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
									"There is already a pending request from the same user and type.");
						} 
						// check whether the user already have the privileges
						else {
							if (userSetupRequestQueries.getUserSetupPrivilegesExist(username))
								throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR,
										"The user already has the privileges.");							
						}
					}				
				} else {			
					// other changes excluding status: verify that the requester is the owner or an admin 
					AccessRequestServices.instance().verifyUserCanAccessRequest(requestId, username);
				}
					
			} else {
				// other types.. doesn't exist yet
			}
			

			if (requestContent != null)
				contentString = requestContent.toString();
			
			userSetupRequestQueries.updateAccessRequest(requestId, emailAddress, contentString, requestType, requestStatus, updateUserPrivileges);

		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	
	/**
	 * Deletes the request.
	 * 
	 * @param requestIds A list of request UUIDs to be deleted. 
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	// only an admin can do this
	public void deleteAccessRequests(final Collection<String> requestIds, final String username) 
			throws ServiceException {
		
		try {
			
			// Check whether the request with the requestId exist
			if (! userSetupRequestQueries.getRequestsExist(requestIds))
				throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR, "Some request ids do not exist!");

			// check whether user can delete them all
			if (! userSetupRequestQueries.getUserCanAccessRequests(requestIds, username))
				throw new ServiceException(ErrorCode.USER_ACCESS_REQUEST_EXECUTION_ERROR, "User doesn't have access to all requests!");
						
			
			userSetupRequestQueries.deleteAccessRequests(requestIds);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}
