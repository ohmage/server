package org.ohmage.validator;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating survey response-based items.
 * 
 * @author John Jenkins
 */
public final class SurveyResponseValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private SurveyResponseValidators() {}
	
	/**
	 * Validates that a survey ID is a valid survey ID.
	 * 
	 * @param request The Request performing this validation.
	 * 
	 * @param surveyId A survey ID as a string to be validated.
	 * 
	 * @return Returns the survey ID or null if it was null or whitespace.
	 * 
	 * @throws ValidationException Thrown if the survey ID is not null, not
	 * 							   whitespace only, and not valid.
	 */
	public static Long validateSurveyId(Request request, String surveyId) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyId)) {
			return null;
		}
		
		// Right now, this is simply the database ID, so as long as it is a
		// long we should be fine.
		try {
			return Long.decode(surveyId);
		}
		catch(NumberFormatException e) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, "Invalid survey ID given: " + surveyId);
			throw new ValidationException("Invalid survey ID given: " + surveyId);
		}
	}
	
	/**
	 * Validates that a privacy state is a valid survey response privacy state.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param privacyState The privacy state to be validated.
	 * 
	 * @return Returns null if the privacy state is null or whitespace only;
	 * 		   otherwise, the privacy state is returned.
	 * 
	 * @throws ValidationException Thrown if the privacy state is not null, not
	 * 							   whitespace only, and not a valid survey 
	 * 							   response privacy state.
	 */
	public static String validatePrivacyState(Request request, String privacyState) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		if(SurveyResponsePrivacyStateCache.instance().getKeys().contains(privacyState.trim())) {
			return privacyState.trim();
		}
		else {
			request.setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "The privacy state is unknown: " + privacyState);
			throw new ValidationException("The privacy state is unknown: " + privacyState);
		}
	}
}
