package org.ohmage.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.User;
import org.ohmage.service.AuthenticationService;
import org.ohmage.service.ServiceException;
import org.ohmage.util.CookieUtils;

/**
 * A request that contains a User object and a client String that represents
 * how the requester is making this request.
 * 
 * @author John Jenkins
 */
public abstract class UserRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(UserRequest.class);
	
	protected static enum TokenLocation { COOKIE, PARAMETER, EITHER };
	
	public static final long MILLIS_IN_A_SECOND = 1000;
	
	private final User user;
	private final String client;
	
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
	 * 
	 * @deprecated This does not check if there are duplicate parameters. Use
	 * 			   {@link #UserRequest(HttpServletRequest, boolean)} instead.
	 */
	public UserRequest(String username, String password, boolean hashPassword, String client) {
		// This will either be reset as a new User object or the request will 
		// have failed.
		User tempUser = null;
		
		try {
			// Create the new User object for this request.
			tempUser = new User(username, password, hashPassword);
		}
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
	 * 
	 * @deprecated This does not check if there are duplicate parameters. Use
	 * 			   {@link #UserRequest(HttpServletRequest, TokenLocation)}
	 * 			   instead.
	 */
	public UserRequest(String token, String client) {
		// This will either be reset as a new User object or the request will 
		// have failed.
		User tempUser = null;
		
		if(token != null) {
			tempUser = UserBin.getUser(token);
		}
		
		// If the bin doesn't know about the user, set the request as 
		// failed.
		if(tempUser == null) {
			LOGGER.info("The user object could not be created because the token was unknown.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Unkown token.");
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
	 * 
	 * @deprecated This does not check if there are duplicate parameters. Use
	 * 			   {@link #UserRequest(HttpServletRequest, TokenLocation, boolean)}
	 * 			   instead.
	 */
	public UserRequest(String username, String password, boolean hashPassword, String token, String client) {
		// This will either be reset as a new User object or the request will 
		// have failed.
		User tempUser = null;
		
		try {
			// Create the new User object for this request.
			tempUser = new User(username, password, hashPassword);
		}
		// If user creation failed, try to lookup a user with the token.
		catch(IllegalArgumentException e) {
			LOGGER.info("The username and/or password were missing. Attempting to validate the user with a token.");
			
			// Attempt to retrieve the user.
			if(token != null) {
				tempUser = UserBin.getUser(token);
			}
			
			// If the bin doesn't know about the user, set the request as 
			// failed.
			if(tempUser == null) {
				LOGGER.info("The username and/or password and the token were all invalid or missing.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing username and password and authentication token.");
			}
		}
		
		user = tempUser;
		this.client = client;
	}
	
	/**
	 * Creates a Request from a username and password in the request.
	 * 
	 * @param httpRequest The HttpServletRequest with the username, password,
	 * 					  and client parameters.
	 * 
	 * @param hashPassword Whether or not to hash the user's password when 
	 * 					   authenticating the user.
	 */
	public UserRequest(HttpServletRequest httpRequest, boolean hashPassword) {
		super();
		
		User tUser = null;
		
		// Attempt to retrieve all usernames passed to the server.
		String[] usernames = httpRequest.getParameterValues(InputKeys.USER);
		
		// If it is missing, fail the request.
		if((usernames == null) || (usernames.length == 0)) {
			LOGGER.info("The username is missing from the request.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing username.");
		}
		// If there is more than one, fail the request.
		else if(usernames.length > 1) {
			LOGGER.info("More than one username was given.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one username was given.");
		}
		else {
			// If exactly one username is found, attempt to retrieve all 
			// paswords sent to the server.
			String[] passwords = httpRequest.getParameterValues(InputKeys.PASSWORD);
			
			// If it is missing, fail the request.
			if((passwords == null) || (passwords.length == 0)) {
				LOGGER.info("The password is missing from the request.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing password.");
			}
			// If there are more than one, fail the request.
			else if(passwords.length > 1) {
				LOGGER.info("More than one password was given.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one password was given.");
			}
			else {
				// Attempt to create the new User object for this request.
				try {
					tUser = new User(usernames[0], passwords[0], hashPassword);
				}
				catch(IllegalArgumentException e) {
					LOGGER.info("The username and/or password are invalid.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The username and/or password are invalid.");
				}
			}
		}
		
		// Retrieve the client parameter(s) from the request.
		String tClient = null;
		String[] clients = httpRequest.getParameterValues(InputKeys.CLIENT);
		
		// If there is no client, throw an error.
		if((clients == null) || (clients.length == 0)) {
			LOGGER.info("The client is missing from the request.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing client.");
		}
		// If there are multiple clients, throw an error.
		else if(clients.length > 1) {
			LOGGER.info("More than one client was given.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one client was given.");
		}
		else {
			// Save the client.
			tClient = clients[0];
			
			// Push the client into the logs.
			NDC.push("client=" + tClient);
		}
		
		user = tUser;
		client = tClient;
	}
	
	/**
	 * Creates a Request from an authentication token.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the token and 
	 * 					  client parameters.
	 * 
	 * @param tokenLocation This indicates where the token must be located.
	 */
	public UserRequest(HttpServletRequest httpRequest, TokenLocation tokenLocation) {
		super();
		
		User tUser = null;
		
		// First, check if we allow it to be a cookie.
		if(tokenLocation.equals(TokenLocation.COOKIE) || tokenLocation.equals(TokenLocation.EITHER)) {
			// Retrieve all of the authentication token cookies from the 
			// request.
			List<String> cookies = CookieUtils.getCookieValues(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
			
			// If there are no authentication token cookies and the
			// authentication token cannot be retrieved from another location,
			// fail the request.
			if((cookies.size() == 0) && (! tokenLocation.equals(TokenLocation.EITHER))) {
				LOGGER.info("The authentication token is missing.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The authentication token is missing as a cookie: " + InputKeys.AUTH_TOKEN);
			}
			else if(cookies.size() == 1) {
				// Attempt to retrieve the user.
				tUser = UserBin.getUser(cookies.get(0));
				
				// If the bin doesn't know about the user, set the request as 
				// failed.
				if(tUser == null) {
					LOGGER.info("Unknown token.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The token is unknown.");
				}
			}
			// If there are multipile authentication token cookies, fail the
			// request.
			else if(cookies.size() > 1) {
				LOGGER.info("Multiple authentication token cookies were found.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Multiple authentication token cookies were found.");
			}
		}
		
		// Now, if we haven't yet failed or authenticated the user, see if we
		// allow the token to be a parameter.
		if((tUser == null) && (! isFailed()) &&
		   (tokenLocation.equals(TokenLocation.PARAMETER) || tokenLocation.equals(TokenLocation.EITHER))) {
			// Retrieve all of the authentication tokens that were parameters.
			String[] tokens = httpRequest.getParameterValues(InputKeys.AUTH_TOKEN);
			
			if((tokens == null) || (tokens.length == 0)) {
				LOGGER.info("The authentication token is missing.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The authentication token is missing as a parameter: " + InputKeys.AUTH_TOKEN);
			}
			else if(tokens.length == 1) {
				// Attempt to retrieve the user.
				tUser = UserBin.getUser(tokens[0]);
				
				// If the bin doesn't know about the user, set the request as 
				// failed.
				if(tUser == null) {
					LOGGER.info("Unknown token.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The token is unknown.");
				}
			}
			else if(tokens.length > 1){
				LOGGER.info("Multiple authentication token parameters were found.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Multiple authentication token parameters were found.");
			}
		}
		
		// Retrieve the client parameter(s) from the request.
		String tClient = null;
		String[] clients = httpRequest.getParameterValues(InputKeys.CLIENT);
		
		// If there is no client, throw an error.
		if((clients == null) || (clients.length == 0)) {
			LOGGER.info("The client is missing from the request.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing client.");
		}
		// If there are multiple clients, throw an error.
		else if(clients.length > 1) {
			LOGGER.info("More than one client was given.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one client was given.");
		}
		else {
			// Save the client.
			tClient = clients[0];
			
			// Push the client into the logs.
			NDC.push("client=" + tClient);
		}
		
		user = tUser;
		client = tClient;
	}
	
	/**
	 * Creates a Request from either a username and password or from an
	 * authentication token. It will default to using the username and 
	 * password; however, if one is missing it will fall back to using the 
	 * token.
	 * 
	 * @param httpRequest The Request that contains username and password 
	 * 					  parameters or an authentication token parameter. 
	 * 					  Either way, it must contain a client parameter.
	 * 
	 * @param tokenLocation This indicates where the token must be located.
	 * 
	 * @param hashPassword If using a username and password, this indicates
	 * 					   whether or not to hash the user's password when 
	 * 					   authenticating them.
	 */
	public UserRequest(HttpServletRequest httpRequest, TokenLocation tokenLocation, boolean hashPassword) {
		super();
		
		User tUser = null;
		
		// A flag to indicate whether or not we should look for the token after
		// looking for a username and password.
		boolean getToken = false;
		
		// Attempt to retrieve all usernames passed to the server.
		String[] usernames = httpRequest.getParameterValues(InputKeys.USER);
		
		// If it is missing, search for a token.
		if((usernames == null) || (usernames.length == 0)) {
			getToken = true;
		}
		// If there is more than one, fail the request.
		else if(usernames.length > 1) {
			LOGGER.info("More than one username was given.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one username was given.");
		}
		else {
			// If exactly one username is found, attempt to retrieve all 
			// paswords sent to the server.
			String[] passwords = httpRequest.getParameterValues(InputKeys.PASSWORD);
			
			// If it is missing, fail the request.
			if((passwords == null) || (passwords.length == 0)) {
				getToken = true;
			}
			// If there are more than one, fail the request.
			else if(passwords.length > 1) {
				LOGGER.info("More than one password was given.");
				setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one password was given.");
			}
			else {
				// Attempt to create the new User object for this request.
				try {
					tUser = new User(usernames[0], passwords[0], hashPassword);
				}
				catch(IllegalArgumentException e) {
					LOGGER.info("The username and/or password are invalid.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The username and/or password are invalid.");
				}
			}
		}
		
		if(getToken) {
			// First, check if we allow it to be a cookie.
			if(tokenLocation.equals(TokenLocation.COOKIE) || tokenLocation.equals(TokenLocation.EITHER)) {
				// Retrieve all of the authentication token cookies from the 
				// request.
				List<String> cookies = CookieUtils.getCookieValues(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
				
				// If there are no authentication token cookies and the
				// authentication token cannot be retrieved from another 
				// location, fail the request.
				if((cookies.size() == 0) && (! tokenLocation.equals(TokenLocation.EITHER))) {
					LOGGER.info("Either a username and password or an authentication token are required.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Either a username and password or an authentication token are required.");
				}
				else if(cookies.size() == 1) {
					// Attempt to retrieve the user.
					tUser = UserBin.getUser(cookies.get(0));
					
					// If the bin doesn't know about the user, set the request as 
					// failed.
					if(tUser == null) {
						LOGGER.info("Unknown token.");
						setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The token is unknown.");
					}
				}
				// If there are multipile authentication token cookies, fail the
				// request.
				else if(cookies.size() > 1){
					LOGGER.info("Multiple authentication token cookies were found.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Multiple authentication token cookies were found.");
				}
			}
			
			// Now, if we haven't yet failed or authenticated the user, see if we
			// allow the token to be a parameter.
			if((tUser == null) && (! isFailed()) &&
			   (tokenLocation.equals(TokenLocation.PARAMETER) || tokenLocation.equals(TokenLocation.EITHER))) {
				// Retrieve all of the authentication tokens that were parameters.
				String[] tokens = httpRequest.getParameterValues(InputKeys.AUTH_TOKEN);
				
				if((tokens == null) || (tokens.length == 0)) {
					LOGGER.info("Either a username and password or an authentication token are required.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Either a username and password or an authentication token are required.");
				}
				else if(tokens.length == 1) {
					// Attempt to retrieve the user.
					tUser = UserBin.getUser(tokens[0]);
					
					// If the bin doesn't know about the user, set the request as 
					// failed.
					if(tUser == null) {
						LOGGER.info("Unknown token.");
						setFailed(ErrorCodes.AUTHENTICATION_FAILED, "The token is unknown.");
					}
				}
				else if(tokens.length > 1) {
					LOGGER.info("Multiple authentication token parameters were found.");
					setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Multiple authentication token parameters were found.");
				}
			}
		}
		
		// Retrieve the client parameter(s) from the request.
		String tClient = null;
		String[] clients = httpRequest.getParameterValues(InputKeys.CLIENT);
		
		// If there is no client, throw an error.
		if((clients == null) || (clients.length == 0)) {
			LOGGER.info("The client is missing from the request.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "Missing client.");
		}
		// If there are multiple clients, throw an error.
		else if(clients.length > 1) {
			LOGGER.info("More than one client was given.");
			setFailed(ErrorCodes.AUTHENTICATION_FAILED, "More than one client was given.");
		}
		else {
			// Save the client.
			tClient = clients[0];
			
			// Push the client into the logs.
			NDC.push("client=" + tClient);
		}
		
		user = tUser;
		client = tClient;
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
	 * Returns the client parameter from the request.
	 * 
	 * @return The client parameter from the request.
	 */
	public final String getClient() {
		return client;
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
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		return new HashMap<String, String[]>();
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
	 * @param value The value to assign to the second key.
	 */
	protected void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String key, Object value) {
		if(user != null) {
			final String token = user.getToken(); 
			if(token != null) {
				CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
			}
		}
		
		super.respond(httpRequest, httpResponse, key, value);
	}
}