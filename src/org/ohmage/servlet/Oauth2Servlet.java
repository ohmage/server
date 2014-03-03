package org.ohmage.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;
import org.ohmage.bin.AuthorizationCodeBin;
import org.ohmage.bin.AuthorizationTokenBin;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.OauthClientBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.auth.AuthorizationCodeResponse;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.auth.OauthClient;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.auth.Scope.Type;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.user.User;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all OAuth2 requests.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(Oauth2Servlet.ROOT_MAPPING)
public class Oauth2Servlet extends OhmageServlet {
    /**
     * The root API mapping for this Servlet.
     */
    public static final String ROOT_MAPPING = "/oauth";

    /**
     * The path element for a OAuth client to have a user redirected to the
     * authorization page.
     */
    public static final String PATH_AUTHORIZE = "authorize";
    /**
     * The path element for a OAuth client to exchange an authorization code for
     * an authorization token.
     */
    public static final String PATH_TOKEN = "token";

    /**
     * The name of the web page where a user authorizes a request.
     */
    public static final String AUTHORIZATION_PAGE = "Authorize.html";
    /**
     * The path element for a user to respond to an authorization request.
     */
    public static final String PATH_AUTHORIZATION = "authorization";

    /**
     * The request parameter from a user when they are responding to an
     * authorization request indicating whether or not the are granting the
     * request.
     */
    public static final String PARAMETER_GRANTED = "granted";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(Oauth2Servlet.class.getName());

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
        value = PATH_AUTHORIZE,
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

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to get a user's authorization.");

        LOGGER.log(Level.INFO, "Retrieving the OAuth client.");
        OauthClient oauthClient =
            OauthClientBin.getInstance().getOauthClient(clientId);

        LOGGER.log(Level.INFO, "Verifying that the OAuth client exists.");
        if(oauthClient == null) {
            throw new InvalidArgumentException("The OAuth client is unknown.");
        }

        LOGGER.log(Level.INFO, "Validating the scopes.");
        if(scopeString == null) {
            throw new InvalidArgumentException("The scope is missing.");
        }
        String[] scopeStrings = scopeString.split(" ");
        Set<Scope> scopes = new HashSet<Scope>(scopeStrings.length);
        for(String currScopeString : scopeStrings) {
            // Build the scope, which will make sure it conforms to our format.
            Scope scope = (new Scope.Builder(currScopeString)).build();

            // Verify that the schema exists.
            if(Type.STREAM.equals(scope.getType())) {
                if(!
                    StreamBin
                        .getInstance()
                        .exists(
                            scope.getSchemaId(),
                            scope.getSchemaVersion(),
                            false)) {

                    throw
                        new InvalidArgumentException(
                            "The stream is unknown: " +
                                scope.getSchemaId() +
                                ((scope.getSchemaVersion() == null) ?
                                    "" :
                                    " : " + scope.getSchemaVersion()));
                }
            }
            else if(Type.SURVEY.equals(scope.getType())) {
                if(!
                    SurveyBin
                        .getInstance()
                        .exists(
                            scope.getSchemaId(),
                            scope.getSchemaVersion(),
                            false)) {

                    throw
                        new InvalidArgumentException(
                            "The survey is unknown: " +
                                scope.getSchemaId() +
                                ((scope.getSchemaVersion() == null) ?
                                    "" :
                                    " : " + scope.getSchemaVersion()));
                }
            }
            else {
                throw
                    new IllegalStateException(
                        "The type is not being checked.");
            }

            // Add it to the list of scopes.
            scopes.add(scope);
        }

        LOGGER.log(Level.INFO, "Validating the redirect URI.");
        URI validatedRedirectUri;
        if(redirectUri == null) {
            LOGGER
                .log(
                    Level.INFO,
                    "Using the OAuth client's default redirect URI.");
            validatedRedirectUri = oauthClient.getRedirectUri();
        }
        else {
            LOGGER.log(Level.INFO, "Using the supplied redirect URI.");

            LOGGER.log(Level.INFO, "Normallizing the redirct URI.");
            validatedRedirectUri = redirectUri.normalize();

            LOGGER
                .log(
                    Level.INFO,
                    "Verifying that the normalized redirect URI is a " +
                        "sub-URI of the default redirect URI.");
            supercedes(oauthClient.getRedirectUri(), validatedRedirectUri);
        }

        LOGGER.log(Level.INFO, "Generating a new authorization code.");
        AuthorizationCode authorizationCode =
            new AuthorizationCode(
                oauthClient.getId(),
                scopes,
                validatedRedirectUri,
                state);

        LOGGER.log(Level.INFO, "Storing the authorization code.");
        AuthorizationCodeBin.getInstance().addCode(authorizationCode);

        try {
            LOGGER.log(Level.INFO, "Building the redirect URI.");
            URIBuilder authorizationUriBuilder =
                new URIBuilder(
                    rootUrl +
                    ROOT_MAPPING +
                    "/" + AUTHORIZATION_PAGE);
            authorizationUriBuilder
                .addParameter(
                    AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
                    authorizationCode.getCode());

            return "redirect:" + authorizationUriBuilder.build().toString();
        }
        catch(URISyntaxException e) {
            throw
                new IllegalStateException(
                    "There was a problem building the authorization " +
                        "redirect URI.",
                    e);
        }
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
        value = PATH_AUTHORIZATION,
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
        @RequestParam(value = PARAMETER_GRANTED, required = true)
            final boolean granted) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to handle a user's authorization code " +
                    "response.");


        LOGGER.log(Level.INFO, "Verifying that auth information was given.");
        if(email == null) {
            throw new AuthenticationException("No email address was given.");
        }
        if(password == null) {
            throw new AuthenticationException("No password was given.");
        }

        LOGGER
            .log(Level.INFO, "Retrieving the user associated with the token.");
        User user = UserBin.getInstance().getUserFromEmail(email);

        LOGGER.log(Level.INFO, "Verifying that the user exists.");
        if(user == null) {
            throw new AuthenticationException("The user is unknown.");
        }

        LOGGER.log(Level.INFO, "Verifying the user's password.");
        if(! user.verifyPassword(password)) {
            throw new AuthenticationException("The password was incorrect.");
        }

        LOGGER.log(Level.INFO, "Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.log(Level.INFO, "Verifying that the code exists.");
        if(code == null) {
            throw new InvalidArgumentException("The code is missing.");
        }

        LOGGER.log(Level.INFO, "Verifying that the code has not expired.");
        if(code.getExpirationTimestamp() < System.currentTimeMillis()) {
            throw new InvalidArgumentException("The code has expired.");
        }

        LOGGER.log(Level.INFO, "Retrieving the response.");
        AuthorizationCodeResponse response = code.getResponse();

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the code has not yet been responded to.");
        if(response == null) {
            LOGGER
                .log(
                    Level.INFO,
                    "No response exists, so a new one is being created.");
            response =
                new AuthorizationCodeResponse(
                    user.getId(),
                    granted);

            LOGGER.log(Level.INFO, "Updating the code with the response.");
            code =
                (new AuthorizationCode.Builder(code))
                    .setUsedTimestamp(System.currentTimeMillis())
                    .setResponse(response)
                    .build();

            LOGGER.log(Level.INFO, "Storing the updated authorization code.");
            AuthorizationCodeBin.getInstance().updateCode(code);
        }
        else if(! response.getUserId().equals(user.getId())) {
            throw
                new InvalidArgumentException(
                    "Another user already responded to this request.");
        }
        else if(response.getGranted() != granted) {
            throw
                new InvalidArgumentException(
                    "The user has already responded to this request, " +
                        "however they gave a different answer last time.");
        }
        // Otherwise, they are simply replaying the same request, and we don't
        // care.

        LOGGER
            .log(
                Level.INFO,
                "Building the specific redirect request for the user back " +
                    "to the origin.");
        URIBuilder redirectBuilder = new URIBuilder(code.getRedirectUri());
        redirectBuilder
            .addParameter(
                AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
                code.getCode());
        redirectBuilder
            .addParameter(
                AuthorizationCode.JSON_KEY_STATE,
                code.getState());

        LOGGER
            .log(Level.INFO, "Redirecting the user back to the OAuth client.");
        try {
            return "redirect:" + redirectBuilder.build().toString();
        }
        catch(URISyntaxException e) {
            throw
                new IllegalStateException(
                    "There was a problem building the redirect URI.",
                    e);
        }
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
        value = PATH_TOKEN,
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

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to handle a user's authorization " +
                    "request response.");

        LOGGER.log(Level.INFO, "Verifying that the OAuth client ID was given.");
        if(oauthClientId == null) {
            throw
                new AuthenticationException("The OAuth client ID is missing.");
        }

        LOGGER.log(Level.INFO, "Retrieving the OAuth client.");
        OauthClient oauthClient =
            OauthClientBin.getInstance().getOauthClient(oauthClientId);

        LOGGER.log(Level.INFO, "Verifying the OAuth client exists.");
        if(oauthClient == null) {
            throw new AuthenticationException("The OAuth client is unknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the OAuth client secret was given.");
        if(oauthClientSecret == null) {
            throw
                new AuthenticationException(
                     "The OAuth client secret is missing.");
        }

        LOGGER.log(Level.INFO, "Verifying the OAuth client's secret.");
        if(! oauthClient.getSecret().equals(oauthClientSecret)) {
            throw
                new AuthenticationException(
                    "The OAuth client secret is incorrect.");
        }

        LOGGER.log(Level.INFO, "Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.log(Level.INFO, "Verifying that the code exists.");
        if(code == null) {
            throw new InvalidArgumentException("The code is uknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the code corresponds to this OAuth client.");
        if(! code.getOauthClientId().equals(oauthClientId)) {
            throw
                new InvalidArgumentException(
                    "This code belongs to a different OAuth client.");
        }

        LOGGER.log(Level.INFO, "Verifying that the code has not expired.");
        if(code.getExpirationTimestamp() < System.currentTimeMillis()) {
            throw new InvalidArgumentException("The code has expired.");
        }

        LOGGER.log(Level.INFO, "Validating the redirect URI.");
        if(redirectUri == null) {
            if(! oauthClient.getRedirectUri().equals(code.getRedirectUri())) {
                throw
                    new InvalidArgumentException(
                        "The code request provided a non-default redirect " +
                            "URI, but this call did not provide a redirect " +
                            "URI.");
            }
        }
        else {
            if(! redirectUri.equals(code.getRedirectUri())) {
                throw
                    new InvalidArgumentException(
                        "The code request redirect URI does not match the " +
                            "given redirect URI.");
            }
        }

        LOGGER.log(Level.INFO, "Retrieving the code response.");
        AuthorizationCodeResponse response = code.getResponse();

        LOGGER.log(Level.INFO, "Verifying that the user has responded.");
        if(response == null) {
            throw
                new InvalidArgumentException(
                    "The user has not yet responded.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Checking if the user accepted or declined the request.");
        if(! response.getGranted()) {
            throw
                new InvalidArgumentException("The user declined the request.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Retrieving the first token that was created from this code " +
                    "response.");
        AuthorizationToken token =
            AuthorizationTokenBin
                .getInstance()
                .getTokenFromAuthorizationCode(code.getCode());

        if(token == null) {
            LOGGER
                .log(
                    Level.INFO,
                    "No such token exists, so we are creating a new one.");
            token = new AuthorizationToken(code);

            LOGGER.log(Level.INFO, "Storing the newly created token.");
            AuthorizationTokenBin.getInstance().addToken(token);
        }
        else {
            LOGGER
                .log(
                    Level.INFO,
                    "A token was already issued for this response. Checking " +
                        "if it has also been refreshed.");
            if(token.wasRefreshed()) {
                throw
                    new InvalidArgumentException(
                        "This code has already been used to create a token, " +
                            "and that token has already been refreshed.");
            }
        }

        LOGGER.log(Level.INFO, "Returning the token to the user.");
        return token;
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
        value = PATH_TOKEN,
        method = RequestMethod.POST,
        params = "grant_type" + "=" + "refresh_token")
    public static @ResponseBody AuthorizationToken tokenFromRefresh(
        @RequestParam(value = "client_id", required = true)
            final String oauthClientId,
        @RequestParam(value = "client_secret", required = true)
            final String oauthClientSecret,
        @RequestParam(value = "refresh_token", required = true)
            final String refreshToken
        /* FIXME: Scope? */) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to handle a user's authorization " +
                    "request response.");

        LOGGER.log(Level.INFO, "Verifying that the OAuth client ID was given.");
        if(oauthClientId == null) {
            throw
                new AuthenticationException("The OAuth client ID is missing.");
        }

        LOGGER.log(Level.INFO, "Retrieving the OAuth client.");
        OauthClient oauthClient =
            OauthClientBin.getInstance().getOauthClient(oauthClientId);

        LOGGER.log(Level.INFO, "Verifying the OAuth client exists.");
        if(oauthClient == null) {
            throw new AuthenticationException("The OAuth client is unknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the OAuth client secret was given.");
        if(oauthClientSecret == null) {
            throw
                new AuthenticationException(
                     "The OAuth client secret is missing.");
        }

        LOGGER.log(Level.INFO, "Verifying the OAuth client's secret.");
        if(! oauthClient.getSecret().equals(oauthClientSecret)) {
            throw
                new AuthenticationException(
                    "The OAuth client secret is incorrect.");
        }

        LOGGER.log(Level.INFO, "Retrieving the original token.");
        AuthorizationToken originalToken =
            AuthorizationTokenBin
                .getInstance()
                .getTokenFromRefreshToken(refreshToken);

        LOGGER.log(Level.INFO, "Verifying that this token exists.");
        if(originalToken == null) {
            throw new InvalidArgumentException("The token is unknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that this refresh token was issued via an " +
                    "authorization code.");
        if(originalToken.getAuthorizationCode() == null) {
            throw
                new InvalidArgumentException(
                    "This refresh token was not issued via an authorization " +
                        "flow, so it cannot be refreshed via this call.");
        }

        LOGGER.log(Level.INFO, "Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin
                .getInstance()
                .getCode(originalToken.getAuthorizationCode());

        LOGGER.log(Level.INFO, "Verifying that the code exists.");
        if(code == null) {
            throw new IllegalStateException("The code is uknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the code corresponds to this OAuth client.");
        if(! code.getOauthClientId().equals(oauthClientId)) {
            throw
                new InvalidArgumentException(
                    "This code belongs to a different OAuth client.");
        }

        LOGGER.log(Level.INFO, "Checking if the token has been refreshed.");
        AuthorizationToken newToken;
        if(originalToken.wasRefreshed()) {
            LOGGER.log(Level.INFO, "The token has been refreshed.");

            LOGGER.log(Level.INFO, "Retrieving the refreshed token.");
            newToken =
                AuthorizationTokenBin
                    .getInstance()
                    .getTokenFromAccessToken(originalToken.getNextToken());

            LOGGER
                .log(
                    Level.INFO,
                    "Verifing that the refreshed token has not also been " +
                        "refreshed.");
            if(newToken.wasRefreshed()) {
                throw
                    new InvalidArgumentException(
                        "This token has already been refreshed and its " +
                            "refreshed token has also been refreshed.");
            }
        }
        else {
            LOGGER.log(Level.INFO, "The token has not been refreshed.");

            LOGGER.log(Level.INFO, "Creating a new token.");
            newToken = new AuthorizationToken(originalToken);

            LOGGER.log(Level.INFO, "Storing the new token.");
            AuthorizationTokenBin.getInstance().addToken(newToken);

            LOGGER
                .log(
                    Level.INFO,
                    "Updating the original token to reference this new " +
                        "token.");
            AuthorizationToken invalidatedOldToken =
                (new AuthorizationToken.Builder(originalToken))
                    .setNextToken(newToken.getAccessToken())
                    .build();

            LOGGER.log(Level.INFO, "Updating the original token.");
            AuthorizationTokenBin
                .getInstance()
                .updateToken(invalidatedOldToken);
        }

        LOGGER.log(Level.INFO, "Returning the new token.");
        return newToken;
    }

    /**
     * Creates a new OAuth client.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param oauthClientBuilder
     *        A builder to use to this new OAuth client.
     *
     * @return The constructed OAuth client.
     */
    @RequestMapping(
        value = "/clients",
        method = RequestMethod.POST)
    public static @ResponseBody ResponseEntity<OauthClient> createClient(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestBody final OauthClient.Builder oauthClientBuilder) {

        LOGGER.log(Level.INFO, "Creating a new OAuth client.");

        LOGGER.log(Level.INFO, "Validating the user from the token");
        User user = OhmageServlet.validateAuthorization(authToken, null);

        LOGGER.log(Level.FINE, "Setting the owner of the OAuth client.");
        oauthClientBuilder.setOwner(user.getId());

        if(oauthClientBuilder.getRedirectUri() != null) {
            LOGGER.log(Level.INFO, "Normalizing the redirect URI.");
            oauthClientBuilder
                .setRedirectUri(
                    oauthClientBuilder.getRedirectUri().normalize());
        }

        LOGGER.log(Level.FINE, "Building the OAuth client.");
        OauthClient oauthClient = oauthClientBuilder.build();

        LOGGER.log(Level.INFO, "Saving the new OAuth client.");
        OauthClientBin.getInstance().addOauthClient(oauthClient);

        LOGGER.log(Level.INFO, "Building the headers.");
        HttpHeaders headers = new HttpHeaders();

        LOGGER.log(Level.INFO, "Extracting the OAuth client's shared secret.");
        headers
            .add(OauthClient.JSON_KEY_SHARED_SECRET, oauthClient.getSecret());

        LOGGER.log(Level.INFO, "Returning the OAuth client.");
        return
            new ResponseEntity<OauthClient>(
                oauthClient,
                headers,
                HttpStatus.OK);
    }

    /**
     * Retrieves the set of client unique identifiers that are owned by the
     * requesting user.
     *
     * @param rootUrl
     *        The base URL for this request.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @return The set of client unique identifiers that are owned by the
     *         requesting user.
     */
    @RequestMapping(
        value = "/clients",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<String>> getClients(
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to retrieve the user-owned client IDs.");

        LOGGER.log(Level.INFO, "Validating the user from the token");
        User user = OhmageServlet.validateAuthorization(authToken, null);

        LOGGER
            .log(
                Level.INFO,
                "Retrieving the visible client IDs.");
        MultiValueResult<String> clientIds =
            OauthClientBin.getInstance().getClientIds(user.getId());

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    0,
                    Long.MAX_VALUE,
                    Collections.<String, String>emptyMap(),
                    clientIds,
                    rootUrl + ROOT_MAPPING + "/clients");

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<String>> result =
            new ResponseEntity<MultiValueResult<String>>(
                clientIds,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the codes.");
        return result;
    }

    /**
     * Retrieves the specific information about a client.
     *
     * @param clientId
     *        The client's unique identifier.
     *
     * @return The information about the client.
     */
    @RequestMapping(
        value = "/clients/{" + OauthClient.JSON_KEY_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody OauthClient getClients(
        @PathVariable(OauthClient.JSON_KEY_ID) final String clientId) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to retrieve the client's information.");

        LOGGER.log(Level.INFO, "Retrieving the client information.");
        OauthClient client =
            OauthClientBin.getInstance().getOauthClient(clientId);

        LOGGER.log(Level.INFO, "Verifying that the client is known.");
        if(client == null) {
            throw new UnknownEntityException("The client is unknown.");
        }

        LOGGER.log(Level.INFO, "Returning the client's information.");
        return client;
    }

    /**
     * Retrieves the set of codes that the user has approved.
     *
     * @param rootUrl
     *        The base URL for this request.
     *
     * @param authToken
     *        The user's authorization token.
     *
     * @return The set of codes that the user has approved.
     */
    @RequestMapping(
        value = "/codes",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<String>> getCodes(
        @ModelAttribute(OhmageServlet.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to retrieve the codes that a user has " +
                    "approved.");

        LOGGER.log(Level.INFO, "Validating the user from the token");
        User user = OhmageServlet.validateAuthorization(authToken, null);

        LOGGER
            .log(
                Level.INFO,
                "Retrieving the set of codes that the user has approved.");
        MultiValueResult<String> codes =
            AuthorizationCodeBin.getInstance().getCodes(user.getId());

        LOGGER.log(Level.INFO, "Building the paging headers.");
        HttpHeaders headers =
            OhmageServlet
                .buildPagingHeaders(
                    0,
                    Long.MAX_VALUE,
                    Collections.<String, String>emptyMap(),
                    codes,
                    rootUrl + ROOT_MAPPING + "/codes");

        LOGGER.log(Level.INFO, "Creating the response object.");
        ResponseEntity<MultiValueResult<String>> result =
            new ResponseEntity<MultiValueResult<String>>(
                codes,
                headers,
                HttpStatus.OK);

        LOGGER.log(Level.INFO, "Returning the codes.");
        return result;
    }

    /**
     * Retrieves the set of codes that the user has approved.
     *
     * @param authToken
     *        The user's authorization token. This is only required if the code
     *        has been responded to.
     *
     * @param codeString
     *        The code in question.
     *
     * @return The information about the desired code.
     */
    @RequestMapping(
        value =
            "/codes/{" + AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE + "}",
        method = RequestMethod.GET)
    public static @ResponseBody AuthorizationCode getCode(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE)
            final String codeString) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to retrieve the information about an " +
                    "authorization code.");

        LOGGER.log(Level.INFO, "Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.log(Level.INFO, "Verifying that the code exists.");
        if(code == null) {
            throw new UnknownEntityException("The code is unknown.");
        }

        LOGGER.log(Level.INFO, "Checking if the code has been responded to.");
        if(code.getResponse() != null) {
            LOGGER.log(Level.INFO, "The code has been responded to.");

            LOGGER.log(Level.INFO, "Validating the user from the token");
            User user = OhmageServlet.validateAuthorization(authToken, null);

            LOGGER
                .log(
                    Level.INFO,
                    "Verifying that the requesting user is responder for " +
                        "the code.");
            if(! user.getId().equals(code.getResponse().getUserId())) {
                throw
                    new InsufficientPermissionsException(
                        "The requesting user is not the responder for this " +
                            "code.");
            }
        }

        LOGGER.log(Level.INFO, "Returning the code.");
        return code;
    }

    /**
     * Invalidates a user's response to a code.
     *
     * @param authToken
     *        The user's authorization token.
     *
     * @param codeString
     *        The code's value.
     */
    @RequestMapping(
        value =
            "/codes/{" + AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE + "}",
        method = RequestMethod.DELETE)
    public static @ResponseBody void invalidateCode(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestParam(AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE)
            final String codeString) {

        LOGGER
            .log(
                Level.INFO,
                "Creating a request to invalidate an authorization code.");

        LOGGER.log(Level.INFO, "Validating the user from the token");
        User user = OhmageServlet.validateAuthorization(authToken, null);

        LOGGER.log(Level.INFO, "Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.log(Level.INFO, "Verifying that the code exists.");
        if(code == null) {
            throw new UnknownEntityException("The code is unknown.");
        }

        LOGGER
            .log(
                Level.INFO,
                "Verifying that the requesting user is responder for the " +
                    "code.");
        if(! user.getId().equals(code.getResponse().getUserId())) {
            throw
                new InsufficientPermissionsException(
                    "The requesting user is not the responder for this code.");
        }

        LOGGER.log(Level.INFO, "Invalidating the code response.");
        AuthorizationCode updatedCode =
            (new AuthorizationCode.Builder(code))
                .setResponse(
                    (new AuthorizationCodeResponse.Builder(code.getResponse()))
                        .setInvalidationTimestamp(System.currentTimeMillis())
                        .build())
                .build();

        LOGGER.log(Level.INFO, "Storing the updated code.");
        AuthorizationCodeBin.getInstance().updateCode(updatedCode);
    }

    /**
     * Validates that some child URI resolves to some child of the base URI.
     *
     * @param base
     *        The base URI.
     *
     * @param child
     *        The child URI.
     */
    private static void supercedes(final URI base, final URI child) {
        if(
            ((base.getScheme() == null) && (child.getScheme() == null)) ||
            (! base.getScheme().equals(child.getScheme()))) {

            throw
                new InvalidArgumentException(
                    "The default scheme, '" +
                        base.getScheme() +
                        "', doesn't match the given scheme: " +
                        child.getScheme());
        }

        if(
            ((base.getHost() == null) && (child.getHost() == null)) ||
            (! base.getHost().equals(child.getHost()))) {

            throw
                new InvalidArgumentException(
                    "The default host, '" +
                        base.getHost() +
                        "', doesn't match the given host: " +
                        child.getHost());
        }

        if(
            ((base.getPort() == -1) && (child.getPort() == -1)) ||
            (base.getPort() != child.getPort())) {

            throw
                new InvalidArgumentException(
                    "The default port, '" +
                        base.getPort() +
                        "', doesn't match the given port: " +
                        child.getPort());
        }

        if((base.getPath() != null) && (child.getPath() != null)) {
            String[] basePathElements = base.getPath().split("/");
            String[] childPathElements = child.getPath().split("/");

            if(basePathElements.length > childPathElements.length) {
                throw
                    new InvalidArgumentException(
                        "The given path is not as long as the default path.");
            }

            int i = 0;
            for(String basePathElement : basePathElements) {
                if(! basePathElement.equals(childPathElements[i++])) {
                    throw
                        new InvalidArgumentException(
                            "The given path diverges from the default path.");
                }
            }
        }
        else if(child.getPath() == null) {
            throw
                new InvalidArgumentException(
                    "The given path is not a sub-path of the default path: " +
                        base.getPath());
        }
    }
}