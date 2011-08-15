package org.ohmage.validator;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.domain.MobilityInformation.MobilityException;
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
				result.add(new MobilityInformation(jsonArray.getJSONObject(i)));
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
}