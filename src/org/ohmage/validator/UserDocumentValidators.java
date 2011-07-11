package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
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
	public static Map<String, String> validateUsernameAndDocumentRoleList(Request request, String usernameAndRoleList) throws ValidationException {
		LOGGER.info("Validating a list of username and document role pairs.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(usernameAndRoleList)) {
			return null;
		}
		
		Map<String, String> result = new HashMap<String, String>();
		
		String[] usernameAndRoleArray = usernameAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < usernameAndRoleArray.length; i++) {
			String[] usernameAndRole = usernameAndRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			if(usernameAndRole.length != 2) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_USER_ROLE_LIST, "An invalid user-role pair was found: " + usernameAndRoleArray[i]);
				throw new ValidationException("Invalid user-role pair found: " + usernameAndRoleArray[i]);
			}
			
			String username = UserValidators.validateUsername(request, usernameAndRole[0]);
			if(username == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_USER_ROLE_LIST, "Missing the username in a username, document role pair: " + usernameAndRoleArray[i]);
				throw new ValidationException("Missing the username in a username, document role pair: " + usernameAndRoleArray[i]);
			}
			
			String documentRole = DocumentValidators.validateRole(request, usernameAndRole[1]);
			if(documentRole == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_CLASS_ROLE_LIST, "Missing the document role in a class ID, document role pair: " + usernameAndRole[i]);
				throw new ValidationException("Missing the document role in a class ID, document role pair: " + usernameAndRole[i]);
			}
			
			result.put(username, documentRole);
		}
		
		return result;
	}
}
