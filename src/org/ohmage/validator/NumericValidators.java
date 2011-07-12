package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class contains all of the functionality for validating numeric values.
 * 
 * @author John Jenkins
 */
public class NumericValidators {
	private static final Logger LOGGER = Logger.getLogger(NumericValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private NumericValidators() {}
	
	/**
	 * Validates that some String, 'value', is a valid integer and that it 
	 * falls between two values, inclusively.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param value A String representation of an integer value.
	 * 
	 * @param minValue The minimum allowed value for the integer, inclusive.
	 * 
	 * @param maxValue The maximum allowed value for the integer, inclusive.
	 * 
	 * @return Returns null if the 'value' is null or whitespace only. 
	 * 		   Otherwise, it returns an integer representation of the value.
	 * 
	 * @throws ValidationException Thrown if 'value' is not null, not 
	 * 							   whitespace only, and not a valid String
	 * 							   representation of an integer value.
	 */
	public static Integer validateInteger(Request request, String value, int minValue, int maxValue) throws ValidationException {
		LOGGER.info("Validating an integer value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			Integer result =  Integer.decode(value);
			
			if(result < minValue) {
				request.setFailed(ErrorCodes.INVALID_INTEGER_VALUE, "A value is less than its minimum allowed value of " + minValue + ": " + result);
				throw new ValidationException("A value is less than the minimum allowed value of " + minValue + ": " + result);
			}
			else if(result > maxValue) {
				request.setFailed(ErrorCodes.INVALID_INTEGER_VALUE, "A value is greater than its maximum allowed value of " + maxValue + ": " + result);
				throw new ValidationException("A value is greater than its maximum allowed value of " + maxValue + ": " + result);
			}
			else {
				return result;
			}
		}
		catch(NumberFormatException e) {
			request.setFailed(ErrorCodes.INVALID_INTEGER_VALUE, "Not a valid integer value: " + value);
			throw new ValidationException("Not a valid integer value: " + value, e);
		}
	}
}
