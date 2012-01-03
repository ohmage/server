package org.ohmage.request.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.cache.UserBin;
import org.ohmage.request.UserRequest;

public class AuthTokenLogoutRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(AuthTokenLogoutRequest.class);
	
	public AuthTokenLogoutRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		LOGGER.info("Creating a logout request.");
	}

	@Override
	public void service() {
		LOGGER.info("Servicing the logout request.");

		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		UserBin.expireUser(getUser().getToken());
	}

	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the logout request.");
		
		respond(httpRequest, httpResponse, null);
	}
}