package org.ohmage.validator;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information pertaining to Mobility
 * data.
 * 
 * @author John Jenkins
 */
public final class MobilityValidators {
	private static final Logger LOGGER = Logger.getLogger(MobilityValidators.class);
	
	/**
	 * The multiplier used to convert the time given by the user into a valid
	 * milliseconds value.<br />
	 * <br />
	 * Current value: Minutes
	 */
	public static final long CHUNK_DURATION_MULTIPLIER = 1000 * 60; 
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private MobilityValidators() {}
	
	/**
	 * Validates Mobility data in the format of a JSONArray of JSONObjects 
	 * where each JSONObject is a Mobility data point. It returns the decoded
	 * Mobility points and adds the invalid Mobility points and their 
	 * respective ID (if retrievable) to the parameterized lists.
	 * 
	 * @param data The data to be validated. The expected value is a JSONArray
	 * 			   of JSONObjects where each JSONObject is a Mobility data
	 * 			   point.
	 * 
	 * @param invalidPointIds The IDs from their respective JSON objects where
	 * 						  the entire point contained some error(s).
	 * 
	 * @param invalidPoint The JSONObject for each point that contained 
	 * 					   some error. There may or may not be a 
	 * 					   corresponding ID in the 'invalidPointIds' list
	 * 					   depending on whether the ID was retrievable or
	 * 					   not.
	 * 
	 * @return Returns null if the data is null or whitespace only; otherwise,
	 * 		   a list of MobilityPoint objects is returned.
	 * 
	 * @throws ValidationException Thrown if the data is not null, not
	 * 							   whitespace only, and the data String cannot
	 * 							   be decoded into JSON.
	 */
	public static List<MobilityPoint> validateDataAsJsonArray(
			final String data, 
			Collection<JSONObject> invalidPoint) 
			throws ValidationException {
		
		LOGGER.info("Validating a JSONArray of data points.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(data)) {
			return null;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(data.trim());
			
			List<MobilityPoint> result = new LinkedList<MobilityPoint>();
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject mobilityPointJson = jsonArray.getJSONObject(i);
				
				try {
					result.add(
							new MobilityPoint(
									mobilityPointJson, 
									MobilityPoint.PrivacyState.PRIVATE));
				}
				catch(DomainException invalidPointException) {
					LOGGER.warn("Invalid point.", invalidPointException);
					invalidPoint.add(mobilityPointJson);
				}
			}
			
			return result;
		}
		catch(JSONException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_JSON, 
					"The JSONArray containing the data is malformed.", 
					e);
		}
	}
	
	/**
	 * Validates that the date is valid and returns it or fails the request.
	 * 
	 * @param date The date value as a string.
	 * 
	 * @return The date decoded into a Date object or null if the string was
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the date string was not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateDate(final String date) 
			throws ValidationException {
		
		LOGGER.info("Validating a date value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(date)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(date);
		if(result == null) {
			result = StringUtils.decodeDate(date);
		}
		
		if(result == null) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_DATE, 
					"The date value is unknown: " + date);
		}
		else {
			return result;
		}
	}
	
	/**
	 * Validates that the duration is a valid number and that it is not too 
	 * large. The largeness restriction is based on the multiplier used to
	 * convert this value to its internal milliseconds representation.
	 * 
	 * @param duration The duration to be validated.
	 * 
	 * @return The duration as a long value or null if the duration was null or
	 * 		   an empty string.
	 * 
	 * @throws ValidationException Thrown if there is an error.
	 */
	public static Long validateChunkDuration(final String duration)
			throws ValidationException {
		
		LOGGER.info("Validating the chunk duration.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(duration)) {
			return null;
		}
		
		try {
			Long longDuration = Long.decode(duration);
			
			if(longDuration > (Long.MAX_VALUE / CHUNK_DURATION_MULTIPLIER)) {
				throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_CHUNK_DURATION,
						"The chunk duration is too great. It can be at most " +
								(Long.MAX_VALUE / CHUNK_DURATION_MULTIPLIER));
			}
			
			return longDuration * CHUNK_DURATION_MULTIPLIER;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.MOBILITY_INVALID_CHUNK_DURATION,
					"The chunk duration is invalid: " + duration);
		}
	}
}