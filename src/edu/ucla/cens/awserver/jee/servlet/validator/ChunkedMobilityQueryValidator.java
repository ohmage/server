package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound data to the chunked mobility query.
 * 
 * @author selsky
 */
public class ChunkedMobilityQueryValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(MobilityQueryValidator.class);
	private List<String> _parameterList;
	
	public ChunkedMobilityQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"e","s","u","ci","t"}));
	}
	
	public boolean validate(HttpServletRequest httpServletRequest) {
		Map<String,String[]> parameterMap = getParameterMap(httpServletRequest); 
		
		// Check for missing or extra parameters
		if(parameterMap.size() != _parameterList.size()) {				
			_logger.warn("an incorrect number of parameters was found for a mobility query: " + parameterMap.size());
			return false;
		}
		
		// Check for duplicate parameter values
		Iterator<?> iterator = parameterMap.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String[] valuesForKey = (String[]) parameterMap.get(key);
			
			if(valuesForKey.length != 1) {
				_logger.warn("an incorrect number of values (" + valuesForKey.length + ") was found for parameter " + key);
				return false;
			}
		}
		
		// Check for parameters with unknown names
		if(containsUnknownParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		String s = (String) httpServletRequest.getParameter("s");
		String e = (String) httpServletRequest.getParameter("e");
		String u = (String) httpServletRequest.getParameter("u");
		String ci = (String) httpServletRequest.getParameter("ci");
		String t = (String) httpServletRequest.getParameter("t");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("startDate", "s", s, 10) 
		   || greaterThanLength("startDate", "e", e, 10)		
		   || greaterThanLength("client", "ci",ci, 250)
		   || greaterThanLength("userName", "u", u, 15)
		   || greaterThanLength("authToken", "t", t, 36)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
