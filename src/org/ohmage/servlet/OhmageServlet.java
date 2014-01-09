package org.ohmage.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.format.DateTimeFormatter;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.ISOW3CDateTimeFormat;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.http.HttpHeaders;
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
    public static final long DEFAULT_NUM_TO_RETURN = 100;
    /**
     * The global default number of entities to return as a string.
     */
    public static final String DEFAULT_NUM_TO_RETURN_STRING = "100";

    /**
     * The header for the URL to the next set of data for list requests.
     */
    public static final String HEADER_COUNT = "Count";
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
        Logger.getLogger(OhmageServlet.class.getName());

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
    protected String buildRequestUrlRoot(
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
}