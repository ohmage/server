package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.InputKeys;

/**
 * @author selsky
 */
public class AuthValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(AuthValidator.class);
	private List<String> _parameterList;
	
	/**
	 * 
	 */
	public AuthValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{ InputKeys.CLIENT, InputKeys.PASSWORD, InputKeys.USER}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String user = (String) httpServletRequest.getParameter(InputKeys.USER);
		String password = (String) httpServletRequest.getParameter(InputKeys.PASSWORD);
		String client = (String) httpServletRequest.getParameter(InputKeys.CLIENT);
		
		// Check for abnormal lengths (buffer overflow attack)
		// The max lengths are based on the column widths in the db
		
		if(greaterThanLength(InputKeys.USER, InputKeys.USER, user, 15) 
			|| greaterThanLength(InputKeys.PASSWORD, InputKeys.PASSWORD, password, 100) 
			|| greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 250)) { 
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
