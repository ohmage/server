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
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.User;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.jee.filter.ClientFilter;
import org.ohmage.service.AuthenticationService;
import org.ohmage.util.CookieUtils;

/**
 * A request that contains a User object and a client String that represents
 * how the requester is making this request.
 * 
 * @author John Jenkins
 */
public abstract class UserRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(UserRequest.class);
	
	public static enum TokenLocation { COOKIE, PARAMETER, EITHER };
	protected static enum AllowNewAccount { NEW_ACCOUNT_ALLOWED, NEW_ACCOUNT_DISALLOWED };
	
	protected static final long MILLIS_IN_A_SECOND = 1000;
	
	private final User user;
	private final String client;
	
	/**
	 * Creates a user request based on the information in the HTTP request. All
	 * parameters except the HTTP request are optional and dictate where to 
	 * search for the authentication information.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param hashPassword Whether or not to hash the password in the request.
	 * 					   If this is null, the request will not use the 
	 * 					   username and password for authentication.
	 * 
	 * @param tokenLocation Where to search for the authentication token. If
	 * 						this is null, the request will not use the 
	 * 						authentication token for authentication.
	 * 
	 * @param parameters A preset map of parameters. If this is null, the 
	 * 					 parameters are decoded from the HTTP request. 
	 * 					 Otherwise, these parameters are used.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public UserRequest(
			final HttpServletRequest httpRequest,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final Map<String, String[]> parameters) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, parameters);
		
		User tUser = null;
		String tClient = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user request.");
			
			try {
				if(hashPassword != null) { 
					tUser = retrieveUser(hashPassword);
				}
				
				if((tokenLocation != null) && (tUser == null)) {
					tUser = retrieveToken(httpRequest, tokenLocation);
				}
				
				if(tUser == null) {
					throw new ValidationException(
						ErrorCode.AUTHENTICATION_FAILED,
						"Authentication credentials were not provided.");
				}
				
				tClient = retrieveClient(httpRequest, false);
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
	 * This is a slight variation on 
	 * {@link #UserRequest(HttpServletRequest, Boolean, TokenLocation, Map)} 
	 * for OMH requests. There is a slight discrepancy between what they call
	 * their 'client' parameter; this is fixed by this call which explicitly
	 * requests the client value rather than pulling it from the request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param hashPassword Whether or not to hash the password in the request.
	 * 					   If this is null, the request will not use the 
	 * 					   username and password for authentication.
	 * 
	 * @param tokenLocation Where to search for the authentication token. If
	 * 						this is null, the request will not use the 
	 * 						authentication token for authentication.
	 * 
	 * @param parameters A preset map of parameters. If this is null, the 
	 * 					 parameters are decoded from the HTTP request. 
	 * 					 Otherwise, these parameters are used.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public UserRequest(
			final HttpServletRequest httpRequest,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final Map<String, String[]> parameters,
			final boolean callClientRequester) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, parameters);
		
		User tUser = null;
		String tClient = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user request.");
			
			try {
				if(hashPassword != null) { 
					tUser = retrieveUser(hashPassword);
				}
				
				if((tokenLocation != null) && (tUser == null)) {
					tUser = retrieveToken(httpRequest, tokenLocation);
				}
				
				if(tUser == null) {
					throw new ValidationException(
						ErrorCode.AUTHENTICATION_FAILED,
						"Authentication credentials were not provided.");
				}
				
				tClient = retrieveClient(httpRequest, callClientRequester);
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
			LOGGER.info("HT: Authenticating the user: " + getUser().getUsername());
			return
				AuthenticationService
					.instance()
					.authenticate(
						this, 
						AllowNewAccount
							.NEW_ACCOUNT_ALLOWED
							.equals(newAccountsAllowed));
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
		
		if(user != null) {
			String[] userArray = new String[1];
			userArray[0] = user.getUsername();
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
	public void refreshTokenCookie(final HttpServletResponse httpResponse) {
		if(user != null) {
			String userToken = user.getToken();
			if(userToken != null) {
				User binUser = UserBin.getUser(userToken);
				if(binUser != null) {
					final String token = binUser.getToken(); 
					if(token != null) {
						CookieUtils.setCookieValue(
							httpResponse, 
							InputKeys.AUTH_TOKEN, 
							token);
					}
				}
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
	 * Redirects the user to a different URL. If the request failed, setting
	 * the status code fails, or the redirect URL is null, a standard JSON
	 * respnose is returned.
	 * 
	 * @param httpRequest The HTTP request that began this exchange.
	 * 
	 * @param httpResponse The HTTP response back to the requester.
	 * 
	 * @param redirect The URL to redirect the user to if the request was
	 * 				   successful.
	 */
	protected void respond(
			final HttpServletRequest httpRequest, 
			final HttpServletResponse httpResponse,
			final URL redirect) {
		
		refreshTokenCookie(httpResponse);

		boolean respond = true;
		if(redirect == null) {
			LOGGER.error("A null redirect was given.");
			setFailed();
		}
		else {
			try {
				httpResponse.sendRedirect(redirect.toString());
				respond = false;
			}
			catch(IOException e) {
				LOGGER.info("Could not respond to the user.", e);
				setFailed();
			}
		}
		
		if(respond) {
			super.respond(httpRequest, httpResponse, null);
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
	 * Retrieves the user's credentials from the request and creates a User
	 * object or returns null if the credentials didn't exist.
	 * 
	 * @param hashPassword Whether or not to hash the user's password.
	 * 
	 * @return A User object generated from the username and password or null
	 * 		   if insufficient information was given. 
	 * 
	 * @throws ValidationException The user's credentials were given but were
	 * 							   invalid.
	 */
	private final User retrieveUser(
			final boolean hashPassword) 
			throws ValidationException {
		
		// Attempt to retrieve all usernames passed to the server.
		String[] usernames = getParameterValues(InputKeys.USER);
		
		// If there is more than one, fail the request.
		if(usernames.length > 1) {
			throw new ValidationException(
				ErrorCode.AUTHENTICATION_FAILED, 
				"More than one user was given.");
		}
		else if(usernames.length == 1) {
			// If exactly one username is found, attempt to retrieve all 
			// paswords sent to the server.
			String[] passwords = getParameterValues(InputKeys.PASSWORD);
			
			// If there are more than one, fail the request.
			if(passwords.length > 1) {
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"More than one password was given.");
			}
			else if(passwords.length == 1) {
				// Attempt to create the new User object for this request.
				try {
					return new User(usernames[0], passwords[0], hashPassword);
				}
				catch(DomainException e) {
					throw new ValidationException(
						ErrorCode.AUTHENTICATION_FAILED, 
						"The user and/or password are invalid.",
						e);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if a token exists in any of the places specified. If so, it will
	 * attempt to retrieve the user associated with it and thrown an exception
	 * if no such user exists. If not, it will return null.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param tokenLocation Where to look for the token.
	 * 
	 * @return The user based on the token found or null if no tokens were
	 * 		   supplied.
	 * 
	 * @throws ValidationException The token doesn't exist.
	 */
	protected final User retrieveToken(
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
		
		// Check if it is allowed to be a parameter.
		if(TokenLocation.PARAMETER.equals(tokenLocation) ||
			TokenLocation.EITHER.equals(tokenLocation)) {
			
			// Retrieve all of the authentication tokens that were parameters.
			String[] tokens = getParameterValues(InputKeys.AUTH_TOKEN);
	
			if(tokens.length > 1){
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"Multiple authentication token parameters were found.");
			}
			else if(tokens.length == 1) {
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
		
		// First, check if we allow it to be a cookie.
		if(tokenLocation.equals(TokenLocation.COOKIE) || 
			tokenLocation.equals(TokenLocation.EITHER)) {
			
			// Retrieve all of the authentication token cookies from the 
			// request.
			List<String> cookies = 
				CookieUtils.getCookieValues(
					httpRequest.getCookies(), 
					InputKeys.AUTH_TOKEN);
			
			// If there are multiple authentication token cookies, fail the
			// request.
			 if(cookies.size() > 1) {
				throw new ValidationException(
					ErrorCode.AUTHENTICATION_FAILED, 
					"Multiple authentication token cookies were given.");
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
		}
		
		// If it didn't exist as a parameter or as a cookie, return null.
		return null;
	}
	
	/**
	 * Retrieves the client value as an attribute to the request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @return The client value.
	 * 
	 * @throws ValidationException The client value was missing.
	 */
	private final String retrieveClient(
			final HttpServletRequest httpRequest,
			final boolean callClientRequester)
			throws ValidationException {
		
		String client = null;
		
		// Get the client object as an attribute.
		Object clientObject =
			httpRequest
				.getAttribute(ClientFilter.ATTRIBUTE_KEY_CLIENT);
		
		// Ensure that the client is a string.
		if(clientObject instanceof String) {
			client = (String) clientObject; 
		}
		
		// If the 'clientObject' was null or not a string, fail the request 
		// indicating that it must have been missing. If it was invalid, it 
		// would have been rejected in the filter.
		if(client == null) {
			throw
				new ValidationException(
					ErrorCode.SERVER_INVALID_CLIENT,
					"The '" +
						((callClientRequester) ? "requester" : "client") +
						"' value is missing.");
		}
		
		// Return the client value.
		return client;
	}
}