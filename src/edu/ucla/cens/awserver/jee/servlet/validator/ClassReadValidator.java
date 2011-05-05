package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validates that the basic, required parameters exist for this request.
 * 
 * @author John Jenkins
 */
public class ClassReadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(ClassReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassReadValidator() {
		// Does nothing.
	}
	
	/**
	 * Validates that all required parameters exist and that their size is not
	 * excessive.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String classList = httpRequest.getParameter(InputKeys.CLASS_URN_LIST);
		
		if(token == null) {
			return false;
		}
		else if(classList == null) {
			return false;
		}
		else if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn("Token of incorrect size.");
			return false;
		}
		// Based on MAX_CLASS_URN_LENGTH * 100 (arbitrary); however the actual
		// number of classes may be much greater than 100 as most won't be the
		// entire MAX_CLASS_URN_LENGTH length. 
		else if(greaterThanLength(InputKeys.CLASS_URN_LIST, InputKeys.CLASS_URN_LIST, classList, 25600)) {
			_logger.warn("List of classes is too long.");
			return false;
		}
		
		return true;
	}

}
