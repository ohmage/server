package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Validator for inbound data to the EMA Viz query feature.
 * 
 * @author selsky
 */
public class EmaVizValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(EmaVizValidator.class);
	private List<String> _parameterList;
	
	/**
	 */
	public EmaVizValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"s","e"}));
	}
	
	/**
	 * 
	 */
	public boolean validate(HttpServletRequest httpServletRequest) {
		
		Map<?,?> parameterMap = httpServletRequest.getParameterMap(); // String, String[]
		
		// Check for missing or extra parameters
		
		if(parameterMap.size() != _parameterList.size()) {
			_logger.warn("an incorrect number of parameters was found for an EMA viz query: " + parameterMap.size());
			return false;
		}
		
		// Check for duplicate parameters
		
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
		
		iterator = parameterMap.keySet().iterator(); // there is no way to reset the iterator so just obtain a new one
		
		while(iterator.hasNext()) {
			String name = (String) iterator.next();
			if(! _parameterList.contains(name)) {
			
				_logger.warn("an incorrect parameter name was found: " + name);
				return false;
			}
		}
		
		String s = (String) httpServletRequest.getParameter("s");
		String e = (String) httpServletRequest.getParameter("e");
		
		// Check for abnormal lengths (buffer overflow attack)
		// 50 is an arbitrary number for the length, but it would be very strange
		
		if(greaterThanLength("startDate", "s", s, 50) || greaterThanLength("endDate", "e", e, 50)) { 
			return false;
		}
		
		return true;
		
	}
	
}
