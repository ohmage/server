package org.ohmage.request;

import org.ohmage.util.StringUtils;

/**
 * A request to update a user's information.
 * 
 * @author John Jenkins
 */
public class UserUpdateRequest extends ResultListAwRequest {
	/**
	 * Creates a user update request.
	 * 
	 * @param token The authentication / session token for the requester.
	 * 
	 * @param user The user whose data is being manipulated.
	 * 
	 * @param admin Whether or not the user should be an admin.
	 * 
	 * @param enabled Whether or not the user's account should enabled.
	 * 
	 * @param newAccount Whether or not the user's account is new.
	 * 
	 * @param campaignCreationPrivilege Whether or not the user can create
	 * 									campaigns.
	 * 
	 * @param firstName The first name of the user.
	 * 
	 * @param lastName The last name of the user.
	 * 
	 * @param organization The organization of the user.
	 * 
	 * @param personalId The personal identifier for the user.
	 * 
	 * @param emailAddress The email address of the user.
	 * 
	 * @param jsonData Extra JSON data about the user.
	 * 
	 * @throws IllegalArgumentException Thrown if 'token' or 'user' are null or
	 * 									whitespace only.
	 */
	public UserUpdateRequest(String token, String user, String admin, String enabled, String newAccount, String campaignCreationPrivilege,
			String firstName, String lastName, String organization, String personalId, String emailAddress, String jsonData) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new IllegalArgumentException("The authentication / session token cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(user)) {
			throw new IllegalArgumentException("The user cannot be null or whitespace only.");
		}
		
		setUserToken(token);
		addToValidate(InputKeys.USER, user, true);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(admin)) {
			addToValidate(InputKeys.USER_ADMIN, admin, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(enabled)) {
			addToValidate(InputKeys.USER_ENABLED, enabled, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(newAccount)) {
			addToValidate(InputKeys.NEW_ACCOUNT, newAccount, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(campaignCreationPrivilege)) {
			addToValidate(InputKeys.CAMPAIGN_CREATION_PRIVILEGE, campaignCreationPrivilege, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(firstName)) {
			addToValidate(InputKeys.FIRST_NAME, firstName, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(lastName)) {
			addToValidate(InputKeys.LAST_NAME, lastName, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(organization)) {
			addToValidate(InputKeys.ORGANIZATION, organization, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(personalId)) {
			addToValidate(InputKeys.PERSONAL_ID, personalId, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(emailAddress)) {
			addToValidate(InputKeys.EMAIL_ADDRESS, emailAddress, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(jsonData)) {
			addToValidate(InputKeys.USER_JSON_DATA, jsonData, true);
		}
	}
}