package org.ohmage.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.ohmage.auth.provider.Provider;
import org.ohmage.auth.provider.ProviderRegistry;
import org.ohmage.bin.AuthenticationTokenBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.ProviderUserInformation;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.HttpStatusCodeExceptionResponder;
import org.ohmage.domain.exception.OhmageException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all requests for authentication tokens.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(AuthenticationTokenServlet.ROOT_MAPPING)
public class AuthenticationTokenServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/auth_token";

	/**
	 * The parameter key for user-names.
	 */
	public static final String PARAMETER_USERNAME = "username";
	/**
	 * The parameter key for passwords.
	 */
	public static final String PARAMETER_PASSWORD = "password";
	/**
	 * The parameter key for ohmage refresh tokens.
	 */
	public static final String PARAMETER_REFRESH_TOKEN = "refresh_token";
	/**
	 * The parameter key for provider identifiers.
	 */
	public static final String PARAMETER_PROVIDER = "provider";
	/**
	 * The parameter key for provider access tokens.
	 */
	public static final String PARAMETER_ACCESS_TOKEN = "access_token";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(AuthenticationTokenServlet.class.getName());

	/**
	 * <p>
	 * An exception when a user attempts to create
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class AccountNotRegisteredException
		extends OhmageException
		implements HttpStatusCodeExceptionResponder {

		/**
		 * The default serial version used for serializing an instance of this
		 * class.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception with only a reason.
		 *
		 * @param reason The reason this exception was thrown.
		 */
		public AccountNotRegisteredException(final String reason) {
			super(reason);
		}

		/**
		 * Creates a new exception with a reason and an underlying cause.
		 *
		 * @param reason The reason this exception was thrown.
		 *
		 * @param cause The underlying exception that caused this exception.
		 */
		public AccountNotRegisteredException(
			final String reason,
			final Throwable cause) {

			super(reason, cause);
		}

		/**
		 * @returns {@link HttpServletResponse#SC_PRECONDITION_FAILED}
		 */
		@Override
		public int getStatusCode() {
			return HttpServletResponse.SC_PRECONDITION_FAILED;
		}
	}

	/**
	 * Creates a new authentication token for a user using their ohmage
	 * credentials.
	 *
	 * @param username
	 *        The user's user-name.
	 *
	 * @param password
	 *        The user's password.
	 *
	 * @return A new authentication token for the user.
	 *
	 * @throws IllegalArgumentException
	 *         The user is unknown or the password is incorrect.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = { RequestMethod.GET, RequestMethod.POST },
		params = { PARAMETER_USERNAME, PARAMETER_PASSWORD })
	public static @ResponseBody AuthorizationToken getTokenFromOhmageAccount(
		@RequestParam(value = PARAMETER_USERNAME, required = true)
			final String username,
		@RequestParam(value = PARAMETER_PASSWORD, required = true)
			final String password) {

		LOGGER
			.log(
				Level.INFO,
				"Creating an authentication token from a username and " +
					"password.");

		// Create a universal response to make it less obvious as to why we are
		// rejecting the request.
		final String errorResponse = "Unknown user or incorrect password.";

		LOGGER
			.log(
				Level.INFO,
				"Retrieveing the user based on the username: " + username);
		User user = UserBin.getInstance().getUser(username);
		if(user == null) {
			LOGGER.log(Level.INFO, "The user is unknown: " + username);
			throw new AuthenticationException(errorResponse);
		}

		LOGGER.log(Level.INFO, "Validating the user's password.");
		if(user.getPassword() == null) {
			LOGGER
				.log(
					Level.INFO,
					"The user's account does not have a password.");
			throw
				new AuthenticationException(
					"The account uses a provider's access token for " +
						"authentication.");
		}
		else if(! user.verifyPassword(password)) {
			LOGGER.log(Level.INFO, "The given password is incorrect.");
			throw new AuthenticationException(errorResponse);
		}

		LOGGER.log(Level.INFO, "Creating a new authentication token.");
		AuthorizationToken token = new AuthorizationToken(user);

		LOGGER.log(Level.INFO, "Adding the authentication token to the bin.");
		AuthenticationTokenBin.getInstance().addToken(token);

		LOGGER.log(Level.INFO, "Returning the token to the user.");
		return token;
	}

	/**
	 * Creates a new authentication token for a user by validating a provider's
	 * access token and looking up the existing, associated ohmage account.
	 *
	 * @param providerId
	 *        The provider's internal identifier.
	 *
	 * @param accessToken
	 *        The access token the provider generated after the user
	 *        authenticated themselves.
	 *
	 * @return A new authentication token for the user.
	 *
	 * @throws IllegalArgumentException
	 *         The provider is unknown, the access token is invalid, or there
	 *         is no ohmage account associated with this provider-authenticated
	 *         user.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = { RequestMethod.GET, RequestMethod.POST },
		params = { PARAMETER_PROVIDER, PARAMETER_ACCESS_TOKEN })
	public static @ResponseBody AuthorizationToken getTokenFromProvider(
		@RequestParam(value = PARAMETER_PROVIDER, required = true)
			final String providerId,
		@RequestParam(value = PARAMETER_ACCESS_TOKEN, required = true)
			final String accessToken) {

		LOGGER
			.log(
				Level.INFO,
				"Creating an authentication token from a provider's access " +
					"token.");

		LOGGER
			.log(
				Level.FINE,
				"Retrieving the implementation for this provider.");
		Provider provider = ProviderRegistry.get(providerId);

		LOGGER
			.log(Level.INFO, "Retrieving the user based on the access token.");
		ProviderUserInformation newUserInformation =
			provider.getUserInformation(accessToken);

		LOGGER
			.log(
				Level.INFO,
				"Retrieving the ohmage account linked with the " +
					"provider-given ID.");
		User user =
			UserBin
				.getInstance()
				.getUserFromProvider(
					newUserInformation.getProviderId(),
					newUserInformation.getUserId());
		if(user == null) {
			LOGGER
				.log(
					Level.INFO,
					"The user has not yet linked this provider's " +
						"information to their ohmage account.");
			throw
				new AccountNotRegisteredException(
					"The user has not yet created an ohmage account.");
		}

		LOGGER
			.log(
				Level.FINE,
				"Pulling out the saved information from the provider.");
		ProviderUserInformation savedUserInformation =
			user.getProvider(newUserInformation.getProviderId());

		LOGGER
			.log(
				Level.FINE,
				"Determining if the user's information has been updated.");
		if(! newUserInformation.equals(savedUserInformation)) {
			LOGGER.log(Level.INFO, "Updating the user's information.");
			user = user.updateProvider(newUserInformation);

			LOGGER
				.log(
					Level.INFO,
					"Updating the user with the new information from the " +
						"provider.");
			UserBin.getInstance().updateUser(user);
		}

		LOGGER.log(Level.INFO, "Creating a new authentication token.");
		AuthorizationToken token = new AuthorizationToken(user);

		LOGGER.log(Level.INFO, "Adding the authentication token to the bin.");
		AuthenticationTokenBin.getInstance().addToken(token);

		LOGGER.log(Level.INFO, "Returning the token to the user.");
		return token;
	}

	/**
	 * Creates a new authentication token for a user using the ohmage refresh
	 * they were given when they last authenticated.
	 *
	 * @param refreshToken
	 *        The refresh token they were given from their last access token or
	 *        refresh token request.
	 *
	 * @return A new authentication token for the user.
	 *
	 * @throws IllegalArgumentException
	 *         The refresh token is unknown or has already been used.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = { RequestMethod.GET, RequestMethod.POST },
		params = { PARAMETER_REFRESH_TOKEN })
	public static @ResponseBody AuthorizationToken refreshToken(
		@RequestParam(value = PARAMETER_REFRESH_TOKEN, required = true)
			final String refreshToken) {

		LOGGER
			.log(
				Level.INFO,
				"Creating an authentication token from a refresh token.");

		LOGGER
			.log(
				Level.INFO,
				"Retrieveing the authentication token based on the refresh " +
					"token.");
		AuthorizationToken oldToken =
			AuthenticationTokenBin
				.getInstance()
				.getTokenFromRefreshToken(refreshToken);

		LOGGER.log(Level.FINE, "Ensuring that the refresh token is valid.");
		if(oldToken == null) {
			throw
				new AuthenticationException(
					"The given refresh token is unknown.");
		}

		LOGGER.log(Level.FINE, "Checking whether or not the token is valid.");
		LOGGER.log(Level.FINER, "Checking if the token was invalidated.");
		if(oldToken.wasInvalidated()) {
			throw
				new AuthenticationException(
					"This token has been invalidated.");
		}
		LOGGER.log(Level.FINER, "Checking if the token was invalidated.");
		if(oldToken.wasRefreshed()) {
			throw
				new AuthenticationException(
					"This token has already been refreshed.");
		}

		LOGGER.log(Level.INFO, "Creating a new authentication token.");
		AuthorizationToken token = new AuthorizationToken(oldToken);

		LOGGER.log(Level.INFO, "Adding the authentication token to the bin.");
		AuthenticationTokenBin.getInstance().addToken(token);

		LOGGER.log(Level.INFO, "Invalidating the old token.");
		AuthenticationTokenBin.getInstance().updateToken(oldToken);

		LOGGER.log(Level.INFO, "Returning the token to the user.");
		return token;
	}

	/**
	 * Invalidates an authentication token. This would most likely be used on
	 * logout.
     *
     * @param authHeader
     *        The Authorization header with the corresponding authorization
     *        token.
	 *
	 * @throws IllegalArgumentException
	 *         The authentication was not given or was not given as a
	 *         parameter.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.DELETE)
	public static @ResponseBody void invalidateAuthToken(
        @RequestHeader(AuthorizationToken.HEADER_AUTHORIZATION)
            final String authHeader)
		throws IllegalArgumentException {

        LOGGER.log(Level.INFO, "Retrieving the authorization token.");
        AuthorizationToken token =
            AuthenticationTokenBin
                .getInstance()
                .getTokenFromAccessToken(
                    AuthorizationToken.getTokenFromHeader(authHeader));

        if(token == null) {
            LOGGER.log(Level.INFO, "The token doesn't exist. Returning.");
            return;
        }

        LOGGER.log(Level.INFO, "Invalidating the token.");
		token.invalidate();

		LOGGER.log(Level.INFO, "Updating the token.");
		AuthenticationTokenBin.getInstance().updateToken(token);
	}
}