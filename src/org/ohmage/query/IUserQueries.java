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
package org.ohmage.query;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.impl.QueryResultsList;

public interface IUserQueries {
	/**
	 * Creates a new user.
	 * 
	 * @param username The username for the new user.
	 * 
	 * @param hashedPassword The hashed password for the new user.
	 * 
	 * @param emailAddress The user's email address, which may be null.
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
	void createUser(
			final String username, 
			final String hashedPassword, 
			final String emailAddress,
			final Boolean admin,
			final Boolean enabled, 
			final Boolean newAccount,
			final Boolean campaignCreationPrivilege) 
			throws DataAccessException;
	
	/**
	 * Creates a user registration by first creating the user, second adding 
	 * them to the public class, and finally by storing their registration
	 * information in the registration table.
	 * 
	 * @param username The new user's username.
	 * 
	 * @param hashedPassword The new user's hashed password.
	 * 
	 * @param emailAddress The new user's email address.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public void createUserRegistration(
			final String username,
			final String hashedPassword,
			final String emailAddress,
			final String registrationId)
			throws DataAccessException;

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
	 * Verifies that a registration exists with the given ID.
	 * 
	 * @param registrationId The registration ID.
	 * 
	 * @return True if it does exist; false, otherwise.
	 * 
	 * @throws DataAccessException There was an error executing the SQL.
	 */
	public boolean registrationIdExists(
			final String registrationId)
			throws DataAccessException;
	
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
	 * Returns the date and time when the user generated the registration.
	 * 
	 * @param registrationId The registration's unique identifier.
	 * 
	 * @return The date and time at which the user generated the registration 
	 * 		   or null indicating that no such registration exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Date getRegistrationRequestedDate(
			final String registrationId)
			throws DataAccessException;
	
	/**
	 * Returns the date and time when the user accepted the registration.
	 * 
	 * @param registrationId The registration's unique identifier.
	 * 
	 * @return The date and time at which the user accepted the registration or
	 * 		   null indicating that either the user has not yet accepted the
	 * 		   registration or that no registration with that ID exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Date getRegistrationAcceptedDate(
			final String registrationId)
			throws DataAccessException;
	
	/**
	 * Gathers the information about a person including the classes and 
	 * campaigns to which they belong. Any of the Object parameters may be 
	 * null except 'requesterUsername'.
	 * 
	 * @param requesterUsername The username of the user that is requesting 
	 * 							this information.
	 * 
	 * @param usernames Limits the results to only those whose username is in
	 * 					this list.
	 * 
	 * @param likeUsername Limits the results to only those whose username is
	 * 					   like this username. The 'like' parameter has no
	 * 					   effect on this parameter.
	 * 					
	 * @param emailAddress Limits the results to only those accounts that have
	 * 					   an email address and where that email address 
	 * 					   matches or is like this value, see 'like'.
	 * 
	 * @param admin Limits the results to only those accounts whose admin value
	 * 				matches this value.
	 * 
	 * @param enabled Limits the results to only those accounts whose enabled
	 * 				  value matches this value.
	 * 
	 * @param newAccount Limits the results to only those accounts whose new
	 * 					 account value matches this value.
	 * 
	 * @param canCreateCampaigns Limits the results to only those accounts 
	 * 							 whose new account status matches this value.
	 * 							 
	 * @param firstName Limits the results to only those accounts that have
	 * 					personal information and whose first name matches or is
	 * 					like this value, see 'like'.
	 * 
	 * @param lastName Limits the results to only those accounts that have 
	 * 				   personal information and whose last name matches or is
	 * 				   like this value, see 'like'.
	 * 
	 * @param organization Limits the results to only those accounts that have
	 * 					   personal information and whose organization value
	 * 					   matches or is like this value, see 'like'.
	 * 
	 * @param personalId Limits the results to only those accounts that have a
	 * 					 personal ID and where that personal ID matches or is
	 * 					 like this value, see 'like'.
	 * 
	 * @param campaignIds Limits the results to only those accounts that are in
	 * 					  any of the campaigns listed.
	 * 
	 * @param classIds Limits the results to only those accounts that are in 
	 * 				   any of the classes listed.
	 * 
	 * @param like Switches the SQL to use LIKE instead of a direct matching. 
	 * 			   This only applies to the parameters that mention it.
	 * 
	 * @param numToSkip The number of results to skip. The results are sorted
	 * 					alphabetically by username.
	 * 
	 * @param numToReturn The number of results to return. The results are 
	 * 					  sorted alphabetically by username.
	 * 
	 * @return A QueryResultsList object containing the users' information and the
	 * 		   total number of results.
	 * 
	 * @throws DataAccessException There was an error aggregating the 
	 * 							   information.
	 */
	public QueryResultsList<UserInformation> getUserInformation(
			final String requesterUsername,
			final Collection<String> usernames,
			final String likeUsername,
			final String emailAddress,
			final Boolean admin,
			final Boolean enabled,
			final Boolean newAccount,
			final Boolean canCreateCampaigns,
			final String firstName,
			final String lastName,
			final String organization,
			final String personalId,
			final Collection<String> campaignIds,
			final Collection<String> classIds,
			final boolean like,
			final long numToSkip,
			final long numToReturn)
			throws DataAccessException;

	/**
	 * Updates a user's account information.
	 * 
	 * @param username The username of the user whose information is to be
	 * 				   updated.
	 * 
	 * @param emailAddress The user's new email address. A null value indicates
	 * 					   that this value should not be updated.
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
	 * @param firstName The user's new first name. A null value indicates that
	 * 					this field should not be updated.
	 * 
	 * @param lastName The users's last name. A null value indicates that this
	 * 				   field should not be updated.
	 * 
	 * @param organization The user's new organization. A null value indicates
	 * 					   that this field should not be updated.
	 * 
	 * @param personalId The user's new personal ID. A null value indicates 
	 * 					 that this field should not be updated.
	 * 
	 * @param deletePersonalInfo Whether or not to delete the user's personal
	 * 							 information.
	 */
	void updateUser(
			final String username, 
			final String emailAddress,
			final Boolean admin, 
			final Boolean enabled,
			final Boolean newAccount, 
			final Boolean campaignCreationPrivilege,
			final String firstName,
			final String lastName,
			final String organization,
			final String personalId,
			final boolean deletePersonalInfo) 
			throws DataAccessException;

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
	 * Activates a user's account by updating the enabled status to true and
	 * updates the registration table's entry.
	 * 
	 * @param registrationId The registration's unique identifier.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public void activateUser(
			final String registrationId) 
			throws DataAccessException;

	/**
	 * Deletes all of the users in a Collection.
	 * 
	 * @param usernames A Collection of usernames for the users to delete.
	 */
	void deleteUsers(Collection<String> usernames) throws DataAccessException;
}