package org.ohmage.service;

import java.util.Collection;
import java.util.Map;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserClassDaos;
import org.ohmage.dao.UserDaos;
import org.ohmage.domain.UserInformation;
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
			UserDaos.createUser(username, password, admin, enabled, newAccount, campaignCreationPrivilege);
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
}