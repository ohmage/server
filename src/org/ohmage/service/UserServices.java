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
package org.ohmage.service;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jbcrypt.BCrypt;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.User;
import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.domain.UserSummary;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IImageQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserClassQueries;
import org.ohmage.query.IUserImageQueries;
import org.ohmage.query.IUserQueries;
import org.ohmage.query.impl.QueryResult;

/**
 * This class contains the services for users.
 * 
 * @author John Jenkins
 */
public final class UserServices {
	private static UserServices instance;
	
	private IUserQueries userQueries;
	private IUserCampaignQueries userCampaignQueries;
	private IUserClassQueries userClassQueries;
	private IUserImageQueries userImageQueries;
	private IImageQueries imageQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iUserQueries or iUserClassQueries
	 * or iUserCampaignQueries is null
	 */
	private UserServices(IUserQueries iUserQueries, 
			IUserCampaignQueries iUserCampaignQueries, IUserClassQueries iUserClassQueries,
			IUserImageQueries iUserImageQueries, IImageQueries iImageQueries) {
		
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iUserQueries == null) {
			throw new IllegalArgumentException("An instance of IUserQueries is required.");
		}
		if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignQueries is required.");
		}
		if(iUserClassQueries == null) {
			throw new IllegalArgumentException("An instance of IUserClassQueries is required.");
		}
		if(iUserImageQueries == null) {
			throw new IllegalArgumentException("An instance of IUserImageQueries is required.");
		}
		if(iImageQueries == null) {
			throw new IllegalArgumentException("An instance of IIimageQueries is required.");
		}
		
		userQueries = iUserQueries;
		userCampaignQueries = iUserCampaignQueries;
		userClassQueries = iUserClassQueries;
		userImageQueries = iUserImageQueries;
		imageQueries = iImageQueries;
		
		instance = this;		
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static UserServices instance() {
		return instance;
	}

	
	/**
	 * Creates a new user.
	 * 
	 * @param username The username for the new user.
	 * 
	 * @param password The password for the new user.
	 * 
	 * @param emailAddress The user's email address or null.
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
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void createUser(
			final String username, 
			final String password, 
			final String emailAddress,
			final Boolean admin, 
			final Boolean enabled, 
			final Boolean newAccount, 
			final Boolean campaignCreationPrivilege)
			throws ServiceException {
		
		try {
			String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(User.BCRYPT_COMPLEXITY));
			
			userQueries.createUser(username, hashedPassword, emailAddress, admin, enabled, newAccount, campaignCreationPrivilege);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks that a user's existence matches that of 'shouldExist'.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param shouldExist Whether or not the user should exist.
	 * 
	 * @throws ServiceException Thrown if there was an error, if the user 
	 * 							exists but shouldn't, or if the user doesn't
	 * 							exist but should.
	 */
	public void checkUserExistance(final String username, 
			final boolean shouldExist) throws ServiceException {
		
		try {
			if(userQueries.userExists(username)) {
				if(! shouldExist) {
					throw new ServiceException(
							ErrorCode.USER_INVALID_USERNAME, 
							"The following user already exists: " + username);
				}
			}
			else {
				if(shouldExist) {
					throw new ServiceException(
							ErrorCode.USER_INVALID_USERNAME, 
							"The following user does not exist: " + username);
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks that a Collection of users' existence matches that of 
	 * 'shouldExist'.
	 * 
	 * @param usernames A Collection of usernames to check that each exists.
	 * 
	 * @param shouldExist Whether or not all of the users should exist or not.
	 * 
	 * @throws ServiceException Thrown if there was an error, if one of the 
	 * 							users should have existed and didn't, or if one 
	 * 							of the users shouldn't exist but does.
	 */
	public void verifyUsersExist(final Collection<String> usernames, 
			final boolean shouldExist) throws ServiceException {
		
		for(String username : usernames) {
			checkUserExistance(username, shouldExist);
		}
	}
	
	/**
	 * Checks if the user is an admin.
	 * 
	 * @return Returns true if the user is an admin; false if not or there is
	 * 		   an error.
	 * 
	 * @throws ServiceException Thrown if there was an error or if the user is
	 * 							not an admin.
	 */
	public void verifyUserIsAdmin(final String username) 
			throws ServiceException {
		
		try {
			if(! userQueries.userIsAdmin(username)) {
				throw new ServiceException(
						ErrorCode.USER_INSUFFICIENT_PERMISSIONS, 
						"The user is not an admin."
					);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user can create campaigns.
	 * 
	 * @param username The username of the user whose campaign creation ability
	 * 				   is being checked.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to create campaigns.
	 */
	public void verifyUserCanCreateCampaigns(final String username) 
			throws ServiceException {
		
		try {
			if(! userQueries.userCanCreateCampaigns(username)) {
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
						"The user does not have permission to create new campaigns.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a given user is allowed to read the personal information
	 * about a group of users.
	 * 
	 * @param username The username of the reader.
	 * 
	 * @param usernames The usernames of the readees.
	 * 
	 * @throws ServiceException There was an error or the user is not allowed 
	 * 							to read the personal information of one or more
	 * 							of the users.
	 */
	public void verifyUserCanReadUsersPersonalInfo(
			final String username, final Collection<String> usernames) 
			throws ServiceException {
		
		if((usernames == null) || (usernames.size() == 0) ||
				((usernames.size() == 1) && 
				 usernames.iterator().next().equals(username))) {
			return;
		}
		
		Set<String> supervisorCampaigns = 
			UserCampaignServices.instance().getCampaignsForUser(username, 
					null, null, null, null, null, null, 
					Campaign.Role.SUPERVISOR);
		
		Set<String> privilegedClasses = 
			UserClassServices.instance().getClassesForUser(
					username, 
					Clazz.Role.PRIVILEGED);
		
		for(String currUsername : usernames) {
			if(UserCampaignServices.instance().getCampaignsForUser( 
					currUsername, supervisorCampaigns, privilegedClasses, 
					null, null, null, null, null).size() == 0) {
				
				throw new ServiceException(
						ErrorCode.USER_INSUFFICIENT_PERMISSIONS, 
						"The user is not allowed to view personal information about a user in the list: " + 
							currUsername);
			}
		}
	}

	/**
	 * Verifies that if the user already has personal information in which it 
	 * is acceptable to update any combination of the pieces or that they 
	 * supplied all necessary pieces to update the information.
	 * 
	 * @param username The username of the user whose personal information is
	 * 				   being queried.
	 * 
	 * @param firstName The new first name of the user or null if the first 
	 * 					name is not being updated.
	 * 
	 * @param lastName The new last name of the user or null if the last name 
	 * 				   is not being updated.
	 * 
	 * @param organization The new organization of the user or null if the
	 * 					   organization is not being updated.
	 * 
	 * @param personalId The new personal ID of the user or null if the 
	 * 					 personal ID is not being updated.
	 * 
	 * @throws ServiceException The user doesn't have personal information in
	 * 							the system and is attempting to update some 
	 * 							fields but not all of them. If the user doesn't
	 * 							have personal information already, they must
	 * 							create a new one with all of the information. 
	 * 							Or there was an error.
	 */
	public void verifyUserHasOrCanCreatePersonalInfo(
			final String username, 
			final String firstName,
			final String lastName,
			final String organization,
			final String personalId) 
			throws ServiceException {
		
		// If the user already has personal information, then they are allowed
		// to edit it as they wish.
		try {
			if(userQueries.userHasPersonalInfo(username)) {
				return;
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// If they are all null and the user isn't trying to update the 
		// personal information, then that is fine.
		if((firstName == null) &&
				(lastName == null) &&
				(organization == null) &&
				(personalId == null)) {
			
			return;
		}
		
		if(firstName == null) {
			throw new ServiceException(
					ErrorCode.USER_INVALID_FIRST_NAME_VALUE, 
					"The user doesn't have personal information yet, and a first name is necessary to create one.");
		}
		else if(lastName == null) {
			throw new ServiceException(
					ErrorCode.USER_INVALID_LAST_NAME_VALUE, 
					"The user doesn't have personal information yet, and a last name is necessary to create one.");
		}
		else if(organization == null) {
			throw new ServiceException(
					ErrorCode.USER_INVALID_ORGANIZATION_VALUE, 
					"The user doesn't have personal information yet, and an organization is necessary to create one.");
		}
		else if(personalId == null) {
			throw new ServiceException(
					ErrorCode.USER_INVALID_PERSONAL_ID_VALUE, 
					"The user doesn't have personal information yet, and a personal ID is necessary to create one.");
		}
	}
	
	/**
	 * Searches through all of the users in the system and returns those that
	 * match the criteria. All Object parameters are optional; by passing a 
	 * null value, it will be omitted from the search. 
	 * 
	 * @param usernames Limits the results to only those whose username is in
	 * 					this list.
	 * 
	 * @param emailAddress Limits the results to only those users whose email
	 * 					   address matches this value.
	 * 
	 * @param admin Limits the results to only those users whose admin value
	 * 				matches this value.
	 * 
	 * @param enabled Limits the results to only those user whose enabled value
	 * 				  matches this value.
	 * 
	 * @param newAccount Limits the results to only those users whose new 
	 * 					 account value matches this value.
	 * 
	 * @param campaignCreationPrivilege Limits the results to only those 
	 * 									users whose campaign creation privilege
	 * 									matches this value.
	 * 
	 * @param firstName Limits the results to only those that have personal 
	 * 					information and their first name equals this value.
	 * 
	 * @param partialLastName Limits the results to only those users that have 
	 * 						  personal information and their last name matches 
	 * 						  this value.
	 * 
	 * @param partialOrganization Limits the results to only those users that 
	 * 							  have personal information and their 
	 * 							  organization value matches this value.
	 * 
	 * @param partialPersonalId Limits the results to only those users that 
	 * 							have personal information and their personal ID
	 * 							matches this value.
	 * 
	 * @param numToSkip The number of results to skip.
	 * 
	 * @param numToReturn The number of results to return.
	 * 
	 * @param results The user information for the users that matched the
	 * 				  criteria.
	 * 
	 * @return The number of usernames that matched the given criteria.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public long getUserInformation(
			final Collection<String> usernames,
			final String emailAddress,
			final Boolean admin,
			final Boolean enabled,
			final Boolean newAccount,
			final Boolean canCreateCampaigns,
			final String firstName,
			final String lastName,
			final String organization,
			final String personalId,
			final long numToSkip,
			final long numToReturn,
			final List<UserInformation> results) 
			throws ServiceException {
		
		try {
			QueryResult<UserInformation> result =
					userQueries.getUserInformation(
							usernames, 
							null, 
							emailAddress, 
							admin, 
							enabled, 
							newAccount, 
							canCreateCampaigns, 
							firstName, 
							lastName, 
							organization, 
							personalId, 
							false, 
							numToSkip, 
							numToReturn);
			
			results.addAll(result.getResults());
			
			return result.getTotalNumResults();
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Searches through all of the users in the system and returns those that
	 * match the criteria. All Object parameters are optional; by passing a 
	 * null value, it will be omitted from the search. 
	 * 
	 * @param partialUsername Limits the results to only those users whose 
	 * 						  username contain this value.
	 * 
	 * @param partialEmailAddress Limits the results to only those users whose
	 * 							  email address contains this value.
	 * 
	 * @param admin Limits the results to only those usernames that belong to
	 * 				users whose admin value matches this one.
	 * 
	 * @param enabled Limits the results to only those usernames that belong to
	 * 				  users whose enabled value matches this one.
	 * 
	 * @param newAccount Limits the results to only those usernames that belong
	 * 					 to users whose new account value matches this one.
	 * 
	 * @param campaignCreationPrivilege Limits the results to only those 
	 * 									usernames that belong to users whose	
	 * 									campaign creation privilege matches 
	 * 									this one.
	 * 
	 * @param partialFirstName Limits the results to only those usernames that
	 * 						   belong to users that have personal information
	 * 						   and their first name contains this value.
	 * 
	 * @param partialLastName Limits the results to only those usernames that
	 * 						  belong to users that have personal information 
	 * 						  and their last name contains this value.
	 * 
	 * @param partialOrganization Limits the results to only those usernames
	 * 							  that belong to users that have personal 
	 * 							  information and their organization value 
	 * 							  contains this value.
	 * 
	 * @param partialPersonalId Limits the results to only those usernames that
	 * 							belong to users that have personal information
	 * 							and their personal ID contains this value.
	 * 
	 * @param numToSkip The number of results to skip.
	 * 
	 * @param numToReturn The number of results to return.
	 * 
	 * @param results The user information for the users that matched the
	 * 				  criteria. This cannot be null and will be populated with
	 * 				  the results.
	 * 
	 * @return The number of usernames that matched the given criteria.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public long userSearch(
			final String partialUsername,
			final String partialEmailAddress,
			final Boolean admin,
			final Boolean enabled,
			final Boolean newAccount,
			final Boolean campaignCreationPrivilege,
			final String partialFirstName,
			final String partialLastName,
			final String partialOrganization,
			final String partialPersonalId,
			final int numToSkip,
			final int numToReturn,
			final Collection<UserInformation> results)
			throws ServiceException {
		
		try {
			QueryResult<UserInformation> result =
					userQueries.getUserInformation(
							null, 
							partialUsername, 
							partialEmailAddress, 
							admin, 
							enabled, 
							newAccount, 
							campaignCreationPrivilege, 
							partialFirstName, 
							partialLastName, 
							partialOrganization, 
							partialPersonalId, 
							true, 
							numToSkip, 
							numToReturn);
			
			try {
				for(UserInformation currResult : result.getResults()) {
					currResult.addCampaigns(
							userCampaignQueries.getCampaignAndRolesForUser(
									currResult.getUsername()));
				
					currResult.addClasses(
							userClassQueries.getClassAndRoleForUser(
									currResult.getUsername()));
				}
			}
			catch(DomainException e) {
				throw new ServiceException(e);
			}
			
			results.addAll(result.getResults());

			return result.getTotalNumResults();
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Gathers the summary about a user.
	 * 
	 * @param username The username of the user whose summary is being 
	 * 				   requested.
	 * 
	 * @return Returns a UserSummary object that contains the necessary
	 * 		   information about a user.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public UserSummary getUserSummary(final String username)
			throws ServiceException {
		
		try {
			// Get the campaigns and their names for the requester.
			Map<String, String> campaigns = userCampaignQueries.getCampaignIdsAndNameForUser(username);
						
			// Get the requester's campaign roles for each of the campaigns.
			Set<Campaign.Role> campaignRoles = new HashSet<Campaign.Role>();
			for(String campaignId : campaigns.keySet()) {
				campaignRoles.addAll(
						userCampaignQueries.getUserCampaignRoles(
								username, 
								campaignId));
			}

			// Get the classes and their names for the requester.
			Map<String, String> classes = userClassQueries.getClassIdsAndNameForUser(username);
			
			// Get the requester's class roles for each of the classes.
			Set<Clazz.Role> classRoles = new HashSet<Clazz.Role>();
			for(String classId : classes.keySet()) {
				classRoles.add(
						userClassQueries.getUserClassRole(classId, username));
			}
			
			// Get campaign creation privilege.
			try {
				return new UserSummary(
						userQueries.userIsAdmin(username), 
						userQueries.userCanCreateCampaigns(username),
						campaigns,
						campaignRoles,
						classes,
						classRoles);
			} 
			catch(DomainException e) {
				throw new ServiceException(e);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the personal information for all of the users in the list.
	 * 
	 * @param usernames The usernames.
	 * 
	 * @return A map of usernames to personal information or null if no 
	 * 		   personal information is available.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Map<String, UserPersonal> gatherPersonalInformation(
			final Collection<String> usernames) throws ServiceException {
		
		try {
			Map<String, UserPersonal> result = 
				new HashMap<String, UserPersonal>(usernames.size());
			
			for(String username : usernames) {
				result.put(username, userQueries.getPersonalInfoForUser(username));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates a user's account information.
	 * 
	 * @param username The username of the user whose information is to be
	 * 				   updated.
	 * 
	 * @param emailAddress The new email address for the user. A null value 
	 * 					   indicates that this field should not be updated.
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
	 * 									Value indicates that this field should
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
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void updateUser(
			final String username, 
			final String emailAddress,
			final Boolean admin, 
			final Boolean enabled, 
			final Boolean newAccount, 
			final Boolean campaignCreationPrivilege, 
			final String firstName,
			final String lastName,
			final String organization,
			final String personalId) 
			throws ServiceException {
		
		try {
			userQueries.updateUser(
					username, 
					emailAddress,
					admin, 
					enabled, 
					newAccount, 
					campaignCreationPrivilege,
					firstName,
					lastName,
					organization,
					personalId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates the user's password.
	 * 
	 * @param username The username of the user whose password is being 
	 * 				   updated.
	 * 
	 * @param plaintextPassword The plaintext password for the user.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void updatePassword(final String username, 
			final String plaintextPassword) throws ServiceException {
		
		try {
			String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt(13));
			
			userQueries.updateUserPassword(username, hashedPassword);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes all of the users from the Collection.
	 * 
	 * @param usernames A Collection of usernames of the users to delete.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void deleteUser(final Collection<String> usernames) 
			throws ServiceException {
		// First, retrieve the path information for all of the images 
		// associated with each user.
		Collection<URL> imageUrls = new HashSet<URL>();
		try {
			for(String username : usernames) {
				imageUrls.addAll(
					userImageQueries.getImageUrlsFromUsername(username));
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
				
		try {
			userQueries.deleteUsers(usernames);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// If the transaction succeeded, delete all of the images from the 
		// disk.
		for(URL imageUrl : imageUrls) {
			imageQueries.deleteImageDiskOnly(imageUrl);
		}
	}
}
