package org.ohmage.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.ColumnKey;
import org.ohmage.domain.campaign.SurveyResponse.Function;
import org.ohmage.domain.campaign.SurveyResponse.FunctionPrivacyStateItem;
import org.ohmage.domain.campaign.SurveyResponse.OutputFormat;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
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
	 * @param surveyIds The string list of survey IDs.
	 * 
	 * @return A Set of unique survey IDs.
	 */
	public static Set<String> validateSurveyIds( final String surveyIds) 
			throws ValidationException {
		
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
	 * @param promptIds The string list of prompt IDs.
	 * 
	 * @return A Set of unique prompt IDs.
	 */
	public static Set<String> validatePromptIds(final String promptIds) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptIds)) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		String[] promptIdsArray = promptIds.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		for(int i = 0; i < promptIdsArray.length; i++) {
			String currPromptId = promptIdsArray[i].trim();
			
			if(! StringUtils.isEmptyOrWhitespaceOnly(currPromptId)) {
				result.add(currPromptId);
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that a survey database ID is a valid survey database ID.
	 * 
	 * @param surveyId A survey ID as a string to be validated.
	 * 
	 * @return Returns the survey ID or null if it was null or whitespace.
	 * 
	 * @throws ValidationException Thrown if the survey ID is not null, not
	 * 							   whitespace only, and not valid.
	 */
	public static UUID validateSurveyResponseId(final String surveyId) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyId)) {
			return null;
		}
		
		// Right now, this is simply the database ID, so as long as it is a
		// long we should be fine.
		try {
			return UUID.fromString(surveyId);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.SURVEY_INVALID_SURVEY_ID, 
					"Invalid survey ID given: " + surveyId);
		}
	}
	
	/**
	 * Validates that a privacy state is a valid survey response privacy state.
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
	public static SurveyResponse.PrivacyState validatePrivacyState(
			final String privacyState) throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		try {
			return SurveyResponse.PrivacyState.getValue(privacyState);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.SURVEY_INVALID_PRIVACY_STATE, 
					"The privacy state is unknown: " + privacyState);
		}
	}
	
	/**
	 * Validates that a set of usernames is either the special URN value or is 
	 * a valid list of usernames.
	 * 
	 * @param usernames The usernames list as a string.
	 * 
	 * @return A set of usernames that may only contain the special URN value,
	 * 		   or null if the usernames string is null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the usernames string listcontains 
	 * 							   a username that is invalid.
	 */
	public static Set<String> validateUsernames(final String usernames) 
			throws ValidationException {
		
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
		
		return UserValidators.validateUsernames(usernames);
	}
	
	/**
	 * Validates that the column list string contains only valid column keys or
	 * none at all. 
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
	public static Set<SurveyResponse.ColumnKey> validateColumnList(
			final String columnList) throws ValidationException {
		
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
						throw new ValidationException(
								ErrorCode.SURVEY_MALFORMED_COLUMN_LIST, 
								"The column list contains an unknown value: " +
									currValue, 
								e);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that a string value represents a known output format value.
	 * 
	 * @param outputFormat The output format as a string.
	 * 
	 * @return An OutputFormat object, or null if the output format string was
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the output format is unknown.
	 */
	public static OutputFormat validateOutputFormat(final String outputFormat) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			return null;
		}
		
		try {
			return OutputFormat.getValue(outputFormat);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, 
					"The output format is unknown: " + outputFormat);
		}
	}
	
	/**
	 * Validates that a list of sort order values contains all of the required
	 * values with no duplicates or is empty.
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
	public static List<SortParameter> validateSortOrder(final String sortOrder)
			throws ValidationException {
		
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
				throw new ValidationException(
						ErrorCode.SURVEY_INVALID_SORT_ORDER, 
						"An unknown sort order value was given: " + 
							sortOrderArray[i]);
			}
			
			if(result.contains(currSortParameter)) {
				throw new ValidationException(
						ErrorCode.SURVEY_INVALID_SORT_ORDER, 
						"The same sort order value was given multiple times: " + 
							currSortParameter.toString());
			}
			else {
				result.add(currSortParameter);
			}
		}
		
		if(result.size() != SortParameter.values().length) {
			throw new ValidationException(
					ErrorCode.SURVEY_INVALID_SORT_ORDER, 
					"There are " + 
						SortParameter.values().length + 
						" sort order values and " + 
						result.size() + 
						" were given.");
		}
		
		return result;
	}
	
	/**
	 * Validates that a function ID is a known function ID and returns it.
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
	public static Function validateFunction(final String function) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(function)) {
			return null;
		}
		
		try {
			return Function.valueOf(function.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.SURVEY_INVALID_SURVEY_FUNCTION_ID, 
					"The survey response function ID is unknown: " + 
						function);
		}
	}
	
	/**
	 * Validates a list of privacy state grouping items.
	 * 
	 * @param list The list of grouping items as a string.
	 * 
	 * @return The decoded set of grouping items.
	 * 
	 * @throws ValidationException Thrown if one of the items in the list 
	 * 							   wasn't decodable.
	 */
	public static Set<FunctionPrivacyStateItem> validatePrivacyStateGroupList(
			final String list)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(list)) {
			return Collections.emptySet();
		}
		
		String[] listArray = list.split(InputKeys.LIST_ITEM_SEPARATOR);
		int numItems = listArray.length;
		
		Set<FunctionPrivacyStateItem> result = 
			new HashSet<FunctionPrivacyStateItem>(numItems);
		
		for(int i = 0; i < listArray.length; i++) {
			String item = listArray[i];
			
			if((item != null) && 
					(! StringUtils.isEmptyOrWhitespaceOnly(item))) {
				
				try {
					result.add(FunctionPrivacyStateItem.getValue(item));
				}
				catch(IllegalArgumentException e) {
					throw new ValidationException(
							ErrorCode.SURVEY_FUNCTION_INVALID_PRIVACY_STATE_GROUP_ITEM,
							"The function grouping item is unknown.",
							e);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that a date is a valid Date and returns it.
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
	public static Date validateStartDate(final String startDate) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(startDate);
		if(result == null) {
			result = StringUtils.decodeDate(startDate);
		}
		
		if(result == null) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_DATE, 
					"The start date was unknown: " + 
						startDate);
		}
		else {
			return result;
		}
	}
	
	/**
	 * Validates that a date is a valid Date and returns it.
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
	public static Date validateEndDate(final String endDate) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			return null;
		}
		
		Date result = StringUtils.decodeDateTime(endDate);
		if(result == null) {
			result = StringUtils.decodeDate(endDate);
		}
		
		if(result == null) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_DATE, 
					"The end date was unknown: " + 
						endDate);
		}
		else {
			return result;
		}
	}
	
	/**
	 * Validates the optional suppressMetadata boolean.
	 * 
	 * @param suppressMetadata  The value to validate.
	 * @return  the Boolean equivalent of suppressMetadata 
	 * @throws ValidationException if suppressMetadata is not null and non-boolean.
	 * @throws IllegalArgumentException if the request is null
	 */
	public static Boolean validateSuppressMetadata(
			final String suppressMetadata) throws ValidationException {
		
		return validateOptionalBoolean(
				suppressMetadata, 
				ErrorCode.SURVEY_INVALID_SUPPRESS_METADATA_VALUE, 
				"The suppress metadata value is invalid: ");
	}

	/**
	 * Validates the optional returnId boolean.
	 * 
	 * @param returnId  The value to validate.
	 * @return  the Boolean equivalent of returnId 
	 * @throws ValidationException if returnId is not null and non-boolean.
	 */
	public static Boolean validateReturnId(final String returnId) 
			throws ValidationException {
		
		return validateOptionalBoolean(
				returnId, 
				ErrorCode.SURVEY_INVALID_RETURN_ID, 
				"The return ID value is invalid: ");
	}

	/**
	 * Validates the optional prettyPrint boolean.
	 * 
	 * @param returnId  The value to validate.
	 * @return  the Boolean equivalent of prettyPrint 
	 * @throws ValidationException if prettyPrint is not null and non-boolean.
	 */
	public static Boolean validatePrettyPrint(final String prettyPrint) 
			throws ValidationException {
		
		return validateOptionalBoolean(
				prettyPrint, 
				ErrorCode.SURVEY_INVALID_PRETTY_PRINT_VALUE, 
				"The pretty print value is invalid: ");
	}
	
	/**
	 * Validates the optional collapse boolean.
	 * 
	 * @param returnId  The value to validate.
	 * @return  the Boolean equivalent of collapse 
	 * @throws ValidationException if collapse is not null and non-boolean.
	 */
	public static Boolean validateCollapse(final String collapse) 
			throws ValidationException {
		
		return validateOptionalBoolean(
				collapse, 
				ErrorCode.SURVEY_INVALID_COLLAPSE_VALUE, 
				"The collapse value is invalid: ");
	}


	/**
	 * Utility for validating optional booleans where booleans must adhere to
	 * the strict values of "true" or "false" if the booleanString is not null.
	 * 
	 * @param booleanString  The string to validate.
	 * @param errorCode  The error code to use when failing the request.
	 * @param errorMessage  The error message to use when failing the request.
	 * @return A valid Boolean or null 
	 * @throws ValidationException if the booleanString cannot be strictly
	 * decoded to "true" or "false"
	 * @throws IllegalArgumentException if the request is null; if the error
	 * code is empty or null; or if the error message is empty or null.
	 */
	private static Boolean validateOptionalBoolean(final String booleanString, 
			final ErrorCode errorCode, final String errorMessage) 
			throws ValidationException {
		
		// don't validate the optional value if it doesn't exist
		if(StringUtils.isEmptyOrWhitespaceOnly(booleanString)) {
			return null;
		}

		// perform validation
		if(StringUtils.decodeBoolean(booleanString) == null) {
			throw new ValidationException(
					errorCode, 
					errorMessage + booleanString
				);
		}
		
		return Boolean.valueOf(booleanString);
	}
}