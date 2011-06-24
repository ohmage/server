package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * Basic validation for a class creation request.
 * 
 * @author John Jenkins
 */
public class ClassCreationValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(ClassCreationValidator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassCreationValidator() {
		// Do nothing.
	}

	/**
	 * Validates that the class creation request contains all the necessary
	 * parameters.
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
		
		String classUrn = httpRequest.getParameter(InputKeys.CLASS_URN);
		String className = httpRequest.getParameter(InputKeys.CLASS_NAME);

		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			_logger.warn("Missing required key: " + InputKeys.AUTH_TOKEN);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(classUrn)) {
			_logger.warn("Missing required key: " + InputKeys.CLASS_URN);
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(className)) {
			_logger.warn("Missing required key: " + InputKeys.CLASS_NAME);
			return false;
		}
		
		return true;
	}
}