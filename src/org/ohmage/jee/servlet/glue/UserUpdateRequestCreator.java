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
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating a user update request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		return new UserUpdateRequest(
				token,
				httpRequest.getParameter(InputKeys.USER),
				httpRequest.getParameter(InputKeys.USER_ADMIN),
				httpRequest.getParameter(InputKeys.USER_ENABLED),
				httpRequest.getParameter(InputKeys.NEW_ACCOUNT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE),
				httpRequest.getParameter(InputKeys.FIRST_NAME),
				httpRequest.getParameter(InputKeys.LAST_NAME),
				httpRequest.getParameter(InputKeys.ORGANIZATION),
				httpRequest.getParameter(InputKeys.PERSONAL_ID),
				httpRequest.getParameter(InputKeys.EMAIL_ADDRESS),
				httpRequest.getParameter(InputKeys.USER_JSON_DATA));
	}
}