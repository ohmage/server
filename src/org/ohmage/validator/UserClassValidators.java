package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class for validating username and class pair values.
 * 
 * @author John Jenkins
 */
public final class UserClassValidators {
	private static final Logger LOGGER = Logger.getLogger(UserClassValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserClassValidators() {}
	
	/**
	 * Checks that the user class-role list is syntactically valid, that all of
	 * the usernames are syntactically valid, and that all of the roles are 
	 * valid. If the list String is null or whitespace only, null is returned.
	 * If there is any error in validating the list, a ValidationException is
	 * thrown. Otherwise, a Map of usernames to class roles is returned.
	 *  
	 * @param request The request that is having this list validated.
	 * 
	 * @param userClassRoleList A String representing a list of username and
	 * 							class-role pairs. The pairs should be separated
	 * 							by 
	 * 							{@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}
	 * 							and the username and class-role should be 
	 * 							separated by
	 * 							{@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.
	 * 
	 * @return Returns null if the list string is null, whitespace only, or 
	 * 		   only contains separators and no meaningful information. 
	 * 		   Otherwise, a map of username to class roles is returned with at
	 * 		   least one entry.
	 * 
	 * @throws ValidationException Thrown if the list String or any of the 
	 * 							   usernames in the list String are 
	 * 							   syntactically invalid. Also, thrown if any
	 * 							   of the roles in the list String are invalid.
	 */
	public static Map<String, ClassRoleCache.Role> validateUserAndClassRoleList(Request request, String userClassRoleList) throws ValidationException {
		LOGGER.info("Validating the user and class role list.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(userClassRoleList)) {
			return null;
		}
		
		Map<String, ClassRoleCache.Role> result = new HashMap<String, ClassRoleCache.Role>();
		String[] userAndRoleArray = userClassRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < userAndRoleArray.length; i++) {
			String currUserAndRole = userAndRoleArray[i].trim();
			
			if((! StringUtils.isEmptyOrWhitespaceOnly(currUserAndRole)) &&
					(! currUserAndRole.equals(InputKeys.ENTITY_ROLE_SEPARATOR))) {
				String[] userAndRole = currUserAndRole.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				if(userAndRole.length != 2) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ROLE, "The username, class role is invalid: " + currUserAndRole);
					throw new ValidationException("The user class-role list at index " + i + " is invalid: " + currUserAndRole);
				}
				
				String username = UserValidators.validateUsername(request, userAndRole[0].trim());
				if(username == null) {
					request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username in the username, class role list is missing: " + currUserAndRole);
					throw new ValidationException("The username in the username, class role list is missing: " + currUserAndRole);
				}
				
				ClassRoleCache.Role role = ClassValidators.validateClassRole(request, userAndRole[1].trim());
				if(role == null) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ROLE, "The class role in the username, class role list is missing: " + currUserAndRole);
					throw new ValidationException("The class role in the username, class role list is missing: " + currUserAndRole);
				}
				
				ClassRoleCache.Role oldRole = result.put(username, role);
				if((oldRole != null) && (! oldRole.equals(role))) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ROLE, "The username '" + username + "' contains multiple, different roles.");
					throw new ValidationException("The username '" + username + "' contains multiple, different roles.");
				}
			}
		}
		
		if(result.size() == 0) {
			return null;
		}
		else {
			return result;
		}
	}
}
