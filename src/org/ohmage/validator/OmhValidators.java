package org.ohmage.validator;

import java.util.regex.Pattern;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * Class for validating OMH parameters.
 *
 * @author John Jenkins
 */
public class OmhValidators {
	private static final String SCHEMA_PAYLOAD_ID = 
		"^omh:([a-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$";
	private static final Pattern PATTERN_PAYLOAD_ID = 
		Pattern.compile(SCHEMA_PAYLOAD_ID);
	
	/**
	 * Default constructor made private as the static methods should be used.
	 */
	private OmhValidators() {}
	
	/**
	 * Validates that a payload ID is valid.
	 * 
	 * @param value The payload ID to validate.
	 * 
	 * @return The trimmed and validated payload ID.
	 * 
	 * @throws ValidationException The payload ID is not valid.
	 */
	public static String validatePayloadId(
			final String value) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		String trimmedValue = value.trim();
		if(PATTERN_PAYLOAD_ID.matcher(trimmedValue).matches()) {
			return trimmedValue;
		}
		else {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. It must match the schema '" +
					SCHEMA_PAYLOAD_ID +
					"': " +
					trimmedValue);
		}
	}
	
	/**
	 * Validates that a payload version string is a valid payload version and
	 * then returns it.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return The long value representation of the version.
	 * 
	 * @throws ValidationException The version was not a valid number.
	 */
	public static Long validatePayloadVersion(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return Long.decode(value);
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
				"The payload version is not a whole number: " +
					value);
		}
	}
}
