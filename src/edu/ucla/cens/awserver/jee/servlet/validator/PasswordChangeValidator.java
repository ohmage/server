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
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		String username = httpRequest.getParameter(InputKeys.USERNAME);
		String password = httpRequest.getParameter(InputKeys.PASSWORD);
		String newPassword = httpRequest.getParameter(InputKeys.NEW_PASSWORD);
		
		if(client == null) {
			return false;
		}
		else if(username == null) {
			// Missing username.
			return false;
		}
		else if(password == null) {
			// Missing password.
			return false;
		}
		else if(greaterThanLength(InputKeys.USERNAME, InputKeys.USERNAME, username, 15)) {
			// Username isn't the right length.
			return false;
		}
		else if(greaterThanLength(InputKeys.PASSWORD, InputKeys.PASSWORD, password, 100)) {
			// Password isn't the right length.
			return false;
		}
		else if(newPassword == null) {
			// Missing required parameter.
			return false;
		}
		else if(greaterThanLength(InputKeys.NEW_PASSWORD, InputKeys.NEW_PASSWORD, newPassword, 15)) {
			// The new password isn't the right length.
			_logger.warn("Attempting to update to a new password that is of an incorrect length: " + newPassword.length());
			return false;
		}
		
		return true;
	}

}
