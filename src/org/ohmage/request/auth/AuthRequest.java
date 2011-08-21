package org.ohmage.request.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.request.UserRequest;

/**
 * <p>An authentication request where the user's actual password is submitted
 * and, if correct, the hashed version of the password is returned. This hashed
 * password is what is used throughout the rest of the APIs.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username of the requester.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The plaintext password of the requester.</td>
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
public class AuthRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthRequest.class);

	private static final String KEY_HASHED_PASSWORD = "hashed_password";
	
	/**
	 * Creates this authentication request based on the information in the 
	 * httpRequest.
	 * 
	 * @param httpRequest A HttpServletRequest containing the parameters for
	 * 					  this request.
	 */
	public AuthRequest(HttpServletRequest httpRequest) {
		super(httpRequest, true);
		
		LOGGER.info("Building an username / password authentication request.");
	}

	/**
	 * Services the request. This simply authenticates the user.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the authentication request.");
		
		authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED);
	}

	/**
	 * Responds with either success and the user's hashed password or failure
	 * and an error message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to hte authentication request.");
		
		respond(httpRequest, httpResponse, KEY_HASHED_PASSWORD, (getUser() == null) ? null : getUser().getPassword());
	}
}