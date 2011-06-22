package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
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
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating a new user deletion request.");
		
		return new UserDeletionRequest(
				CookieUtils.getCookieValue(request.getCookies(), InputKeys.AUTH_TOKEN).get(0), 
				request.getParameter(InputKeys.USER_LIST));
	}
}