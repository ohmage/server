package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A request for deleting a list of users.
 * 
 * @author John Jenkins
 */
public class UserDeletionRequest extends ResultListAwRequest {
	/**
	 * Creates a new user deletion request.
	 * 
	 * @param token The authentication / session token from the requester.
	 * 
	 * @param userList The list of users to be deleted.
	 * 
	 * @throws IllegalArgumentException Thrown if either the token or the user
	 * 									list are null or obviously incorrect.
	 */
	public UserDeletionRequest(String token, String userList) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The authentication / session token cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(userList)) {
			throw new IllegalArgumentException(userList);
		}
		
		setUserToken(token);
		addToValidate(InputKeys.USER_LIST, userList, true);
	}
}
