package org.ohmage.validator;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.exception.ErrorCodeException;
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
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private MobilityValidators() {}
	
	/**
	 * Validates Mobility data in the format of a JSONArray of JSONObjects 
	 * where each JSONObject is a Mobility data point.
	 * 
	 * @param data The data to be validated. The expected value is a JSONArray
	 * 			   of JSONObjects where each JSONObject is a Mobility data
	 * 			   point.
	 * 
	 * @return Returns null if the data is null or whitespace only; otherwise,
	 * 		   a list of MobilityInformation objects is returned.
	 * 
	 * @throws ValidationException Thrown if the data is not null, not
	 * 							   whitespace only, and the data String is not
	 * 							   a JSONArray of JSONObjects or any of the 
	 * 							   JSONObjects cannot become a 
	 * 							   MobilityInformation object.
	 */
	public static List<MobilityPoint> validateDataAsJsonArray(
			final String data) throws ValidationException {
		
		LOGGER.info("Validating a JSONArray of data points.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(data)) {
			return null;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(data.trim());
			
			List<MobilityPoint> result = new LinkedList<MobilityPoint>();
			for(int i = 0; i < jsonArray.length(); i++) {
				result.add(new MobilityPoint(jsonArray.getJSONObject(i), MobilityPoint.PrivacyState.PRIVATE));
			}
			
			return result;
		}
		catch(JSONException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_JSON, 
					"The JSONArray containing the data is malformed.", 
					e);
		}
		catch(ErrorCodeException e) {
			throw new ValidationException(e);
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
}