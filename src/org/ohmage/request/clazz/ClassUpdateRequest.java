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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.UserClassValidators;
import org.ohmage.validator.UserValidators;

/**
 * <p>This class is responsible for updating a class. The requesting user must
 * be privileged in the class or an admin.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN}</td>
 *     <td>The unique identifier for the class to be updated.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_NAME}</td>
 *     <td>A new name for the class.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>A new description for the class.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_ADD}</td>
 *     <td>A list of users and respective roles to associate with this class.
 *       A user may only have one role with a class. To change a user's role,
 *       add their username and old role to the 
 *       {@value org.ohmage.request.InputKeys#USER_ROLE_LIST_REMOVE} parameter
 *       and their username and new role in this parameter. The value should be
 *       formatted where each username is separated from its respective role 
 *       with a {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR} and
 *       each username-role pair should be separated with a 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_REMOVE}</td>
 *     <td>A list of users and respective roles to disassociate with this 
 *       class. The value should be formatted where each username is separated
 *       from its respective role with a 
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR} and each
 *       username-role pair should be separated with a 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ClassUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassUpdateRequest.class);
	
	private final String classId;
	private final String className;
	private final String classDescription;
	private final Map<String, Clazz.Role> usersToAdd;
	private final Collection<String> usersToRemove;
	
	/**
	 * Creates a new class update request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the necessary
	 * 					  parameters for this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public ClassUpdateRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		String tempClassId = null;
		String tempClassName = null;
		String tempClassDescription = null;
		Map<String, Clazz.Role> tempUsersToAdd = null;
		Set<String> tempUsersToRemove = null;
		
		if(! isFailed()) {
			try {
				tempClassId = ClassValidators.validateClassId(httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Missing the class ID: " + InputKeys.CLASS_URN);
					throw new ValidationException("Missing the class ID: " + InputKeys.CLASS_URN);
				}
				else if(httpRequest.getParameterValues(InputKeys.CLASS_URN).length > 1) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class IDs were found.");
					throw new ValidationException("Multiple class IDs were found.");
				}
				
				tempClassName = ClassValidators.validateName(httpRequest.getParameter(InputKeys.CLASS_NAME));
				if((tempClassName != null) && (httpRequest.getParameterValues(InputKeys.CLASS_NAME).length > 1)) {
					setFailed(ErrorCode.CLASS_INVALID_NAME, "Multiple name parameters were found.");
					throw new ValidationException("Multiple name parameters were found.");
				}
				
				tempClassDescription = ClassValidators.validateDescription(httpRequest.getParameter(InputKeys.DESCRIPTION));
				if((tempClassDescription != null) && (httpRequest.getParameterValues(InputKeys.DESCRIPTION).length > 1)) {
					setFailed(ErrorCode.CLASS_INVALID_DESCRIPTION, "Multiple description parameters were found.");
					throw new ValidationException("Multiple description parameters were found.");
				}
				
				tempUsersToAdd = UserClassValidators.validateUserAndClassRoleList(httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD));
				if((tempUsersToAdd != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE_LIST_ADD).length > 1)) {
					setFailed(ErrorCode.USER_INVALID_USERNAME, "Multiple username, campaign role add parameters were found.");
					throw new ValidationException("Multiple username, campaign role add parameters were found.");
				}
				
				tempUsersToRemove = UserValidators.validateUsernames(httpRequest.getParameter(InputKeys.USER_LIST_REMOVE));
				if((tempUsersToRemove != null) && (httpRequest.getParameterValues(InputKeys.USER_LIST_REMOVE).length > 1)) {
					setFailed(ErrorCode.USER_INVALID_USERNAME, "Multiple username list parameters were found.");
					throw new ValidationException("Multiple username list parameters were found.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}

		classId = tempClassId;
		className = tempClassName;
		classDescription = tempClassDescription;
		usersToAdd = tempUsersToAdd;
		usersToRemove = tempUsersToRemove;
	}

	/**
	 * Services the request. This includes authenticating the user, checking
	 * that the class exists, checking that the requester has permissions to
	 * modify the class, and updating the class.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a class update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {	
			LOGGER.info("Checking that the user is privileged in the class or is an admin.");
			UserClassServices
				.instance().
				userIsAdminOrPrivileged(classId, getUser().getUsername());
			
			LOGGER.info("Checking that the class exists.");
			ClassServices.instance().checkClassExistence(classId, true);
			
			// Validate the list of users to add.
			if(usersToAdd != null) {
				// Get the existing list of users in the class.
				List<String> existingUsers =
					UserClassServices.instance().getUsersInClass(classId);
				
				// Get the users that are being added to the class.
				Set<String> addingUsers =
					new HashSet<String>(usersToAdd.keySet());
				
				// Remove any users that already exist in the class.
				addingUsers.removeAll(existingUsers);
				
				// If the user is attempting to add new users to the class,
				// they must be an admin.
				if(! addingUsers.isEmpty()) {
					LOGGER
						.info(
							"The user is attempting to add users to the " +
								"class, so we must ensure that they are an " +
								"admin.");
					if(! 
						UserServices
							.instance()
							.isUserAnAdmin(getUser().getUsername())) {
						
						throw
							new ServiceException(
								ErrorCode.CLASS_INSUFFICIENT_PERMISSIONS,
								"The requesting user must be an admin to " +
									"add users to a class.");
					}
					
					LOGGER.info("Verifying that the new users exist.");
					UserServices
						.instance()
						.verifyUsersExist(usersToAdd.keySet(), true);
				}
			}
			
			LOGGER.info("Updating the class.");
			ClassServices.instance().updateClass(classId, className, classDescription, usersToAdd, usersToRemove);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success if everything worked or with an error code and
	 * explanation should something have failed.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
}
