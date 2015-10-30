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
package org.ohmage.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.util.DateTimeUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.UserValidators;

/**
 * An internal representation of a user setup request. 
 * 
 * @author Hongsuda T.
 */

public class AccessRequest {

	private static final Logger LOGGER = Logger.getLogger(AccessRequest.class);
	private static String JSON_KEY_UUID = "uuid";
	private static String JSON_KEY_USERNAME = "user";
	private static String JSON_KEY_EMAIL_ADDRESS = "email_address";
	private static String JSON_KEY_REQUEST_TYPE = "type";
	private static String JSON_KEY_REQUEST_CONTENT = "content";
	private static String JSON_KEY_STATUS = "status";
	private static String JSON_KEY_CREATION_TIME = "creation_time";
	
	// the keys expected from the content json object
	private static String REQUEST_JSON_KEY_CONTENT = "request";
	
	// default request status
	private static Status DEFAULT_STATUS = Status.PENDING;
	private static Type DEFAULT_TYPE = Type.USER_SETUP;

	private static int MAX_LIST_LENGTH = 100; 
	
	private final String uuid;
	private final String username;
	private final String emailAddress;
	private final String requestType;
	private final String requestContent;
	private final Status status;
	private final DateTime creationTime;
	private final DateTime lastModifiedTime;
	
	/**
	 * User Setup Request potential status
	 * 
	 * @author Hongsuda T.
	 */
	public static enum Status {
		PENDING,
		APPROVED,
		REJECTED;
		
		/**
		 * Converts a String value into a Status or throws an exception if there
		 * is no comparable status.
		 * 
		 * @param status The string to be converted into a status enum.
		 * 
		 * @return A comparable Status enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									Status enum.
		 */
		public static Status getValue(String status) {
			if (status == null)
				return null;
			return valueOf(status.toUpperCase());
		}
		
		/**
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * User Setup Request potential status
	 * 
	 * @author Hongsuda T.
	 */
	public static enum Type {
		USER_SETUP;
		
		/**
		 * Converts a String value into a Status or throws an exception if there
		 * is no comparable status.
		 * 
		 * @param type The string to be converted into a status enum.
		 * 
		 * @return A comparable Status enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									Status enum.
		 */
		public static Type getValue(String type) {
			if (type == null)
				return null;
			return valueOf(type.toUpperCase());
		}
		
		/**
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * Creates a new UserSetupRequest object that contains information about
	 * a user setup request. All values should be taken from the database.
	 * 
	 * @param requestId The unique identifier for the new request.
	 * 
	 * @param username The username associated with the request.
	 * 
	 * @param emailAddress The user's email address.
	 * 
	 * @param requestContent The request content.
	 * 
	 * @param status The status associated with the request.
	 * 
	 * @param creationTime The creation time of the request. 
	 * 
	 * @param lastModifiedTime The last time when the request was modified. 
	 * 
	 * @throws DomainException Thrown if any of the parameters are invalid.
	 */

	public AccessRequest(
			final String id,
			final String username, 
			final String emailAddress,
			final String requestType,
			final String requestContent, 
			final String status,
			final DateTime creationTime,
			final DateTime lastModifiedTime) 
			throws DomainException {

		this.uuid = id;
		this.username = username;
		this.emailAddress = emailAddress;
		this.requestType = requestType;
		this.requestContent = requestContent;
		this.status = Status.getValue(status);
		this.creationTime = creationTime;
		this.lastModifiedTime = lastModifiedTime;
	}
	
	/**
	 * Returns this UserSetupRequest object as a JSONObject 
	 *  
	 * @return Returns a JSONObject containing the user setup request information.
	 * 
	 * @throws JSONException Thrown if generating the object caused an error.
	 */
	public JSONObject toJsonObject() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_USERNAME, username);
		// result.put(JSON_KEY_UUID, uuid.toString());
		result.put(JSON_KEY_EMAIL_ADDRESS, emailAddress);
		result.put(JSON_KEY_REQUEST_TYPE, requestType);
		result.put(JSON_KEY_REQUEST_CONTENT, requestContent);
		if (status != null)
			result.put(JSON_KEY_STATUS, status.toString());
		if (creationTime != null)
			result.put(JSON_KEY_CREATION_TIME, DateTimeUtils.getIso8601DateString(creationTime, true));
		if (lastModifiedTime != null)
			result.put(JSON_KEY_CREATION_TIME, DateTimeUtils.getIso8601DateString(lastModifiedTime, true));
		
		return result;
	}
	
	/**
	 *  Get the user setup request uuid.
	 * 
	 * @return uuid of the user setup request.
	 */
	public String getRequestId(){
		return uuid;
	}

	/**
	 *  Get the default status associated with a request.
	 * 
	 * @return default status of a new request.
	 */
	public static Status getDefaultStatus() {
		return DEFAULT_STATUS;
	}
	
	/**
	 *  Get the default status associated with a request.
	 * 
	 * @return default status of a new request.
	 */
	public static Type getDefaultType() {
		return DEFAULT_TYPE;
	}

	/**
	 *  Validate that the request id is a proper UUID. If the request id is inappropriate,
	 * a null is returned.  
	 * 
	 * @param requestId The request Id string to be validated. 
	 * 
	 * @return the request id if the supplied argument is valid, otherwise, returns null.
	 */
	public static String validateRequestId(
			final String requestId) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(requestId)) {
			return null;
		}
		
		try {
			return UUID.fromString(requestId).toString();
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The request ID is not a valid UUID: " + requestId);
		}
	}
	
	/**
	 *  Validate that the request content string is a proper JSON object. The system 
	 * expect that the string contains a "content" element which is a JSON array. 
	 * The array cannot be empty. 
	 * 
	 * @param content The request content string to be validated.  
	 * 
	 * @return the JSON object representing the content string if the supplied content 
	 *         is a non-empty JSON string. Otherwise, returns null.
	 */
	public static JSONObject validateRequestContent(String content) 
		throws ValidationException {
		JSONObject jsonContent = null;
		
		if (StringUtils.isEmptyOrWhitespaceOnly(content))
			return null;
	
		try {
			// get the json object
			jsonContent = new JSONObject(content);
			// extract the content JSONarray element
			JSONArray contentArray = jsonContent.getJSONArray(REQUEST_JSON_KEY_CONTENT);
			
			// check that the content is not empty
			if ((contentArray == null) || (contentArray.length() == 0)) {
				throw new ValidationException(ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
						"Invalid user setup request format. Excpect a JSONObject with non-empty 'content' JSONArray element.");
			} 
			
		} catch (JSONException e) {
			throw new ValidationException(ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER,
					"Invalid user setup request format. Excpect a JSONObject with 'content' element.", 
					e);
		}
		return jsonContent;
	}
	
	/**
	 *  Validate the user setup status.
	 * 
	 * @param status The string representation of the request status. 
	 * 
	 * @return status string if the argument is a valid status. 
	 * 
	 */
	public static String validateRequestStatus(
			final String status) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(status)) {
			return null;
		}
		
		try {
			return Status.getValue(status).toString();
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The request status is not a valid value (i.e. PENDING/APPROVED/REJECTED): " + status);
		}
	}

	/**
	 *  Validate the user setup status.
	 * 
	 * @param type The string representation of the request status. 
	 * 
	 * @return status string if the argument is a valid status. 
	 * 
	 */
	public static String validateRequestType(
			final String type) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(type)) {
			return null;
		}
		
		try {
			return Type.getValue(type).toString();
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The request type is not a valid type (i.e. USER_SETUP): " + type);
		}
	}

	/**
	 * Validates that all of the request identifiers in a list String are valid 
	 * UUIDs. If the requestIdListString is null or whitespace only, it will 
	 * return null. If not, it will attempt to parse the string and evaluate each 
	 * of the request id in the list. If any id is invalid, it will return an error 
	 * message.
	 *  
	 * @param requestIdListString The request id list as a string where each item is
	 * 							separated by
	 * 						  	{@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.
	 * 
	 * @return Returns a list of request ids derived from the supplied argument, 
	 * 		   or null if argument is null, whitespace only, or
	 * 		   would otherwise be empty because it doesn't contain any 
	 * 		   meaningful data.
	 * 
	 * @throws ValidationException Thrown if the request Id list string contains an
	 * 							 invalid UUID.
	 */
	public static Collection<String> validateRequestIdList(
			final String requestIdListString) throws ValidationException {
		
		LOGGER.info("Validating the list of request ids.");
		
		// If the class list is an empty string, then we return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(requestIdListString)) {
			return null;
		}
				
		// Otherwise, attempt to parse and evaluate the list

		// Create the list of class IDs to be returned to the caller.
		Set<String> requestIdList = new HashSet<String>();

		String[] requestListArray = requestIdListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		if (requestListArray.length > MAX_LIST_LENGTH) {
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The list cannot exceed the maximum length of " + MAX_LIST_LENGTH);	
		}
		
		for (String requestId : requestListArray) {
			// Validate the current request ID.
			try {
				String currRequestId = validateRequestId(requestId);
			
				// if it is null, then ignore since it is due to white space.
				if(currRequestId != null) {
					requestIdList.add(currRequestId);
				} 
			} catch (ValidationException e) {
				throw new ValidationException(
						ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
						"The lsit contains invalid uuid(s): " + requestId);
				
			}
		}
		
		return requestIdList;
	}	

	/**
	 * Validates that all of user identifiers in the comma-separated list are valid 
	 * usernames. If the argument is null or whitespace only, it will 
	 * return null. If not, it will attempt to parse the string and evaluate each 
	 * of the element in the list. If any user name is invalid, it will return an error 
	 * message.
	 *  
	 * @param userListString The username list as a string where each item is
	 * 							separated by
	 * 						  	{@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.
	 * 
	 * @return Returns a list of usernames from the argument or null if the argument string 
	 *         is null, whitespace only, or would otherwise be empty.
	 * 
	 * @throws ValidationException Thrown if the request Id list string contains an
	 * 							 invalid UUID.
	 */
	public static Collection<String> validateUserList(
			final String userListString) throws ValidationException {
		
		LOGGER.info("Validating the list of users.");
		
		// If the class list is an empty string, then we return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(userListString)) {
			return null;
		}
				
		// Otherwise, attempt to parse and evaluate the list
	
		// Create the list of class IDs to be returned to the caller.
		Set<String> userList = new HashSet<String>();

		String[] requestListArray = userListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		if (requestListArray.length > MAX_LIST_LENGTH) {
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The list cannot exceed the maximum length of " + MAX_LIST_LENGTH);	
		}

		for (String user : requestListArray) {
			// Validate the current request ID.
			try {
				String currUser = UserValidators.validateUsername(user);
			
				// if it is null, then ignore since it is due to white space.
				if(currUser != null) {
					userList.add(currUser);
				} 
			} catch (ValidationException e) {
				throw new ValidationException(
						ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
						"The lsit contains invalid username(s): " + user);
			}
		}
		
		return userList;
	}	


	/**
	 * Validates that all of search tokens in the search string are valid 
	 * tokens. If the argument is null or whitespace only, it will 
	 * return null. If not, it will attempt to parse the string and evaluate each 
	 * of the element in the list. If any element is invalid, it will return an error 
	 * message.
	 *  
	 * @param value The search string to be used in restricting results to only 
	 *        those requests whose email address contains this value. Multiple searching 
	 *        tokens are separated by space (e.g. "email1 email2")
	 * 
	 * @return Returns a list of search tokens or null if the argument string 
	 *         is null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the request Id list string contains an
	 * 							 invalid UUID.
	 */
	public static Collection<String> validateEmailAddressSearch(final String value) 
			throws ValidationException {
		
		Collection<String> result = null;
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		result = StringUtils.decodeSearchString(value);
		if (result.size() > MAX_LIST_LENGTH)
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The list cannot exceed the maximum length of " + MAX_LIST_LENGTH);	
		
		return result;
	}
	
	/**
	 * Validates that all of search tokens in the search string are valid 
	 * tokens. If the argument is null or whitespace only, it will 
	 * return null. If not, it will attempt to parse the string and evaluate each 
	 * of the element in the list. If any element is invalid, it will return an error 
	 * message.
	 *  
	 * @param value The search string to be used in restricting results to only 
	 *        those requests whose request content contains this value. Multiple searching 
	 *        tokens are separated by space (e.g. "email1 email2")
	 * 
	 * @return Returns a list of search tokens or null if the argument string 
	 *         is null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the request Id list string contains an
	 * 							 invalid UUID.
	 */
	public static Collection<String> validateContentSearch(final String value) 
			throws ValidationException {
		
		Collection<String> result = null;
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		result = StringUtils.decodeSearchString(value);
		if (result.size() > MAX_LIST_LENGTH)
			throw new ValidationException(
					ErrorCode.USER_SETUP_REQUEST_INVALID_PRAMETER, 
					"The list cannot exceed the maximum length of " + MAX_LIST_LENGTH);	
		
		return result;
	}

	/**
	 * Validates that a date is a valid Date and returns it.
	 * 
	 * @param inputDate The start date to be validated and decoded into a Date
	 * 					object.
	 * 
	 * @return Returns null if the start date was null or whitespace only.
	 * 		   Otherwise, it returns the decoded Date.
	 * 
	 * @throws ValidationException Thrown if the start date was not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static DateTime validateDate(final String inputDate) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(inputDate)) {
			return null;
		}

		try {
			return DateTimeUtils.getDateTimeFromString(inputDate);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_DATE, 
					"The date is not valid: " + inputDate);
		}
	}

	
	
}
