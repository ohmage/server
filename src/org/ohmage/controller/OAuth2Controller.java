package org.ohmage.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ohmage.javax.servlet.listener.ConfigurationFileImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.client.utils.URIBuilder;
import org.ohmage.bin.*;
import org.ohmage.bin.OAuthClientBin;
import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.auth.AuthorizationCodeResponse;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.auth.OAuthClient;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.auth.Scope.Type;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.user.User;
import org.ohmage.javax.servlet.filter.AuthFilter;
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
@RequestMapping(OAuth2Controller.ROOT_MAPPING)
public class OAuth2Controller extends OhmageController {
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
     * The path element for a user to respond to an authorization request.
     */
    public static final String PATH_AUTHORIZATION_WITH_TOKEN = "authorization_with_token";

    /**
     * The request parameter from a user when they are responding to an
     * authorization request indicating whether or not the are granting the
     * request.
     */
    public static final String PARAMETER_GRANTED = "granted";

    /**
     * The path and parameter key for ohmlet IDs.
     */
    public static final String KEY_OAUTH_CLIENT_ID = "id";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(OAuth2Controller.class.getName());

    private static final String REQUIRE_HTTPS_KEY = "ohmage.require_https";

    private static final String readRequireHttpsValue = ConfigurationFileImport
        .getCustomProperties()
        .getProperty(REQUIRE_HTTPS_KEY);
    /**
     * Configuration option that controls the https requirement. Default is true, where https is required.
     */
    private static final boolean REQUIRE_HTTPS =
        readRequireHttpsValue == null ? true : Boolean.parseBoolean(readRequireHttpsValue);

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
     */
    @RequestMapping(
        value = PATH_AUTHORIZE,
        method = RequestMethod.GET,
        params = "response_type" + "=" + "code")
    public static String authorize(
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl,
        @RequestParam(value = "client_id", required = true)
            final String clientId,
        @RequestParam(value = "scope", required = true)
            final String scopeString,
        @RequestParam(value = "redirect_uri", required = false)
            final URI redirectUri,
        @RequestParam(value = "state", required = false)
            final String state) {

        LOGGER.info("Creating a request to get a user's authorization.");

        LOGGER.info("Retrieving the OAuth client.");
        OAuthClient oAuthClient =
            OAuthClientBin.getInstance().getOAuthClient(clientId);

        LOGGER.info("Verifying that the OAuth client exists.");
        if(oAuthClient == null) {
            throw new InvalidArgumentException("The OAuth client is unknown.");
        }

        LOGGER.info("Validating the scopes.");
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

        LOGGER.info("Validating the redirect URI.");
        URI validatedRedirectUri;
        if(redirectUri == null) {
            LOGGER.info("Using the OAuth client's default redirect URI.");
            validatedRedirectUri = oAuthClient.getRedirectUri();
        }
        else {
            LOGGER.info("Using the supplied redirect URI: " + redirectUri);

            if(! isValidlyFormattedRedirectURIForOAuth(redirectUri)) {
                throw new InvalidArgumentException("The redirect URI is invalid for OAuth.");
            }

            LOGGER.info("Normalizing the redirect URI.");
            validatedRedirectUri = redirectUri.normalize();
        }

        LOGGER.info("Generating a new authorization code.");
        AuthorizationCode authorizationCode =
            new AuthorizationCode(
                oAuthClient.getId(),
                scopes,
                validatedRedirectUri,
                state);

        LOGGER.info("Storing the authorization code.");
        AuthorizationCodeBin.getInstance().addCode(authorizationCode);

        try {
            LOGGER.info("Building the redirect URI.");
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
        method = { RequestMethod.GET, RequestMethod.POST } )
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

        LOGGER.info("Handling a user's authorization code response.");

        LOGGER.info("Verifying that auth information was given.");
        if(email == null) {
            throw new AuthenticationException("No email address was given.");
        }
        if(password == null) {
            throw new AuthenticationException("No password was given.");
        }

        LOGGER.info("Retrieving the user associated with the token.");
        User user = UserBin.getInstance().getUserFromEmail(email);

        LOGGER.info("Verifying that the user exists.");
        if(user == null) {
            throw new AuthenticationException("The user is unknown.");
        }

        LOGGER.info("Verifying the user's password.");
        if(! user.verifyPassword(password)) {
            throw new AuthenticationException("The password was incorrect.");
        }

        return authorization(codeString, user, granted);
    }

    /**
     * <p>
     * Handles the response from the user regarding whether or not the user
     * granted permission to a OAuth client via OAuth. Convenience method for
     * the case where the ohmage user is already authenticated at the time of
     * the authorization step. If the user's token is invalid or there was a
     * general error reading the request, an error message will be returned and
     * displayed to the user. As long as there is not an internal error, we will
     * redirect the user back to the OAuth client with a code, which the OAuth
     * client can then use to call us to determine the user's response.
     * </p>
     *
     * @param authToken
     *        The user's authentication token.
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
        value = PATH_AUTHORIZATION_WITH_TOKEN,
        method = { RequestMethod.GET, RequestMethod.POST } )
    public static String authorization(
            @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
            @RequestParam(
                value = AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
                required = true)
            final String codeString,
            @RequestParam(value = PARAMETER_GRANTED, required = true)
            final boolean granted) {

        LOGGER.info("Handling a user's authorization code response.");

        User user = OhmageController.validateAuthorization(authToken, null);

        return authorization(codeString, user, granted);
    }

    /**
     * Handle the authorization flow for both email-password and token
     * authenticated users.
     */
    private static String authorization(String codeString, User user, boolean granted) {

        LOGGER.info("Retrieving the code.");
        AuthorizationCode code =
                AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.info("Verifying that the code exists.");
        if(code == null) {
            throw new InvalidArgumentException("The code is missing.");
        }

        LOGGER.info("Verifying that the code has not expired.");
        if(code.getExpirationTimestamp() < System.currentTimeMillis()) {
            throw new InvalidArgumentException("The code has expired.");
        }

        LOGGER.info("Retrieving the response.");
        AuthorizationCodeResponse response = code.getResponse();

        LOGGER.info("Verifying that the code has not yet been responded to.");

        if(response == null) {
            LOGGER.info("No response exists, so a new one is being created.");

            response =
                    new AuthorizationCodeResponse(
                            user.getId(),
                            granted);

            LOGGER.info("Updating the code with the response.");
            code =
                    (new AuthorizationCode.Builder(code))
                            .setUsedTimestamp(System.currentTimeMillis())
                            .setResponse(response)
                            .build();

            LOGGER.info("Storing the updated authorization code.");
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

        LOGGER.info("Building the specific redirect request for the user back " +
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

        LOGGER.info("Redirecting the user back to the OAuth client.");

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
     * @param oAuthClientId
     *        The unique identifier for the OAuth client that is exchanging the
     *        code for a token.
     *
     * @param oAuthClientSecret
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
            final String oAuthClientId,
        @RequestParam(value = "client_secret", required = true)
            final String oAuthClientSecret,
        @RequestParam(value = "code", required = true) final String codeString,
        @RequestParam(value = "redirect_uri", required = false)
            final URI redirectUri) {

        LOGGER.info("Creating a request to handle a user's authorization " +
                    "request response.");

        LOGGER.info("Verifying that the OAuth client ID was given.");
        if(oAuthClientId == null) {
            throw
                new AuthenticationException("The OAuth client ID is missing.");
        }

        LOGGER.info("Retrieving the OAuth client.");
        OAuthClient oAuthClient =
            OAuthClientBin.getInstance().getOAuthClient(oAuthClientId);

        LOGGER.info("Verifying the OAuth client exists.");
        if(oAuthClient == null) {
            throw new AuthenticationException("The OAuth client is unknown.");
        }

        LOGGER.info("Verifying that the OAuth client secret was given.");
        if(oAuthClientSecret == null) {
            throw
                new AuthenticationException(
                     "The OAuth client secret is missing.");
        }

        LOGGER.info("Verifying the OAuth client's secret.");
        if(! oAuthClient.getSecret().equals(oAuthClientSecret)) {
            throw
                new AuthenticationException(
                    "The OAuth client secret is incorrect.");
        }

        LOGGER.info("Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.info("Verifying that the code exists.");
        if(code == null) {
            throw new InvalidArgumentException("The code is uknown.");
        }

        LOGGER.info("Verifying that the code corresponds to this OAuth client.");
        if(! code.getOAuthClientId().equals(oAuthClientId)) {
            throw
                new InvalidArgumentException(
                    "This code belongs to a different OAuth client.");
        }

        LOGGER.info("Verifying that the code has not expired.");
        if(code.getExpirationTimestamp() < System.currentTimeMillis()) {
            throw new InvalidArgumentException("The code has expired.");
        }

        LOGGER.info("Validating the redirect URI.");
        if(redirectUri == null) {
            if(! oAuthClient.getRedirectUri().equals(code.getRedirectUri())) {
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

        LOGGER.info("Retrieving the code response.");
        AuthorizationCodeResponse response = code.getResponse();

        LOGGER.info("Verifying that the user has responded.");
        if(response == null) {
            throw
                new InvalidArgumentException(
                    "The user has not yet responded.");
        }

        LOGGER.info("Checking if the user accepted or declined the request.");
        if(! response.getGranted()) {
            throw
                new InvalidArgumentException("The user declined the request.");
        }

        LOGGER.info("Retrieving the first token that was created from this code " +
                    "response.");
        AuthorizationToken token =
            AuthorizationTokenBin
                .getInstance()
                .getTokenFromAuthorizationCode(code.getCode());

        if(token == null) {
            LOGGER.info("No such token exists, so we are creating a new one.");
            token = new AuthorizationToken(code);

            LOGGER.info("Storing the newly created token.");
            AuthorizationTokenBin.getInstance().addToken(token);
        }
        else {
            LOGGER.info("A token was already issued for this response. Checking " +
                        "if it has also been refreshed.");
            if(token.wasRefreshed()) {
                throw
                    new InvalidArgumentException(
                        "This code has already been used to create a token, " +
                            "and that token has already been refreshed.");
            }
        }

        LOGGER.info("Returning the token to the user.");
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
     * @param oAuthClientId
     *        The unique identifier for the OAuth client that is exchanging the
     *        code for a token.
     *
     * @param oAuthClientSecret
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
            final String oAuthClientId,
        @RequestParam(value = "client_secret", required = true)
            final String oAuthClientSecret,
        @RequestParam(value = "refresh_token", required = true)
            final String refreshToken
        /* FIXME: Scope? */) {

        LOGGER.info("Creating a request to handle a user's authorization " +
                    "request response.");

        LOGGER.info("Verifying that the OAuth client ID was given.");
        if(oAuthClientId == null) {
            throw
                new AuthenticationException("The OAuth client ID is missing.");
        }

        LOGGER.info("Retrieving the OAuth client.");
        OAuthClient oAuthClient =
            OAuthClientBin.getInstance().getOAuthClient(oAuthClientId);

        LOGGER.info("Verifying the OAuth client exists.");
        if(oAuthClient == null) {
            throw new AuthenticationException("The OAuth client is unknown.");
        }

        LOGGER.info("Verifying that the OAuth client secret was given.");
        if(oAuthClientSecret == null) {
            throw
                new AuthenticationException(
                     "The OAuth client secret is missing.");
        }

        LOGGER.info("Verifying the OAuth client's secret.");
        if(! oAuthClient.getSecret().equals(oAuthClientSecret)) {
            throw
                new AuthenticationException(
                    "The OAuth client secret is incorrect.");
        }

        LOGGER.info("Retrieving the original token.");
        AuthorizationToken originalToken =
            AuthorizationTokenBin
                .getInstance()
                .getTokenFromRefreshToken(refreshToken);

        LOGGER.info("Verifying that this token exists.");
        if(originalToken == null) {
            throw new InvalidArgumentException("The token is unknown.");
        }

        LOGGER.info("Verifying that this refresh token was issued via an " +
                    "authorization code.");
        if(originalToken.getAuthorizationCode() == null) {
            throw
                new InvalidArgumentException(
                    "This refresh token was not issued via an authorization " +
                        "flow, so it cannot be refreshed via this call.");
        }

        LOGGER.info("Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin
                .getInstance()
                .getCode(originalToken.getAuthorizationCode());

        LOGGER.info("Verifying that the code exists.");
        if(code == null) {
            throw new IllegalStateException("The code is uknown.");
        }

        LOGGER.info("Verifying that the code corresponds to this OAuth client.");
        if(! code.getOAuthClientId().equals(oAuthClientId)) {
            throw
                new InvalidArgumentException(
                    "This code belongs to a different OAuth client.");
        }

        LOGGER.info("Checking if the token has been refreshed.");
        AuthorizationToken newToken;
        if(originalToken.wasRefreshed()) {
            LOGGER.info("The token has been refreshed.");

            LOGGER.info("Retrieving the refreshed token.");
            newToken =
                AuthorizationTokenBin
                    .getInstance()
                    .getTokenFromAccessToken(originalToken.getNextToken());

            LOGGER.info("Verifing that the refreshed token has not also been " +
                        "refreshed.");
            if(newToken.wasRefreshed()) {
                throw
                    new InvalidArgumentException(
                        "This token has already been refreshed and its " +
                            "refreshed token has also been refreshed.");
            }
        }
        else {
            LOGGER.info("The token has not been refreshed.");

            LOGGER.info("Creating a new token.");
            newToken = new AuthorizationToken(originalToken);

            LOGGER.info("Storing the new token.");
            AuthorizationTokenBin.getInstance().addToken(newToken);

            LOGGER.info("Updating the original token to reference this new " +
                        "token.");
            AuthorizationToken invalidatedOldToken =
                (new AuthorizationToken.Builder(originalToken))
                    .setNextToken(newToken.getAccessToken())
                    .build();

            LOGGER.info("Updating the original token.");
            AuthorizationTokenBin
                .getInstance()
                .updateToken(invalidatedOldToken);
        }

        LOGGER.info("Returning the new token.");
        return newToken;
    }

    /**
     * Creates a new OAuth client.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param oAuthClientBuilder
     *        A builder to use to this new OAuth client.
     *
     * @return The constructed OAuth client.
     */
    @RequestMapping(
        value = "/clients",
        method = RequestMethod.POST)
    public static @ResponseBody ResponseEntity<OAuthClient> createClient(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @RequestBody final OAuthClient.Builder oAuthClientBuilder) {

        LOGGER.info("Creating a new OAuth client.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.debug("Setting the owner of the OAuth client.");
        oAuthClientBuilder.setOwner(user.getId());

        if(oAuthClientBuilder.getRedirectUri() != null) {
            LOGGER.debug("Redirect URI is set to ["+oAuthClientBuilder.getRedirectUri()+"], scheme=["+oAuthClientBuilder.getRedirectUri().getScheme()+"]");
            if(REQUIRE_HTTPS) {
                if (!"https".equals(oAuthClientBuilder.getRedirectUri().getScheme())) {
                    throw new InvalidArgumentException("The redirect URI is required to use https.");
                }
            }
            if(! isValidlyFormattedRedirectURIForOAuth(oAuthClientBuilder.getRedirectUri())) {
                throw new InvalidArgumentException("The redirect URI is invalid for OAuth.");
            }

            LOGGER.info("Normalizing the redirect URI.");
            oAuthClientBuilder
                .setRedirectUri(
                    oAuthClientBuilder.getRedirectUri().normalize());
        }

        LOGGER.debug("Building the OAuth client.");
        OAuthClient oAuthClient = oAuthClientBuilder.build();

        LOGGER.info("Saving the new OAuth client.");
        OAuthClientBin.getInstance().addOAuthClient(oAuthClient);

        LOGGER.info("Building the headers.");
        HttpHeaders headers = new HttpHeaders();

        LOGGER.info("Extracting the OAuth client's shared secret.");
        headers
            .add(OAuthClient.JSON_KEY_SHARED_SECRET, oAuthClient.getSecret());

        LOGGER.info("Returning the OAuth client.");
        return
            new ResponseEntity<OAuthClient>(
                    oAuthClient,
                headers,
                HttpStatus.OK);
    }

    /**
     * Creates a new OAuth client.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param oAuthClientBuilder
     *        A builder to use to this new OAuth client.
     *
     * @return The constructed OAuth client.
     */
    @RequestMapping(
        value = "/clients/{" + KEY_OAUTH_CLIENT_ID + "}",
        method = RequestMethod.POST)
    public static @ResponseBody ResponseEntity<OAuthClient> updateClient(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
        final AuthorizationToken authToken,
        @PathVariable(KEY_OAUTH_CLIENT_ID) final String oauthClientId,
        @RequestBody final OAuthClient.Builder oAuthClientBuilder) {

        LOGGER.info("Creating a new OAuth client.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.info("Retrieving the OAuth client.");
        OAuthClient oauthClient =
            OAuthClientBin.getInstance().getOAuthClient(oauthClientId);

        LOGGER.debug("Setting the owner of the OAuth client.");
        oAuthClientBuilder.setOwner(user.getId());

        if(oAuthClientBuilder.getRedirectUri() != null) {
            LOGGER.debug("Redirect URI is set to ["+oAuthClientBuilder.getRedirectUri()+"], scheme=["+oAuthClientBuilder.getRedirectUri().getScheme()+"]");
            if(REQUIRE_HTTPS) {
                if (!"https".equals(oAuthClientBuilder.getRedirectUri().getScheme())) {
                    throw new InvalidArgumentException("The redirect URI is required to use https.");
                }
            }
            if(! isValidlyFormattedRedirectURIForOAuth(oAuthClientBuilder.getRedirectUri())) {
                throw new InvalidArgumentException("The redirect URI is invalid for OAuth.");
            }

            LOGGER.info("Normalizing the redirect URI.");
            oAuthClientBuilder
                .setRedirectUri(
                    oAuthClientBuilder.getRedirectUri().normalize());
        }
        LOGGER.debug("Creating a new builder based on the existing OAuth client.");
        OAuthClient.Builder newOAuthClientBuilder =
            new OAuthClient.Builder(oauthClient);


        LOGGER.debug("Merging the changes into the old ohmlet.");
        newOAuthClientBuilder.merge(oAuthClientBuilder);

        LOGGER.debug("Building the OAuth client.");
        OAuthClient oAuthClient = newOAuthClientBuilder.build();

        LOGGER.info("Saving the new OAuth client.");
        OAuthClientBin.getInstance().updateOAuthClient(oAuthClient);

        LOGGER.info("Building the headers.");
        HttpHeaders headers = new HttpHeaders();

        LOGGER.info("Extracting the OAuth client's shared secret.");
        headers
            .add(OAuthClient.JSON_KEY_SHARED_SECRET, oAuthClient.getSecret());

        LOGGER.info("Returning the OAuth client.");
        return
            new ResponseEntity<OAuthClient>(
                oAuthClient,
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
     * @return The set oSf client unique identifiers that are owned by the
     *         requesting user.
     */
    @RequestMapping(
        value = "/clients",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<MultiValueResult<String>> getClients(
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken) {

        LOGGER.info("Creating a request to retrieve the user-owned client IDs.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.info("Retrieving the visible client IDs.");
        MultiValueResult<String> clientIds =
            OAuthClientBin.getInstance().getClientIds(user.getId());

        LOGGER.info("Building the paging headers.");
        HttpHeaders headers =
            OhmageController
                .buildPagingHeaders(
                        0,
                        Long.MAX_VALUE,
                        Collections.<String, String>emptyMap(),
                        clientIds,
                        rootUrl + ROOT_MAPPING + "/clients");

        LOGGER.info("Creating the response object.");
        ResponseEntity<MultiValueResult<String>> result =
            new ResponseEntity<MultiValueResult<String>>(
                clientIds,
                headers,
                HttpStatus.OK);

        LOGGER.info("Returning the codes.");
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
        value = "/clients/{" + OAuthClient.JSON_KEY_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody
    OAuthClient getClients(
        @PathVariable(OAuthClient.JSON_KEY_ID) final String clientId) {

        LOGGER.info("Creating a request to retrieve the client's information.");

        LOGGER.info("Retrieving the client information.");
        OAuthClient client =
            OAuthClientBin.getInstance().getOAuthClient(clientId);

        LOGGER.info("Verifying that the client is known.");
        if(client == null) {
            throw new UnknownEntityException("The client is unknown.");
        }

        LOGGER.info("Returning the client's information.");
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
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl,
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken) {

        LOGGER.info("Creating a request to retrieve the codes that a user has " +
                    "approved.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.info("Retrieving the set of codes that the user has approved.");
        MultiValueResult<String> codes =
            AuthorizationCodeBin.getInstance().getCodes(user.getId());

        LOGGER.info("Building the paging headers.");
        HttpHeaders headers =
            OhmageController
                .buildPagingHeaders(
                        0,
                        Long.MAX_VALUE,
                        Collections.<String, String>emptyMap(),
                        codes,
                        rootUrl + ROOT_MAPPING + "/codes");

        LOGGER.info("Creating the response object.");
        ResponseEntity<MultiValueResult<String>> result =
            new ResponseEntity<MultiValueResult<String>>(
                codes,
                headers,
                HttpStatus.OK);

        LOGGER.info("Returning the codes.");
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

        LOGGER.info("Creating a request to retrieve the information about an " +
                    "authorization code.");

        LOGGER.info("Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.info("Verifying that the code exists.");
        if(code == null) {
            throw new UnknownEntityException("The code is unknown.");
        }

        LOGGER.info("Checking if the code has been responded to.");
        if(code.getResponse() != null) {
            LOGGER.info("The code has been responded to.");

            LOGGER.info("Validating the user from the token");
            User user = OhmageController.validateAuthorization(authToken, null);

            LOGGER.info("Verifying that the requesting user is responder for " +
                        "the code.");

            if(! user.getId().equals(code.getResponse().getUserId())) {
                throw
                    new InsufficientPermissionsException(
                        "The requesting user is not the responder for this " +
                            "code.");
            }
        }

        LOGGER.info("Returning the code.");
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

        LOGGER.info("Creating a request to invalidate an authorization code.");

        LOGGER.info("Validating the user from the token");
        User user = OhmageController.validateAuthorization(authToken, null);

        LOGGER.info("Retrieving the code.");
        AuthorizationCode code =
            AuthorizationCodeBin.getInstance().getCode(codeString);

        LOGGER.info("Verifying that the code exists.");
        if(code == null) {
            throw new UnknownEntityException("The code is unknown.");
        }

        LOGGER.info("Verifying that the requesting user is responder for the " +
                    "code.");
        if(! user.getId().equals(code.getResponse().getUserId())) {
            throw
                new InsufficientPermissionsException(
                    "The requesting user is not the responder for this code.");
        }

        LOGGER.info("Invalidating the code response.");
        AuthorizationCode updatedCode =
            (new AuthorizationCode.Builder(code))
                .setResponse(
                    (new AuthorizationCodeResponse.Builder(code.getResponse()))
                        .setInvalidationTimestamp(System.currentTimeMillis())
                        .build())
                .build();

        LOGGER.info("Storing the updated code.");
        AuthorizationCodeBin.getInstance().updateCode(updatedCode);
    }

    /**
     * OAuth2 redirect URIs must be absolute and cannot contain fragments.
     *
     * @param redirectURI
     * @return
     */
    private static boolean isValidlyFormattedRedirectURIForOAuth(URI redirectURI) {
        return (redirectURI == null) ? true :
            redirectURI.isAbsolute() && redirectURI.getRawFragment() == null;
    }
}
