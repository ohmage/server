package org.ohmage.service;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IAuthenticationQuery;
import org.ohmage.query.impl.AuthenticationQuery.UserInformation;
import org.ohmage.request.UserRequest;

/**
 * This class contains the authentication services.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class AuthenticationService {
	private static AuthenticationService instance;
	private IAuthenticationQuery authenticationQuery;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iAuthenticationQuery is null 
	 */
	private AuthenticationService(IAuthenticationQuery iAuthenticationQuery) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}

		if(iAuthenticationQuery == null) {
			throw new IllegalArgumentException("An instance of IAuthenticationQuery is required.");
		}

		authenticationQuery = iAuthenticationQuery;
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static AuthenticationService instance() {
		return instance;
	}
	
	/**
	 * Checks if the user in the request is already logged in. If so, returns
	 * true. If not, it attempts to get their information. If no information
	 * exists about them, then false is returned. If such information does
	 * exist, that information is checked to ensure that they are allowed to
	 * login.
	 * 
	 * @param newAccountsAllowed Whether or not new accounts are allowed.
	 * 
	 * @return Returns true if the user successfully logged in or was already
	 * 		   logged in; false, otherwise.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public boolean authenticate(UserRequest request, boolean newAccountsAllowed) throws ServiceException {
		// If the user is already logged in, then they are already 
		// authenticated.
		if(request.getUser().isLoggedIn()) {
			return true;
		}
		
		// Get the user's information should the username and password be
		// correct.
		UserInformation userInformation;
		try {
			userInformation = authenticationQuery.execute(request);
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
			request.setFailed(ErrorCode.AUTHENTICATION_FAILED, "The account is disabled.");
			return false;
		}
		// If the user is a new user and we aren't allowing new users for this
		// call, update the annotator and set the request as failed.
		else if((! newAccountsAllowed) && userInformation.getNewAccount()) {
			request.setFailed(ErrorCode.AUTHENTICATION_FAILED, "New accounts aren't allowed to use this service.");
			return false;
		}
		// Otherwise, the user is valid and should be logged in.
		else {
			request.getUser().isLoggedIn(true);
			return true;
		}
	}
}