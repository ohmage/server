package org.ohmage.servlet;

import javax.servlet.http.HttpServletRequest;

import org.ohmage.domain.AuthorizationToken;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * <p>
 * The root class for all Servlets.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class OhmageServlet {
    /**
     * Retrieves the auth token from the request's attributes.
     *
     * @param request
     *        The HTTP request.
     *
     * @return The decoded {@link AuthorizationToken} from the request or null
     *         if no token was found.
     */
    @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
    private AuthorizationToken getAuthToken(final HttpServletRequest request) {
        return
            (AuthorizationToken)
                request.getAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN);
    }
}