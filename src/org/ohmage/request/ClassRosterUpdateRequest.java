package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A class roster update request.
 * 
 * @author John Jenkins
 */
public class ClassRosterUpdateRequest extends ResultListAwRequest {
	public static final String KEY_WARNING_MESSAGES = "_class_roster_update_request_warning_messages_";
	
	/**
	 * Creates a class roster update request.
	 * 
	 * @param token The authentication / session token.
	 * 
	 * @param roster The contents of the class roster CSV file. It should be
	 * 				 lines of the form:
	 * 				
	 * 				 &lt;class_id&gt;,&lt;user_id&gt;,&lt;class_role&gt;
	 * 
	 * @throws IllegalArgumentException Thrown if the token is null or 
	 * 									whitespace only or if the roster is
	 * 									null.
	 */
	public ClassRosterUpdateRequest(String token, String roster) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The token cannot be null or whitespace only.");
		}
		else if(roster == null) {
			throw new IllegalArgumentException("The roster is missing.");
		}
		
		setUserToken(token);
		addToValidate(InputKeys.ROSTER, roster, true);
	}
}
