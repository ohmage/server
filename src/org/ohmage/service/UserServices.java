package org.ohmage.service;

import java.util.Collection;
import java.util.Map;

import jbcrypt.BCrypt;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserClassDaos;
import org.ohmage.dao.UserDaos;
import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services for users.
 * 
 * @author John Jenkins
 */
public final class UserServices {
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private UserServices() {}
	
	/**
	 * Creates a new user.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username for the new user.
	 * 
	 * @param password The password for the new user.
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
	public static void createUser(Request request, String username, String password, 
			Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege) throws ServiceException {
		try {
			String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(13));
			
			UserDaos.createUser(username, hashedPassword, admin, enabled, newAccount, campaignCreationPrivilege);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks that a user's existence matches that of 'shouldExist'.
	 * 
	 * @param request The request that is performing this check.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param shouldExist Whether or not the user should exist.
	 * 
	 * @throws ServiceException Thrown if there was an error, if the user 
	 * 							exists but shouldn't, or if the user doesn't
	 * 							exist but should.
	 */
	public static void checkUserExistance(Request request, String username, boolean shouldExist) throws ServiceException {
		try {
			if(UserDaos.userExists(username)) {
				if(! shouldExist) {
					request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The following user already exists: " + username);
					throw new ServiceException("The following user already exists: " + username);
				}
			}
			else {
				if(shouldExist) {
					request.setFailed(ErrorCodes.USER_INVALID_USERNAME, "The following user does not exist: " + username);
					throw new ServiceException("The following user does not exist: " + username);
				}
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks that a Collection of users' existence matches that of 
	 * 'shouldExist'.
	 * 
	 * @param request The Request that is performing this check.
	 * 
	 * @param usernames A Collection of usernames to check that each exists.
	 * 
	 * @param shouldExist Whether or not all of the users should exist or not.
	 * 
	 * @throws ServiceException Thrown if there was an error, if one of the 
	 * 							users should have existed and didn't, or if one 
	 * 							of the users shouldn't exist but does.
	 */
	public static void verifyUsersExist(Request request, Collection<String> usernames, boolean shouldExist) throws ServiceException {
		for(String username : usernames) {
			checkUserExistance(request, username, shouldExist);
		}
	}
	
	/**
	 * Checks if the user is an admin.
	 * 
	 * @param request The request that is checking if the user is an admin.
	 * 
	 * @param username The username of the user whose admin status is being
	 * 				   checked.
	 * 
	 * @return Returns true if the user is an admin; false if not or there is
	 * 		   an error.
	 * 
	 * @throws ServiceException Thrown if there was an error or if the user is
	 * 							not an admin.
	 */
	public static void verifyUserIsAdmin(Request request, String username) throws ServiceException {
		try {
			if(! UserDaos.userIsAdmin(username)) {
				request.setFailed(ErrorCodes.USER_INSUFFICIENT_PERMISSIONS, "The user is not an admin.");
				throw new ServiceException("The user is not an admin.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user can create campaigns.
	 * 
	 * @param request The request that is performing this check.
	 * 
	 * @param username The username of the user whose campaign creation ability
	 * 				   is being checked.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to create campaigns.
	 */
	public static void verifyUserCanCreateCampaigns(Request request, String username) throws ServiceException {
		try {
			if(! UserDaos.userCanCreateCampaigns(username)) {
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user does not have permission to create new campaigns.");
				throw new ServiceException("The user does not have permission to create new campaigns.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}

	/**
	 * Verifies that if the 'personalInfo' is not null nor empty, that either
	 * there already exists a personal information entry for some user or that
	 * there is sufficient information in the 'personalInfo' object to create a
	 * new entry.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose personal information is
	 * 				   being queried.
	 * 
	 * @param personalInfo The personal information to use to populate the 
	 * 					   user's personal information entry in the database
	 * 					   should one not exist.
	 * 
	 * @throws ServiceException Thrown if the 'personalInfo' object is not null
	 * 							nor is it empty, there is not a personal
	 * 							information entry for this user in the 
	 * 							database, and there is some required field 
	 * 							missing in the 'personalInfo' object to create
	 * 							a new personal information entry in the
	 * 							database. Also, it is thrown if there is an 
	 * 							error. 
	 */
	public static void verifyUserHasOrCanCreatePersonalInfo(Request request, String username, UserPersonal personalInfo) throws ServiceException {
		if((personalInfo != null) && (! personalInfo.isEmpty())) {
			try {
				if(! UserDaos.userHasPersonalInfo(username)) {
					if(personalInfo.getFirstName() == null) {
						request.setFailed(ErrorCodes.USER_INVALID_FIRST_NAME_VALUE, "The user doesn't have personal information yet, and a first name is necessary to create one.");
						throw new ServiceException("The user doesn't have personal information yet, and a first name is necessary to create one.");
					}
					else if(personalInfo.getLastName() == null) {
						request.setFailed(ErrorCodes.USER_INVALID_LAST_NAME_VALUE, "The user doesn't have personal information yet, and a last name is necessary to create one.");
						throw new ServiceException("The user doesn't have personal information yet, and a last name is necessary to create one.");
					}
					else if(personalInfo.getOrganization() == null) {
						request.setFailed(ErrorCodes.USER_INVALID_ORGANIZATION_VALUE, "The user doesn't have personal information yet, and a organization is necessary to create one.");
						throw new ServiceException("The user doesn't have personal information yet, and an organization is necessary to create one.");
					}
					else if(personalInfo.getPersonalId() == null) {
						request.setFailed(ErrorCodes.USER_INVALID_PERSONAL_ID_VALUE, "The user doesn't have personal information yet, and a personal ID is necessary to create one.");
						throw new ServiceException("The user doesn't have personal information yet, and a personal ID is necessary to create one.");
					}
				}
			}
			catch(DataAccessException e) {
				request.setFailed();
				throw new ServiceException(e);
			}
		}
	}
	
	/**
	 * Gathers the personal information about a user.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose information is being
	 * 				   requested.
	 * 
	 * @return Returns a UserInformation object that contains the necessary
	 * 		   information about a user.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static UserInformation gatherUserInformation(Request request, String username) throws ServiceException {
		try {
			// Get campaign creation privilege.
			UserInformation userInformation = new UserInformation(UserDaos.userCanCreateCampaigns(username));
			
			// Get the campaigns and their names for the requester.
			Map<String, String> campaigns = UserCampaignDaos.getCampaignIdsAndNameForUser(username);
			userInformation.addCampaigns(campaigns);
			
			// Get the requester's campaign roles for each of the campaigns.
			for(String campaignId : campaigns.keySet()) {
				userInformation.addCampaignRoles(UserCampaignDaos.getUserCampaignRoles(username, campaignId));
			}
			
			// Get the classes and their names for the requester.
			Map<String, String> classes = UserClassDaos.getClassIdsAndNameForUser(username);
			userInformation.addClasses(classes);
			
			// Get the requester's class roles for each of the classes.
			for(String classId : classes.keySet()) {
				userInformation.addClassRole(UserClassDaos.getUserClassRole(classId, username));
			}
			
			return userInformation;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
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
	 * 									Value indicates that this field should
	 * 									not be updated.
	 * 
	 * @param personalInfo Personal information about a user. If this is null,
	 * 					   none of the user's personal information will be
	 * 					   updated. If it is not null, all non-null values 
	 * 					   inside this object will be used to update the user's
	 * 					   personal information database record; all null 
	 * 					   values will be ignored.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void updateUser(Request request, String username, Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege, UserPersonal personalInfo) throws ServiceException {
		try {
			UserDaos.updateUser(username, admin, enabled, newAccount, campaignCreationPrivilege, personalInfo);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates the user's password.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose password is being 
	 * 				   updated.
	 * 
	 * @param plaintextPassword The plaintext password for the user.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void updatePassword(Request request, String username, String plaintextPassword) throws ServiceException {
		try {
			String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt(13));
			
			UserDaos.updateUserPassword(username, hashedPassword);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes all of the users from the Collection.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param usernames A Collection of usernames of the users to delete.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void deleteUser(Request request, Collection<String> usernames) throws ServiceException {
		try {
			UserDaos.deleteUsers(usernames);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
