package org.ohmage.query;

import java.util.Collection;
import java.util.List;

import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.DataAccessException;

public interface IUserQueries {

	/**
	 * Creates a new user.
	 * 
	 * @param username The username for the new user.
	 * 
	 * @param password The hashed password for the new user.
	 * 
	 * @param admin Whether or not the user should initially be an admin.
	 * 
	 * @param enabled Whether or not the user should initially be enabled.
	 * 
	 * @param newAccount Whether or not the new user must change their password
	 * 					 before using any other APIs.
	 * 
	 * @param campaignCreationPrivilege Whether or not the new user is allowed
	 * 									to create campaigns.
	 */
	void createUser(String username, String hashedPassword, Boolean admin,
			Boolean enabled, Boolean newAccount,
			Boolean campaignCreationPrivilege) throws DataAccessException;

	/**
	 * Returns whether or not a user exists.
	 * 
	 * @param username The username for which to check.
	 * 
	 * @return Returns true if the user exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Boolean userExists(String username) throws DataAccessException;

	/**
	 * Gets whether or not the user is an admin.
	 * 
	 * @param username The user's username.
	 * 
	 * @return Whether or not they are an admin.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	Boolean userIsAdmin(String username) throws DataAccessException;

	/**
	 * Gets whether or not the user's account is enabled.
	 * 
	 * @param username The user's username.
	 * 
	 * @return Whether or not the user's account is enabled.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	Boolean userIsEnabled(String username) throws DataAccessException;

	/**
	 * Gets whether or not the user has a new account.
	 * 
	 * @param username The user's username.
	 * 
	 * @return Whether or not the user's account is new.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	Boolean userHasNewAccount(String username) throws DataAccessException;

	/**
	 * Gets whether or not the user is allowed to create campaigns.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @return Whether or not the user can create campaigns.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	Boolean userCanCreateCampaigns(String username) throws DataAccessException;

	/**
	 * Checks if a user has a personal information entry in the database.
	 *  
	 * @param username The username of the user.
	 * 
	 * @return Returns true if the user has a personal information entry; 
	 * 		   returns false otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Boolean userHasPersonalInfo(String username) throws DataAccessException;
	
	/**
	 * Retrieves all of the usernames in the system.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getAllUsernames() throws DataAccessException;
	
	/**
	 * Retrieves all of the usernames that contain the parameterized username.
	 * 
	 * @param username The partial username.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialUsername(String username) 
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users whose admin value matches
	 * the given one.
	 * 
	 * @param admin The admin value.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesWithAdminValue(Boolean admin)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users whose enabled value matches
	 * the given one.
	 * 
	 * @param enabled The enabled value.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesWithEnabledValue(Boolean enabled)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users whose new account value
	 * matches the given one.
	 * 
	 * @param newAccount The new account value.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesWithNewAccountValue(Boolean newAccount)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users whose campaign creation
	 * privileges matches the given one.
	 * 
	 * @param campaignCreationPrivilege The campaign creation privilege.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesWithCampaignCreationPrivilege(
			Boolean campaignCreationPrivilege)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users that have personal 
	 * information and whose first name value contains the given one.
	 * 
	 * @param partialFirstName The partial first name value to match.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialFirstName(String partialFirstName)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users that have personal 
	 * information and whose last name value contains the given one.
	 * 
	 * @param partialLastName The partial last name value to match.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialLastName(String partialLastName)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users that have personal 
	 * information and whose organization value contains the given one.
	 * 
	 * @param partialOrganization The partial organization value to match.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialOrganization(
			String partialOrganization)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users that have personal 
	 * information and whose personal ID value contains the given one.
	 * 
	 * @param partialPersonalId The partial personal ID value to match.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialPersonalId(String partialPersonalId)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users that have personal  
	 * information, have an email address, and whose email address contains the
	 * given one.
	 * 
	 * @param partialEmailAddress The partial email address to match.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialEmailAddress(
			String partialEmailAddress)
			throws DataAccessException;
	
	/**
	 * Retrieves the usernames of all of the users that have personal  
	 * information, have JSON data, and whose JSON data contains the given one.
	 * 
	 * @param partialJsonData The partial JSON data to match.
	 * 
	 * @return The list of usernames.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getUsernamesFromPartialJsonData(String partialJsonData)
			throws DataAccessException;

	/**
	 * Retrieves the personal information for a user or null if the user 
	 * doesn't have any personal information.
	 *
	 * @param username The username of the user whose information is being
	 * 				   retrieved.
	 * 
	 * @return If the user has a personal entry in the database, a UserPersonal
	 * 		   object with that information is returned; otherwise, null is
	 * 		   returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	UserPersonal getPersonalInfoForUser(String username)
			throws DataAccessException;

	/**
	 * Updates a user's account information.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose information is to be
	 * 				   updated.
	 * 
	 * @param admin Whether or not the user should be an admin. A null value
	 * 			    indicates that this field should not be updated.
	 * 
	 * @param enabled Whether or not the user's account should be enabled. A
	 * 				  null value indicates that this field should not be
	 * 				  updated.
	 * 
	 * @param newAccount Whether or not the user should be required to change
	 * 					 their password. A null value indicates that this field
	 * 					 should not be updated.
	 * 
	 * @param campaignCreationPrivilege Whether or not the user should be 
	 * 									allowed to create campaigns. A null
	 * 									value indicates that this field should
	 * 									not be updated.
	 * 
	 * @param personalInfo Personal information about a user. If this is null,
	 * 					   none of the user's personal information will be
	 * 					   updated. If it is not null, all non-null values 
	 * 					   inside this object will be used to update the user's
	 * 					   personal information database record; all null 
	 * 					   values will be ignored.
	 */
	void updateUser(String username, Boolean admin, Boolean enabled,
			Boolean newAccount, Boolean campaignCreationPrivilege,
			UserPersonal personalInfo) throws DataAccessException;

	/**
	 * Updates a user's password.
	 * 
	 * @param username The username of the user to be updated.
	 * 
	 * @param hashedPassword The new, hashed password for the user.
	 */
	void updateUserPassword(String username, String hashedPassword)
			throws DataAccessException;

	/**
	 * Deletes all of the users in a Collection.
	 * 
	 * @param usernames A Collection of usernames for the users to delete.
	 */
	void deleteUsers(Collection<String> usernames) throws DataAccessException;

}