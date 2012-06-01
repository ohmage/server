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

import java.io.IOException;
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
import org.ohmage.exception.InvalidRequestException;
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
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserRequest(HttpServletRequest httpRequest, boolean hashPassword) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest);

		User tUser = null;
		String tClient = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user request.");
			
			try {
				tUser = validateUser(hashPassword);
				tClient = validateClient();
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
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
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserRequest(
			final HttpServletRequest httpRequest, 
			final TokenLocation tokenLocation) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest);
		
		User tUser = null;
		String tClient = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user request.");
			
			try {
				tUser = validateToken(httpRequest, tokenLocation);
				tClient = validateClient();
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
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
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserRequest(
			final HttpServletRequest httpRequest,
			final TokenLocation tokenLocation, 
			final boolean hashPassword)
			throws IOException, InvalidRequestException {
		
		super(httpRequest);
		
		User tUser = null;
		String tClient = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user request.");
			
			try {
				try {
					tUser = validateUser(hashPassword);
				}
				catch(ValidationException e) {
					tUser = validateToken(httpRequest, tokenLocation);
				}
				
				tClient = validateClient();
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		user = tUser;
		client = tClient;
	}
	
	/**
	 * Creates a user request but does not decode the request for the 
	 * parameters. Instead it relys on the parameters given.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param tokenLocation Where to look for the token.
	 * 
	 * @param hashPassword Whether or not to hash the user's password.
	 * 
	 * @param parameters The parameters already read from the request.
	 */
	public UserRequest(
			final HttpServletRequest httpRequest,
			final TokenLocation tokenLocation,
			final boolean hashPassword,
			final Map<String, String[]> parameters) {
		
		super(parameters, httpRequest.getRemoteAddr());
		
		User tUser = null;
		String tClient = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user request.");
			
			try {
				try {
					tUser = validateUser(hashPassword);
				}
				catch(ValidationException e) {
					tUser = validateToken(httpRequest, tokenLocation);
				}
				
				tClient = validateClient();
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
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
		Map<String, String[]> result = super.getAuditInformation();
		
		if(! isFailed()) {
			String[] userArray = new String[1];
			userArray[0] = getUser().getUsername();
			result.put(InputKeys.USER, userArray);
		}
		
		return result;
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
	 * Refreshes the token cookie for the request.
	 * 
	 * @param httpResponse The HTTP response.
	 */
	protected void refreshTokenCookie(final HttpServletResponse httpResponse) {
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
		
		refreshTokenCookie(httpResponse);
		
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
		
		refreshTokenCookie(httpResponse);
		
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
	 * Validates that a user's credentials exist in the request.
	 * 
	 * @param hashPassword Whether or not to hash the user's password.
	 * 
	 * @return The user.
	 * 
	 * @throws ValidationException The user's credentials don't exist.
	 */
	private final User validateUser(
			final boolean hashPassword) 
			throws ValidationException {
		
		// Attempt to retrieve all usernames passed to the server.
		String[] usernames = getParameterValues(InputKeys.USER);
		
		// If it is missing, fail the request.
		if(usernames.length == 0) {
			throw new ValidationException(
				ErrorCode.AUTHENTICATION_FAILED,
				"The user is missing.");
		}
		// If there is more than one, fail the request.
		else if(usernames.length > 1) {
			throw new ValidationException(
				ErrorCode.AUTHENTICATION_FAILED, 
				"More than one user was given.");
		}
		else {
			// If exactly one username is found, attempt to retrieve all 
			// paswords sent to the server.
			String[] passwords = getParameterValues(InputKeys.PASSWORD);
			
			// If it is missing, fail the request.
			if(passwords.length == 0) {
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"The password is missing.");
			}
			// If there are more than one, fail the request.
			else if(passwords.length > 1) {
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"More than one password was given.");
			}
			else {
				// Attempt to create the new User object for this request.
				try {
					return new User(usernames[0], passwords[0], hashPassword);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.AUTHENTICATION_FAILED, 
						"The user and/or password are invalid.");
				}
			}
		}
	}
	
	/**
	 * Validates that a token exists and that it references an existing user.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param tokenLocation Where to look for the token.
	 * 
	 * @return The user based on the token found.
	 * 
	 * @throws ValidationException No token was found or it didn't reference
	 * 							   an existing token.
	 */
	private final User validateToken(
			final HttpServletRequest httpRequest,
			final TokenLocation tokenLocation) 
			throws ValidationException {
		
		// Validate the parameters.
		if(httpRequest == null) {
			throw new ValidationException(
				"The HTTP request was null.");
		}
		else if(tokenLocation == null) {
			throw new ValidationException(
				"The token location was null.");
		}
		
		// First, check if we allow it to be a cookie.
		if(tokenLocation.equals(TokenLocation.COOKIE) || 
			tokenLocation.equals(TokenLocation.EITHER)) {
			
			// Retrieve all of the authentication token cookies from the 
			// request.
			List<String> cookies = 
				CookieUtils.getCookieValues(
					httpRequest.getCookies(), 
					InputKeys.AUTH_TOKEN);
			
			// If there are no authentication token cookies and the
			// authentication token cannot be retrieved from another location,
			// fail the request.
			if((cookies.size() == 0) && 
				(! tokenLocation.equals(TokenLocation.EITHER))) {
				
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"The authentication token is missing as a cookie: " + 
						InputKeys.AUTH_TOKEN);
			}
			else if(cookies.size() == 1) {
				// Attempt to retrieve the user.
				User user = UserBin.getUser(cookies.get(0));
				
				// If the bin doesn't know about the user, set the request as
				// failed.
				if(user == null) {
					throw new ValidationException(
						ErrorCode.AUTHENTICATION_FAILED, 
						"The token cookie is unknown.");
				}
				
				return user;
			}
			// If there are multiple authentication token cookies, fail the
			// request.
			else if(cookies.size() > 1) {
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"Multiple authentication token cookies were given.");
			}
		}
		
		// At this point, it must have been either as a parameter or either and
		// no cookies were found. If it was anything else, we would have 
		// returned or thrown an exception.
		// Retrieve all of the authentication tokens that were parameters.
		String[] tokens = getParameterValues(InputKeys.AUTH_TOKEN);
		
		if(tokens.length == 0) {
			throw new ValidationException(
				ErrorCode.AUTHENTICATION_FAILED, 
				"The authentication token is missing as a parameter: " + 
					InputKeys.AUTH_TOKEN);
		}
		else if(tokens.length > 1){
			throw new ValidationException(
				ErrorCode.AUTHENTICATION_FAILED, 
				"Multiple authentication token parameters were found.");
		}
		else {
			// Attempt to retrieve the user.
			User user = UserBin.getUser(tokens[0]);
			
			// If the bin doesn't know about the user, set the request as 
			// failed.
			if(user == null) {
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"The token is unknown.");
			}
			
			return user;
		}
	}
	
	/**
	 * Validates the client value from the parameters.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @return The client value.
	 * 
	 * @throws ValidationException The client value was invalid.
	 */
	private final String validateClient() throws ValidationException {
		
		// Get the list of clients.
		String[] clients = getParameterValues(InputKeys.CLIENT);
		
		// If there is no client, throw an error.
		if(clients.length == 0) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_CLIENT, 
				"The client is missing.");
		}
		// If there are multiple clients, throw an error.
		else if(clients.length > 1) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_CLIENT, 
				"More than one client was given.");
		}
		else {
			// Get the client.
			String client = validateClient(clients[0]);
			
			// Push the client into the logs.
			NDC.push("client=" + client);
			
			return client;
		}
	}
	
	/**
	 * Validates that a client value is valid.
	 * 
	 * @param client The client value to be validated.
	 * 
	 * @return Returns the client value.
	 * 
	 * @throws ValidationException The client value was invalid.
	 */
	private final String validateClient(
			final String client)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_CLIENT,
				"The client is missing.");
		}
		
		if(client.length() > MAX_CLIENT_LENGTH) {
			throw new ValidationException(
				ErrorCode.SERVER_INVALID_CLIENT, 
				"The client value is too long.");
		}
		
		return client;
	}
}