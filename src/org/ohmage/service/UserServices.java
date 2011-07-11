package org.ohmage.service;

import java.util.Collection;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserDaos;
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
	 * Verifies that a user exists.
	 * 
	 * @param request The request that is performing this check.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @throws ServiceException Thrown if there was an error or if the user
	 * 							does not exist.
	 */
	public static void verifyUserExists(Request request, String username) throws ServiceException {
		try {
			if(! UserDaos.userExists(username)) {
				request.setFailed(ErrorCodes.USER_DOES_NOT_EXIST, "The following user does not exist: " + username);
				throw new ServiceException("The following user does not exist: " + username);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a Collection of users exist.
	 * 
	 * @param request The Request that is performing this check.
	 * 
	 * @param usernames A Collection of usernames to check that each exists.
	 * 
	 * @throws ServiceException Thrown if there was an error or if one of the
	 * 							usernames does not exist.
	 */
	public static void verifyUsersExist(Request request, Collection<String> usernames) throws ServiceException {
		for(String username : usernames) {
			verifyUserExists(request, username);
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
				request.setFailed(ErrorCodes.USER_NOT_ADMIN, "The user is not an admin.");
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
				request.setFailed(ErrorCodes.AUTHENTICATION_INSUFFICIENT_PERMISSIONS_TO_CREATE_CAMPAIGN, "The user does not have permission to create new campaigns.");
				throw new ServiceException("The user does not have permission to create new campaigns.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
