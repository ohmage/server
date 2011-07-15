package org.ohmage.request.clazz;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.StringValidators;
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
	private final Map<String, String> usersToAdd;
	private final List<String> usersToRemove;
	
	/**
	 * Creates a new class update request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the necessary
	 * 					  parameters for this request.
	 */
	public ClassUpdateRequest(HttpServletRequest httpRequest) {
		super(CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN), httpRequest.getParameter(InputKeys.CLIENT));
		
		String tempClassId = null;
		String tempClassName = null;
		String tempClassDescription = null;
		Map<String, String> tempUsersToAdd = null;
		List<String> tempUsersToRemove = null;
		
		if(! failed) {
			try {
				tempClassId = ClassValidators.validateClassId(this, httpRequest.getParameter(InputKeys.CLASS_URN));
				if(tempClassId == null) {
					setFailed(ErrorCodes.CLASS_INVALID_ID, "The required class URN is missing or whitespace only.");
					throw new ValidationException("Missing required parameter: " + InputKeys.CLASS_URN);
				}
				
				tempClassName = StringValidators.validateString(this, httpRequest.getParameter(InputKeys.CLASS_NAME));
				tempClassDescription = StringValidators.validateString(this, httpRequest.getParameter(InputKeys.DESCRIPTION));
				tempUsersToAdd = UserClassValidators.validateUserAndClassRoleList(this, httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD));
				tempUsersToRemove = UserValidators.validateUsernames(this, httpRequest.getParameter(InputKeys.USER_LIST_REMOVE));
			}
			catch(ValidationException e) {
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
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Checking that the class exists.");
			ClassServices.checkClassExistence(this, classId, true);
			
			LOGGER.info("Checking that the user is privileged in the class or is an admin.");
			UserClassServices.userIsPrivilegedOrAdmin(this, classId, user.getUsername());
			
			LOGGER.info("Updating the class.");
			ClassServices.updateClass(this, classId, className, classDescription, usersToAdd, usersToRemove);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success if everything worked or with an error code and
	 * explanation should something have failed.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}