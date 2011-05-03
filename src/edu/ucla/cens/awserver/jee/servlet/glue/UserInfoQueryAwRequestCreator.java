package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.request.UserInfoQueryAwRequest;

/**
 * Creator for inbound queries about user information.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryAwRequestCreator implements AwRequestCreator {
	/**
	 * Default constructor.
	 */
	public UserInfoQueryAwRequestCreator() {
		// Does nothing.
	}

	/**
	 * Creates an AwRequest object from the previously validated 'request'
	 * parameter.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		String token = request.getParameter(InputKeys.AUTH_TOKEN);
		String commaSeparatedUsernames = request.getParameter(InputKeys.USER_LIST);
		
		UserInfoQueryAwRequest awRequest = new UserInfoQueryAwRequest(commaSeparatedUsernames);
		awRequest.setUserToken(token);
		
		return awRequest;
	}

}
