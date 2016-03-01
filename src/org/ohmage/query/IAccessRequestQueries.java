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
package org.ohmage.query;

import java.util.Collection;
import java.util.List;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.domain.AccessRequest;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;

/**
 * Interface to facilitate mocking concrete implementations for test cases. 
 * 
 * @author Hongsuda T.
 */
public interface IAccessRequestQueries {

	/**
	 * Create a new user setup request record with specified properties 
	 * in the system. 
	 * 
	 * @param requestId The request id associated with the request.
	 * 
	 * @param username The username associated with the request.
	 * 
	 * @param emailAddress An email address associated with the request.
	 * 
	 * @param requestContent The request content.
	 * 
	 * @param requestStatus The status associated with the request. 
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */	
	public void createAccessRequest(
			final String requestId, 
			final String username, 
			final String emailAddress, 
			final JSONObject requestContent, 
			final String requestType,
			final String requestStatus) throws DataAccessException;
	

	/**
	 * Check whether a request with specified properties exist.
	 * 
	 * @param requestId The request id associated with the request.
	 * 
	 * @return a boolean indicating whether a request exists in the system.
	 *
	 * @throws ServiceException Thrown if there is an error.
	 */	
	Boolean getRequestExists(String requestId) throws DataAccessException;


	/**
	 * Check whether a request with specified properties exist.
	 * 
	 * @param username The username associated with the request.
	 * 
	 * @param requestType The request type. 
	 * 
	 * @param requestStatus The status associated with the request. 
	 * 
	 * @return a Boolean indicating whether a request exists in the system.
	 *
	 * @throws ServiceException Thrown if there is an error.
	 */	
	Boolean getRequestExists(String username, String requestType, String requestStatus)
			throws DataAccessException;


	/**
	 * Check whether a list of requests exist in the system.
	 * 
	 * @param requestIds A list of request UUIDs
	 * 
	 * @return a Boolean indicating whether all requests in the list exist in the system.
	 *
	 * @throws ServiceException Thrown if there is an error.
	 */	
	Boolean getRequestsExist(Collection<String> requestIds)
			throws DataAccessException;


	/**
	 * Check whether a user has the user creation and user setup privileges. 
	 * 
	 * @param username The username to check for the privileges. 
	 * 
	 * @return a Boolean indicating whether the user has the user creation and 
	 *         user setup privileges. 
	 *
	 * @throws ServiceException Thrown if there is an error.
	 */	
	Boolean getUserSetupPrivilegesExist(String username)
			throws DataAccessException;


	/**
	 * Check whether a user has access to the request. An admin can access any request. 
	 * The requester can access his/her own requests.  
	 * 
	 * @param requestId The UUID of the request to check against. 
	 * 
	 * @param uername The username to check whether the user has access to the request. 
	 * 
	 * @return a Boolean indicating whether the user can access the request. 
	 *
	 * @throws ServiceException Thrown if there is an error.
	 */	
	Boolean getUserCanAccessRequest(String requestId, String username)
			throws DataAccessException;


	/**
	 * Check whether a user has access to all requests in the list. If any item in the list is not 
	 * accessible by the user, the method returns false. An admin can access any request. 
	 * The requester can access his/her own requests.   
	 * 
	 * @param requestIds A list of request UUIDs.  
	 * 
	 * @param uername The username to check whether the user has access to the requests. 
	 * 
	 * @return a Boolean indicating whether the user can access to all the requests in the list. 
	 *
	 * @throws ServiceException Thrown if there is an error.
	 */	
	Boolean getUserCanAccessRequests(Collection<String> requestIds,
			String username) throws DataAccessException;

	/**
	 * Search for user setup requests with provided criteria. 
	 * 
	 * @param requester The requester's username
	 * 
	 * @param requestIds A list of user setup request UUIDs to be used to limit
	 *                   the requests whose ids are in the list. 
	 * 
	 * @param userIds A list of usernames to be used to limit the requests to 
	 * 	              only requests with owners that are in the list. 
	 * 
	 * @param emailAddressTokens A token to limit the results only
	 * 					to those requests with email addresses that 
	 * 					contain the value. 
	 * 
	 * @param requestContent A token to limit the results only
	 * 					to those requests with request content that 
	 * 					contain the value. 
	 * 
	 * @param requestType Only return the requests with specified type.
	 * 
	 * @param requestStatus Only return the requests with specified status.
	 * 
	 * @param fromDate A start date limiting the results only to 
	 * 				those that were created after this date.
	 * 
	 * @param toDate An end date limiting the results only to 
	 * 				those that were created prior to this date.
	 *
	 * @return A collection of UserSetupRequest.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	List<AccessRequest> getAccessRequests(String requester,
			Collection<String> requestIds, Collection<String> userIds,
			Collection<String> emailAddressTokens,
			Collection<String> requestContentTokens, 
			String requestType, String requestStatus,
			DateTime fromDate, DateTime toDate) throws DataAccessException;

	
	/**
	 * Update user setup request. If the status is set to APPROVED, 
	 * update user's class creation and user setup privileges. 
	 * If the status is set to REJECTED, revoke user's class creation
	 * and user setup privileges. 
	 * 
	 * @param requestId The requestId to be updated.
	 * 
	 * @param emailAddress The email address associated with the request
	 * 					to be updated.
	 * 
	 * @param requestContent The request content to be updated.
	 * 
	 * @param requestType The request type to be updated.
	 * 
	 * @param requestStatus The request status to be updated.
	 * 
	 * @param updateUserPrivileges If true, grant access. If false, 
	 * 				revoke access. If null, do nothing. 
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	void updateAccessRequest(String requestId, String emailAddress,
			JSONObject requestContent, String requestType, String requestStatus,
			Boolean updateUserPrivileges)
			throws DataAccessException;


	/**
	 * Delete user setup request.
	 * 
	 * @param requestIds A list of request UUIDs to be deleted.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	void deleteAccessRequests(Collection<String> requestIds)
			throws DataAccessException;
	
	
}