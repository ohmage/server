package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

public class PasswordChangeValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(PasswordChangeValidator.class);
	
	/**
	 * Default constructor.
	 */
	public PasswordChangeValidator() {
		// Do nothing.
	}
	
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String username = httpRequest.getParameter(InputKeys.USERNAME);
		String password = httpRequest.getParameter(InputKeys.PASSWORD);
		String newPassword = httpRequest.getParameter(InputKeys.NEW_PASSWORD);
		
		if((authToken == null) && ((username == null) || (password == null))) {
			// Invalid credentials.
			_logger.debug("Invalid credentials.");
			return false;
		}
		else if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, authToken, 36)) {
			// Auth token is of an invalid length.
			_logger.debug("auth_token too large.");
			return false;
		}
		else if(greaterThanLength(InputKeys.USERNAME, InputKeys.USERNAME, username, 15)) {
			// Username isn't the right length.
			_logger.debug("username too large.");
			return false;
		}
		else if(greaterThanLength(InputKeys.PASSWORD, InputKeys.PASSWORD, password, 100)) {
			// Password isn't the right length.
			_logger.debug("password too large.");
			return false;
		}
		else if(newPassword == null) {
			// Missing required parameter.
			_logger.debug("Missing new password.");
			return false;
		}
		else if(greaterThanLength(InputKeys.NEW_PASSWORD, InputKeys.NEW_PASSWORD, newPassword, 15)) {
			// The new password isn't the right length.
			_logger.warn("Attempting to update to a new password that is of an incorrect length: " + newPassword);
			return false;
		}
		
		return true;
	}

}
