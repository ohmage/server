package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

public class UserDeletionValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(UserDeletionValidator.class);
	
	/**
	 * Default constructor.
	 */
	public UserDeletionValidator() {
		// Do nothing.
	}

	/**
	 * Checks that all the required parameters exist and are reasonable.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is no in the header.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		String userList = httpRequest.getParameter(InputKeys.USER_LIST);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(userList)) {
			_logger.info("Required parameter is missing or invalid: " + InputKeys.USER_LIST);
			return false;
		}
		
		// Get the authentication / session token from the header.
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			if(httpRequest.getParameter(InputKeys.AUTH_TOKEN) == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}

		return true;
	}
}