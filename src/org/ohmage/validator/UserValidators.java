package org.ohmage.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class for validating user information.
 * 
 * @author John Jenkins
 */
public final class UserValidators {
	private static final Logger LOGGER = Logger.getLogger(UserValidators.class);
	
	private static final String USERNAME_PATTERN_STRING = "[a-z\\.]{9,15}";
	private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_PATTERN_STRING);
	
	private static final String PLAINTEXT_PASSWORD_PATTERN_STRING = "[a-z\\.]{9,15}";
	private static final Pattern PLAINTEXT_PASSWORD_PATTERN = Pattern.compile(PLAINTEXT_PASSWORD_PATTERN_STRING);
	
	private static final String HASHED_PASSWORD_PATTERN_STRING = "[\\w\\.\\$\\/]{50,60}";
	private static final Pattern HASHED_PASSWORD_PATTERN = Pattern.compile(HASHED_PASSWORD_PATTERN_STRING);

	private static final int MAX_FIRST_NAME_LENGTH = 255;
	private static final int MAX_LAST_NAME_LENGTH = 255;
	private static final int MAX_ORGANIZATION_LENGTH = 255;
	private static final int MAX_PERSONAL_ID_LENGTH = 255;
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserValidators() {}
	
	/**
	 * Validates that a given username follows our conventions. If it is null 
	 * or whitespace only, null is returned. If it doesn't follow our 
	 * conventions, a ValidationException is thrown. Otherwise, the username is
	 * passed back to the caller.
	 * 
	 * @param request The request that is having this username validated.
	 * 
	 * @param username The username to validate.
	 * 
	 * @return Returns null if the username is null or whitespace only. 
	 * 		   Otherwise, it returns the username.
	 * 
	 * @throws ValidationException Thrown if the username isn't null or 
	 * 							   whitespace only and doesn't follow our 
	 * 							   conventions.
	 */
	public static String validateUsername(Request request, String username) throws ValidationException {
		LOGGER.info("Validating that the username follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			return null;
		}
		
		if(USERNAME_PATTERN.matcher(username).matches()) {
			return username;
		}
		else {
			// TODO: This might be where we tell them what a username must look
			// 		 like.
			request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username is invalid: " + username);
			throw new ValidationException("The username is invalid: " + username);
		}
	}
	
	/**
	 * Validates that a String representation of a list of usernames is well
	 * formed and that each of the usernames follows our conventions. It then
	 * returns the list of usernames as a List.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param usernameList A String representation of a list of usernames where
	 * 					   the usernames should be separated by
	 * 					   {@Value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.
	 * 
	 * @return Returns a, possibly empty, List of usernames without duplicates.
	 * 
	 * @throws ValidationException Thrown if the list is malformed or if any of
	 * 							   the items in the list is malformed.
	 */
	public static List<String> validateUsernames(Request request, String usernameList) throws ValidationException {
		LOGGER.info("Validating that a list of usernames follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(usernameList)) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		
		String[] usernameArray = usernameList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < usernameArray.length; i++) {
			String username = validateUsername(request, usernameArray[i]);
			
			if(username != null) {
				result.add(username);
			}
		}
		
		if(result.size() == 0) {
			return null;
		}
		
		return new ArrayList<String>(result);
	}
	
	/**
	 * Validates that a given plaintext password follows our conventions. If it
	 * is null or whitespace only, null is returned. If it doesn't follow our
	 * conventions, a ValidationException is thrown. Otherwise, the password is
	 * passed back to the caller.
	 * 
	 * @param request The request that is having this password validated.
	 * 
	 * @param password The plaintext password to validate.
	 * 
	 * @return Returns null if the password is null or whitespace only. 
	 * 		   Otherwise, it returns the password.
	 * 
	 * @throws ValidationException Thrown if the password isn't null or 
	 * 							   whitespace only and doesn't follow our 
	 * 							   conventions.
	 */
	public static String validatePlaintextPassword(Request request, String password) throws ValidationException {
		LOGGER.info("Validating that the plaintext password follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			return null;
		}
		
		if(PLAINTEXT_PASSWORD_PATTERN.matcher(password).matches()) {
			return password;
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_PASSWORD, "The password is invalid.");
			throw new ValidationException("The plaintext password is invalid.");
		}
	}
	
	/**
	 * Validates that a given hashed password follows our conventions. If it is
	 * null or whitespace only, null is returned. If it doesn't follow our
	 * conventions, a ValidationException is thrown. Otherwise, the password is
	 * passed back to the caller.
	 * 
	 * @param request The request that is having this password validated.
	 * 
	 * @param password The hashed password to validate.
	 * 
	 * @return Returns null if the password is null or whitespace only. 
	 * 		   Otherwise, it returns the password.
	 * 
	 * @throws ValidationException Thrown if the password isn't null or 
	 * 							   whitespace only and doesn't follow our 
	 * 							   conventions.
	 */
	public static String validateHashedPassword(Request request, String password) throws ValidationException {
		LOGGER.info("Validating that the hashed password follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			return null;
		}
		
		if(HASHED_PASSWORD_PATTERN.matcher(password).matches()) {
			return password;
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_PASSWORD, "The password is invalid.");
			throw new ValidationException("The hashed password is invalid.");
		}
	}
	
	/**
	 * Validates that a Value is a valid admin Value. If the Value is null or 
	 * whitespace only, null is returned. If the Value is a valid admin Value,
	 * it is returned. If the Value is not null, not whitespace only, and not a
	 * valid admin Value, a ValidationException is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String representation of the admin Value to be  
	 * 				validated.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   the Value is returned.
	 * 
	 * @throws ValidationException Thrown if the Value is not null, not 
	 * 							   whitespace only, and not a valid admin 
	 * 							   Value.
	 */
	public static Boolean validateAdminValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating an admin Value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value)) {
			return StringUtils.decodeBoolean(value);
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_ADMIN_VALUE, "The admin Value is invalid: " + value);
			throw new ValidationException("The admin Value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a Value is a valid enabled Value. If the Value is null or
	 * whitespace only, null is returned. If the Value is a valid enabled 
	 * Value, it is returned. If the Value is not null, not whitespace only, 
	 * and not a valid enabled Value, a ValidationException is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String representation of the enabled Value to be  
	 * 				validated.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   the Value is returned.
	 * 
	 * @throws ValidationException Thrown if the Value is not null, not 
	 * 							   whitespace only, and not a valid enabled 
	 * 							   Value.
	 */
	public static Boolean validateEnabledValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a Value is a valid enabled Value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value)) {
			return StringUtils.decodeBoolean(value);
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_ENABLED_VALUE, "The enabled Value is invalid: " + value);
			throw new ValidationException("The enabled Value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a Value is a valid new account Value. If the Value is 
	 * null or whitespace only, null is returned. If the Value is a valid new 
	 * account Value, it is returned. If the Value is not null, not whitespace
	 * only, and not a valid new account Value, a ValidationException is 
	 * thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String representation of the new account Value to be  
	 * 				validated.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   the Value is returned.
	 * 
	 * @throws ValidationException Thrown if the Value is not null, not 
	 * 							   whitespace only, and not a valid new account 
	 * 							   Value.
	 */
	public static Boolean validateNewAccountValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that the Value is a valid new account Value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value)) {
			return StringUtils.decodeBoolean(value);
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_NEW_ACCOUNT_VALUE, "The new account Value is invalid: " + value);
			throw new ValidationException("The new account Value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a Value is a valid campaign creation privilege Value. If
	 * the Value is null or whitespace only, null is returned. If the Value is
	 * a valid campaign creation privilege Value, it is returned. If the Value
	 * is not null, not whitespace only, and not a valid campaign creation 
	 * privilege Value, a ValidationException is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String representation of the campaign creation 
	 * 				privilege Value to be validated.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   the Value is returned.
	 * 
	 * @throws ValidationException Thrown if the Value is not null, not 
	 * 							   whitespace only, and not a valid campaign 
	 * 							   creation privilege Value.
	 */
	public static Boolean validateCampaignCreationPrivilegeValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that the Value is a valid campaign creation privilege Value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value)) {
			return StringUtils.decodeBoolean(value);
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, "The campaign creation privilege Value is invalid: " + value);
			throw new ValidationException("The campaign creation privilege Value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that the first name Value for a user is a valid first name
	 * Value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String Value of the user's first name.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   it returns the first name Value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@Value #MAX_FIRST_NAME_LENGTH}.
	 */
	public static String validateFirstName(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a first name Value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value)) {
			request.setFailed(ErrorCodes.USER_INVALID_FIRST_NAME_VALUE, "The first name Value for the user contains profanity: " + value);
			throw new ValidationException("The first name Value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value, 0, MAX_FIRST_NAME_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_FIRST_NAME_VALUE, "The first name Value for the user is too long. The limit is " + MAX_FIRST_NAME_LENGTH + " characters.");
			throw new ValidationException("The first name Value for the user is too long. The limit is " + MAX_FIRST_NAME_LENGTH + " characters.");
		}
		else {
			return value;
		}
	}
	
	/**
	 * Validates that the last name Value for a user is a valid last name
	 * Value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String Value of the user's last name.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   it returns the last name Value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@Value #MAX_LAST_NAME_LENGTH}.
	 */
	public static String validateLastName(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a last name Value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value)) {
			request.setFailed(ErrorCodes.USER_INVALID_LAST_NAME_VALUE, "The last name Value for the user contains profanity: " + value);
			throw new ValidationException("The last name Value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value, 0, MAX_LAST_NAME_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_LAST_NAME_VALUE, "The last name Value for the user is too long. The limit is " + MAX_LAST_NAME_LENGTH + " characters.");
			throw new ValidationException("The last name Value for the user is too long. The limit is " + MAX_LAST_NAME_LENGTH + " characters.");
		}
		else {
			return value;
		}
	}
	
	/**
	 * Validates that the organization Value for a user is a valid organization
	 * Value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String Value of the user's organization.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   it returns the organization Value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@Value #MAX_ORGANIZATION_LENGTH}.
	 */
	public static String validateOrganization(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that an organization name Value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value)) {
			request.setFailed(ErrorCodes.USER_INVALID_ORGANIZATION_VALUE, "The organization Value for the user contains profanity: " + value);
			throw new ValidationException("The organization Value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value, 0, MAX_ORGANIZATION_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_ORGANIZATION_VALUE, "The organization Value for the user is too long. The limit is " + MAX_ORGANIZATION_LENGTH + " characters.");
			throw new ValidationException("The organization Value for the user is too long. The limit is " + MAX_ORGANIZATION_LENGTH + " characters.");
		}
		else {
			return value;
		}
	}
	
	/**
	 * Validates that the personal ID Value for a user is a valid personal ID
	 * Value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String Value of the user's personal ID.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   it returns the personal ID Value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@Value #MAX_PERSONAL_ID_LENGTH}.
	 */
	public static String validatePersonalId(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a personal ID Value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value)) {
			request.setFailed(ErrorCodes.USER_INVALID_PERSONAL_ID_VALUE, "The personal ID Value for the user contains profanity: " + value);
			throw new ValidationException("The personal ID Value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value, 0, MAX_PERSONAL_ID_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_PERSONAL_ID_VALUE, "The personal ID Value for the user is too long. The limit is " + MAX_PERSONAL_ID_LENGTH + " characters.");
			throw new ValidationException("The personal ID Value for the user is too long. The limit is " + MAX_PERSONAL_ID_LENGTH + " characters.");
		}
		else {
			return value;
		}
	}
	
	/**
	 * Validates that the email address for a user is a valid email address.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String Value of the user's email address.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   it returns the email address.
	 * 
	 * @throws ValidationException Thrown if the email address is not a valid
	 * 							   email address.
	 */
	public static String validateEmailAddress(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a first name Value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidEmailAddress(value)) {
			return value;
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_EMAIL_ADDRESS, "The email address Value for the user is invalid: " + value);
			throw new ValidationException("The email address Value for the user is invalid: " + value);
		}
	}
	
	/**
	 * Validates that some String is a valid JSONObject, creates a JSONObject
	 * from the String, and returns it.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The String representation of the JSONObject.
	 * 
	 * @return Returns null if the Value is null or whitespace only; otherwise,
	 * 		   it returns a new JSONObject built from the String.
	 * 
	 * @throws ValidationException Thrown if the Value is not null, not 
	 * 							   whitespace only, and not a valid JSONObject.
	 */
	public static JSONObject validateJsonData(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a first name Value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return new JSONObject(value);
		}
		catch(JSONException e) {
			request.setFailed(ErrorCodes.USER_INVALID_JSON_DATA, "The user's JSON data object is not a valid JSONObject: " + value);
			throw new ValidationException("The user's JSON data object is not a valid JSONObject: " + value, e);
		}
	}
}