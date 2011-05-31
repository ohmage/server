package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

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
		
		UserInfoQueryAwRequest awRequest = new UserInfoQueryAwRequest();
		awRequest.setUserToken(token);
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return awRequest;
	}

}
