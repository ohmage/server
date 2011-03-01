package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound data to the image query API.
 * 
 * @author selsky
 */
public class ImageQueryValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(ImageQueryValidator.class);
	private List<String> _parameterList;
	
	public ImageQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"u","c","ci","i","t","cv"}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		if(! basicValidation(getParameterMap(httpServletRequest), _parameterList)) {
			return false;
		}
		
		String u = (String) httpServletRequest.getParameter("u");
		String c = (String) httpServletRequest.getParameter("c");
		String ci = (String) httpServletRequest.getParameter("ci");
		String t = (String) httpServletRequest.getParameter("t");
		String cv = (String) httpServletRequest.getParameter("cv");
		String i = (String) httpServletRequest.getParameter("i");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("campaignName", "c", c, 250)
		   || greaterThanLength("campaignVersion", "cv", cv, 500)
		   || greaterThanLength("client", "ci",ci, 250)		   
		   || greaterThanLength("authToken", "t", t, 36)
		   || greaterThanLength("userName", "u", u, 15)
		   || greaterThanLength("imageId", "i", i, 36)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
