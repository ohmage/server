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
		String user = httpRequest.getParameter(InputKeys.USER);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(user)) {
			_logger.info("Missing required parameter: " + InputKeys.USER);
			return false;
		}
		else if((client == null) || (greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 255))) {
			_logger.info("The client parameter is missing or too long.");
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