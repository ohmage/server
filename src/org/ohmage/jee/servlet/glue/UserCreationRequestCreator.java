package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
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
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating the user creation request.");
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return new UserCreationRequest(
				token,
				httpRequest.getParameter(InputKeys.NEW_USERNAME),
				httpRequest.getParameter(InputKeys.NEW_PASSWORD),
				httpRequest.getParameter(InputKeys.USER_ADMIN),
				httpRequest.getParameter(InputKeys.USER_ENABLED),
				httpRequest.getParameter(InputKeys.NEW_ACCOUNT),
				httpRequest.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE));
	}
}