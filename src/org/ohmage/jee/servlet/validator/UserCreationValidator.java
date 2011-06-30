package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * Basic validation for a user creation request.
 * 
 * @author John Jenkins
 */
public class UserCreationValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(UserCreationValidator.class);
	
	/**
	 * Default constructor.
	 */
	public UserCreationValidator() {
		// Do nothing.
	}

	/**
	 * Validates that the required parameters exist.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is not in the header.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		String username = httpRequest.getParameter(InputKeys.NEW_USERNAME);
		String password = httpRequest.getParameter(InputKeys.NEW_PASSWORD);
		String admin = httpRequest.getParameter(InputKeys.USER_ADMIN);
		String enabled = httpRequest.getParameter(InputKeys.USER_ENABLED);

		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			_logger.warn("Missing required key: " + InputKeys.NEW_USERNAME);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			_logger.warn("Missing required key: " + InputKeys.NEW_PASSWORD);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(admin)) {
			_logger.warn("Missing required key: " + InputKeys.USER_ADMIN);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(enabled)) {
			_logger.warn("Missing required key: " + InputKeys.USER_ENABLED);
			return false;
		}
		else if((client == null) || (greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 255))) {
			_logger.warn("The client is missing or too long.");
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