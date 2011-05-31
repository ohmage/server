package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validator for inbound queries about the requesting user's information.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryValidator extends AbstractGzipHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(UserInfoQueryValidator.class);

	/**
	 * Default constructor that sets up the viable parameters for this query.
	 */
	public UserInfoQueryValidator() {
		// Do nothing.
	}
	
	/**
	 * Validates that none of the parameters is not too long and that the list
	 * of usernames contains at least one username. 
	 */
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {		
		String token = httpServletRequest.getParameter(InputKeys.AUTH_TOKEN);
		String client = httpServletRequest.getParameter(InputKeys.CLIENT);
		
		if(token == null) {
			return false;
		}
		else if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36) || (token.length() < 36)) {
			_logger.warn("Incorrectly sized " + InputKeys.AUTH_TOKEN);
			return false;
		}
		else if(client == null) {
			_logger.warn("Missing " + InputKeys.CLIENT);
			return false;
		}
		
		return true;
	}
}
