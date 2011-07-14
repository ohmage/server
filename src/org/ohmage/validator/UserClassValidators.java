package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class for validating usernames and class pair values.
 * 
 * @author John Jenkins
 */
public final class UserClassValidators {
	private static final Logger LOGGER = Logger.getLogger(UserClassValidators.class);
	
	/**
	 * Class for handling username and class-roles.
	 * 
	 * @author John Jenkins
	 */
	public static final class UserAndRole {
		private final String username;
		private final String role;
		
		/**
		 * Creates a new username and class-role pair.
		 * 
		 * @param username The username in the pair.
		 * 
		 * @param role The class role in the pair.
		 */
		public UserAndRole(String username, String role) {
			this.username = username;
			this.role = role;
		}
		
		/**
		 * Returns the username.
		 * 
		 * @return The username
		 */
		public String getUsername() {
			return username;
		}
		
		/**
		 * Returns the class role.
		 * 
		 * @return The class role.
		 */
		public String getRole() {
			return role;
		}
	}
	
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
	 * @return Returns null if the list String is null or whitespace only. 
	 * 		   Otherwise, it returns a Map of usernames to class roles.
	 * 
	 * @throws ValidationException Thrown if the list String or any of the 
	 * 							   usernames in the list String are 
	 * 							   syntactically invalid. Also, thrown if any
	 * 							   of the roles in the list String are invalid.
	 */
	public static Map<String, String> validateUserAndClassRoleList(Request request, String userClassRoleList) throws ValidationException {
		LOGGER.info("Validating the user and class role list.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(userClassRoleList)) {
			return null;
		}
		
		Map<String, String> result = new HashMap<String, String>();
		String[] userAndRoleArray = userClassRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < userAndRoleArray.length; i++) {
			String currUserAndRole = userAndRoleArray[i];
			
			if(! "".equals(currUserAndRole)) {
				String[] userAndRole = currUserAndRole.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				String username;
				if(userAndRole.length != 2) {
					username = UserValidators.validateUsername(request, userAndRole[0]);
					
					request.setFailed(ErrorCodes.CLASS_INVALID_ROLE, "The class role is missing: " + currUserAndRole);
					throw new ValidationException("The user class-role list at index " + i + " is invalid: " + currUserAndRole);
				}
				
				username = UserValidators.validateUsername(request, userAndRole[0]);
				String role = ClassValidators.validateClassRole(request, userAndRole[1]);
				
				result.put(username, role);
			}
		}
		
		return result;
	}
}