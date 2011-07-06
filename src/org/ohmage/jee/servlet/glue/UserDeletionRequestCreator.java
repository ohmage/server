package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserDeletionRequest;
import org.ohmage.util.CookieUtils;

/**
 * Creates a user deletion request.
 * 
 * @author John Jenkins
 */
public class UserDeletionRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(UserDeletionRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public UserDeletionRequestCreator() {
		// Do nothing.
	}
	
	/**
	 * Creates a user deletion request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating a new user deletion request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return new UserDeletionRequest(
				token, 
				httpRequest.getParameter(InputKeys.USER_LIST));
	}
}