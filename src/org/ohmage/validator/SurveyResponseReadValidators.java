package org.ohmage.validator;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.util.StringUtils;

/**
 * Validation utilities for survey response read.
 * 
 * @author Joshua Selsky
 */
public final class SurveyResponseReadValidators {
	
//	private static Logger LOGGER = Logger.getLogger(SurveyResponseReadValidators.class);
	
	// list size constants
	private static final int MAX_NUMBER_OF_USERS = 10;
	private static final int MAX_NUMBER_OF_PROMPTS = 10;
	private static final int MAX_NUMBER_OF_SURVEYS = 10;
//	private static final int MAX_NUMBER_OF_COLUMNS = 10;
	private static final int ALLOWED_NUMBER_OF_SORT_ORDER_ITEMS = 3;
	
	// logical errors
	private static final String NULL_REQUEST = "The Request cannot be null";
	private static final String EMPTY_OR_NULL_ALLOWED_COLUMN_LIST = "The allowedColumnList cannot be empty or null";
	private static final String EMPTY_OR_NULL_ALLOWED_OUTPUT_FORMAT_LIST = "The allowedOuputFormatList cannot be empty or null";
	private static final String EMPTY_OR_NULL_ALLOWED_SORT_ORDER_LIST = "The allowedSortOrderList is cannot be empty or null";
	private static final String EMPTY_ERROR_CODE = "The errorCode cannot be empty or null";
	private static final String EMPTY_ERROR_MESSAGE = "The errorMessage cannot be empty or null";
	
	// user list error messages
	private static final String ERROR_NO_USER_LIST_FOUND = "A user list was not found.";
	private static final String ERROR_TOO_MANY_USERS = "Too many users in user_list. The max allowed is " + MAX_NUMBER_OF_USERS;
	private static final String ERROR_DUPLICATE_USER = "Duplicate user found in user_list.";
	
	// prompt id list and survey id list error messages
	private static final String ERROR_BOTH_PROMPT_LIST_AND_SURVEY_LIST_EXIST = "Both prompt_id_list and survey_id_list are present,"
			+ " but only one of them is allowed";
	private static final String ERROR_NEITHER_PROMPT_LIST_NOR_SURVEY_LIST_EXIST = "Neither prompt_id_list nor survey_id_list are present";
	private static final String ERROR_TOO_MANY_PROMPTS = "Too many prompts in prompt_id_list. The max allowed is " + MAX_NUMBER_OF_PROMPTS;
	private static final String ERROR_TOO_MANY_SURVEYS = "Too many surveys in survey_id_list. The max allowed is " + MAX_NUMBER_OF_SURVEYS;
	private static final String ERROR_DUPLICATE_PROMPT_ID = "A duplicate prompt id was found in prompt_id_list";
	private static final String ERROR_DUPLICATE_SURVEY_ID = "A duplicate survey id was found in survey_id_list";
	private static final String ERROR_MALFORMED_USER_LIST = "The user_list is malformed: ";
	private static final String ERROR_MALFORMED_PROMPT_ID_LIST = "The prompt_id_list is malformed";
	private static final String ERROR_MALFORMED_SURVEY_ID_LIST = "The survey_id_list is malformed";
	private static final String ERROR_INVALID_PROMPT_ID = "An invalid prompt id was detected: ";
	private static final String ERROR_INVALID_SURVEY_ID = "An invalid survey id was detected: ";
	// regexp to validate prompt ids and survey ids
	private static Pattern promptIdSurveyIdPattern = Pattern.compile("[0-9a-zA-Z]{1,}");
	
	// column list messages
	private static final String ERROR_EMPTY_COLUMN_LIST = "The column_list is empty.";
	private static final String ERROR_MALFORMED_COLUMN_LIST = "The column_list is malformed.";
//	private static final String ERROR_TOO_MANY_COLUMNS_IN_COLUMN_LIST = "The column_list cannot be larger than "
//		+ MAX_NUMBER_OF_COLUMNS + "items";
	private static final String ERROR_DUPLICATE_IN_COLUMN_LIST = "The column_list contains duplicates.";
	private static final String ERROR_INVALID_COLUMN_IN_COLUMN_LIST = "The column_list contains an invalid column id.";
	
	// output format error messages
	private static final String ERROR_MISSING_OUTPUT_FORMAT = "The output format is missing";
	private static final String ERROR_INVALID_OUTPUT_FORMAT = "The output format is invalid: ";
	
	// sort order error message
	private static final String ERROR_INVALID_SORT_ORDER = "The sort order is invalid: ";
	
	// suppress metadata error message
	private static final String ERROR_INVALID_SUPPRESS_METADATA = "The suppress metadata value is invalid: ";
	
	// return id error message
	private static final String ERROR_INVALID_RETURN_ID = "The return id value is invalid: ";
	
	// pretty print error message
	private static final String ERROR_INVALID_PRETTY_PRINT = "The pretty print value is invalid: ";
	
	// collapse error message
	private static final String ERROR_INVALID_COLLAPSE = "The collapse value is invalid: ";
	
	/**
	 * Private to prevent instantiation.
	 */
	private SurveyResponseReadValidators() { }
	
	/**
	 * Validates that the userList contains at least one username, not more
	 * than ten usernames, or the special value
	 * {@value org.ohmage.request.InputKeys#USER_LIST}.
	 * 
	 * @param request  The request to fail should the userList be invalid.
	 * @param userList The userList to validate.
	 * @throws ValidationException If userList is empty, null or all
	 * whitespace; if there are more than ten users; or if any of the 
	 * users in the list are syntactically invalid.
	 * @throws IllegalArgumentException if the request is null. 
	 * @see org.ohmage.validator.UserValidators.validateUsername.
	 */
	public static List<String> validateUserList(Request request, String userList) throws ValidationException {
		
		// check for logical errors
		if(request == null) {
			throw new IllegalArgumentException(NULL_REQUEST);
		}
		
		// perform validation
		if(StringUtils.isEmptyOrWhitespaceOnly(userList)) {
			request.setFailed(ErrorCodes.SURVEY_NO_USERS, ERROR_NO_USER_LIST_FOUND);
			throw new ValidationException(ERROR_NO_USER_LIST_FOUND);
		}
		
		if(! SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(userList)) {
			
			if(StringUtils.isMalformedDelimitedList(userList, InputKeys.LIST_ITEM_SEPARATOR)) {
				request.setFailed(ErrorCodes.SURVEY_MALFORMED_USER_LIST, ERROR_MALFORMED_USER_LIST);
				throw new ValidationException(ERROR_MALFORMED_USER_LIST);
			}
			
			List<String> splitUserList = StringUtils.splitString(userList, InputKeys.LIST_ITEM_SEPARATOR);	
			
			if(splitUserList.size() > MAX_NUMBER_OF_USERS) {
				request.setFailed(ErrorCodes.SURVEY_TOO_MANY_USERS, ERROR_TOO_MANY_USERS);
				throw new ValidationException(ERROR_TOO_MANY_USERS);
			} 
			
			if(splitUserList.size() != (new HashSet<String>(splitUserList).size())) {
				request.setFailed(ErrorCodes.SURVEY_MALFORMED_USER_LIST, ERROR_DUPLICATE_USER);
				throw new ValidationException(ERROR_DUPLICATE_USER);
			}
			
			int numberOfUsers = splitUserList.size();
			for(int i = 0; i < numberOfUsers; i++) {
				UserValidators.validateUsername(request, splitUserList.get(i));
			}
			
			return splitUserList;
			
		} 
		else {
			
			return SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST;
		}
	}
	
	/**
	 * Validates that either the prompt id list or the survey id list exists, but not both.
	 *  
	 * @param request The request to fail should any validation fail. 
	 * @param promptIdList The prompt id list to check
	 * @param surveyIdList The survey id list to check
	 * @throws ValidationException If both promptIdList and surveyIdList are 
	 * present; if both promptIdList and surveyIdList are missing; if there are
	 * too many prompts; if there are too many surveys; if there is a duplicate
	 * prompt id; or if there is a duplicate survey id.
	 * @throws IllegalArgumentException if the request is null.
	 */
	public static List<String> validatePromptIdSurveyIdLists(Request request, String promptIdList, String surveyIdList) throws ValidationException {
		// check for logical errors
		if(request == null) {
			throw new IllegalArgumentException(NULL_REQUEST);
		}
		
		// perform validation
		if(! StringUtils.isEmptyOrWhitespaceOnly(promptIdList) && ! StringUtils.isEmptyOrWhitespaceOnly(surveyIdList)) {
			request.setFailed(ErrorCodes.SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY, ERROR_BOTH_PROMPT_LIST_AND_SURVEY_LIST_EXIST);
			throw new ValidationException(ERROR_BOTH_PROMPT_LIST_AND_SURVEY_LIST_EXIST);
		}

		if(StringUtils.isEmptyOrWhitespaceOnly(promptIdList) && StringUtils.isEmptyOrWhitespaceOnly(surveyIdList)) {
			request.setFailed(ErrorCodes.SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY, ERROR_NEITHER_PROMPT_LIST_NOR_SURVEY_LIST_EXIST);
			throw new ValidationException(ERROR_NEITHER_PROMPT_LIST_NOR_SURVEY_LIST_EXIST);
		}
		
		// If there is no promptIdList, the user must be requesting results
		// against a surveyIdList
		if(StringUtils.isEmptyOrWhitespaceOnly(promptIdList)) {
			
			if(! SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(surveyIdList)) { 
			
				if(StringUtils.isMalformedDelimitedList(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR)) {
					request.setFailed(ErrorCodes.SURVEY_MALFORMED_SURVEY_ID_LIST, ERROR_MALFORMED_SURVEY_ID_LIST);
					throw new ValidationException(ERROR_MALFORMED_SURVEY_ID_LIST);
				}
				
				List<String> splitSurveyIdList = StringUtils.splitString(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR);	
				
				if(splitSurveyIdList.size() > MAX_NUMBER_OF_SURVEYS) {
					request.setFailed(ErrorCodes.SURVEY_TOO_MANY_SURVEY_IDS, ERROR_TOO_MANY_SURVEYS);
					throw new ValidationException(ERROR_TOO_MANY_SURVEYS);
				}
				
				if(splitSurveyIdList.size() != (new HashSet<String>(splitSurveyIdList).size())) {
					request.setFailed(ErrorCodes.SURVEY_MALFORMED_SURVEY_ID_LIST, ERROR_DUPLICATE_SURVEY_ID);
					throw new ValidationException(ERROR_DUPLICATE_SURVEY_ID);
				}
				
				int numberOfSurveyIds = splitSurveyIdList.size();

				for(int i = 0; i < numberOfSurveyIds; i++) {
					if(! promptIdSurveyIdPattern.matcher(splitSurveyIdList.get(i)).matches()) {
						request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, ERROR_INVALID_SURVEY_ID + splitSurveyIdList.get(i));
						throw new ValidationException(ERROR_INVALID_SURVEY_ID + splitSurveyIdList.get(i));
					}
				}
				
				return splitSurveyIdList;
			}
		
			else {
				
				return SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST;
				
			}
		}
		
		else { 
			
			// Validate the prompt id list
			
			if(! SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(promptIdList)) { 
				
				if(StringUtils.isMalformedDelimitedList(promptIdList, InputKeys.LIST_ITEM_SEPARATOR)) {
					request.setFailed(ErrorCodes.SURVEY_MALFORMED_PROMPT_ID_LIST, ERROR_MALFORMED_PROMPT_ID_LIST + promptIdList);
					throw new ValidationException(ERROR_MALFORMED_PROMPT_ID_LIST + promptIdList);
				}
				
				List<String> splitPromptIdList = StringUtils.splitString(promptIdList, InputKeys.LIST_ITEM_SEPARATOR);	
				
				if(splitPromptIdList.size() > MAX_NUMBER_OF_PROMPTS) {
					request.setFailed(ErrorCodes.SURVEY_TOO_MANY_PROMPT_IDS, ERROR_TOO_MANY_PROMPTS);
					throw new ValidationException(ERROR_TOO_MANY_PROMPTS);
				} 
				
				if(splitPromptIdList.size() != (new HashSet<String>(splitPromptIdList).size())) {
					request.setFailed(ErrorCodes.SURVEY_MALFORMED_PROMPT_ID_LIST, ERROR_DUPLICATE_PROMPT_ID);
					throw new ValidationException(ERROR_DUPLICATE_PROMPT_ID);
				}
				
				int numberOfPromptIds = splitPromptIdList.size();

				for(int i = 0; i < numberOfPromptIds; i++) {
					if(! promptIdSurveyIdPattern.matcher(splitPromptIdList.get(i)).matches()) {
						request.setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, ERROR_INVALID_PROMPT_ID + splitPromptIdList.get(i));
						throw new ValidationException(ERROR_INVALID_PROMPT_ID + splitPromptIdList.get(i));
					}
				}
				
				return splitPromptIdList;
				
			}
			else {
				
				return SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST;
				
			}
		}
	}
	
	/**
	 * Validates that the items in the provided columnList are present in the
	 * allowedColumnList.
	 * 
	 * @param request  The request to fail should the columnList be invalid.
	 * @param columnList  The columnList to validate.
	 * @param allowedColumns  The allowed values for the columnList
	 * @return The columnList converted to a List, if the columnList is valid.
	 * @throws ValidationException  if the columnList contains an invalid entry
	 * i.e., an entry not present in the allowedColumnList; if the columnList
	 * contains a duplicate entry; if the columnList is malformed; or if the 
	 * columnList is empty.
	 * @throws IllegalArgumentException if the request is null.
	 * @throws IllegalArgumentException if the allowedColumnList is null or empty.
	 */
	public static List<String> validateColumnList(Request request, String columnList, List<String> allowedColumnList) 
		throws ValidationException {

		// check for logical errors
		if(request == null) {
			throw new IllegalArgumentException(NULL_REQUEST);
		}
		
		if(allowedColumnList == null || allowedColumnList.isEmpty()) {
			throw new IllegalArgumentException(EMPTY_OR_NULL_ALLOWED_COLUMN_LIST);
		}
		
		// perform validation
		if(StringUtils.isEmptyOrWhitespaceOnly(columnList)) {
			request.setFailed(ErrorCodes.SURVEY_MALFORMED_COLUMN_LIST, ERROR_EMPTY_COLUMN_LIST);
			throw new ValidationException(ERROR_EMPTY_COLUMN_LIST);
		}
		
		if(StringUtils.isMalformedDelimitedList(columnList, InputKeys.LIST_ITEM_SEPARATOR)) {
			request.setFailed(ErrorCodes.SURVEY_MALFORMED_COLUMN_LIST, ERROR_MALFORMED_COLUMN_LIST);
			throw new ValidationException(ERROR_MALFORMED_COLUMN_LIST);
		}
		
		if(SurveyResponseReadRequest.URN_SPECIAL_ALL.equals(columnList)) {
			return SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST;
		}
		
		List<String> splitColumnList = StringUtils.splitString(columnList, InputKeys.LIST_ITEM_SEPARATOR);	
		
//		if(splitColumnList.size() > MAX_NUMBER_OF_COLUMNS) {
//			request.setFailed(ErrorCodes.SURVEY_MALFORMED_COLUMN_LIST, ERROR_TOO_MANY_COLUMNS_IN_COLUMN_LIST);
//			throw new ValidationException(ERROR_TOO_MANY_COLUMNS_IN_COLUMN_LIST);
//		} 
		
		if(splitColumnList.size() != (new HashSet<String>(splitColumnList)).size()) {
			request.setFailed(ErrorCodes.SURVEY_MALFORMED_COLUMN_LIST, ERROR_DUPLICATE_IN_COLUMN_LIST);
			throw new ValidationException(ERROR_DUPLICATE_IN_COLUMN_LIST);
		}
		
		int numberOfColumns = splitColumnList.size();
		
		for(int i = 0; i < numberOfColumns; i++) {
			if(! allowedColumnList.contains(splitColumnList.get(i))) {
				request.setFailed(ErrorCodes.SURVEY_MALFORMED_COLUMN_LIST, ERROR_INVALID_COLUMN_IN_COLUMN_LIST);
				throw new ValidationException(ERROR_INVALID_COLUMN_IN_COLUMN_LIST);
			}
		}
		
		return splitColumnList;
	}
	
	/**
	 * Validates the provided outputFormat: it must exist in the 
	 * allowedOutputFormatList.
	 * 
	 * @param request  The request to fail should the outputFormat be invalid.
	 * @param outputFormat  The output format to validate.
	 * @param allowedOutputFormatList The allowed output formats.
	 * @return  The output format.
	 * @throws ValidationException if the output format is null or empty or if
	 * the output format is not one of the allowed output formats.
	 * @throws IllegalArgumentException if the request is null.
	 * @throws IllegalArgumentException if the allowedOutputFormatList is null or empty.
	 */
	public static String validateOutputFormat(Request request, String outputFormat, List<String> allowedOutputFormatList) throws ValidationException {
		
		// check for logical errors
		if(request == null) {
			throw new IllegalArgumentException(NULL_REQUEST);
		}
		
		if(allowedOutputFormatList == null || allowedOutputFormatList.isEmpty()) {
			throw new IllegalArgumentException(EMPTY_OR_NULL_ALLOWED_OUTPUT_FORMAT_LIST);
		}
		
		// perform validation
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_OUTPUT_FORMAT, ERROR_MISSING_OUTPUT_FORMAT);
			throw new ValidationException(ERROR_MISSING_OUTPUT_FORMAT);
		}
		
		if(! allowedOutputFormatList.contains(outputFormat)) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_OUTPUT_FORMAT, ERROR_INVALID_OUTPUT_FORMAT + outputFormat);
			throw new ValidationException(ERROR_INVALID_OUTPUT_FORMAT + outputFormat);
		}
		
		return outputFormat;
	}
	
	/**
	 * Validates the provided sortOrder. Sort order is optional, but if it
	 * exists it must be a comma-separated list composed of all the items in
	 * the provided allowedSortOrderList in any order.
	 * 
	 * @param request  The request to fail should the sort order be invalid.
	 * @param sortOrder The sort order to validate.
	 * @param allowedSortOrderList  A list of valid sort orders.
	 * @return
	 * @throws ValidationException
	 */
	public static String validateSortOrder(Request request, String sortOrder, List<String> allowedSortOrderList) 
		throws ValidationException {
		
		// check for logical errors
		if(request == null) {
			throw new IllegalArgumentException(NULL_REQUEST);
		}
		
		if(allowedSortOrderList == null || allowedSortOrderList.isEmpty()) {
			throw new IllegalArgumentException(EMPTY_OR_NULL_ALLOWED_SORT_ORDER_LIST);
		}
		
		// sortOrder is optional
		if(StringUtils.isEmptyOrWhitespaceOnly(sortOrder)) {
			return null;
		}
		
		// perform validation
		if(StringUtils.isMalformedDelimitedList(sortOrder, InputKeys.LIST_ITEM_SEPARATOR)) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, ERROR_INVALID_SORT_ORDER + sortOrder);
			throw new ValidationException(ERROR_INVALID_SORT_ORDER + sortOrder);
		}
		
		List<String> sortOrderList = StringUtils.splitString(sortOrder, InputKeys.LIST_ITEM_SEPARATOR);
		
		if(sortOrderList.size() != ALLOWED_NUMBER_OF_SORT_ORDER_ITEMS) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, ERROR_INVALID_SORT_ORDER + sortOrder);
			throw new ValidationException(ERROR_INVALID_SORT_ORDER + sortOrder);
		}
		
		if(sortOrderList.size() != (new HashSet<String>(sortOrderList)).size()) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, ERROR_INVALID_SORT_ORDER + sortOrder);
			throw new ValidationException(ERROR_INVALID_SORT_ORDER + sortOrder);
		}

		for(String sortOrderItem : sortOrderList) {
			if(! allowedSortOrderList.contains(sortOrderItem)) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_SORT_ORDER, ERROR_INVALID_SORT_ORDER + sortOrder);
				throw new ValidationException(ERROR_INVALID_SORT_ORDER + sortOrder);
			}
		}
		
		return sortOrder;
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
		
		return validateOptionalBoolean(request, suppressMetadata, ErrorCodes.SURVEY_INVALID_SUPPRESS_METADATA_VALUE, ERROR_INVALID_SUPPRESS_METADATA);
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
		
		return validateOptionalBoolean(request, returnId, ErrorCodes.SURVEY_INVALID_RETURN_ID, ERROR_INVALID_RETURN_ID);
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
		
		return validateOptionalBoolean(request, prettyPrint, ErrorCodes.SURVEY_INVALID_PRETTY_PRINT_VALUE, ERROR_INVALID_PRETTY_PRINT);
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
		
		return validateOptionalBoolean(request, collapse, ErrorCodes.SURVEY_INVALID_COLLAPSE_VALUE, ERROR_INVALID_COLLAPSE);
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
			throw new IllegalArgumentException(NULL_REQUEST);
		}

		if(StringUtils.isEmptyOrWhitespaceOnly(errorCode)) {
			throw new IllegalArgumentException(EMPTY_ERROR_CODE);
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(errorMessage)) {
			throw new IllegalArgumentException(EMPTY_ERROR_MESSAGE);
		}
		
		// don't validate the optional value if it doesn't exist
		if(StringUtils.isEmptyOrWhitespaceOnly(booleanString)) {
			return Boolean.FALSE;
		}

		// perform validation
		if(StringUtils.decodeBoolean(booleanString) == null) {
			request.setFailed(errorCode, errorMessage + booleanString);
			throw new ValidationException(errorMessage + booleanString);
		}
		
		return Boolean.valueOf(booleanString);
	}
	
}
 