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
	
	private static final String TOKEN_PATTERN_STRING = "[a-fA-F0-9]{8}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{12}";
	private static final Pattern TOKEN_PATTERN = Pattern.compile(TOKEN_PATTERN_STRING);
	
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
	 * Validates that a given authentication / session token follows our 
	 * conventions. If it is null or whitespace only, null is returned. If it
	 * doesn't follow our conventions, a ValidationException is thrown. 
	 * Otherwise, the token is passed back to the caller.
	 * 
	 * @param request The request that is having this token validated.
	 * 
	 * @param token The authentication / session token to validate.
	 * 
	 * @return Returns null if the token is null or whitespace only. Otherwise,
	 * 		   it returns the token.
	 * 
	 * @throws ValidationException Thrown if the token isn't null or whitespace
	 * 							   only and doesn't follow our conventions.
	 */
	public static String validateToken(Request request, String token) throws ValidationException {
		LOGGER.info("Validating that the requester's authentication / session token follows our conventions.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			return null;
		}
		
		if(TOKEN_PATTERN.matcher(token).matches()) {
			return token;
		}
		else {
			request.setFailed(ErrorCodes.USER_INVALID_TOKEN, "The authentication / session token is invalid.");
			throw new ValidationException("The authentication / session token is invalid.");
		}
	}
}