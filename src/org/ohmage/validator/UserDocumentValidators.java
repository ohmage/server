package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class contains the functionality for validating user-document
 * information.
 * 
 * @author John Jenkins
 */
public class UserDocumentValidators {
	private static final Logger LOGGER = Logger.getLogger(UserDocumentValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserDocumentValidators() {}

	/**
	 * Validates a list of username, document role pairs.
	 *  
	 * @param request The request that is performing this validation.
	 * 
	 * @param usernameAndRoleList A String representing the username,  
	 * 							  document role pairs. The pairs should be 
	 * 							  separated by
	 * 							  {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s 
	 * 							  and each pair should have its username  
	 * 							  separated from its document role by a 
	 * 							  {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.
	 * 
	 * @return Returns a Map of username, document role pairs. If the 
	 * 		   usernameAndRoleList is null or whitespace only, null is 
	 * 		   returned.
	 * 
	 * @throws ValidationException Thrown if the list is malformed, any of the
	 * 							   pairs are malformed, or any of the 
	 * 							   individual values in the pairs are malformed
	 * 							   or missing.
	 */
	public static Map<String, DocumentRoleCache.Role> validateUsernameAndDocumentRoleList(Request request, String usernameAndRoleList) throws ValidationException {
		LOGGER.info("Validating a list of username and document role pairs.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(usernameAndRoleList)) {
			return null;
		}
		
		Map<String, DocumentRoleCache.Role> result = new HashMap<String, DocumentRoleCache.Role>();
		
		String[] usernameAndRoleArray = usernameAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < usernameAndRoleArray.length; i++) {
			String usernameAndRoleString = usernameAndRoleArray[i].trim(); 
			
			if((! StringUtils.isEmptyOrWhitespaceOnly(usernameAndRoleString)) &&
					(! usernameAndRoleString.equals(InputKeys.ENTITY_ROLE_SEPARATOR))) {
				String[] usernameAndRole = usernameAndRoleString.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				if(usernameAndRole.length != 2) {
					request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username, document role pair is invalid: " + usernameAndRoleString);
					throw new ValidationException("The username, document role pair is invalid: " + usernameAndRoleString);
				}
				
				String username = UserValidators.validateUsername(request, usernameAndRole[0].trim());
				if(username == null) {
					request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username in the username, document role pair is missing: " + usernameAndRoleString);
					throw new ValidationException("The username in the username, document role pair is missing: " + usernameAndRoleString);
				}
				
				DocumentRoleCache.Role documentRole = DocumentValidators.validateRole(request, usernameAndRole[1].trim());
				if(documentRole == null) {
					request.setFailed(ErrorCodes.DOCUMENT_INVALID_ROLE, "The document role in the username, document role pair is missing: " + usernameAndRoleString);
					throw new ValidationException("The document role in the username, document role pair is missing: " + usernameAndRoleString);
				}
				
				result.put(username, documentRole);
			}
		}
		
		return result;
	}
}
