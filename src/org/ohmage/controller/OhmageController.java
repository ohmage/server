package org.ohmage.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.format.DateTimeFormatter;
import org.ohmage.bin.AuthorizationCodeBin;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.ISOW3CDateTimeFormat;
import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.user.User;
import org.ohmage.javax.servlet.filter.AuthFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * The root class for all Controllers.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class OhmageController {
    /**
     * The key for the earliest allowed time-stamp for some point when querying
     * a list of points.
     */
    public static final String PARAM_START_DATE = "start_date";
    /**
     * The key for the latest allowed time-stamp for some point when querying a
     * list of points.
     */
    public static final String PARAM_END_DATE = "end_date";

    /**
     * The parameter for whether or not the records should be returned in
     * chronological order.
     */
    public static final String PARAM_CHRONOLOGICAL = "chronological";
    /**
     * The default value for the {@link #PARAM_CHRONOLOGICAL} parameter.
     */
    public static final String PARAM_DEFAULT_CHRONOLOGICAL = "true";

    /**
     * The key for the number of elements to skip for requests that return a
     * list of results.
     */
    public static final String PARAM_PAGING_NUM_TO_SKIP = "num_to_skip";
    /**
     * The key for the number of elements to return for requests that return a
     * list of results.
     */
    public static final String PARAM_PAGING_NUM_TO_RETURN = "num_to_return";

    /**
     * The global default number of entities to skip.
     */
    public static final long DEFAULT_NUM_TO_SKIP = 0;
    /**
     * The global default number of entities to skip as a string.
     */
    public static final String DEFAULT_NUM_TO_SKIP_STRING = "0";
    /**
     * The global default number of entities to return.
     */
    public static final long DEFAULT_NUM_TO_RETURN = 50;
    /**
     * The global default number of entities to return as a string.
     */
    public static final String DEFAULT_NUM_TO_RETURN_STRING = "100";
    /**
     * The maximum number of results that may be returned by any API that
     * returns a list of items.
     */
    public static final long MAX_NUM_TO_RETURN = 100;

    /**
     * The header for the number of elements being returned in this response.
     */
    public static final String HEADER_COUNT = "Count";
    /**
     * The header for the total number of elements that in this list.
     */
    public static final String HEADER_TOTAL_COUNT = "Total-Count";
    /**
     * The header for the URL to the previous set of data for list requests.
     */
    public static final String HEADER_PREVIOUS = "Previous";
    /**
     * The header for the URL to the next set of data for list requests.
     */
    public static final String HEADER_NEXT = "Next";

    /**
     * The encoding for the previous and next URLs.
     */
    private static final String URL_ENCODING_UTF_8 = "UTF-8";

    /**
     * The date-time formatter to be used when parsing any date-times in
     * ohmage, unless a specific use-case dictates otherwise.
     */
    public static final DateTimeFormatter OHMAGE_DATE_TIME_FORMATTER =
        ISOW3CDateTimeFormat.any();

    /**
     * The attribute value for the request URL root.
     */
    protected static final String ATTRIBUTE_REQUEST_URL_ROOT =
        "request_url_root";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(OhmageController.class.getName());

    /**
     * Retrieves the user from the authorization token. Also, if the token was
     * granted via OAuth, the token's scope will be checked to ensure that it
     * matches the given scope.
     *
     * @param token
     *        The authorization token. A null check will be performed.
     *
     * @param scope
     *        The required scope for this token. If null, OAuth-based tokens
     *        will always be rejected.
     *
     * @return The user that corresponds to the token.
     *
     * @throws AuthenticationException
     *         The token was not given.
     *
     * @throws InsufficientPermissionsException
     *         The token was generated via OAuth and its scope is insufficient
     *         for the parameterized scope.
     *
     * @throws IllegalStateException
     *         The user associated with the token no longer exists.
     */
    protected static User validateAuthorization(
        final AuthorizationToken token,
        final Scope scope)
        throws
            AuthenticationException,
            InsufficientPermissionsException,
            IllegalStateException {

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(token == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Checking if this token was generated via OAuth.");
        if(token.getAuthorizationCode() != null) {
            LOGGER.log(Level.INFO, "This code was generated via OAuth.");

            LOGGER.log(Level.FINE, "Verifying that a scope was given.");
            if(scope == null) {
                throw
                    new InsufficientPermissionsException(
                        "This token does not grant its bearer sufficient " +
                            "access to the requested data.");
            }

            LOGGER
                .log(Level.INFO, "Retrieving the code that backs this token.");
            AuthorizationCode code =
                AuthorizationCodeBin
                    .getInstance()
                    .getCode(token.getAuthorizationCode());

            LOGGER
                .log(
                    Level.INFO,
                    "Verifying that this OAuth code grants the necessary " +
                        "permissions.");
            Set<Scope> scopes = code.getScopes();
            boolean found = false;
            for(Scope currScope : scopes) {
                if(currScope.covers(scope)) {

                    found = true;
                    break;
                }
            }
            if(! found) {
                throw
                    new InsufficientPermissionsException(
                        "This token does not grant its bearer sufficient " +
                            "access to the requested data.");
            }
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = UserBin.getInstance().getUser(token.getUserId());

        LOGGER.log(Level.INFO, "Verifying that the user still exists.");
        if(user == null) {
            throw
                new IllegalStateException(
                    "The user that is associated with this token no longer " +
                        "exists.");
        }

        LOGGER.log(Level.INFO, "Returning the user.");
        return user;
    }

    /**
     * Builds and returns the paging headers. This includes the number of
     * elements in the response as well as fully-qualified URLs to the previous
     * and next portions of the data.
     *
     * @param numToSkip
     *        The number of elements that was requested to be skipped.
     *
     * @param numToReturn
     *        The number of elements that was requested to be returned.
     *
     * @param parameters
     *        The servlet-specific parameters that should be added to the
     *        previous and next URLs.
     *
     * @param response
     *        A reference to the response to be used to determine if previous
     *        and/or next headers should be created and what their values
     *        should be.
     *
     * @param servletUrl
     *        The servlet-specific URL that was used to make the request.
     *
     * @return The paging headers.
     */
    protected static HttpHeaders buildPagingHeaders(
        final long numToSkip,
        final long numToReturn,
        final Map<String, String> parameters,
        final MultiValueResult<?> response,
        final String servletUrl) {

        // Build the result object to be updated below.
        HttpHeaders result = new HttpHeaders();

        // Add the count header.
        result.add(HEADER_COUNT, Long.toString(response.size()));

        // Add the total count header.
        result.add(HEADER_TOTAL_COUNT, Long.toString(response.count()));

        // Build the base URL.
        StringBuilder urlParamBuilder = new StringBuilder(servletUrl);

        // Add the query separator.
        urlParamBuilder.append('?');

        // Add each of the custom parameters.
        try {
            for(String parameterKey : parameters.keySet()) {
                urlParamBuilder
                    .append(
                        URLEncoder
                            .encode(parameterKey, URL_ENCODING_UTF_8))
                    .append('=')
                    .append(
                        URLEncoder
                            .encode(
                                parameters.get(parameterKey),
                                URL_ENCODING_UTF_8))
                    .append('&');
            }
        }
        catch(UnsupportedEncodingException e) {
            LOGGER
                .log(
                    Level.SEVERE,
                    "The encoding is unknown so the parameters could not be " +
                        "added to the previous or next headers.",
                    e);
            return result;
        }

        // If we skipped any data, create a Previous header.
        if(numToSkip > 0) {
            // Build the base URL.
            StringBuilder previousBuilder = new StringBuilder(urlParamBuilder);

            // Use a try-catch in case our encoding, which is the same for
            // each parameter, is unknown.
            try {
                // Calculate the previous number to skip.
                long previousNumToSkip =
                    numToSkip -
                        numToReturn;
                // If the previous number to skip is greater than zero, add
                // the number to skip.
                if(previousNumToSkip > 0) {
                    previousBuilder
                        .append(
                            URLEncoder
                                .encode(
                                    PARAM_PAGING_NUM_TO_SKIP,
                                    URL_ENCODING_UTF_8))
                        .append('=')
                        .append(
                            URLEncoder
                            .encode(
                                Long.toString(previousNumToSkip),
                                URL_ENCODING_UTF_8));
                    previousBuilder.append('&');
                }

                // Always add the number to return.
                previousBuilder
                    .append(
                        URLEncoder
                            .encode(
                                PARAM_PAGING_NUM_TO_RETURN,
                                URL_ENCODING_UTF_8))
                    .append('=')
                    .append(
                        URLEncoder
                        .encode(
                            Long.toString(
                                Math.min(
                                    numToSkip,
                                    numToReturn)),
                            URL_ENCODING_UTF_8));
            }
            catch(UnsupportedEncodingException e) {
                LOGGER
                    .log(
                        Level.SEVERE,
                        "The encoding is unknown so the " +
                            HEADER_PREVIOUS +
                            " header could not be built.",
                        e);
            }

            // Add the previous header.
            result.add(HEADER_PREVIOUS, previousBuilder.toString());
        }

        // If the total data-set size is greater than the number of points
        // skipped plus the number of points requested, then there must be more
        // data, and a Next header should be added.
        if(response.count() > (numToSkip + numToReturn)) {
            // Build the base URL.
            StringBuilder nextBuilder = new StringBuilder(urlParamBuilder);

            // Use a try-catch in case our encoding, which is the same for
            // each parameter, is unknown.
            try {
                // Calculate the previous number to skip.
                long nextNumToSkip = numToSkip + numToReturn;

                // Always add the number to skip.
                nextBuilder
                    .append(
                        URLEncoder
                            .encode(
                                PARAM_PAGING_NUM_TO_SKIP,
                                URL_ENCODING_UTF_8))
                    .append('=')
                    .append(
                        URLEncoder
                        .encode(
                            Long.toString(nextNumToSkip),
                            URL_ENCODING_UTF_8))
                    .append('&');

                // Always add the number to return.
                nextBuilder
                    .append(
                        URLEncoder
                            .encode(
                                PARAM_PAGING_NUM_TO_RETURN,
                                URL_ENCODING_UTF_8))
                    .append('=')
                    .append(
                        URLEncoder
                        .encode(
                            Long.toString(numToReturn),
                            URL_ENCODING_UTF_8));
            }
            catch(UnsupportedEncodingException e) {
                LOGGER
                    .log(
                        Level.SEVERE,
                        "The encoding is unknown so the " +
                            HEADER_NEXT +
                            " header could not be built.",
                        e);
            }

            // Add the previous header.
            result.add(HEADER_NEXT, nextBuilder.toString());
        }

        // Return the, possibly, updated headers.
        return result;
    }

    /**
     * Retrieves the auth token from the request's attributes.
     *
     * @param request
     *        The HTTP request.
     *
     * @return The decoded {@link AuthorizationToken} from the request.
     *
     * @throws AuthenticationException
     *         No authorization information was given.
     *
     * @throws IllegalStateException
     *         The request's authorization token was not an AuthorizationToken
     *         object.
     */
    @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
    private AuthorizationToken getAuthToken(final HttpServletRequest request) {
        return
            (AuthorizationToken)
                request.getAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN);
    }

    /**
     * Builds the request URL root. This is a URL that can be used as the basis
     * for subsequent redirect requests as it is built off of the current
     * request.
     *
     * @param httpRequest
     *        The current request.
     *
     * @return The request URL root of the form
     *         <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     */
    @ModelAttribute(ATTRIBUTE_REQUEST_URL_ROOT)
    private String buildRequestUrlRoot(
        final HttpServletRequest httpRequest) {

        // It must be a HTTP request.
        StringBuilder builder = new StringBuilder("http");

        // If security was used add the "s" to make it "https".
        boolean secure = false;
        if(httpRequest.isSecure()) {
            secure = true;
            builder.append('s');
        }

        // Add the protocol separator.
        builder.append("://");

        // Add the name of the server where the request was sent.
        builder.append(httpRequest.getServerName());

        // Add the port separator and the port.
        int port = httpRequest.getServerPort();
        if(!
            ((! secure) && (port == 80)) ||
            (secure && (port == 443))) {

            builder.append(':').append(port);
        }

        // Add the context path, e.g. "/ohmage".
        builder.append(httpRequest.getContextPath());

        // Return the root URL.
        return builder.toString();
    }

    /**
     * Retrieves and validates the number of elements to skip.
     *
     * @param numToSkip
     *        The number of elements to skip from the request.
     *
     * @return A valid number of elements to skip.
     */
    @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP)
    private long getNumToSkip(
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip) {

        LOGGER.log(Level.INFO, "Validating the number to skip.");
        if(numToSkip < 0) {
            throw
                new InvalidArgumentException(
                    "The number to skip must be greater than or equal to 0.");
        }

        return numToSkip;
    }

    /**
     * Retrieves and validates the number of elements to return.
     *
     * @param numToReturn
     *        The number of elements to return from the request.
     *
     * @return A valid number of elements to return.
     */
    @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN)
    private long getNumToReturn(
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn) {

        LOGGER.log(Level.INFO, "Validating the number to return.");
        if(numToReturn <= 0) {
            throw
                new InvalidArgumentException(
                    "The number to return must be greater than 0.");
        }
        LOGGER
            .log(
                Level.INFO,
                "Validating the upper bound of the number to return.");
        if(numToReturn > MAX_NUM_TO_RETURN) {
            throw
                new InvalidArgumentException(
                    "The number to return must be less than the upper limit " +
                        "of " +
                        MAX_NUM_TO_RETURN +
                        ".");
        }

        return numToReturn;
    }
}