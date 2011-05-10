package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Request for handling the update of a class.
 * 
 * @author John Jenkins
 */
public class ClassUpdateAwRequest extends ResultListAwRequest {
	/**
	 * Populates the toValidate map with the applicable paramters.
	 * 
	 * @param classUrn The class that is being updated. The only parameter
	 * 				   that must be non-null.
	 * 
	 * @param description The new description of the class.
	 *  
	 * @param userListAdd A comma-separated list of users to be added to the
	 * 					  class.
	 * 
	 * @param userListRemove A comma-separated list of users to remove from
	 * 						 the class.
	 */
	public ClassUpdateAwRequest(String classUrn, String name, String description, String userListAdd, String userListRemove, String privilegedUserListAdd) {
		addToValidate(InputKeys.CLASS_URN, classUrn, true);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(name)) {
			addToValidate(InputKeys.CLASS_NAME, name, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(description)) {
			addToValidate(InputKeys.DESCRIPTION, description, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListAdd)) {
			addToValidate(InputKeys.USER_LIST_ADD, userListAdd, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListRemove)) {
			addToValidate(InputKeys.USER_LIST_REMOVE, userListRemove, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(privilegedUserListAdd)) {
			addToValidate(InputKeys.PRIVILEGED_USER_LIST_ADD, privilegedUserListAdd, true);
		}
	}
}
