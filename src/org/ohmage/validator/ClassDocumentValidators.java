package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

public class ClassDocumentValidators {
	private static final Logger LOGGER = Logger.getLogger(ClassDocumentValidators.class);
	
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
	public static Map<String, String> validateClassIdAndDocumentRoleList(Request request, String classAndRoleList) throws ValidationException {
		LOGGER.info("Validating a list of class ID and document role pairs.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(classAndRoleList)) {
			return null;
		}
		
		Map<String, String> result = new HashMap<String, String>();
		
		String[] classAndRoleArray = classAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classAndRoleArray.length; i++) {
			String[] classAndRole = classAndRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			if(classAndRole.length != 2) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_CLASS_ROLE_LIST, "An invalid class-role pair was found: " + classAndRoleArray[i]);
				throw new ValidationException("Invalid class-role pair found: " + classAndRoleArray[i]);
			}
			
			String classId = ClassValidators.validateClassId(request, classAndRole[0]);
			if(classId == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_CLASS_ROLE_LIST, "Missing the class ID in a class ID, document role pair: " + classAndRoleArray[i]);
				throw new ValidationException("Missing the class ID in a class ID, document role pair: " + classAndRoleArray[i]);
			}
			
			String documentRole = DocumentValidators.validateRole(request, classAndRole[1]);
			if(documentRole == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_CLASS_ROLE_LIST, "Missing the document role in a class ID, document role pair: " + classAndRoleArray[i]);
				throw new ValidationException("Missing the document role in a class ID, document role pair: " + classAndRoleArray[i]);
			}
			
			result.put(classId, documentRole);
		}
		
		return result;
	}
}
