package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserUpdateRequest;
import org.ohmage.util.CookieUtils;

/**
 * Creates a new user update request.
 * 
 * @author John Jenkins
 */
public class UserUpdateRequestCreator implements AwRequestCreator {
	private static final Logger _logger = Logger.getLogger(UserUpdateRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public UserUpdateRequestCreator() {
		// Do nothing.
	}

	/**
	 * Builds a new UserUpdateRequest.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating a user update request.");
		
		return new UserUpdateRequest(
				CookieUtils.getCookieValue(request.getCookies(), InputKeys.AUTH_TOKEN).get(0),
				request.getParameter(InputKeys.USER),
				request.getParameter(InputKeys.USER_ADMIN),
				request.getParameter(InputKeys.USER_ENABLED),
				request.getParameter(InputKeys.NEW_ACCOUNT),
				request.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE),
				request.getParameter(InputKeys.FIRST_NAME),
				request.getParameter(InputKeys.LAST_NAME),
				request.getParameter(InputKeys.ORGANIZATION),
				request.getParameter(InputKeys.PERSONAL_ID),
				request.getParameter(InputKeys.EMAIL_ADDRESS),
				request.getParameter(InputKeys.USER_JSON_DATA));
	}
}