package edu.ucla.cens.awserver.request;


/**
 * Request class for user info queries.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryAwRequest extends ResultListAwRequest {
	public static final String RESULT = "user_info_query_result";
	
	private String _usernames;
	
	/**
	 * Default constructor.
	 */
	public UserInfoQueryAwRequest() {
		super();
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
