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
	 * Validates that some String, 'value', is a valid boolean value. The 
	 * String must be in all lower case and English.
	 * 
	 * @param value A String representation of a boolean value. This must be 
	 * 				in English and all lower case.
	 * 
	 * @return Returns true if the value is null, whitespace only, or a valid
	 * 		   boolean value; otherwise, false is returned.
	 */
	public static boolean validateBoolean(String value) {
		LOGGER.info("Validating a boolean value.");
		
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
