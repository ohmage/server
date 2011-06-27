package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A class roster read request.
 * 
 * @author John Jenkins
 */
public class ClassRosterReadRequest extends ResultListAwRequest {
	public static final String KEY_RESULT = "_class_roster_read_request_result_";
	
	/**
	 * Creates a class roster read request.
	 * 
	 * @param token The authentication / session token for the requester.
	 * 
	 * @param classIdList The class ID list for which the rosters are desired.
	 */
	public ClassRosterReadRequest(String token, String classIdList) {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The token cannot be null or whitespace only.");
		}
		else if(classIdList == null) {
			throw new IllegalArgumentException("The class ID list cannot be null.");
		}
		
		setUserToken(token);
		addToValidate(InputKeys.CLASS_URN_LIST, classIdList, true);
	}
}
