package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * Basic validation for a class roster read request.
 * 
 * @author John Jenkins
 */
public class ClassRosterReadValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(ClassRosterReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassRosterReadValidator() {
		// Do nothing.
	}

	/**
	 * Validates that the required parameters exist.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Get the authentication / session token from the header.
		String token;
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
			
			if(token == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		else {
			token = tokens.get(0);
		}
		
		String classList = httpRequest.getParameter(InputKeys.CLASS_URN_LIST);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			_logger.info("Required parameter is missing or invalid: " + InputKeys.AUTH_TOKEN);
			return false;
		}
		else if(classList == null) {
			_logger.info("Required parameter is missing or invalid: " + InputKeys.CLASS_URN_LIST);
			return false;
		}

		return true;
	}
}