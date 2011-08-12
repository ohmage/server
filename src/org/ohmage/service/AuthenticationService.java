package org.ohmage.service;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.AuthenticationDao;
import org.ohmage.dao.AuthenticationDao.UserInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.UserRequest;

/**
 * This class contains the authentication services.
 * 
 * @author John Jenkins
 */
public final class AuthenticationService {
	/**
	 * Default constructor. Private to facilitate the Singleton pattern.
	 */
	private AuthenticationService() {}
	
	/**
	 * Checks if the user in the request is already logged in. If so, returns
	 * true. If not, it attempts to get their information. If no information
	 * exists about them, then false is returned. If such information does
	 * exist, that information is checked to ensure that they are allowed to
	 * login.
	 * 
	 * @param request The request with the user that is attempting to login.
	 * 
	 * @param newAccountsAllowed Whether or not new accounts are allowed.
	 * 
	 * @return Returns true if the user successfully logged in or was already
	 * 		   logged in; false, otherwise.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static boolean authenticate(UserRequest request, boolean newAccountsAllowed) throws ServiceException {
		// If the user is already logged in, then they are already 
		// authenticated.
		if(request.getUser().isLoggedIn()) {
			return true;
		}
		
		// Get the user's information should the username and password be
		// correct.
		UserInformation userInformation;
		try {
			userInformation = AuthenticationDao.execute(request);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
		
		// If the username and/or password were incorrect, then null was 
		// returned. Therefore, return false.
		if(userInformation == null) {
			return false;
		}
		
		// If the account is disabled, update the annotator and set the request
		// as failed.
		if(! userInformation.getEnabled()) {
			request.setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The account is disabled.");
			return false;
		}
		// If the user is a new user and we aren't allowing new users for this
		// call, update the annotator and set the request as failed.
		else if((! newAccountsAllowed) && userInformation.getNewAccount()) {
			request.setFailed(ErrorCodes.AUTHENTICATION_FAILED, "New accounts aren't allowed to use this service.");
			return false;
		}
		// Otherwise, the user is valid and should be logged in.
		else {
			request.getUser().isLoggedIn(true);
			return true;
		}
	}
}