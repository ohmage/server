package edu.ucla.cens.awserver.request;

import java.util.Map;

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
		Map<String, Object> toValidate = getToValidate();
		
		toValidate.put(InputKeys.CLASS_URN, classUrn);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(name)) {
			toValidate.put(InputKeys.CLASS_NAME, name);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(description)) {
			toValidate.put(InputKeys.DESCRIPTION, description);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListAdd)) {
			toValidate.put(InputKeys.USER_LIST_ADD, userListAdd);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(userListRemove)) {
			toValidate.put(InputKeys.USER_LIST_REMOVE, userListRemove);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(privilegedUserListAdd)) {
			toValidate.put(InputKeys.PRIVILEGED_USER_LIST_ADD, privilegedUserListAdd);
		}
		
		setToValidate(toValidate);
	}
}
