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
package org.ohmage.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.User;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.service.AuthenticationService;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * A request that contains a User object and a client String that represents
 * how the requester is making this request.
 * 
 * @author John Jenkins
 */
public abstract class UserRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(UserRequest.class);
	
	protected static enum TokenLocation { COOKIE, PARAMETER, EITHER };
	protected static enum AllowNewAccount { NEW_ACCOUNT_ALLOWED, NEW_ACCOUNT_DISALLOWED };
	
	protected static final long MILLIS_IN_A_SECOND = 1000;
	
	private static final int MAX_CLIENT_LENGTH = 255;
	
	private final User user;
	private final String client;
	
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
		super(httpRequest);
		
		if(isFailed()) {
			user = null;
			client = null;
			
			return;
		}
		
		User tUser = null;
		
		// Attempt to retrieve all usernames passed to the server.
		String[] usernames = getParameterValues(InputKeys.USER);
		
		// If it is missing, fail the request.
		if(usernames.length == 0) {
			LOGGER.info("The user is missing from the request.");
			setFailed(ErrorCode.AUTHENTICATION_FAILED, "Missing user.");
		}
		// If there is more than one, fail the request.
		else if(usernames.length > 1) {
			LOGGER.info("More than one user was given.");
			setFailed(ErrorCode.AUTHENTICATION_FAILED, "More than one user was given.");
		}
		else {
			// If exactly one username is found, attempt to retrieve all 
			// paswords sent to the server.
			String[] passwords = getParameterValues(InputKeys.PASSWORD);
			
			// If it is missing, fail the request.
			if(passwords.length == 0) {
				LOGGER.info("The password is missing from the request.");
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "Missing password.");
			}
			// If there are more than one, fail the request.
			else if(passwords.length > 1) {
				LOGGER.info("More than one password was given.");
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "More than one password was given.");
			}
			else {
				// Attempt to create the new User object for this request.
				try {
					tUser = new User(usernames[0], passwords[0], hashPassword);
				}
				catch(DomainException e) {
					LOGGER.info("The user and/or password are invalid.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "The user and/or password are invalid.");
				}
			}
		}
		
		// Retrieve the client parameter(s) from the request.
		String tClient = null;
		String[] clients = getParameterValues(InputKeys.CLIENT);
		
		if(! isFailed()) {
			// If there is no client, throw an error.
			if(clients.length == 0) {
				LOGGER.info("The client is missing from the request.");
				setFailed(ErrorCode.SERVER_INVALID_CLIENT, "Missing client.");
			}
			// If there are multiple clients, throw an error.
			else if(clients.length > 1) {
				LOGGER.info("More than one client was given.");
				setFailed(ErrorCode.SERVER_INVALID_CLIENT, "More than one client was given.");
			}
			else {
				// Save the client.
				try {
					tClient = validateClient(clients[0]);
					
					// Push the client into the logs.
					NDC.push("client=" + tClient);
				}
				catch(ValidationException e) {
					LOGGER.info(e.toString());
				}
			}
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
		super(httpRequest);
		
		if(isFailed()) {
			user = null;
			client = null;
			
			return;
		}
		
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
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "The authentication token is missing as a cookie: " + InputKeys.AUTH_TOKEN);
			}
			else if(cookies.size() == 1) {
				// Attempt to retrieve the user.
				tUser = UserBin.getUser(cookies.get(0));
				
				// If the bin doesn't know about the user, set the request as 
				// failed.
				if(tUser == null) {
					LOGGER.info("Unknown token.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "The token is unknown.");
				}
			}
			// If there are multipile authentication token cookies, fail the
			// request.
			else if(cookies.size() > 1) {
				LOGGER.info("Multiple authentication token cookies were found.");
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "Multiple authentication token cookies were found.");
			}
		}
		
		// Now, if we haven't yet failed or authenticated the user, see if we
		// allow the token to be a parameter.
		if((tUser == null) && (! isFailed()) &&
		   (tokenLocation.equals(TokenLocation.PARAMETER) || tokenLocation.equals(TokenLocation.EITHER))) {
			// Retrieve all of the authentication tokens that were parameters.
			String[] tokens = getParameterValues(InputKeys.AUTH_TOKEN);
			
			if(tokens.length == 0) {
				LOGGER.info("The authentication token is missing.");
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "The authentication token is missing as a parameter: " + InputKeys.AUTH_TOKEN);
			}
			else if(tokens.length == 1) {
				// Attempt to retrieve the user.
				tUser = UserBin.getUser(tokens[0]);
				
				// If the bin doesn't know about the user, set the request as 
				// failed.
				if(tUser == null) {
					LOGGER.info("Unknown token.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "The token is unknown.");
				}
			}
			else if(tokens.length > 1){
				LOGGER.info("Multiple authentication token parameters were found.");
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "Multiple authentication token parameters were found.");
			}
		}
		
		// Retrieve the client parameter(s) from the request.
		String tClient = null;
		String[] clients = getParameterValues(InputKeys.CLIENT);

		if(! isFailed()) {
			// If there is no client, throw an error.
			if(clients.length == 0) {
				LOGGER.info("The client is missing from the request.");
				setFailed(ErrorCode.SERVER_INVALID_CLIENT, "Missing client.");
			}
			// If there are multiple clients, throw an error.
			else if(clients.length > 1) {
				LOGGER.info("More than one client was given.");
				setFailed(ErrorCode.SERVER_INVALID_CLIENT, "More than one client was given.");
			}
			else {
				// Save the client.
				try {
					tClient = validateClient(clients[0]);
					
					// Push the client into the logs.
					NDC.push("client=" + tClient);
				}
				catch(ValidationException e) {
					LOGGER.info(e.toString());
				}
			}
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
		super(httpRequest);
		
		if(isFailed()) {
			user = null;
			client = null;
			
			return;
		}
		
		User tUser = null;
		
		// A flag to indicate whether or not we should look for the token after
		// looking for a username and password.
		boolean getToken = false;
		
		// Attempt to retrieve all usernames passed to the server.
		String[] usernames = getParameterValues(InputKeys.USER);
		
		// If it is missing, search for a token.
		if(usernames.length == 0) {
			getToken = true;
		}
		// If there is more than one, fail the request.
		else if(usernames.length > 1) {
			LOGGER.info("More than one user was given.");
			setFailed(ErrorCode.AUTHENTICATION_FAILED, "More than one user was given.");
		}
		else {
			// If exactly one username is found, attempt to retrieve all 
			// paswords sent to the server.
			String[] passwords = getParameterValues(InputKeys.PASSWORD);
			
			// If it is missing, fail the request.
			if(passwords.length == 0) {
				getToken = true;
			}
			// If there are more than one, fail the request.
			else if(passwords.length > 1) {
				LOGGER.info("More than one password was given.");
				setFailed(ErrorCode.AUTHENTICATION_FAILED, "More than one password was given.");
			}
			else {
				// Attempt to create the new User object for this request.
				try {
					tUser = new User(usernames[0], passwords[0], hashPassword);
				}
				catch(DomainException e) {
					LOGGER.info("The user and/or password are invalid.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "The user and/or password are invalid.");
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
					LOGGER.info("Either a user and password or an authentication token are required.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "Either a user and password or an authentication token are required.");
				}
				else if(cookies.size() == 1) {
					// Attempt to retrieve the user.
					tUser = UserBin.getUser(cookies.get(0));
					
					// If the bin doesn't know about the user, set the request as 
					// failed.
					if(tUser == null) {
						LOGGER.info("Unknown token.");
						setFailed(ErrorCode.AUTHENTICATION_FAILED, "The token is unknown.");
					}
				}
				// If there are multipile authentication token cookies, fail the
				// request.
				else if(cookies.size() > 1){
					LOGGER.info("Multiple authentication token cookies were found.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "Multiple authentication token cookies were found.");
				}
			}
			
			// Now, if we haven't yet failed or authenticated the user, see if we
			// allow the token to be a parameter.
			if((tUser == null) && (! isFailed()) &&
			   (tokenLocation.equals(TokenLocation.PARAMETER) || tokenLocation.equals(TokenLocation.EITHER))) {
				// Retrieve all of the authentication tokens that were parameters.
				String[] tokens = getParameterValues(InputKeys.AUTH_TOKEN);
				
				if(tokens.length == 0) {
					LOGGER.info("Either a user and password or an authentication token are required.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "Either a user and password or an authentication token are required.");
				}
				else if(tokens.length == 1) {
					// Attempt to retrieve the user.
					tUser = UserBin.getUser(tokens[0]);
					
					// If the bin doesn't know about the user, set the request as 
					// failed.
					if(tUser == null) {
						LOGGER.info("Unknown token.");
						setFailed(ErrorCode.AUTHENTICATION_FAILED, "The token is unknown.");
					}
				}
				else if(tokens.length > 1) {
					LOGGER.info("Multiple authentication token parameters were found.");
					setFailed(ErrorCode.AUTHENTICATION_FAILED, "Multiple authentication token parameters were found.");
				}
			}
		}
		
		
		// Retrieve the client parameter(s) from the request.
		String tClient = null;
		String[] clients = getParameterValues(InputKeys.CLIENT);

		if(! isFailed()) {
			// If there is no client, throw an error.
			if(clients.length == 0) {
				LOGGER.info("The client is missing from the request.");
				setFailed(ErrorCode.SERVER_INVALID_CLIENT, "Missing client.");
			}
			// If there are multiple clients, throw an error.
			else if(clients.length > 1) {
				LOGGER.info("More than one client was given.");
				setFailed(ErrorCode.SERVER_INVALID_CLIENT, "More than one client was given.");
			}
			else {
				// Save the client.
				try {
					tClient = validateClient(clients[0]);
					
					// Push the client into the logs.
					NDC.push("client=" + tClient);
				}
				catch(ValidationException e) {
					LOGGER.info(e.toString());
				}
			}
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
	 * Authenticates the user in the request.
	 * 
	 * @param newAccountsAllowed Whether or not new accounts are allowed to
	 * 							 make this call.
	 * 
	 * @return Returns true if the user was successfully authenticated; false
	 * 		   otherwise.
	 */
	public final boolean authenticate(AllowNewAccount newAccountsAllowed) {
		try {
			// Validate that the username and password are valid.
			LOGGER.info("Authenticating the user.");
			return AuthenticationService.instance().authenticate(
					this, 
					AllowNewAccount.NEW_ACCOUNT_ALLOWED.equals(newAccountsAllowed));
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
	
	/**************************************************************************
	 *  Begin JEE Requirements
	 *************************************************************************/
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
	 * Generates the success/fail response for the user with an additional key-
	 * value pair. It also adds a Set-Cookie header in the response for the 
	 * authentication / session token if one exists.
	 * 
	 * @param httpRequest The HTTP request that began this exchange.
	 * 
	 * @param httpResponse The HTTP response back to the requester.
	 * 
	 * @param key The second to key to include when the request succeeds. The
	 * 			  first will be {@link Request#JSON_KEY_RESULT}.
	 * 
	 * @param value The value to assign to the second key.
	 */
	protected void respond(
			final HttpServletRequest httpRequest, 
			final HttpServletResponse httpResponse, 
			final String key, 
			final Object value) {
		
		if(user != null) {
			final String token = user.getToken(); 
			if(token != null) {
				CookieUtils.setCookieValue(
						httpResponse, 
						InputKeys.AUTH_TOKEN, 
						token, 
						(int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
			}
		}
		
		JSONObject response = new JSONObject();
		
		try {
			response.put(key, value);
		}
		catch(JSONException e) {
			LOGGER.error("Error building response object.", e);
			setFailed();
		}
		
		super.respond(httpRequest, httpResponse, response);
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
	 * 					   {@link Request#JSON_KEY_DATA} to the requester.
	 */
	protected void respond(
			final HttpServletRequest httpRequest, 
			final HttpServletResponse httpResponse, 
			final JSONObject jsonResponse) {
		
		respond(httpRequest, httpResponse, (JSONObject) null, jsonResponse);
	}
	
	/**
	 * Generates the response for the user that contains metadata and the 
	 * actual data to return to the user.
	 * 
	 * @param httpRequest The HTTP request that began this request.
	 * 
	 * @param httpResponse The HTTP response back to the requester.
	 * 
	 * @param metadata The metadata to include in the response with the key
	 * 				   {@link Request#JSON_KEY_METADATA}.
	 * 
	 * @param data The data to include in the response with the key
	 * 			   {@link Request#JSON_KEY_DATA}.
	 */
	protected void respond(
			final HttpServletRequest httpRequest, 
			final HttpServletResponse httpResponse, 
			final JSONObject metadata, 
			final JSONObject data) {
		
		if(user != null) {
			final String token = user.getToken(); 
			if(token != null) {
				CookieUtils.setCookieValue(
						httpResponse, 
						InputKeys.AUTH_TOKEN, 
						token, 
						(int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
			}
		}
		
		JSONObject response = new JSONObject();
		try {
			response.put(JSON_KEY_METADATA, metadata);
			response.put(JSON_KEY_DATA, data);
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the response.", e);
			setFailed();
		}
		super.respond(httpRequest, httpResponse, response);
	}
	/**************************************************************************
	 *  End JEE Requirements
	 *************************************************************************/
	
	/**
	 * Validates that a client value is valid.
	 * 
	 * @param client The client value to be validated.
	 * 
	 * @return Returns null if the client value is null or whitespace only;
	 * 		   otherwise, it returns the client value.
	 * 
	 * @throws ValidationException Thrown if the client value is not null, not
	 * 							   whitespace only, and not a valid client
	 * 							   value.
	 */
	private final String validateClient(String client) throws ValidationException {
		if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			return null;
		}
		
		if(client.length() > MAX_CLIENT_LENGTH) {
			setFailed(ErrorCode.SERVER_INVALID_CLIENT, "The client value is too long.");
			throw new ValidationException("The client value is too long.");
		}
		
		return client;
	}
}