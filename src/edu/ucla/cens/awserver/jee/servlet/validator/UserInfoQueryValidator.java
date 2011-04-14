package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound queries about user information.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryValidator extends AbstractGzipHttpServletRequestValidator {
	// List of required parameters
	private static final String TOKEN = "auth_token";
	private static final String USERNAMES = "usernames";
	
	private static final int MAX_USERNAME_LENGTH = 15;
	private static final int MAX_NUM_USERNAMES = 500;
	
	private static Logger _logger = Logger.getLogger(UserStatsQueryValidator.class);
	
	private List<String> _parameterList;
	
	/**
	 * Default constructor that sets up the viable parameters for this query.
	 */
	public UserInfoQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{TOKEN, USERNAMES}));
	}
	
	/**
	 * Validates that none of the parameters is not too long and that the list
	 * of usernames contains at least one username. 
	 */
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String token = httpServletRequest.getParameter(TOKEN);
		String commaSeparatedUsernames = httpServletRequest.getParameter(USERNAMES);
		
		if(greaterThanLength(TOKEN, TOKEN, token, 36)) {
			_logger.warn("Token is too long.");
			return false;
		}
		else if(greaterThanLength(USERNAMES, USERNAMES, commaSeparatedUsernames, MAX_USERNAME_LENGTH * MAX_NUM_USERNAMES)) {
			_logger.warn("Username list exceeds maximum length.");
			return false;
		}
		else if(commaSeparatedUsernames.length() == 0) {
			_logger.warn("No usernames in request.");
			return false;
		}
		
		return true;
	}
}
