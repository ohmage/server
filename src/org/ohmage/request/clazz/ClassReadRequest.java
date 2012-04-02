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
package org.ohmage.request.clazz;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.validator.ClassValidators;

/**
 * <p>Request to read the information about a list of classes including the 
 * list of users in the class and their class roles.</p>
 * <p>The requester must be a member of each of the classes.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes identifiers (URNs) separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_ROLE}</td>
 *     <td>Limits the results to only those classes to which the user belongs
 *       with this role.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_WITH_USER_LIST}</td>
 *     <td>A boolean value indicating if the class user list should be returned 
 *       or not. The default is true.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassReadRequest.class);
	private static final String JSON_KEY_USERS = "users";
	
	private final Collection<String> classIds;
	private final Clazz.Role role;
	private final boolean withUserList;
	
	private final Map<Clazz, Map<String, Clazz.Role>> result;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the 
	 * 					  parameters to and metadata for this request.
	 */
	public ClassReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		Set<String> tClassIds = null;
		Clazz.Role tRole = null;
		boolean tWithUserList = true;
		
		if(! isFailed()) {
			LOGGER.info("Creating a new class read request.");
			
			try {
				String[] t;
				
				t = getParameterValues(InputKeys.CLASS_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID,
							"Multiple class ID lists were found: " +
								InputKeys.CLASS_URN_LIST);
				}
				else if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID, 
							"Missing required class ID list: " + 
								InputKeys.CLASS_URN_LIST);
				}
				else {
					tClassIds = ClassValidators.validateClassIdList(t[0]);
					
					if(tClassIds == null) {
						throw new ValidationException(
								ErrorCode.CLASS_INVALID_ID, 
								"Missing required class ID list: " + 
									InputKeys.CLASS_URN_LIST);
					}
				}
				
				t = getParameterValues(InputKeys.CLASS_ROLE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ROLE,
							"Multiple class roles were given: " +
								InputKeys.CLASS_ROLE);
				}
				else if(t.length == 1) {
					tRole = ClassValidators.validateClassRole(t[0]);
				}
				
				t = getParameterValues(InputKeys.CLASS_WITH_USER_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_WITH_USER_LIST_VALUE,
							"Multiple \"with user list\" parameters were given: " +
								InputKeys.CLASS_WITH_USER_LIST);
				}
				else if(t.length == 1) {
					tWithUserList = 
							ClassValidators.validateWithUserListValue(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		classIds = tClassIds;
		role = tRole;
		withUserList = tWithUserList;
		
		result = new HashMap<Clazz, Map<String, Clazz.Role>>();
	}

	/**
	 * Validates that the classes in the list exist, then aggregates the
	 * requested information.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Gathering the information about the classes in the list.");
			result.putAll(
					ClassServices.instance().getClassesInformation(
							getUser().getUsername(),
							classIds,
							role,
							withUserList));
			
			LOGGER.info("Classes found: " + result.size());
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Writes the response to the HTTP response object.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the result to the user.");
		
		// Populate our result JSONObject with class information.
		LOGGER.info("Creating the result JSONObject with the information about the classes.");
		JSONObject jsonResult = new JSONObject();
		try {
			for(Clazz clazz : result.keySet()) {
				// Create the JSON for the class.
				JSONObject jsonClass = clazz.toJson(false);
				
				// Retrieve the username to class role map.
				Map<String, Clazz.Role> userRole = result.get(clazz);
				
				if(userRole != null) {
					// Generate the user to class role JSON and add it to the 
					// class JSON.
					JSONObject users = new JSONObject();
					for(String username : userRole.keySet()) {
						Clazz.Role role = userRole.get(username);
						
						users.put(username, ((role == null) ? "" : role));
					}
					jsonClass.put(JSON_KEY_USERS, users);
				}
				
				// Add the class JSON to the result JSON with an index of the
				// class' ID.
				jsonResult.put(clazz.getId(), jsonClass);
			}
		}
		catch(JSONException e) {
			LOGGER.error("Error adding a class' information to the result object.", e);
			setFailed();
		}
		
		respond(httpRequest, httpResponse, jsonResult);
	}
	
	/**
	 * Returns the list of classes from the parameters.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		Map<String, String[]> auditInfo = new HashMap<String, String[]>();
		
		if(classIds != null) {
			auditInfo.put(InputKeys.CLASS_URN, classIds.toArray(new String[0]));
		}
		
		return auditInfo;
	}
}
