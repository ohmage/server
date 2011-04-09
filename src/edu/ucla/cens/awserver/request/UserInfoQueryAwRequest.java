package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.domain.UserInfoQueryResult;

/**
 * AndWellness request class for user info queries.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryAwRequest extends ResultListAwRequest {
	private String[] _usernames;
	
	private UserInfoQueryResult _result;
	
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
	public UserInfoQueryAwRequest(String token, String[] usernames) {
		super();
		
		setUserToken(token);
		_usernames = usernames;
		_result = new UserInfoQueryResult();
	}
	
	/**
	 * Gets the usernames from the request. If none were made explicitly then
	 * this will only contain the username of the requester. Either way, this
	 * should always return at least one username.
	 * 
	 * @return A non-empty array of username Strings whose information is
	 * 		   being queried.
	 */
	public String[] getUsernames() {
		return(_usernames);
	}
	
	/**
	 * Returns the result object that contains the results of the query.
	 * 
	 * @return An object containing all the information needed for the 
	 * 		   request.
	 */
	public UserInfoQueryResult getUserInfoQueryResult() {
		return _result;
	}
}
