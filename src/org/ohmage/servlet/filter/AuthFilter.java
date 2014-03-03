package org.ohmage.servlet.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.ohmage.bin.AuthorizationTokenBin;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;

/**
 * <p>
 * This filter is responsible for retrieving the authorization information,
 * validating it, and associating it with the request.
 * </p>
 *
 * @author John Jenkins
 */
public class AuthFilter implements Filter {
	/**
	 * The Authorization header from HTTP requests.
	 */
	public static final String HEADER_AUTHORIZATION = "Authorization";

	/**
	 * The attribute for an authenticated auth token from the request.
	 */
	public static final String ATTRIBUTE_AUTH_TOKEN = "auth_token";

	/**
	 * The logger for this filter.
	 */
	private static final Logger LOGGER =
	    Logger.getLogger(AuthFilter.class.getName());

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
    @Override
    public void init(final FilterConfig config) throws ServletException {
        // Do nothing.
    }

	/**
	 * @throws AuthenticationException
	 *         The auth token is unknown or invalid.
	 */
	@Override
	public void doFilter(
		final ServletRequest request,
		final ServletResponse response,
		final FilterChain chain)
		throws IOException, ServletException, AuthenticationException {

	    // Get the authorization headers.
	    Enumeration<String> authorizationHeaders;
	    if(request instanceof HttpServletRequest) {
	        authorizationHeaders =
	            ((HttpServletRequest) request)
	                .getHeaders(HEADER_AUTHORIZATION);
	    }
	    else {
	        authorizationHeaders = Collections.emptyEnumeration();
	    }

	    // Parse the headers.
	    String authTokenString = null;
	    while(authorizationHeaders.hasMoreElements()) {
	        // Parse the next header.
	        String authorizationHeader = authorizationHeaders.nextElement();

	        // Split the body into its parts.
	        String[] authHeaderParts = authorizationHeader.split(" ");

	        // If there are no parts, then error out.
	        if(authHeaderParts.length == 0) {
	            throw
	                new InsufficientPermissionsException(
	                    "The Authorization header is empty.");
	        }

	        // Check that the first part of the header is 'ohmage'.
	        if(! "ohmage".equals(authHeaderParts[0])) {
	            throw
	                new InsufficientPermissionsException(
	                    "The auth header is not for 'ohmage'.");
	        }

	        // Ensure that there is a corresponding key.
	        if(authHeaderParts.length < 2) {
                throw
                    new InsufficientPermissionsException(
                        "The auth token is missing in the Authorization " +
                            "header.");
	        }
	        else if(authHeaderParts.length == 2) {
	            if(authTokenString == null) {
	                authTokenString = authHeaderParts[1];
	            }
	            else if(! authTokenString.equals(authHeaderParts[1])) {
	                throw
	                    new AuthenticationException(
	                        "Multiple Authorization headers were given with " +
	                            "different token values.");
	            }
	        }
	        else {
	            throw
	                new AuthenticationException(
	                    "The Authorization header has too many parts.");
	        }
	    }

		// If we found an auth token, look it up.
		AuthorizationToken authTokenObject = null;
		if(authTokenString != null) {
			// Attempt to get the authentication token.
			authTokenObject =
				AuthorizationTokenBin
					.getInstance()
					.getTokenFromAccessToken(authTokenString);
			if(authTokenObject == null) {
				throw
					new AuthenticationException(
						"The authentication token is unknown.");
			}

			// Ensure that the authentication token is valid.
			if(! authTokenObject.isValid()) {
				throw
					new AuthenticationException(
						"This token is no longer valid.");
			}

			// Log that we found a valid token.
            LOGGER.log(Level.INFO, "A valid auth token was found.");
		}
		// Associate the authentication token with the request.
		request.setAttribute(ATTRIBUTE_AUTH_TOKEN, authTokenObject);

		// Continue along the filter chain.
		chain.doFilter(request, response);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// Do nothing.
	}
}