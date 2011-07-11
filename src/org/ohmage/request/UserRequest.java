package org.ohmage.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.User;
import org.ohmage.service.AuthenticationService;
import org.ohmage.service.ServiceException;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.UserValidators;
import org.ohmage.validator.ValidationException;

/**
 * A request that contains a User object and a client String that represents
 * how the requester is making this request.
 * 
 * @author John Jenkins
 */
public abstract class UserRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(UserRequest.class);
	
	public static final long MILLIS_IN_A_SECOND = 1000;
	
	protected final User user;
	protected final String client;
	
	/**
	 * Builds a request that contains a user based on a username and password.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param password The password of the requester.
	 * 
	 * @param hashPassword Whether or not the user's password should be hashed 
	 * 					   before being used.
	 * 
	 * @param client The name of the client requester is using to make the
	 * 				 request.
	 */
	public UserRequest(String username, String password, boolean hashPassword, String client) {
		// This will either be reset as a new User object or the request will 
		// have failed.
		User tempUser = null;
		
		try {
			// Validate the username.
			String tempUsername = UserValidators.validateUsername(this, username);
			
			// Validate the password using a validator based on whether or not
			// the password needs to be hashed.
			String tempPassword;
			if(hashPassword) {
				tempPassword = UserValidators.validatePlaintextPassword(this, password);
			}
			else {
				tempPassword = UserValidators.validateHashedPassword(this, password);
			}
			
			// Create the new User object for this request.
			tempUser = new User(tempUsername, tempPassword, hashPassword);
		}
		// If there is a problem validating one of the parameters.
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		// If user creation failed, set the request as failed.
		catch(IllegalArgumentException e) {
			LOGGER.info("The User could not be created because the username and/or password were missing.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing username and/or password.");
		}
		
		user = tempUser;
		this.client = client;
	}

	/**
	 * Builds a request that contains a user based on their user token.
	 * 
	 * @param token The requester's current authentication / session token.
	 * 
	 * @param client The name of the client the requester is using to make the
	 * 				 request.
	 */
	public UserRequest(String token, String client) {
		// This will either be reset as a new User object or the request will 
		// have failed.
		User tempUser = null;
		
		try {
			// Validate the user's token.
			String tempToken = UserValidators.validateToken(this, token);
			
			// Attempt to retrieve the user.
			if(tempToken != null) {
				tempUser = UserBin.getUser(tempToken);
			}
			
			// If the bin doesn't know about the user, set the request as 
			// failed.
			if(tempUser == null) {
				LOGGER.info("The user object could not be created because the token was unknown.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Unkown token.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		user = tempUser;
		this.client = client;
	}
	
	/**
	 * Build a request that contains first a username and password, but if that
	 * fails attempt to create them from a token. If both fail, then set the
	 * request as failed.
	 * 
	 * For a successful login, either the username and the password should be
	 * valid or the token should be valid.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param password The password of the requester.
	 * 
	 * @param hashPassword Whether or not the password should be hashed before
	 * 					   being used.
	 * 
	 * @param token The requester's authentication / session token.
	 * 
	 * @param client The name of the client the requester is using to make the
	 * 				 request.
	 */
	public UserRequest(String username, String password, boolean hashPassword, String token, String client) {
		// This will either be reset as a new User object or the request will 
		// have failed.
		User tempUser = null;
		
		try {
			// Validate the username.
			String tempUsername = UserValidators.validateUsername(this, username);
			
			// Validate the password using a validator based on whether or not
			// the password needs to be hashed.
			String tempPassword;
			if(hashPassword) {
				tempPassword = UserValidators.validatePlaintextPassword(this, password);
			}
			else {
				tempPassword = UserValidators.validateHashedPassword(this, password);
			}
			
			// Create the new User object for this request.
			tempUser = new User(tempUsername, tempPassword, hashPassword);
		}
		// If there is a problem validating one of the parameters.
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		// If user creation failed, try to lookup a user with the token.
		catch(IllegalArgumentException illegalArgumentException) {
			try {
				LOGGER.info("The username and/or password were missing. Attempting to validate the user with a token.");
				
				// Validate the user's token.
				String tempToken = UserValidators.validateToken(this, token);
				
				// Attempt to retrieve the user.
				tempUser = UserBin.getUser(tempToken);
				
				// If the bin doesn't know about the user, set the request as 
				// failed.
				if(tempUser == null) {
					LOGGER.info("The user could not be created because the token was unknown.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Unkown token.");
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		user = tempUser;
		this.client = client;
	}
	
	/**
	 * Returns the user in the request.
	 * 
	 * @return The user in the request.
	 */
	public final User getUser() {
		return user;
	}
	
	/**
	 * Retrieves the authentication / session token from the request. It first
	 * attempts to retrieve it as a cookie and, if it doesn't exist there,
	 * attempts to retrieve it as a parameter. If it didn't exist there either,
	 * null is returned.
	 *  
	 * @param httpRequest The request that contains the cookie in either its
	 * 					  header or as a parameter.
	 * 
	 * @return The authentication / session token from this request or null if
	 * 		   no such cookie or parameter exists.
	 */
	public static String getToken(HttpServletRequest httpRequest) {
		String token;
		
		token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(token == null) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		return token;
	}

	/**
	 * Authenticates the user in the request.
	 * 
	 * @param newAccountsAllowed Whether or not new accounts are allowed to
	 * 							 make this call.
	 */
	public final boolean authenticate(boolean newAccountsAllowed) {
		try {
			// Validate that the username and password are valid.
			LOGGER.info("Authenticating the user.");
			return AuthenticationService.authenticate(this, newAccountsAllowed);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
			return false;
		}
	}
	
	/**
	 * Generates the response for the user based the 'jsonResponse'. It also
	 * adds a Set-Cookie header in the response for the authentication /
	 * session token if one exists.
	 * 
	 * @param httpRequest The HTTP request that began this exchange.
	 * 
	 * @param httpResponse The HTTP response back to the requester.
	 * 
	 * @param jsonResponse A JSONObject representing what should be sent as the
	 * 					   data to the requester.
	 * 
	 * @param token The authentication / session token that is to be placed in
	 * 				the HTTP response's header.
	 */
	protected void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse, JSONObject jsonResponse) {
		if(user != null) {
			final String token = user.getToken(); 
			if(token != null) {
				CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
			}
		}
		
		super.respond(httpRequest, httpResponse, jsonResponse);
	}
	
	/**
	 * Generates the success/fail response for the user with an additional key-
	 * value pair. It also adds a Set-Cookie header in the response for the 
	 * authentication / session token if one exists.
	 * 
	 * @param httpRequest The HTTP request that began this exchange.
	 * 
	 * @param httpResponse The HTTP response back to the requester.
	 * 
	 * @param key The second to key to include when the request succeeds.
	 * 
	 * @param value the value to assign to the second key.
	 * 
	 * @param token The authentication / session token that is to be placed in
	 * 				the HTTP response's header.
	 */
	protected void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String key, String value) {
		if(user != null) {
			final String token = user.getToken(); 
			if(token != null) {
				CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
			}
		}
		
		super.respond(httpRequest, httpResponse, key, value);
	}
}