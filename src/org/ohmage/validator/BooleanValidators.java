package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

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
	 * @param request The request that is performing this validation.
	 * 
	 * @param value A String representation of a boolean value. This must be 
	 * 				in English and all lower case.
	 * 
	 * @return Returns null if the 'value' is null or whitespace only. 
	 * 		   Otherwise, it returns a boolean representation of the value.
	 * 
	 * @throws ValidationException Thrown if 'value' is not null, not 
	 * 							   whitespace only, and not a valid String
	 * 							   representation of a boolean value.
	 */
	public static Boolean validateBoolean(Request request, String value) throws ValidationException {
		LOGGER.info("Validating a boolean value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if("true".equals(value)) {
			return true;
		}
		else if("false".equals(value)) {
			return false;
		}
		else {
			request.setFailed(ErrorCodes.INVALID_BOOLEAN_VALUE, "Invalid boolean value: " + value);
			throw new ValidationException("Invalid boolean value: " + value);
		}
	}
}
