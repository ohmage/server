package org.ohmage.validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.ColumnKey;
import org.ohmage.domain.campaign.SurveyResponse.Function;
import org.ohmage.domain.campaign.SurveyResponse.OutputFormat;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.survey.SurveyResponseReadRequest;
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
	 * Validates a string representing a list of survey IDs.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param surveyIds The string list of survey IDs.
	 * 
	 * @return A Set of unique survey IDs.
	 */
	public static Set<String> validateSurveyIds(final Request request, final String surveyIds) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyIds)) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		String[] surveyIdsArray = surveyIds.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		for(int i = 0; i < surveyIdsArray.length; i++) {
			String currSurveyId = surveyIdsArray[i].trim();
			
			if(! StringUtils.isEmptyOrWhitespaceOnly(currSurveyId)) {
				result.add(currSurveyId);
			}
		}
		
		return result;
	}
	
	/**
	 * Validates a string representing a list of prompt response IDs.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param promptIds The string list of prompt IDs.
	 * 
	 * @return A Set of unique prompt IDs.
	 */
	public static Set<String> validatePromptIds(final Request request, final String promptIds) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(promptIds)) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		String[] surveyIdsArray = promptIds.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		for(int i = 0; i < surveyIdsArray.length; i++) {
			String currSurveyId = surveyIdsArray[i].trim();
			
			if(! StringUtils.isEmptyOrWhitespaceOnly(currSurveyId)) {
				result.add(currSurveyId);
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that a survey database ID is a valid survey database ID.
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
	// TODO: Rename to validateSurveyResponseId
	public static Long validateSurveyDbId(Request request, String surveyId) throws ValidationException {
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
	public static SurveyResponse.PrivacyState validatePrivacyState(Request request, String privacyState) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		try {
			return SurveyResponse.PrivacyState.getValue(privacyState);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "The privacy state is unknown: " + privacyState);
			throw new ValidationException("The privacy state is unknown: " + privacyState);
		}
	}
	
	/**
	 * Validates that a set of usernames is either the special URN value or is 
	 * a valid list of usernames.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param usernames The usernames list as a string.
	 * 
	 * @return A set of usernames that may only contain the special URN value,
	 * 		   or null if the usernames string is null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the usernames string listcontains 
	 * 							   a username that is invalid.
	 */
	public static Set<String> validateUsernames(final Request request, final String usernames) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(usernames)) {
			return null;
		}
		
		String[] usernamesArray = usernames.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < usernamesArray.length; i++) {
			if(SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(usernamesArray[i])) {
				Set<String> result = new HashSet<String>(1);
				result.add(SurveyResponseReadRequest.URN_SPECIAL_ALL);
				return result;
			}
		}
		
		return UserValidators.validateUsernames(request, usernames);
	}
	
	/**
	 * Validates that the column list string contains only valid column keys or
	 * none at all. 
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param columnList The column list as a string.
	 * 
	 * @return Null if the column list string is null or whitespace only;
	 * 		   otherwise, a, possibly empty, list of column keys is returned.
	 * 		   If the special all keys key is given, the resulting collection
	 * 		   will contain all of the known keys.
	 *  
	 * @throws ValidationException Thrown if an unknown key is found.
	 */
	public static Set<SurveyResponse.ColumnKey> validateColumnList(final Request request, final String columnList) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(columnList)) {
			return null;
		}
		
		Set<SurveyResponse.ColumnKey> result = new HashSet<SurveyResponse.ColumnKey>();
		
		// Split the list into the individual items and cycle through them.
		String[] columnListArray = columnList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < columnListArray.length; i++) {
			// Sometimes the split function parses out empty strings, so we 
			// will ignore those.
			if(! StringUtils.isEmptyOrWhitespaceOnly(columnListArray[i])) {
				// Get the current non-null, non-empty string value.
				String currValue = columnListArray[i].trim();
				
				// Attempt to parse it into a known column key and add it.
				try {
					result.add(SurveyResponse.ColumnKey.getValue(currValue));
				}
				catch(IllegalArgumentException e) {
					// If the column key is unknown, check if it is the special
					// key that represents all columns.
					if(SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(currValue)) {
						// It is the special key, so add all of the known 
						// column keys and return the result.
						ColumnKey[] allKeys = SurveyResponse.ColumnKey.values();
						for(int j = 0; j < allKeys.length; j++) {
							result.add(allKeys[j]);
						}
						
						return result;
					}
					// It is not the special key and wasn't a known key, so 
					// throw an exception.
					else {
						request.setFailed(ErrorCodes.SURVEY_MALFORMED_COLUMN_LIST, "The column list contains an unknown value: " + currValue);
						throw new ValidationException("The column list contains an unknown value: " + currValue, e);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that a string value represents a known output format value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param outputFormat The output format as a string.
	 * 
	 * @return An OutputFormat object, or null if the output format string was
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the output format is unknown.
	 */
	public static OutputFormat validateOutputFormat(final Request request, final String outputFormat) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			return null;
		}
		
		try {
			return OutputFormat.getValue(outputFormat);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_OUTPUT_FORMAT, "The output format is unknown: " + outputFormat);
			throw new ValidationException("The output format is unknown: " + outputFormat);
		}
	}
	
	/**
	 * Validates that a list of sort order values contains all of the required
	 * values with no duplicates or is empty.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param sortOrder The sort order values as a string.
	 * 
	 * @return An ordered list of SortParameter representing the sort 
	 * 		   parameters in their required order.
	 * 
	 * @throws ValidationException Thrown if an unknown sort order value is 
	 * 							   given, if the same sort order value is given
	 * 							   multiple times, or if all of the known sort
	 * 							   order values weren't given.
	 */
	public static List<SortParameter> validateSortOrder(final Request request, final String sortOrder) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(sortOrder)) {
			return null;
		}
		
		List<SortParameter> result = new ArrayList<SortParameter>(3);
		
		String[] sortOrderArray = sortOrder.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0 ; i < sortOrderArray.length; i++) {
			SortParameter currSortParameter;
			try {
				currSortParameter = SortParameter.getValue(sortOrderArray[i].trim());
			}
			catch(IllegalArgumentException e) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, "An unknown sort order value was given: " + sortOrderArray[i]);
				throw new ValidationException("An unknown sort order value was given: " + sortOrderArray[i]);
			}
			
			if(result.contains(currSortParameter)) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, "The same sort order value was given multiple times: " + currSortParameter.toString());
				throw new ValidationException("The same sort order value was given multiple times: " + currSortParameter.toString());
			}
			else {
				result.add(currSortParameter);
			}
		}
		
		if(result.size() != SortParameter.values().length) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, "There are " + SortParameter.values().length + " sort order values and " + result.size() + " were given.");
			throw new ValidationException("There are " + SortParameter.values().length + " sort order values and " + result.size() + " were given.");
		}
		
		return result;
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
	
	/**
	 * Validates the optional suppressMetadata boolean.
	 * 
	 * @param request  The request to fail should suppressMetadata be invalid.
	 * @param suppressMetadata  The value to validate.
	 * @return  the Boolean equivalent of suppressMetadata 
	 * @throws ValidationException if suppressMetadata is not null and non-boolean.
	 * @throws IllegalArgumentException if the request is null
	 */
	public static Boolean validateSuppressMetadata(Request request, String suppressMetadata) throws ValidationException {
		
		return validateOptionalBoolean(request, suppressMetadata, ErrorCodes.SURVEY_INVALID_SUPPRESS_METADATA_VALUE, "The suppress metadata value is invalid: ");
	}

	/**
	 * Validates the optional returnId boolean.
	 * 
	 * @param request  The request to fail should returnId be invalid.
	 * @param returnId  The value to validate.
	 * @return  the Boolean equivalent of returnId 
	 * @throws ValidationException if returnId is not null and non-boolean.
	 */
	public static Boolean validateReturnId(Request request, String returnId) throws ValidationException {
		
		return validateOptionalBoolean(request, returnId, ErrorCodes.SURVEY_INVALID_RETURN_ID, "The return ID value is invalid: ");
	}

	/**
	 * Validates the optional prettyPrint boolean.
	 * 
	 * @param request  The request to fail should prettyPrint be invalid.
	 * @param returnId  The value to validate.
	 * @return  the Boolean equivalent of prettyPrint 
	 * @throws ValidationException if prettyPrint is not null and non-boolean.
	 */
	public static Boolean validatePrettyPrint(Request request, String prettyPrint) throws ValidationException {
		
		return validateOptionalBoolean(request, prettyPrint, ErrorCodes.SURVEY_INVALID_PRETTY_PRINT_VALUE, "The pretty print value is invalid: ");
	}
	
	/**
	 * Validates the optional collapse boolean.
	 * 
	 * @param request  The request to fail should collapse be invalid.
	 * @param returnId  The value to validate.
	 * @return  the Boolean equivalent of collapse 
	 * @throws ValidationException if collapse is not null and non-boolean.
	 */
	public static Boolean validateCollapse(Request request, String collapse) throws ValidationException {
		
		return validateOptionalBoolean(request, collapse, ErrorCodes.SURVEY_INVALID_COLLAPSE_VALUE, "The collapse value is invalid: ");
	}


	/**
	 * Utility for validating optional booleans where booleans must adhere to
	 * the strict values of "true" or "false" if the booleanString is not null.
	 * 
	 * @param request The request to fail should the booleanString be invalid.
	 * @param booleanString  The string to validate.
	 * @param errorCode  The error code to use when failing the request.
	 * @param errorMessage  The error message to use when failing the request.
	 * @return A valid Boolean or null 
	 * @throws ValidationException if the booleanString cannot be strictly
	 * decoded to "true" or "false"
	 * @throws IllegalArgumentException if the request is null; if the error
	 * code is empty or null; or if the error message is empty or null.
	 */
	private static Boolean validateOptionalBoolean(Request request, String booleanString, String errorCode, String errorMessage) 
		throws ValidationException {
		
		// check for logical errors
		if(request == null) {
			throw new IllegalArgumentException("The Request cannot be null");
		}
		
		// don't validate the optional value if it doesn't exist
		if(StringUtils.isEmptyOrWhitespaceOnly(booleanString)) {
			return null;
		}

		// perform validation
		if(StringUtils.decodeBoolean(booleanString) == null) {
			request.setFailed(errorCode, errorMessage + booleanString);
			throw new ValidationException(errorMessage + booleanString);
		}
		
		return Boolean.valueOf(booleanString);
	}
}