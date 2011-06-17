package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A request for creating a new user.
 * 
 * @author John Jenkins
 */
public class UserCreationRequest extends ResultListAwRequest {
	/**
	 * Creates a new user creation request.
	 * 
	 * @param token The authentication / session token for this request.
	 * 
	 * @param username The new username value.
	 * 
	 * @param password The new password value.
	 * 
	 * @param admin Whether or not the new user will be an admin.
	 * 
	 * @param enabled Whether or not the new user will be enabled.
	 * 
	 * @param newAccount Whether or not this account is considered a new 
	 * 					 account or not.
	 * 
	 * @param campaignCreationPrivilege Whether or not this account is allowed
	 * 									to create new accounts.
	 * 
	 * @throws IllegalArgumentException Thrown if the token, username, 
	 * 									password, admin, or enabled values are
	 * 									obviously invalid.
	 */
	public UserCreationRequest(String token, String username, String password, String admin, String enabled, 
			String newAccount, String campaignCreationPrivilege) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The authentication / session token cannot be null or whitespace only.");
		}
		else {
			setUserToken(token);
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("The username cannot be null or whitespace only.");
		}
		else {
			addToValidate(InputKeys.NEW_USERNAME, username, true);
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			throw new IllegalArgumentException("The password cannot be null or whitespace only.");
		}
		else {
			addToValidate(InputKeys.NEW_PASSWORD, password, true);
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(admin)) {
			throw new IllegalArgumentException("The admin value cannot be null or whitespace only.");
		}
		else {
			addToValidate(InputKeys.USER_ADMIN, admin, true);
		}
		
		if(StringUtils.isEmptyOrWhitespaceOnly(enabled)) {
			throw new IllegalArgumentException("The enabled value cannot be null or whitespace only.");
		}
		else {
			addToValidate(InputKeys.USER_ENABLED, enabled, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(newAccount)) {
			addToValidate(InputKeys.NEW_ACCOUNT, newAccount, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(campaignCreationPrivilege)) {
			addToValidate(InputKeys.CAMPAIGN_CREATION_PRIVILEGE, campaignCreationPrivilege, true);
		}
	}
}
