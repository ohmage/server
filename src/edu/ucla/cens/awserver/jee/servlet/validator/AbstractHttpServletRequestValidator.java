package edu.ucla.cens.awserver.jee.servlet.validator;

import org.apache.log4j.Logger;

/**
 * Helper class for common validation tasks.
 * 
 * @author selsky
 */
public abstract class AbstractHttpServletRequestValidator implements HttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(AbstractHttpServletRequestValidator.class);
	
	/**
	 * @return true if the provided value is longer than the provided length and print an informative message to the log 
	 * 
	 * TODO remove this same method from AbstractAwHttpServlet
	 * 
	 */
	protected boolean greaterThanLength(String longName, String name, String value, int length) {
		
		if(null != value && value.length() > length) {
			
			_logger.warn("a " + longName + "(request parameter " + name + ") of " + value.length() + " characters was found");
			return true;
		}
		
		return false;
	}
	
}
