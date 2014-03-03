package org.ohmage.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import name.jenkins.paul.john.concordia.Concordia;

import org.joda.time.DateTime;
import org.ohmage.bin.AuthorizationTokenBin;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.MultiValueResultAggregation;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.ColumnList;
import org.ohmage.domain.Schema;
import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.domain.user.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all Open mHealth requests.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(OmhServlet.ROOT_MAPPING)
public class OmhServlet extends OhmageServlet {
    /**
     * <p>
     * The Open mHealth representation of schema IDs.
     * <p>
     *
     * @author John Jenkins
     */
    private static class OmhSchemaId {
        /**
         * <p>
         * The different types of Open mHealth schema IDs based on ohmage's
         * different data types.
         * </p>
         *
         * @author John Jenkins
         */
        public static enum Type {
            /**
             * An Open mHealth schema ID based off of an ohmage stream.
             */
            STREAM,
            /**
             * An Open mHealth schema ID based off of an ohmage survey.
             */
            SURVEY;

            /**
             * Prints the type as a lower-case, user-friendly string.
             */
            @Override
            public String toString() {
                return name().toLowerCase();
            }

            /**
             * Parses some string value into a Type.
             *
             * @param value
             *        The string value to parse.
             *
             * @return The Type that is represented by the given string.
             *
             * @throws IllegalArgumentException
             *         The given string does not represent any known Type.
             */
            public static Type getType(final String value)
                throws IllegalArgumentException {

                for(Type type : values()) {
                    if(type.name().toLowerCase().equals(value)) {
                        return type;
                    }
                }

                throw
                    new IllegalArgumentException(
                        "The type is unknown: " + value);
            }
        }

        /**
         * The ohmage type that is associated with this schema ID.
         */
        public final Type type;
        /**
         * The schema ID usable within ohmage.
         */
        public final String ohmageSchemaId;

        /**
         * Builds a new OmhSchemaId based on an ohmage schema ID.
         *
         * @param type
         *        The schema ID's type.
         *
         * @param ohmageSchemaId
         *        The ohmage schema ID.
         *
         * @throws IllegalArgumentException
         *         The type or ID are null.
         */
        public OmhSchemaId(final Type type, final String ohmageSchemaId)
            throws IllegalArgumentException {

            if(type == null) {
                throw new IllegalArgumentException("The type is null.");
            }
            if(ohmageSchemaId == null) {
                throw
                    new IllegalArgumentException(
                        "The ohmage schema ID is null.");
            }

            this.type = type;
            this.ohmageSchemaId = ohmageSchemaId;
        }

        /**
         * Builds a new OmhSchemaId based on a schema ID in the Open mHealth
         * format.
         *
         * @param omhSchemaId
         *        A schema ID in the Open mHealth format.
         */
        public OmhSchemaId(final String omhSchemaId) {
            // If the parameter is null, error out.
            if(omhSchemaId == null) {
                throw new InvalidArgumentException("The schema ID is null.");
            }

            // Split the schema ID based on the colons.
            String[] schemaIdParts = omhSchemaId.split(":");

            // Validate the schema ID parts.
            if(schemaIdParts.length != 4) {
                throw
                    new InvalidArgumentException(
                        "The schema ID must be four parts each separated by " +
                            "a colon: " +
                                "omh:" +
                                "ohmage:" +
                                "{\"stream\" | \"survey\"}:" +
                                "{schema_id}");
            }
            else if(! "omh".equals(schemaIdParts[0])) {
                throw
                    new InvalidArgumentException(
                        "The schema ID must begin with \"omh:\": " +
                            omhSchemaId);
            }
            else if(! "ohmage".equals(schemaIdParts[1])) {
                throw
                    new InvalidArgumentException(
                        "The second part of the schema ID must be \"ohmage\": " +
                            omhSchemaId);
            }

            // Validate the type.
            try {
                type = Type.getType(schemaIdParts[2]);
            }
            catch(IllegalArgumentException e) {
                throw
                    new InvalidArgumentException(
                        "The third part of the schema ID must be either " +
                            "\"stream\" or \"survey\": " +
                            omhSchemaId);

            }

            // Store the ohmage schema ID.
            ohmageSchemaId = schemaIdParts[3];
        }

        /**
         * Returns this schema ID in the Open mHealth format.
         */
        @Override
        public String toString() {
            return
                "omh" + ":" +
                "ohmage" + ":" +
                type.toString() + ":" +
                ohmageSchemaId;
        }
    }

    /**
     * The root mapping for all Open mHealth APIs.
     */
    public static final String ROOT_MAPPING = "/omh/v1";

    /**
     * The path and parameter key for schema IDs.
     */
    public static final String KEY_SCHEMA_ID = "id";
    /**
     * The path and parameter key for schema versions.
     */
    public static final String KEY_SCHEMA_VERSION = "version";
    /**
     * The name of the parameter for the users auth token.
     */
    public static final String KEY_AUTH_TOKEN = "auth_token";
    /**
     * The name of the parameter for querying for specific values.
     */
    public static final String KEY_QUERY = "query";
    /**
     * The name of the parameter for querying data on or after a given time.
     */
    public static final String KEY_START_DATE = "t_start";
    /**
     * The name of the parameter for querying data on or before a given time.
     */
    public static final String KEY_END_DATE = "t_end";
    /**
     * The name of the parameter for limiting which fields are returned in the
     * data.
     */
    public static final String KEY_COLUMN_LIST = "column_list";

    /**
     * The list of allowed root values for the "column list" parameter.
     */
    public static final Set<String> ALLOWED_COLUMN_LIST_ROOTS =
        new HashSet<String>(
            Arrays
                .asList(
                    StreamData.JSON_KEY_META_DATA,
                    StreamData.JSON_KEY_DATA,
                    SurveyResponse.JSON_KEY_META_DATA,
                    SurveyResponse.JSON_KEY_DATA));

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(OmhServlet.class.getName());

    /**
     * <p>
     * The OAuth call where a user has been redirected to us by some
     * OAuth client in order for us to present them with an authorization
     * request, verify that the user is who they say they are, and grant or
     * deny the request.
     * </p>
     *
     * <p>
     * This call will either redirect the user to the authorization HTML page
     * with the parameters embedded or it will return a non-2xx response with a
     * message indicating what was wrong with the request. Unfortunately,
     * because the problem with the request may be that the given client ID is
     * unknown, we have no way to direct the user back. If we simply force the
     * browser to "go back", it may result in an infinite loop where the
     * OAuth client continuously redirects them back to us and visa-versa. To
     * avoid this, we should simply return an error string and let the user
     * decide.
     * </p>
     *
     * @param rootUrl
     *        The root URL for this request which will be used to generate the
     *        redirect to the authorization page.
     *
     * @param clientId
     *        The client's (OAuth client's) unique identifier.
     *
     * @param scopeString
     *        A string that represents space-delimited scopes.
     *
     * @param redirectUri
     *        The URI that will be used to redirect the user after they have
     *        responded to the authorization request.
     *
     * @param state
     *        A string that is not validated or checked in any way and is
     *        simply echoed between requests.
     *
     * @return A OAuth-specified JSON response that indicates what was wrong
     *         with the request. If nothing was wrong with the request, a
     *         redirect would have been returned.
     *
     * @throws IOException
     *         There was a problem responding to the client.
     *
     * @throws OAuthSystemException
     *         The OAuth library encountered an error.
     */
    @RequestMapping(
        value = "/auth/oauth" + Oauth2Servlet.PATH_AUTHORIZE,
        method = RequestMethod.GET,
        params = "response_type" + "=" + "code")
    public static String authorize(
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl,
        @RequestParam(value = "client_id", required = true)
            final String clientId,
        @RequestParam(value = "scope", required = true)
            final String scopeString,
        @RequestParam(value = "redirect_uri", required = false)
            final URI redirectUri,
        @RequestParam(value = "state", required = false)
            final String state) {

        return
            Oauth2Servlet
                .authorize(rootUrl, clientId, scopeString, redirectUri, state);
    }

    /**
     * <p>
     * Handles the response from the user regarding whether or not the user
     * granted permission to a OAuth client via OAuth. If the user's credentials
     * are invalid or there was a general error reading the request, an error
     * message will be returned and displayed to the user. As long as there is
     * not an internal error, we will redirect the user back to the OAuth client
     * with a code, which the OAuth client can then use to call us to determine
     * the user's response.
     * </p>
     *
     * @param email
     *        The user's email address.
     *
     * @param password
     *        The user's password.
     *
     * @param codeString
     *        The authorization code.
     *
     * @param granted
     *        Whether or not the user granted the OAuth client's request.
     *
     * @return A redirect back to the OAuth client with the code and state.
     */
    @RequestMapping(
        value = "/auth/oauth" + Oauth2Servlet.PATH_AUTHORIZATION,
        method = RequestMethod.POST)
    public static String authorization(
        @RequestParam(value = User.JSON_KEY_EMAIL, required = true)
            final String email,
        @RequestParam(value = User.JSON_KEY_PASSWORD, required = true)
            final String password,
        @RequestParam(
            value = AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
            required = true)
            final String codeString,
        @RequestParam(value = "granted", required = true)
            final boolean granted) {

        return
            Oauth2Servlet.authorization(email, password, codeString, granted);
    }

    /**
     * <p>
     * The OAuth call when a OAuth client is attempting to exchange their
     * authorization code for a valid authorization token. Because this is a
     * back-channel communication from the OAuth client, their ID and secret
     * must be given to authenticate them. They will then be returned either an
     * authorization token or an error message indicating what was wrong with
     * the request.
     * </p>
     *
     * @param oauthClientId
     *        The unique identifier for the OAuth client that is exchanging the
     *        code for a token.
     *
     * @param oauthClientSecret
     *        The OAuth client's secret.
     *
     * @param codeString
     *        The code that is being exchanged.
     *
     * @param redirectUri
     *        The redirect URI that must match what was given when the code was
     *        requested or the default one associated with this OAuth client. If
     *        it is not given, the redirect URI from the code must be the
     *        default one.
     *
     * @return A new authorization token.
     */
    @RequestMapping(
        value = "/auth/oauth" + Oauth2Servlet.PATH_TOKEN,
        method = RequestMethod.POST,
        params = "grant_type" + "=" + "authorization_code")
    public static @ResponseBody AuthorizationToken tokenFromCode(
        @RequestParam(value = "client_id", required = true)
            final String oauthClientId,
        @RequestParam(value = "client_secret", required = true)
            final String oauthClientSecret,
        @RequestParam(value = "code", required = true) final String codeString,
        @RequestParam(value = "redirect_uri", required = false)
            final URI redirectUri) {

        return
            Oauth2Servlet
                .tokenFromCode(
                    oauthClientId,
                    oauthClientSecret,
                    codeString,
                    redirectUri);
    }

    /**
     * <p>
     * The OAuth call when a OAuth client is attempting to exchange an expired
     * authorization token for a new authorization token. Because this is a
     * back-channel communication from the OAuth client, their ID and secret
     * must be given to authenticate them. They will then be returned either an
     * authorization token or an error message indicating what was wrong with
     * the request.
     * </p>
     *
     * @param oauthClientId
     *        The unique identifier for the OAuth client that is exchanging the
     *        code for a token.
     *
     * @param oauthClientSecret
     *        The OAuth client's secret.
     *
     * @param refreshToken
     *        The refresh token value from the latest version of the desired
     *        token.
     *
     * @return A new authorization token.
     */
    @RequestMapping(
        value = "/auth/oauth" + Oauth2Servlet.PATH_TOKEN,
        method = RequestMethod.POST,
        params = "grant_type" + "=" + "refresh_token")
    public static @ResponseBody AuthorizationToken tokenFromRefresh(
        @RequestParam(value = "client_id", required = true)
            final String oauthClientId,
        @RequestParam(value = "client_secret", required = true)
            final String oauthClientSecret,
        @RequestParam(value = "refresh_token", required = true)
            final String refreshToken) {

        return
            Oauth2Servlet
                .tokenFromRefresh(
                    oauthClientId,
                    oauthClientSecret,
                    refreshToken);
    }

    /**
     * Returns a list of visible stream IDs.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     *
     * @return A list of visible schema IDs.
     */
    @RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<String>> getSchemaIds(
        @RequestParam(value = KEY_QUERY, required = false) final String query,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn,
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

        LOGGER.log(Level.INFO, "Creating an OmH registry read request.");

        LOGGER.log(Level.INFO, "Building the result aggregator.");
        MultiValueResultAggregation.Aggregator<String> aggregator =
            new MultiValueResultAggregation.Aggregator<String>();

        LOGGER.log(Level.INFO, "Retrieving the stream IDs.");
        MultiValueResult<String> streamIds =
            StreamBin
                .getInstance()
                .getStreamIds(query, true, 0, numToSkip + numToReturn);
        List<String> omhStreamIds = new ArrayList<String>();
        for(String streamId : streamIds) {
            omhStreamIds.add(
                (new OmhSchemaId(OmhSchemaId.Type.STREAM, streamId))
                    .toString());
        }
        aggregator.add(omhStreamIds, streamIds.count());

        LOGGER.log(Level.INFO, "Retrieving the survey IDs.");
        MultiValueResult<String> surveyIds =
            SurveyBin
                .getInstance()
                .getSurveyIds(query, true, 0, numToSkip + numToReturn);
        List<String> omhSurveyIds = new ArrayList<String>();
        for(String surveyId : surveyIds) {
            omhSurveyIds.add(
                (new OmhSchemaId(OmhSchemaId.Type.SURVEY, surveyId))
                    .toString());
        }
        aggregator.add(omhSurveyIds, surveyIds.count());

        LOGGER.log(Level.FINE, "Compiling the result list.");
        MultiValueResult<String> aggregation =
            aggregator.build(numToSkip, numToReturn);

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    numToSkip,
                    numToReturn,
                    Collections.<String, String>emptyMap(),
                    aggregation,
                    rootUrl + ROOT_MAPPING);

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<String>> resultEntity =
            new ResponseEntity<MultiValueResult<String>>(
                aggregation,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the schema IDs.");
        return resultEntity;
    }

    /**
     * Returns a list of versions for the given schema.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     *
     * @return A list of the visible versions.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<Long>> getSchemaVersions(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @RequestParam(value = KEY_QUERY, required = false) final String query,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn,
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

        LOGGER
            .log(
                Level.INFO,
                "Creating an OmH request to read the versions of a schema: " +
                    schemaId);

        LOGGER.log(Level.INFO, "Parsing the schema ID.");
        OmhSchemaId omhSchemaId = new OmhSchemaId(schemaId);

        LOGGER.log(Level.FINE, "Creating the collection of versions.");
        MultiValueResult<Long> versions;

        LOGGER.log(Level.INFO, "Retrieving the versions.");
        switch(omhSchemaId.type) {
            case STREAM:
                LOGGER.log(Level.INFO, "The schema is a stream.");
                versions =
                    StreamBin
                        .getInstance()
                        .getStreamVersions(
                            omhSchemaId.ohmageSchemaId,
                            query,
                            true,
                            numToSkip,
                            numToReturn);
                break;

            case SURVEY:
                LOGGER.log(Level.INFO, "The schema is a survey.");
                versions =
                    StreamBin
                        .getInstance()
                        .getStreamVersions(
                            omhSchemaId.ohmageSchemaId,
                            query,
                            true,
                            numToSkip,
                            numToReturn);
                break;

            default:
                throw new UnknownEntityException("The schema is unknown.");
        }

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    numToSkip,
                    numToReturn,
                    Collections.<String, String>emptyMap(),
                    versions,
                    rootUrl + ROOT_MAPPING);

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<Long>> result =
            new ResponseEntity<MultiValueResult<Long>>(
                versions,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the schema IDs.");
        return result;
    }

    /**
     * Returns the definition for a given schema.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @return The schema definition.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}",
        method = RequestMethod.GET)
    public static @ResponseBody Concordia getSchemaDefinition(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion) {

        LOGGER
            .log(
                Level.INFO,
                "Creating an OmH request for a schema definition: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.log(Level.INFO, "Parsing the schema ID.");
        OmhSchemaId omhSchemaId = new OmhSchemaId(schemaId);

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        Schema result;
        switch(omhSchemaId.type) {
            case STREAM:
                LOGGER.log(Level.INFO, "The schema is a stream.");
                result =
                    StreamBin
                        .getInstance()
                        .getStream(
                            omhSchemaId.ohmageSchemaId,
                            schemaVersion,
                            true);
                break;

            case SURVEY:
                LOGGER.log(Level.INFO, "The schema is a survey.");
                result =
                    SurveyBin
                        .getInstance()
                        .getSurvey(
                            omhSchemaId.ohmageSchemaId,
                            schemaVersion,
                            true);

            default:
                throw
                    new UnknownEntityException(
                        "The schema ID-verion pair is unknown.");

        }

        LOGGER.log(Level.INFO, "Returning the schema.");
        return result.getDefinition();
    }

    /**
     * Returns the data corresponding to the schema ID and version.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param startDate
     *        The earliest date for a given point.
     *
     * @param endDate
     *        The latest date for a given point.
     *
     * @param columnList
     *        A column-separated list of the fields that should be returned
     *        from the resulting data.
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     *
     * @return The data corresponding to the schema ID and version.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}/data",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<?>> getData(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion,
        @RequestParam(KEY_AUTH_TOKEN) final String authToken,
        @RequestParam(value = KEY_START_DATE, required = false)
            final String startDate,
        @RequestParam(value = KEY_END_DATE, required = false)
            final String endDate,
        @RequestParam(value = KEY_COLUMN_LIST, required = false)
            final List<String> columnList,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_SKIP,
            required = false,
            defaultValue = DEFAULT_NUM_TO_SKIP_STRING)
            final long numToSkip,
        @RequestParam(
            value = PARAM_PAGING_NUM_TO_RETURN,
            required = false,
            defaultValue = DEFAULT_NUM_TO_RETURN_STRING)
            final long numToReturn,
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

        LOGGER
            .log(
                Level.INFO,
                "Creating an OmH request for schema data: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER.log(Level.INFO, "Retrieving the auth information.");
        AuthorizationToken authTokenObject =
            AuthorizationTokenBin
                .getInstance()
                .getTokenFromAccessToken(authToken);

        LOGGER.log(Level.INFO, "Validating the user from the token");
        User user =
            OhmageServlet
                .validateAuthorization(
                    authTokenObject,
                    new Scope(
                        Scope.Type.STREAM,
                        schemaId,
                        schemaVersion,
                        Scope.Permission.READ));

        LOGGER.log(Level.INFO, "Verifying the auth token is valid.");
        if(! authTokenObject.isValid()) {
            throw
                new AuthenticationException(
                    "No auth token is no longer valid.");
        }

        LOGGER.log(Level.INFO, "Parsing the schema ID.");
        OmhSchemaId omhSchemaId = new OmhSchemaId(schemaId);

        LOGGER.log(Level.FINE, "Parsing the start and end dates, if given.");
        DateTime startDateObject =
            (startDate == null) ?
                null :
                OHMAGE_DATE_TIME_FORMATTER.parseDateTime(startDate);
        DateTime endDateObject =
            (endDate == null) ?
                null :
                OHMAGE_DATE_TIME_FORMATTER.parseDateTime(endDate);

        LOGGER.log(Level.INFO, "Validating the column list, if given.");
        ColumnList columnListObject = null;
        if(columnList != null) {
            columnListObject = new ColumnList(columnList);

            LOGGER.log(Level.INFO, "Validating the column list.");
            Set<String> columnListRoots =
                new HashSet<String>(columnListObject.getChildren());
            columnListRoots.removeAll(ALLOWED_COLUMN_LIST_ROOTS);
            if(columnListRoots.size() > 0) {
                throw
                    new InvalidArgumentException(
                        "The root of every element in a column list must be " +
                            "one of: " +
                            ALLOWED_COLUMN_LIST_ROOTS.toString());
            }
        }

        LOGGER
            .log(
                Level.FINE,
                "Generating the list of user IDs, which is only the " +
                    "requester.");
        Set<String> userIds = new HashSet<String>();
        userIds.add(user.getId());

        LOGGER.log(Level.INFO, "Retrieving the definition.");
        MultiValueResult<?> data;
        switch(omhSchemaId.type) {
            case STREAM:
                LOGGER.log(Level.INFO, "The schema is a stream.");
                data =
                    StreamDataBin
                        .getInstance()
                        .getStreamData(
                            omhSchemaId.ohmageSchemaId,
                            schemaVersion,
                            userIds,
                            startDateObject,
                            endDateObject,
                            columnListObject,
                            numToSkip,
                            numToReturn);
                break;

            case SURVEY:
                LOGGER.log(Level.INFO, "The schema is a survey.");
                data =
                    SurveyResponseBin
                        .getInstance()
                        .getSurveyResponses(
                            omhSchemaId.ohmageSchemaId,
                            schemaVersion,
                            userIds,
                            null,
                            startDateObject,
                            endDateObject,
                            columnListObject,
                            numToSkip,
                            numToReturn);
                break;

            default:
                throw
                    new UnknownEntityException(
                        "The schema ID-verion pair is unknown.");
        }

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    numToSkip,
                    numToReturn,
                    Collections.<String, String>emptyMap(),
                    data,
                    rootUrl + ROOT_MAPPING);

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<?>> result =
            new ResponseEntity<MultiValueResult<?>>(
                data,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the data.");
        return result;
    }
}