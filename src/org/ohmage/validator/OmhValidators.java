package org.ohmage.validator;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.OmhThirdPartyRegistration;
import org.ohmage.domain.PayloadId;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * Class for validating OMH parameters.
 *
 * @author John Jenkins
 */
public class OmhValidators {
	/**
	 * Default constructor made private as the static methods should be used.
	 */
	private OmhValidators() {}
	
	/**
	 * Validates that a payload ID is valid and returns a PayloadId object that
	 * represents that payload ID.
	 * 
	 * @param value The payload ID to validate.
	 * 
	 * @return A PayloadId representing the payload ID.
	 * 
	 * @throws ValidationException The payload ID is not valid.
	 */
	public static PayloadId validatePayloadId(
			final String value) 
			throws ValidationException {
		
		// Make sure it is not null or empty.
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		// Trim the white space.
		String trimmedValue = value.trim();
		
		// Split it based on the ":"s.
		String[] split = trimmedValue.split(":");
		
		// Make sure it has at least two parts.
		if(split.length < 2) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"It must contain at least 2 sections, " +
						"\"omh\" and a domain: " +
					trimmedValue);
		}
		
		// The first part must be "omh".
		if(! "omh".equals(split[0])) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The first section must be 'omh': " +
					trimmedValue);
		}
		
		// Lookup the second part in the registry and return the compiled
		// payload ID.
		try {
			return OmhThirdPartyRegistration.getPayloadId(split);
		}
		catch(IllegalArgumentException e) {
			throw
				new ValidationException(
					ErrorCode.OMH_INVALID_PAYLOAD_ID,
					"The payload ID was invalid: " + trimmedValue,
					e);
		}
		catch(IllegalStateException e) {
			throw
				new ValidationException(
					ErrorCode.SYSTEM_GENERAL_ERROR,
					"There was an error creating the PayloadId object.",
					e);
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