/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.User;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
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
		"[" + // Beginning of the definition. The username must consist of only
			  // these characters
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

	// The password's only constraint is that it must be at least 8 characters.
	private static final int MIN_PASSWORD_LENGTH = 8;
	// A description of the password for the user.
	private static final String PASSWORD_REQUIREMENTS = 
		"The password must be at least 8 characters.";
	
	private static final String HASHED_PASSWORD_PATTERN_STRING = "[\\w\\.\\$\\/]{60}";
	private static final Pattern HASHED_PASSWORD_PATTERN = Pattern.compile(HASHED_PASSWORD_PATTERN_STRING);

	/**
	 * The maximum length of a first name value.
	 */
	public static final int MAX_FIRST_NAME_LENGTH = 255;
	/**
	 * The maximum length of a last name value.
	 */
	public static final int MAX_LAST_NAME_LENGTH = 255;
	/**
	 * The maximum length of the organization value.
	 */
	public static final int MAX_ORGANIZATION_LENGTH = 255;
	/**
	 * The maximum length of the personal ID value.
	 */
	public static final int MAX_PERSONAL_ID_LENGTH = 255;
	
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
	 * @param username The username to validate.
	 * 
	 * @return Returns null if the username is null or whitespace only. 
	 * 		   Otherwise, it returns the username.
	 * 
	 * @throws ValidationException Thrown if the username isn't null or 
	 * 							   whitespace only and doesn't follow our 
	 * 							   conventions.
	 */
	public static String validateUsername(final String username) 
			throws ValidationException {
		
		LOGGER.info("Validating that the username follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			return null;
		}
		
		if(USERNAME_PATTERN.matcher(username).matches()) {
			return username;
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_USERNAME, 
					"The username is invalid. " + USERNAME_REQUIREMENTS);
		}
	}
	
	/**
	 * Validates that a String representation of a list of usernames is well
	 * formed and that each of the usernames follows our conventions. It then
	 * returns the list of usernames as a List.
	 * 
	 * @param usernames A String representation of a list of usernames where 
	 * 					the usernames should be separated by
	 * 					{@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.
	 * 
	 * @return Returns a, possibly empty, List of usernames without duplicates.
	 * 
	 * @throws ValidationException Thrown if the list is malformed or if any of
	 * 							   the items in the list is malformed.
	 */
	public static Set<String> validateUsernames(
			final String usernames) 
			throws ValidationException {
		
		LOGGER.info("Validating that a list of usernames follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(usernames)) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();
		
		String[] usernameArray = usernames.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < usernameArray.length; i++) {
			String username = validateUsername(usernameArray[i].trim());
			
			if(username != null) {
				result.add(username);
			}
		}
		
		if(result.size() == 0) {
			return null;
		}
		
		return result;
	}
	
	/**
	 * There is no real validation that takes place here because the user could
	 * search for any piece of information. If a user is searching for an 
	 * illegal character for usernames, we simply remove that string from the
	 * list.
	 * 
	 * @param usernames The space-separated list of search terms.
	 * 
	 * @return A sanitized version of the search terms, which may be empty. If
	 * 		   the given string is null or only whitespace, this will be null.
	 */
	public static Set<String> validateUsernameSearch(
			final String usernames) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(usernames)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(usernames);
	}
	
	/**
	 * Validates that a given plaintext password follows our conventions. If it
	 * is null or whitespace only, null is returned. If it doesn't follow our
	 * conventions, a ValidationException is thrown. Otherwise, the password is
	 * passed back to the caller.
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
	public static String validatePlaintextPassword(final String password) 
			throws ValidationException {
		
		LOGGER.info("Validating that the plaintext password follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			return null;
		}

		// TODO: Maybe we should just hash it here?
		if(password.length() >= MIN_PASSWORD_LENGTH) {
			return password;
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_PASSWORD, 
					"The password is invalid. " + PASSWORD_REQUIREMENTS);
		}
	}
	
	/**
	 * Validates that a given hashed password follows our conventions. If it is
	 * null or whitespace only, null is returned. If it doesn't follow our
	 * conventions, a ValidationException is thrown. Otherwise, the password is
	 * passed back to the caller.
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
	public static String validateHashedPassword(final String password) 
			throws ValidationException {
		
		LOGGER.info("Validating that the hashed password follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			return null;
		}
		
		if(HASHED_PASSWORD_PATTERN.matcher(password).matches()) {
			return password;
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_PASSWORD, 
					"The hashed password is invalid.");
		}
	}
	
	/**
	 * Validates that the email address for a user is a valid email address.
	 * 
	 * @param value The String value of the user's email address.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the email address.
	 * 
	 * @throws ValidationException Thrown if the email address is not a valid
	 * 							   email address.
	 */
	public static String validateEmailAddress(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that an email address is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidEmailAddress(value.trim())) {
			return value.trim();
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_EMAIL_ADDRESS, 
					"The email address value for the user is invalid: " + 
						value);
		}
	}
	
	/**
	 * If the value is null or only whitespace, null is returned. Otherwise, it
	 * tokenizes the search string by whitespace with the exception of quoted
	 * characters.
	 * 
	 * @param value The String value of the user's email address.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the set of search tokens.
	 */
	public static Set<String> validateEmailAddressSearch(
			final String value) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that a value is a valid admin value. If the value is null or 
	 * whitespace only, null is returned. If the value is a valid admin value,
	 * it is returned. If the value is not null, not whitespace only, and not a
	 * valid admin value, a ValidationException is thrown.
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
	public static Boolean validateAdminValue(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating an admin value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_ADMIN_VALUE, 
					"The admin value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a value is a valid enabled value. If the value is null or
	 * whitespace only, null is returned. If the value is a valid enabled 
	 * value, it is returned. If the value is not null, not whitespace only, 
	 * and not a valid enabled value, a ValidationException is thrown.
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
	public static Boolean validateEnabledValue(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a value is a valid enabled value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_ENABLED_VALUE, 
					"The enabled value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a value is a valid new account value. If the value is 
	 * null or whitespace only, null is returned. If the value is a valid new 
	 * account value, it is returned. If the value is not null, not whitespace
	 * only, and not a valid new account value, a ValidationException is 
	 * thrown.
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
	public static Boolean validateNewAccountValue(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that the value is a valid new account value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_NEW_ACCOUNT_VALUE, 
					"The new account value is invalid: " + value);
		}
	}
	
	/**
	 * Validates that a value is a valid campaign creation privilege value. If
	 * the value is null or whitespace only, null is returned. If the value is
	 * a valid campaign creation privilege value, it is returned. If the value
	 * is not null, not whitespace only, and not a valid campaign creation 
	 * privilege value, a ValidationException is thrown.
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
	public static Boolean validateCampaignCreationPrivilegeValue(
			final String value) throws ValidationException {
		
		LOGGER.info("Validating that the value is a valid campaign creation privilege value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			throw new ValidationException(
					ErrorCode.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, 
					"The campaign creation privilege value is invalid: " + 
						value);
		}
	}
	
	/**
	 * Validates that the first name value for a user is a valid first name
	 * value.
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
	public static String validateFirstName(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a first name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_FIRST_NAME_VALUE, 
					"The first name value for the user contains profanity: " + 
						value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_FIRST_NAME_LENGTH)) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_FIRST_NAME_VALUE, 
					"The first name value for the user is too long. The limit is " + 
						MAX_FIRST_NAME_LENGTH + 
						" characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * If the value is null or only whitespace, null is returned. Otherwise, it
	 * tokenizes the search string by whitespace with the exception of quoted
	 * characters whose entire quoted value is returned.
	 * 
	 * @param value The search string to be tokenized.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the set of search tokens.
	 */
	public static Set<String> validateFirstNameSearch(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that the last name value for a user is a valid last name
	 * value.
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
	public static String validateLastName(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a last name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_LAST_NAME_VALUE, 
					"The last name value for the user contains profanity: " + 
						value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_LAST_NAME_LENGTH)) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_LAST_NAME_VALUE, 
					"The last name value for the user is too long. The limit is " + 
						MAX_LAST_NAME_LENGTH + 
						" characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * If the value is null or only whitespace, null is returned. Otherwise, it
	 * tokenizes the search string by whitespace with the exception of quoted
	 * characters whose entire quoted value is returned.
	 * 
	 * @param value The search string to be tokenized.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the set of search tokens.
	 */
	public static Set<String> validateLastNameSearch(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that the organization value for a user is a valid organization
	 * value.
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
	public static String validateOrganization(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that an organization name value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_ORGANIZATION_VALUE, 
					"The organization value for the user contains profanity: " + 
						value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_ORGANIZATION_LENGTH)) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_ORGANIZATION_VALUE, 
					"The organization value for the user is too long. The limit is " + 
						MAX_ORGANIZATION_LENGTH + 
						" characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * If the value is null or only whitespace, null is returned. Otherwise, it
	 * tokenizes the search string by whitespace with the exception of quoted
	 * characters whose entire quoted value is returned.
	 * 
	 * @param value The search string to be tokenized.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the set of search tokens.
	 */
	public static Set<String> validateOrganizationSearch(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that the personal ID value for a user is a valid personal ID
	 * value.
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
	public static String validatePersonalId(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a personal ID value is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_PERSONAL_ID_VALUE, 
					"The personal ID value for the user contains profanity: " + 
						value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_PERSONAL_ID_LENGTH)) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_PERSONAL_ID_VALUE, 
					"The personal ID value for the user is too long. The limit is " + 
						MAX_PERSONAL_ID_LENGTH + 
						" characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * If the value is null or only whitespace, null is returned. Otherwise, it
	 * tokenizes the search string by whitespace with the exception of quoted
	 * characters whose entire quoted value is returned.
	 * 
	 * @param value The search string to be tokenized.
	 * 
	 * @return Returns null if the value is null or whitespace only; otherwise,
	 * 		   it returns the set of search tokens.
	 */
	public static Set<String> validatePersonalIdSearch(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that a number of users to skip is a non-negative number.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return The validated number of users to skip.
	 * 
	 * @throws ValidationException There was a problem decoding the number.
	 */
	public static int validateNumToSkip(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a number of users to skip is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return 0;
		}
		
		try {
			int numToSkip = Integer.decode(value);
			
			if(numToSkip < 0) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
						"The number of users to skip is negative.");
			}
			
			return numToSkip;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
					"The number of users to skip is not a number: " +
							value);
		}
	}
	
	/**
	 * Validates that a number of users to return is a non-negative number
	 * less than or equal to the maximum allowed number of users to return.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A number between 0 and {@link User#MAX_NUM_TO_RETURN}.
	 * 
	 * @throws ValidationException The number was not valid.
	 */
	public static int validateNumToReturn(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a number of users to return is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return User.MAX_NUM_TO_RETURN;
		}
		
		try {
			int numToSkip = Integer.decode(value);
			
			if(numToSkip < 0) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
						"The number of users to return cannot be negative: " +
								value);
			}
			else if(numToSkip > User.MAX_NUM_TO_RETURN) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
						"The number of users to return is greater than the max allowed: " +
							User.MAX_NUM_TO_RETURN);
			}
			
			return numToSkip;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
					"The number of users to return is not a number: " +
							value);
		}
	}
	
	/**
	 * Validates that a delete personal info flag is a valid boolean.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return A valid boolean describing whether or not to delete the user's
	 * 		   personal personal information. The default is false.
	 * 
	 * @throws ValidationException The value was not null nor whitespace only, 
	 * 							   and it could not be decoded as a boolean.
	 */
	public static boolean validateDeletePersonalInfo(
			final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating a delete personal info value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return false;
		}
		
		Boolean result = StringUtils.decodeBoolean(value);
		
		if(result == null) {
			throw new ValidationException(
					ErrorCode.USER_INVALID_DELETE_PERSONAL_INFO,
					"The 'delete personal info' flag was not a valid boolean: " +
						value);
		}
		
		return result;
	}
}