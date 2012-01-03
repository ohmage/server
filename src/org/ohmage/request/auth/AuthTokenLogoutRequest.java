package org.ohmage.request.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.cache.UserBin;
import org.ohmage.request.UserRequest;

/**
 * <p>Removes the user's authentication token from the bin.</p>
 * 
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
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
public class AuthTokenLogoutRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthTokenLogoutRequest.class);
	
	/**
	 * Creates a request for deleting the user's authentication token from the
	 * bin.
	 * 
	 * @param httpRequest The HTTP request containing the parameters.
	 */
	public AuthTokenLogoutRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating a logout request.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the logout request.");

		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		UserBin.expireUser(getUser().getToken());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the logout request.");
		
		respond(httpRequest, httpResponse, null);
	}
}