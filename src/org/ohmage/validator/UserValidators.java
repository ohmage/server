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
import org.ohmage.exception.ValidationException;
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
	
	// This is the regular expression for the username string. The username
	// must contain at least alphanumeric character, upper or lower case. It
	// can then contain any more alphanumeric characters as well as any number
	// of characters from a set. The total length of the username must be
	// between 4 and 25 characters, inclusive.
	private static final String USERNAME_PATTERN_STRING = 
		"^" + // Beginning of the line.
		"(" + // Beginning of group 1.
			"?=.*" + // There must be at least 1 of the following,
			"[" + // Beginning of the definition.
				"a-z" + // A lower case character.
				"A-Z" + // An upper case character.
				"\\d" + // A digit.
			"]" + // End of the definition.
		")" + // End of group 1.
		"[" + // Beginning of the definition. At least one of the following 
		      // must be present.
			"a-z" + // A lower case, alphabetic character. 
			"A-Z" + // An upper case, alphabetic character. 
			"\\d" + // A digit. 
			"." +   // A period. 
			"_" +   // An underscore. 
			"@" +   // An "at" symbol.
			"+" +   // A plus sign. 
			"\\-" + // A minus sign.
		"]" + // End of the definition.
		"{4,25}" + // The total string must be at least 4 characters and no
		           // more than 25 characters.
		"$";  // End of the line.
	// A compiled version of the username pattern string for checking a user's
	// username.
	private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_PATTERN_STRING);
	// The description of the username requirements for the user.
	private static final String USERNAME_REQUIREMENTS = 
		"The username must " +
		"be between 4 and 25 characters and " +
		"must contain at least one alphanumeric character. " + 
		"It may also consist of any of these characters " +
			"'.', " +
			"'_', " +
			"'@', " +
			"'+', " +
			"'-'.";
	
	// This is the regular expression for the password string. The password 
	// must contain at least one lower case character, one upper case
	// character, one digit, and one of a set of special characters. It must be
	// between 8 and 16 characters, inclusive.
	private static final String PLAINTEXT_PASSWORD_PATTERN_STRING = 
		"^" + // Beginning of the line.
		"(" + // Beginning of group 1.
			"(" + // Beginning of subgroup 1-1.
				"?=.*" + // This group must consist of at least one of the 
				         // following characters.
				"[a-z]" + // A lower case character.
			")" + // End of subgroup 1-1.
			"(" + // Beginning of subgroup 1-2.
				"?=.*" + // This group must consist of at least one of the 
		                 // following characters.
				"[A-Z]" +// An upper case character.
			")" + // End of subgroup 1-2.
			"(" + // Beginning of subgroup 1-3.
				"?=.*" + // This group must consist of at least one of the 
		                 // following characters.
				"\\d" + // A digit.
			")" + // End of subgroup 1-3.
			"(" + // Beginning of subgroup 1-4.
				"?=.*" + // This group must consist of at least one of the 
		                 // following characters.
				"[,\\.<>:\\[\\]!@#$%^&*+-/=?_{|}]" +
			")" + // End of subgroup 1-4.
			"." + // All of the previous subgroups must be true.
			"{8,16}" + // There must be at least 8 and no more than 16
			           // characters.
		")" + // End of group 1.
		"$";  // End of the line.
	// A compiled version of the password pattern string for checking a user's
	// password.
	private static final Pattern PLAINTEXT_PASSWORD_PATTERN = Pattern.compile(PLAINTEXT_PASSWORD_PATTERN_STRING);
	// A description of the password for the user.
	private static final String PASSWORD_REQUIREMENTS = 
		"The password must " +
		"be between 8 and 16 characters, " +
		"contain at least one lower case character, " +
		"contain at least one upper case character, " +
		"contain at least one digit, " +
		"and contain at least one of the following characters " +
			"',', " +
			"'.', " +
			"'<', " +
			"'>', " +
			"'[', " +
			"']', " +
			"'!', " +
			"'@', " +
			"'#', " +
			"'$', " +
			"'%', " +
			"'^', " +
			"'&', " +
			"'*', " +
			"'+', " +
			"'-', " +
			"'/', " +
			"'=', " +
			"'?', " +
			"'_', " +
			"'{', " +
			"'}', " +
			"'|', " +
			"':'.";
	
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
			request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username is invalid. " + USERNAME_REQUIREMENTS);
			throw new ValidationException("The username is invalid. " + USERNAME_REQUIREMENTS);
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
	 * 					   {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.
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
			String username = validateUsername(request, usernameArray[i].trim());
			
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
			request.setFailed(ErrorCodes.USER_INVALID_PASSWORD, "The password is invalid. " + PASSWORD_REQUIREMENTS);
			throw new ValidationException("The password is invalid. " + PASSWORD_REQUIREMENTS);
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
	 * Validates that a value is a valid admin value. If the value is null or 
	 * whitespace only, null is returned. If the value is a valid admin value,
	 * it is returned. If the value is not null, not whitespace only, and not a
	 * valid admin value, a ValidationException is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String representation of the admin value to be  
	 * 				validated.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   the value is returned.
	 * 
	 * @throws ValidationException Thrown if the value is not null, not 
	 * 							   whitespace only, and not a valid admin 
	 * 							   value.
	 */
	public static Boolean validateAdminValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating an admin value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_ADMIN_VALUE, "The admin value is invalid: " + value);
			throw new ValidationException("The admin value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a value is a valid enabled value. If the value is null or
	 * whitespace only, null is returned. If the value is a valid enabled 
	 * value, it is returned. If the value is not null, not whitespace only, 
	 * and not a valid enabled value, a ValidationException is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String representation of the enabled value to be  
	 * 				validated.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   the value is returned.
	 * 
	 * @throws ValidationException Thrown if the value is not null, not 
	 * 							   whitespace only, and not a valid enabled 
	 * 							   value.
	 */
	public static Boolean validateEnabledValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a value is a valid enabled value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_ENABLED_VALUE, "The enabled value is invalid: " + value);
			throw new ValidationException("The enabled value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a value is a valid new account value. If the value is 
	 * null or whitespace only, null is returned. If the value is a valid new 
	 * account value, it is returned. If the value is not null, not whitespace
	 * only, and not a valid new account value, a ValidationException is 
	 * thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String representation of the new account value to be  
	 * 				validated.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   the value is returned.
	 * 
	 * @throws ValidationException Thrown if the value is not null, not 
	 * 							   whitespace only, and not a valid new account 
	 * 							   value.
	 */
	public static Boolean validateNewAccountValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that the value is a valid new account value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_NEW_ACCOUNT_VALUE, "The new account value is invalid: " + value);
			throw new ValidationException("The new account value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a value is a valid campaign creation privilege value. If
	 * the value is null or whitespace only, null is returned. If the value is
	 * a valid campaign creation privilege value, it is returned. If the value
	 * is not null, not whitespace only, and not a valid campaign creation 
	 * privilege value, a ValidationException is thrown.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String representation of the campaign creation 
	 * 				privilege value to be validated.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   the value is returned.
	 * 
	 * @throws ValidationException Thrown if the value is not null, not 
	 * 							   whitespace only, and not a valid campaign 
	 * 							   creation privilege value.
	 */
	public static Boolean validateCampaignCreationPrivilegeValue(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that the value is a valid campaign creation privilege value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, "The campaign creation privilege value is invalid: " + value);
			throw new ValidationException("The campaign creation privilege value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that the first name value for a user is a valid first name
	 * value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String value of the user's first name.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the first name value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@value #MAX_FIRST_NAME_LENGTH}.
	 */
	public static String validateFirstName(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a first name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			request.setFailed(ErrorCodes.USER_INVALID_FIRST_NAME_VALUE, "The first name value for the user contains profanity: " + value);
			throw new ValidationException("The first name value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_FIRST_NAME_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_FIRST_NAME_VALUE, "The first name value for the user is too long. The limit is " + MAX_FIRST_NAME_LENGTH + " characters.");
			throw new ValidationException("The first name value for the user is too long. The limit is " + MAX_FIRST_NAME_LENGTH + " characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * Validates that the last name value for a user is a valid last name
	 * value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String value of the user's last name.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the last name value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@value #MAX_LAST_NAME_LENGTH}.
	 */
	public static String validateLastName(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a last name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			request.setFailed(ErrorCodes.USER_INVALID_LAST_NAME_VALUE, "The last name value for the user contains profanity: " + value);
			throw new ValidationException("The last name value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_LAST_NAME_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_LAST_NAME_VALUE, "The last name value for the user is too long. The limit is " + MAX_LAST_NAME_LENGTH + " characters.");
			throw new ValidationException("The last name value for the user is too long. The limit is " + MAX_LAST_NAME_LENGTH + " characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * Validates that the organization value for a user is a valid organization
	 * value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String value of the user's organization.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the organization value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@value #MAX_ORGANIZATION_LENGTH}.
	 */
	public static String validateOrganization(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that an organization name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			request.setFailed(ErrorCodes.USER_INVALID_ORGANIZATION_VALUE, "The organization value for the user contains profanity: " + value);
			throw new ValidationException("The organization value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_ORGANIZATION_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_ORGANIZATION_VALUE, "The organization value for the user is too long. The limit is " + MAX_ORGANIZATION_LENGTH + " characters.");
			throw new ValidationException("The organization value for the user is too long. The limit is " + MAX_ORGANIZATION_LENGTH + " characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * Validates that the personal ID value for a user is a valid personal ID
	 * value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String value of the user's personal ID.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the personal ID value.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or if
	 * 							   its length is greater than 
	 * 							   {@value #MAX_PERSONAL_ID_LENGTH}.
	 */
	public static String validatePersonalId(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a personal ID value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			request.setFailed(ErrorCodes.USER_INVALID_PERSONAL_ID_VALUE, "The personal ID value for the user contains profanity: " + value);
			throw new ValidationException("The personal ID value for the user contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_PERSONAL_ID_LENGTH)) {
			request.setFailed(ErrorCodes.USER_INVALID_PERSONAL_ID_VALUE, "The personal ID value for the user is too long. The limit is " + MAX_PERSONAL_ID_LENGTH + " characters.");
			throw new ValidationException("The personal ID value for the user is too long. The limit is " + MAX_PERSONAL_ID_LENGTH + " characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * Validates that the email address for a user is a valid email address.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String value of the user's email address.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the email address.
	 * 
	 * @throws ValidationException Thrown if the email address is not a valid
	 * 							   email address.
	 */
	public static String validateEmailAddress(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a first name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidEmailAddress(value.trim())) {
			return value.trim();
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_EMAIL_ADDRESS, "The email address value for the user is invalid: " + value);
			throw new ValidationException("The email address value for the user is invalid: " + value);
		}
	}
	
	/**
	 * Validates that some String is a valid JSONObject, creates a JSONObject
	 * from the String, and returns it.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The String representation of the JSONObject.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns a new JSONObject built from the String.
	 * 
	 * @throws ValidationException Thrown if the value is not null, not 
	 * 							   whitespace only, and not a valid JSONObject.
	 */
	public static JSONObject validateJsonData(Request request, String value) throws ValidationException {
		LOGGER.info("Validating that a first name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return new JSONObject(value.trim());
		}
		catch(JSONException e) {
			request.setFailed(ErrorCodes.USER_INVALID_JSON_DATA, "The user's JSON data object is not a valid JSONObject: " + value);
			throw new ValidationException("The user's JSON data object is not a valid JSONObject: " + value, e);
		}
	}
}