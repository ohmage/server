package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validator for inbound queries about user information.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryValidator extends AbstractGzipHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(UserInfoQueryValidator.class);
	
	private static final int MAX_USERNAME_LENGTH = 15;
	private static final int MAX_NUM_USERNAMES = 500;
	
	private List<String> _parameterList;
	
	/**
	 * Default constructor that sets up the viable parameters for this query.
	 */
	public UserInfoQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{ InputKeys.AUTH_TOKEN, InputKeys.USER_LIST }));
	}
	
	/**
	 * Validates that none of the parameters is not too long and that the list
	 * of usernames contains at least one username. 
	 */
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			// Don't flood the logs if inappropriate parameters are given.
			return false;
		}
		
		String token = httpServletRequest.getParameter(InputKeys.AUTH_TOKEN);
		String commaSeparatedUsernames = httpServletRequest.getParameter(InputKeys.USER_LIST);
		
		if(token == null) {
			return false;
		}
		else if(commaSeparatedUsernames == null) {
			return false;
		}
		else if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36) || (token.length() < 36)) {
			// Don't flood the logs when invalid data is given this early.
			_logger.warn("Incorrectly sized " + InputKeys.AUTH_TOKEN);
			return false;
		}
		else if(greaterThanLength(InputKeys.USER_LIST, InputKeys.USER_LIST, commaSeparatedUsernames, MAX_USERNAME_LENGTH * MAX_NUM_USERNAMES) || (commaSeparatedUsernames.length() == 0)) {
			_logger.warn("Incorrectly sized " + InputKeys.USER_LIST);
			return false;
		}
		
		return true;
	}
}
