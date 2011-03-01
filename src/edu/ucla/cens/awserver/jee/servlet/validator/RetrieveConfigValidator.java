package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author selsky
 */
public class RetrieveConfigValidator extends AbstractGzipHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(RetrieveConfigValidator.class);
	private List<String> _parameterList;
	
	public RetrieveConfigValidator() {
		// TODO this can be a static variable
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"ci","t"}));
	}
	
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String ci = (String) httpServletRequest.getParameter("ci");
		String t = (String) httpServletRequest.getParameter("t");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("token", "t", t, 36)
		   || greaterThanLength("client", "ci", ci, 250)
		) {
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
