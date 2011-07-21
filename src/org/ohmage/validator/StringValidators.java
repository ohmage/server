package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class to contain all of the functionality for validating Strings.
 * 
 * @author John Jenkins
 */
public final class StringValidators {
	private static final Logger LOGGER = Logger.getLogger(StringValidators.class);
	
	/**
	 * Default constructor. Private so that no one can instantiate it.
	 */
	private StringValidators() {}
	
	/**
	 * Validates that a String value is not profane.
	 * 
	 * @param request The request that is having this String value checked.
	 * 
	 * @param string The String value to check.
	 * 
	 * @return If the String value is null or whitespace only, null is 
	 * 		   returned. Otherwise, the String value is returned.
	 * 
	 * @throws ValidationException Thrown if the String value is profane.
	 * 
	 * @deprecated
	 */
	public static String validateString(Request request, String string) throws ValidationException{
		LOGGER.info("Validating that a String is not profane.");
		
		// If the value is null or whitespace only, return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(string)) {
			return null;
		}
		
		// TODO: Add a profanity filter.
		
		return string;
	}
	
	/**
	 * Check is a String contains any profanity or not. Note: This is not yet
	 * implemented and will always return true!
	 *  
	 * @param string The String that is being checked for profanity.
	 * 
	 * @return Returns false if the String is null or contains no profanity;
	 * 		   otherwise, it returns false.
	 */
	public static boolean isProfane(String string) {
		if(string == null) {
			return false;
		}
		
		// TODO: Add a profanity filter.
		
		return false;
	}
	
	/**
	 * Checks if a String's length is greater than or equal to some 'min' 
	 * value, and less than or equal to some 'max' value. The String should not
	 * be null, but if it is, false is returned.
	 * 
	 * @param string The String value whose length is being checked.
	 * 
	 * @param min The minimum allowed value for the String.
	 * 
	 * @param max The maximum allowed value for the String.
	 * 
	 * @return Returns false if the String is null, 'min' is greater than 
	 * 		   'max', or if the String's length is not within the bounds; 
	 * 		   otherwise, true is returned.
	 */
	public static boolean lengthWithinLimits(String string, int min, int max) {
		if(string == null) {
			LOGGER.warn("Don't pass a null value to this function.");
			return false;
		}
		
		if(min > max) {
			LOGGER.warn("The min value, '" + min + "', is greater than the max value, '" + max + "'.");
			return false;
		}
		
		int stringLength = string.length();
		return((stringLength >= min) && (stringLength <= max));
	}
}
