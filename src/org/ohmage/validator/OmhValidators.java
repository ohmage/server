package org.ohmage.validator;

import java.util.HashMap;
import java.util.Map;

import org.ohmage.annotator.Annotator.ErrorCode;
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
	 * Validates that a payload ID is valid and returns the observer ID and the
	 * stream ID.
	 * 
	 * @param value The payload ID to validate.
	 * 
	 * @return A map containing one key, the observer ID, mapped to the stream
	 * 		   ID.
	 * 
	 * @throws ValidationException The payload ID is not valid.
	 */
	public static Map<String, String> validatePayloadId(
			final String value) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		String trimmedValue = value.trim();
		String[] split = trimmedValue.split(":");
		if(split.length != 4) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"It must contain 4 sections, each divided by a ':': " +
					trimmedValue);
		}
		
		// The first part must be "omh".
		if("omh".equals(split[0])) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The first section must be 'omh': " +
					trimmedValue);
		}
		
		// The second part must be "ohmage".
		if("ohmage".equals(split[1])) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The second section must be 'ohmage': " +
					trimmedValue);
		}
		
		String observerId;
		try {
			observerId = ObserverValidators.validateObserverId(split[2]);
		}
		catch(ValidationException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The third section must be a valid observer ID: " +
					trimmedValue);
		}
		if(observerId == null) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The third section is empty: " +
					trimmedValue);
		}
		
		String streamId;
		try {
			streamId = ObserverValidators.validateStreamId(split[3]);
		}
		catch(ValidationException e) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The forth section must be a valid stream ID: " +
					trimmedValue);
		}
		if(streamId == null) {
			throw new ValidationException(
				ErrorCode.OMH_INVALID_PAYLOAD_ID,
				"The payload ID is not valid. " +
					"The forth section is empty: " +
					trimmedValue);
		}
		
		Map<String, String> result = new HashMap<String, String>();
		result.put(observerId, streamId);
		
		return result;
		
		
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
