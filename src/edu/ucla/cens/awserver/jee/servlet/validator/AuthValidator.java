package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"p","u","ci"}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String u = (String) httpServletRequest.getParameter("u");
		String p = (String) httpServletRequest.getParameter("p");
		String ci = (String) httpServletRequest.getParameter("ci");
		
		// Check for abnormal lengths (buffer overflow attack)
		// The max lengths are based on the column widths in the db
		
		if(greaterThanLength("user", "u", u, 15) 
			|| greaterThanLength("password", "p", p, 100) 
			|| greaterThanLength("client", "ci", ci, 250)) { 
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
