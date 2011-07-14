package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.util.StringUtils;

/**
 * Validates boolean and boolean-related values.
 * 
 * @author John Jenkins
 */
public final class BooleanValidators {
	private static final Logger LOGGER = Logger.getLogger(BooleanValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private BooleanValidators() {}
	
	/**
	 * Checks if some String, 'value', is a valid boolean value. The String 
	 * must be in all lower case and English. See
	 * {@link org.ohmage.util.StringUtils#decodeBoolean(String)} to find the
	 * exact specifications on what we consider to be a boolean value.
	 * 
	 * @param value A String representation of a boolean value. This must be 
	 * 				in English and all lower case.
	 * 
	 * @return Returns true if the value is null, whitespace only, or a valid
	 * 		   boolean value; otherwise, false is returned.
	 * 
	 * @see org.ohmage.util.StringUtils#decodeBoolean(String)
	 */
	public static boolean validateBoolean(String value) {
		LOGGER.info("Validating whether a value is a boolean value or not.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return true;
		}
		
		if("true".equals(value) || "false".equals(value)) {
			return true;
		}
		else {
			return false;
		}
	}
}
