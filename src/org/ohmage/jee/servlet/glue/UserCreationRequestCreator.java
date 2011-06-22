package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserCreationRequest;
import org.ohmage.util.CookieUtils;

/**
 * Creates a user creation request.
 * 
 * @author John Jenkins
 */
public class UserCreationRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(UserCreationRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public UserCreationRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a new user creation request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating the user creation request.");
		
		return new UserCreationRequest(
				CookieUtils.getCookieValue(request.getCookies(), InputKeys.AUTH_TOKEN).get(0),
				request.getParameter(InputKeys.NEW_USERNAME),
				request.getParameter(InputKeys.NEW_PASSWORD),
				request.getParameter(InputKeys.USER_ADMIN),
				request.getParameter(InputKeys.USER_ENABLED),
				request.getParameter(InputKeys.NEW_ACCOUNT),
				request.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE));
	}
}