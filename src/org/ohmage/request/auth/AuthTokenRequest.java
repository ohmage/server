package org.ohmage.request.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.cache.UserBin;
import org.ohmage.request.UserRequest;

/**
 * <p>Uses the username and password parameters to create a request for an
 * authentication / session token.</p>
 * 
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username of the user that is attempting to login.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The password of the user that is attempting to login.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class AuthTokenRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthTokenRequest.class);
	
	private static final String KEY_AUTH_TOKEN = "token";
	
	/**
	 * A request for an authentication token.
	 * 
	 * @param httpRequest The HTTP request containing the parameters.
	 */
	public AuthTokenRequest(HttpServletRequest httpRequest) {
		super(httpRequest, true);
		
		LOGGER.info("Building an authentication token request.");
	}

	/**
	 * Authenticates the request with the username and password that were 
	 * passed in as parameters, and, if successful, adds the user to the bin 
	 * generating a token.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the authentication token request.");

		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		UserBin.addUser(getUser());
	}

	/**
	 * Replies to the request with no data and instantiates the token in the
	 * request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		respond(httpRequest, httpResponse, KEY_AUTH_TOKEN, (getUser() == null) ? null : getUser().getToken());
	}
}