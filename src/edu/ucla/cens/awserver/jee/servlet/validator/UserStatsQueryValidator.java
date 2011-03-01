package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound data to the user stats API.
 * 
 * @author selsky
 */
public class UserStatsQueryValidator extends AbstractGzipHttpServletRequestValidator {
 	private static Logger _logger = Logger.getLogger(UserStatsQueryValidator.class);
	private List<String> _parameterList;
	
	/**
	 */
	public UserStatsQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"u","c","ci","t"}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String c = (String) httpServletRequest.getParameter("c");
		String ci = (String) httpServletRequest.getParameter("ci");
		String t = (String) httpServletRequest.getParameter("t");
		String u = (String) httpServletRequest.getParameter("u");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("campaignName", "c", c, 250)
		   || greaterThanLength("client", "ci",ci, 500)		   
		   || greaterThanLength("authToken", "t", t, 36)
		   || greaterThanLength("userName", "u", u, 15)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
