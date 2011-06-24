package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A request for class creation.
 * 
 * @author John Jenkins
 */
public class ClassCreationRequest extends ResultListAwRequest {
	/**
	 * Creates a new class creation request object.
	 * 
	 * @param token The requester's authentication / session token.
	 * 
	 * @param urn The URN of this new class.
	 * 
	 * @param name The name of this new class.
	 * 
	 * @param description An optional description for this new class.
	 * 
	 * @throws IllegalArgumentException Thrown if the token, urn, or name are 
	 * 									null or whitespace only.
	 */
	public ClassCreationRequest(String token, String urn, String name, String description) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The token cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(urn)) {
			throw new IllegalArgumentException("The URN cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The name cannot be null or whitespace only.");
		}
		
		setUserToken(token);
		addToValidate(InputKeys.CLASS_URN, urn, true);
		addToValidate(InputKeys.CLASS_NAME, name, true);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(description)) {
			addToValidate(InputKeys.DESCRIPTION, description, true);
		}
	}
}