package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Helper class for common validation tasks.
 * 
 * @author selsky
 */
public abstract class AbstractHttpServletRequestValidator implements HttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(AbstractHttpServletRequestValidator.class);
	
	@SuppressWarnings("unchecked")
	protected Map<String, String[]> getParameterMap(HttpServletRequest request) {
		return (Map<String, String[]>) request.getParameterMap();
	}
	
	protected boolean basicValidation(Map<String, String[]> parameterMap, List<String> nameList) {
		// Check for missing or extra parameters
		if(parameterMap.size() != nameList.size()) {
			_logger.warn("incorrect number of parameters: " + nameList.size() + " expected, " + parameterMap.size() + " found");
			return false;
		}
		
		// Check for duplicate parameters
		if(containsDuplicateParameter(parameterMap, nameList)) {
			return false;
		}
		
		// Check for parameters with unknown names
		if(containsUnknownParameter(parameterMap, nameList)) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * @return true if the provided value is longer than the provided length and print an informative message to the log 
	 */
	protected boolean greaterThanLength(String longName, String name, String value, int length) {
		
		if(null != value && value.length() > length) {
			
			_logger.warn("a " + longName + "(request parameter " + name + ") of " + value.length() + " characters was found");
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return true if more than one value exists in the parameterMap for the keys contained in nameList. otherwise, returns false.
	 */
	protected boolean containsDuplicateParameter(Map<String, String[]> parameterMap, List<String> nameList) {
		Iterator<String> iterator = parameterMap.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = iterator.next();
			String[] valuesForKey = parameterMap.get(key);
			
			if(valuesForKey.length != 1) {
				_logger.warn("an incorrect number of values (" + valuesForKey.length + ") was found for parameter " + key);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return true if a value that does not exist in nameList is found in the parameterMap. otherwise, returns false.
	 */
	protected boolean containsUnknownParameter(Map<String, String[]> parameterMap, List<String> nameList) {
		Iterator<String> iterator = parameterMap.keySet().iterator(); 
		
		while(iterator.hasNext()) {
			String name = (String) iterator.next();
			if(! nameList.contains(name)) {
			
				_logger.warn("an incorrect parameter name was found: " + name);
				return true;
			}
		}
		
		return false;
	}
	
}
