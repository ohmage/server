package org.ohmage.validator;

import java.util.Date;

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
	 * The known "survey response function" functions.
	 * 
	 * @author John Jenkins
	 */
	public static enum Function { COMPLETED_SURVEYS, STATS }
	
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
	public static SurveyResponsePrivacyStateCache.PrivacyState validatePrivacyState(Request request, String privacyState) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		try {
			return SurveyResponsePrivacyStateCache.PrivacyState.getValue(privacyState);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "The privacy state is unknown: " + privacyState);
			throw new ValidationException("The privacy state is unknown: " + privacyState);
		}
	}
	
	/**
	 * Validates that a function ID is a known function ID and returns it.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param function The function ID as a string.
	 * 
	 * @return The decoded function ID as a Function or null if the string was
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the function was not null, not
	 * 							   whitespace only, and not a valid function 
	 * 							   ID.
	 */
	public static Function validateFunction(Request request, String function) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(function)) {
			return null;
		}
		
		try {
			return Function.valueOf(function.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_FUNCTION_ID, "The survey response function ID is unknown: " + function);
			throw new ValidationException("The survey response function ID is unknown: " + function);
		}
	}
	
	/**
	 * Validates that a date is a valid Date and returns it.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param startDate The start date to be validated and decoded into a Date
	 * 					object.
	 * 
	 * @return Returns null if the start date was null or whitespace only.
	 * 		   Otherwise, it returns the decoded Date.
	 * 
	 * @throws ValidationException Thrown if the start date was not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateStartDate(Request request, String startDate) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(startDate);
		if(result == null) {
			result = StringUtils.decodeDate(startDate);
		}
		
		if(result == null) {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date was unknown: " + startDate);
			throw new ValidationException("The start date was unknown: " + startDate);
		}
		else {
			return result;
		}
	}
	
	/**
	 * Validates that a date is a valid Date and returns it.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param endDate The end date to be validated and decoded into a Date
	 * 				  object.
	 * 
	 * @return Returns null if the end date was null or whitespace only.
	 * 		   Otherwise, it returns the decoded Date.
	 * 
	 * @throws ValidationException Thrown if the end date was not null, not
	 * 							   whitespace only, and not a valid date.
	 */
	public static Date validateEndDate(Request request, String endDate) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(endDate);
		if(result == null) {
			result = StringUtils.decodeDate(endDate);
		}
		
		if(result == null) {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The end date was unknown: " + endDate);
			throw new ValidationException("The end date was unknown: " + endDate);
		}
		else {
			return result;
		}
	}
}