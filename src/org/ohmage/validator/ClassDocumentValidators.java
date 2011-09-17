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
 * This class contains the functionality for validating class-document
 * information.
 * 
 * @author John Jenkins
 */
public final class ClassDocumentValidators {
	private static final Logger LOGGER = Logger.getLogger(ClassDocumentValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ClassDocumentValidators() {};
	
	/**
	 * Validates a list of class ID and document role pairs. 
	 * 
	 * @param request The request that is validating this list.
	 * 
	 * @param classAndRoleList A String representing the class ID, document 
	 * 						   role pairs. The pairs should be separated by 
	 * 						   {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s 
	 * 						   and each pair should have its class ID separated
	 * 						   from its document role by a 
	 * 						   {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.
	 * 
	 * @return Returns a Map of class IDs to document roles. If the class role
	 * 		   list is empty or whitespace only, null is returned.
	 * 
	 * @throws ValidationException Thrown if the list is malformed, any of the
	 * 							   pairs are malformed, or any of the 
	 * 							   individual values in the pairs are malformed
	 * 							   or missing. 
	 */
	public static Map<String, DocumentRoleCache.Role> validateClassIdAndDocumentRoleList(Request request, String classAndRoleList) throws ValidationException {
		LOGGER.info("Validating a list of class ID and document role pairs.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(classAndRoleList)) {
			return null;
		}
		
		Map<String, DocumentRoleCache.Role> result = new HashMap<String, DocumentRoleCache.Role>();
		
		String[] classAndRoleArray = classAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classAndRoleArray.length; i++) {
			String classAndRoleString = classAndRoleArray[i].trim();
			
			if((! StringUtils.isEmptyOrWhitespaceOnly(classAndRoleString)) &&
					(! classAndRoleString.equals(InputKeys.ENTITY_ROLE_SEPARATOR))) {
				String[] classAndRole = classAndRoleString.split(InputKeys.ENTITY_ROLE_SEPARATOR);
				
				if(classAndRole.length != 2) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class ID, document role pair is invalid: " + classAndRoleString);
					throw new ValidationException("The class ID, document role pair is invalid: " + classAndRoleString);
				}
				
				String classId = ClassValidators.validateClassId(request, classAndRole[0].trim());
				if(classId == null) {
					request.setFailed(ErrorCodes.CLASS_INVALID_ID, "The class ID in the class ID, document role pair is missing: " + classAndRoleString);
					throw new ValidationException("The class ID in the class ID, document role pair is missing: " + classAndRoleString);
				}
				
				DocumentRoleCache.Role documentRole = DocumentValidators.validateRole(request, classAndRole[1].trim());
				if(documentRole == null) {
					request.setFailed(ErrorCodes.DOCUMENT_INVALID_ROLE, "The document role in the class ID, document role pair is missing: " + classAndRoleString);
					throw new ValidationException("The document role in the class ID, document role pair is missing: " + classAndRoleString);
				}
				
				result.put(classId, documentRole);
			}
		}
		
		return result;
	}
}