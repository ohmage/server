package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.exception.ValidationException;
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
	 */
	public static String validateString(Request request, String string) throws ValidationException{
		LOGGER.info("Validating that a String is not profane.");
		
		// If the value is null or whitespace only, return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(string)) {
			return null;
		}
		
		// TODO: Add a profanity filter.
		
		return string.trim();
	}
}
