package edu.ucla.cens.awserver.jee.servlet.validator;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic validation for a config read request.
 * 
 * @author John Jenkins
 */
public class ConfigReadValidator extends AbstractHttpServletRequestValidator {

	/**
	 * There are no parameters, so we don't care what they send.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		return true;
	}

}
