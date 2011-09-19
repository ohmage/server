package org.ohmage.validator;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.MobilityPrivacyStateCache;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.domain.MobilityInformation.MobilityException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.Request;
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
	 * @param request The Request that is performing this service.
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
	public static List<MobilityInformation> validateDataAsJsonArray(Request request, String data) throws ValidationException {
		LOGGER.info("Validating a JSONArray of data points.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(data)) {
			return null;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(data.trim());
			
			List<MobilityInformation> result = new LinkedList<MobilityInformation>();
			for(int i = 0; i < jsonArray.length(); i++) {
				result.add(new MobilityInformation(jsonArray.getJSONObject(i), MobilityPrivacyStateCache.PrivacyState.PRIVATE));
			}
			
			return result;
		}
		catch(JSONException e) {
			request.setFailed(ErrorCodes.SERVER_INVALID_JSON, "The JSONArray containing the data is malformed.");
			throw new ValidationException("The JSONArray containing the data is malformed.", e);
		}
		catch(MobilityException e) {
			request.setFailed(e.getErrorCode(), e.getErrorText());
			throw new ValidationException(e.getErrorText(), e);
		}
	}
	
	/**
	 * Validates that the date is valid and returns it or fails the request.
	 * 
	 * @param request The Request performing this validation.
	 * 
	 * @param date The date value as a string.
	 * 
	 * @return The date decoded into a Date object or null if the string was
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the date string was not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateDate(Request request, String date) throws ValidationException { 
		LOGGER.info("Validating a date value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(date)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(date);
		if(result == null) {
			result = StringUtils.decodeDate(date);
		}
		
		if(result == null) {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The date value is unknown: " + date);
			throw new ValidationException("The date value is unknown: " + date);
		}
		else {
			return result;
		}
	}
}