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
	 * <p>Validates that some String, 'value', is a valid boolean value. The 
	 * String must be in all lower case and English.</p>
	 * <p>There is no special case for null. See 
	 * {@link org.ohmage.util.StringUtils#decodeBoolean(String)} to determine
	 * what is a valid boolean value.</p>
	 * 
	 * @param value A String representation of a boolean value. This must be 
	 * 				in English and all lower case.
	 * 
	 * @return Returns true if the value is a valid boolean value; returns 
	 * 		   false if it is not a valid boolean value.
	 * 
	 * @see org.ohmage.util.StringUtils#decodeBoolean(String)
	 */
	public static boolean validateBoolean(String value) {
		LOGGER.info("Checking if a boolean value is a valid a boolean value.");
		
		if(StringUtils.decodeBoolean(value) != null) {
			return true;
		}
		else {
			return false;
		}
	}
}
