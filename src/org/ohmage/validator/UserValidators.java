package org.ohmage.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
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
			request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The username is invalid.");
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
			String username = validateUsername(request, usernameArray[i]);
			
			if(username != null) {
				result.add(username);
			}
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
		
		if(BooleanValidators.validateBoolean(value)) {
			return StringUtils.decodeBoolean(value);
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
		
		if(BooleanValidators.validateBoolean(value)) {
			return StringUtils.decodeBoolean(value);
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
		
		if(BooleanValidators.validateBoolean(value)) {
			return StringUtils.decodeBoolean(value);
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
		
		if(BooleanValidators.validateBoolean(value)) {
			return StringUtils.decodeBoolean(value);
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, "The campaign creation privilege value is invalid: " + value);
			throw new ValidationException("The campaign creation privilege value is invalid: " + value);
		}
	}
}