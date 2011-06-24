package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A class deletion request.
 * 
 * @author John Jenkins
 */
public class ClassDeletionRequest extends ResultListAwRequest {
	/**
	 * Creates a new class deletion request object.
	 * 
	 * @param token The autentication / session token for the requester.
	 * 
	 * @param classId The class ID for the class to be deleted.
	 * 
	 * @throws IllegalArgumentException Thrown if the token or class ID are 
	 * 									null or whitespace only.
	 */
	public ClassDeletionRequest(String token, String classId) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The token cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			throw new IllegalArgumentException("The class ID cannot be null or whitespace only.");
		}
		
		setUserToken(token);
		addToValidate(InputKeys.CLASS_URN, classId, true);
	}
}
