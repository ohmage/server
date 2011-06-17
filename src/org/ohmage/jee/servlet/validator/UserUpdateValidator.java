package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * Basic validation for a user update request.
 * 
 * @author John Jenkins
 */
public class UserUpdateValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(UserUpdateValidator.class);
	
	/**
	 * Default constructor.
	 */
	public UserUpdateValidator() {
		// Do nothing.
	}

	/**
	 * Ensures that the required parameters exist.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Get the authentication / session token from the header.
		String token;
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			throw new MissingAuthTokenException("The required authentication / session token is missing.");
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		else {
			token = tokens.get(0);
		}
		
		String user = httpRequest.getParameter(InputKeys.USER);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			_logger.info("Missing the user token.");
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(user)) {
			_logger.info("Missing required parameter: " + InputKeys.USER);
			return false;
		}
		
		return true;
	}
}