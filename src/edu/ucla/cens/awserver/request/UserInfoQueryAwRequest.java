package edu.ucla.cens.awserver.request;

import java.util.Map;

/**
 * AndWellness request class for user info queries.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryAwRequest extends ResultListAwRequest {
	public static final String RESULT = "UserInfoQueryResult";
	
	private String _usernames;
	
	/**
	 * Default constructor which takes in the token that was used to create
	 * this request. Instead of doing this through setters, we do it here
	 * because it will prevent accidentally changing the values later on.
	 * 
	 * @param token The token that was passed in the HTTP request for this
	 * 				query.
	 * 
	 * @param usernames The array of usernames in the request.
	 */
	public UserInfoQueryAwRequest(String usernames) {
		super();
		
		if(usernames == null) {
			throw new IllegalArgumentException("'usernames' is required.");
		}
		
		Map<String, Object> toValidateMap = getToValidate();
		toValidateMap.put(InputKeys.USER_LIST, usernames);
		this.setToValidate(toValidateMap);
	}
	
	/**
	 * Gets the usernames from the request. If none were made explicitly then
	 * this will only contain the username of the requester. Either way, this
	 * should always return at least one username.
	 * 
	 * @return A non-empty array of username Strings whose information is
	 * 		   being queried.
	 */
	public String getUsernames() {
		return(_usernames);
	}
	
	/**
	 * Outputs this object and then calls its super's toString().
	 */
	@Override
	public String toString() {
		return("UserInfoQueryAwRequest [_usernames = " + _usernames +
			   "] super = " + super.toString());
	}
}
