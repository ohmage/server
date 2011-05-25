package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Does basic validation on an incoming user read request.
 * 
 * @author John Jenkins
 */
public class UserReadValidator extends AbstractHttpServletRequestValidator {
	public static Logger _logger = Logger.getLogger(UserReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public UserReadValidator() {
		super();
	}

	/**
	 * Checks that the required parameters exist.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(authToken)) {
			return false;
		}
		else if(authToken.length() != 36) {
			return false;
		}
		
		return true;
	}

}
