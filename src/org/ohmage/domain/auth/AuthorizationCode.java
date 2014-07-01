package org.ohmage.domain.auth;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * An authorization code that is generated on behalf of a OAuth client for a
 * specific user and is used to track the user's response to the OAuth client's
 * request to view their data.
 * </p>
 *
 * @author John Jenkins
 */
public class AuthorizationCode extends OhmageDomainObject {
    /**
     * <p>
     * A builder for {@link AuthorizationCode} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder
        extends OhmageDomainObject.Builder<AuthorizationCode> {

        /**
         * The unique code for this authorization code.
         */
        private final String code;
        /**
         * The unique identifier for the OAuth client that requested this code.
         */
        private final String oAuthClientId;
        /**
         * The set of scopes that are being requested.
         */
        private final Set<Scope> scopes;
        /**
         * The URI to use to redirect the user after they have completed the
         * authorization flow.
         */
        private final URI redirectUri;
        /**
         * A string that should simply be carried around and ultimately echoed
         * back to the OAuth client to reinitialize their state.
         */
        private final String state;
        /**
         * The number of milliseconds since the Unix epoch when this code was
         * created.
         */
        private final long creationTimestamp;
        /**
         * The number of milliseconds since the Unix epoch when this code expires.
         */
        private final Long expirationTimestamp;
        /**
         * The number of milliseconds since the Unix epoch when this code was used.
         */
        private Long usedTimestamp;
        /**
         * The response to this authorization code.
         */
        private AuthorizationCodeResponse response;

        /**
         * Creates a new builder based on the given {@link AuthorizationCode}.
         *
         * @param code
         *        The {@link AuthorizationCode} to use to initialize this
         *        builder.
         */
        public Builder(final AuthorizationCode code) {
            super(code);

            this.code = code.code;
            oAuthClientId = code.oAuthClientId;
            scopes = new HashSet<Scope>(code.scopes);
            redirectUri = code.redirectUri;
            state = code.state;
            creationTimestamp = code.creationTimestamp;
            expirationTimestamp = code.expirationTimestamp;
            usedTimestamp = code.usedTimestamp;
            response = code.response;
        }

        /**
         * Sets the time-stamp when the user responded to the code.
         *
         * @param usedTimestamp
         *        The time-stamp when the user responded to the code.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setUsedTimestamp(final Long usedTimestamp) {
            this.usedTimestamp = usedTimestamp;

            return this;
        }

        /**
         * Sets the response for this authorization code.
         *
         * @param response
         *        The response for this authorization code.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setResponse(final AuthorizationCodeResponse response) {
            this.response = response;

            return this;
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.OhmageDomainObject.Builder#build()
         */
        @Override
        public AuthorizationCode build() {
            return
                new AuthorizationCode(
                    code,
                    oAuthClientId,
                    scopes,
                    redirectUri,
                    state,
                    creationTimestamp,
                    expirationTimestamp,
                    usedTimestamp,
                    response,
                    internalReadVersion,
                    internalReadVersion);
        }
    }

    /**
     * The JSON key for the authorization code value.
     */
    public static final String JSON_KEY_AUTHORIZATION_CODE = "code";
    /**
     * The JSON key for the OAuth client's unique identifier.
     */
    public static final String JSON_KEY_OAUTH_CLIENT_ID = "oauth_client_id";
    /**
     * The JSON key for the scopes.
     */
    public static final String JSON_KEY_SCOPES = "scopes";
    /**
     * The JSON key for the redirect URI.
     */
    public static final String JSON_KEY_REDIRECT_URI = "redirect_uri";
    /**
     * The JSON key for the state.
     */
    public static final String JSON_KEY_STATE = "state";
    /**
     * The JSON key for when this code was created.
     */
    public static final String JSON_KEY_CREATION_TIMESTAMP =
        "creation_timestamp";
    /**
     * The JSON key for when this code expires.
     */
    public static final String JSON_KEY_EXPIRATION_TIMESTAMP =
        "expiration_timestamp";
    /**
     * The JSON key for when this code was used.
     */
    public static final String JSON_KEY_USED_TIMESTAMP =
        "used_timestamp";
    /**
     * The JSON key for the response to this authorization code.
     */
    public static final String JSON_KEY_RESPONSE = "response";

    /**
     * The number of milliseconds before the code expires.
     */
    private static final long CODE_DURATION = 1000 * 60 * 5;

    /**
     * The unique code for this authorization code.
     */
    @JsonProperty(JSON_KEY_AUTHORIZATION_CODE)
    private final String code;
    /**
     * The OAuth client's unique identifier.
     */
    @JsonProperty(JSON_KEY_OAUTH_CLIENT_ID)
    private final String oAuthClientId;
    /**
     * The set of scopes that are being requested by this OAuth client.
     */
    @JsonProperty(JSON_KEY_SCOPES)
    private final Set<Scope> scopes;
    /**
     * The URI to use to redirect the user after they have completed the
     * authorization flow.
     */
    @JsonProperty(JSON_KEY_REDIRECT_URI)
    private final URI redirectUri;
    /**
     * A string provided by the OAuth client when the request for this code was
     * made and that will be echoed back to the OAuth client to help them
     * reinitialize their state.
     */
    @JsonProperty(JSON_KEY_STATE)
    private final String state;
    /**
     * The number of milliseconds since the Unix epoch when this code was
     * created.
     */
    @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
    private final long creationTimestamp;
    /**
     * The number of milliseconds since the Unix epoch when this code expires.
     */
    @JsonProperty(JSON_KEY_EXPIRATION_TIMESTAMP)
    private final long expirationTimestamp;
    /**
     * The number of milliseconds since the Unix epoch when this code was used.
     */
    @JsonProperty(JSON_KEY_USED_TIMESTAMP)
    private final Long usedTimestamp;
    /**
     * The response to this authorization code.
     */
    @JsonProperty(JSON_KEY_RESPONSE)
    private final AuthorizationCodeResponse response;

    /**
     * Creates a new code.
     *
     * @param oAuthClientId
     *        The unique identifier for the OAuth client that is requesting the
     *        code.
     *
     * @param scopes
     *        The set of scopes being requested by the OAuth client.
     *
     * @param redirectUri
     *        The URI to use to redirect the user after they have completed the
     *        authorization flow.
     *
     * @param state
     *        A string that will be echoed around and ultimately returned to
     *        the OAuth client to help them reinitialize their state.
     *
     * @throws InvalidArgumentException
     *         One of the parameters was invalid.
     */
    public AuthorizationCode(
        final String oAuthClientId,
        final Set<Scope> scopes,
        final URI redirectUri,
        final String state)
        throws InvalidArgumentException {

        this(
            generateCode(),
                oAuthClientId,
            scopes,
            redirectUri,
            state,
            System.currentTimeMillis(),
            System.currentTimeMillis() + CODE_DURATION,
            null,
            null,
            null);
    }

    /**
     * Recreates an existing authorization code.
     *
     * @param code
     *        The unique code value for this authorization code.
     *
     * @param oAuthClientId
     *        The unique identifier for the OAuth client that is requesting the
     *        code.
     *
     * @param scopes
     *        The set of scopes being requested by the OAuth client.
     *
     * @param redirectUri
     *        The URI to use to redirect the user after they have completed the
     *        authorization flow.
     *
     * @param state
     *        A string that will be echoed around and ultimately returned to
     *        the OAuth client to help them reinitialize their state.
     *
     * @param creationTimestamp
     *        The number of milliseconds since the Unix epoch when
     *        authorization code was made.
     *
     * @param expirationTimestamp
     *        The number of milliseconds since the Unix epoch when
     *        authorization code expires.
     *
     * @param usedTimestamp
     *        The number of milliseconds since the Unix epoch when
     *        authorization code was used.
     *
     * @param response
     *        The response to this authorization code, or null if no response
     *        has been made.
     *
     * @param internalVersion
     *        The internal version of this authorization code.
     *
     * @throws InvalidArgumentException
     *         One of the parameters was invalid.
     */
    @JsonCreator
    protected AuthorizationCode(
        @JsonProperty(JSON_KEY_AUTHORIZATION_CODE) final String code,
        @JsonProperty(JSON_KEY_OAUTH_CLIENT_ID) final String oAuthClientId,
        @JsonProperty(JSON_KEY_SCOPES) final Set<Scope> scopes,
        @JsonProperty(JSON_KEY_REDIRECT_URI) final URI redirectUri,
        @JsonProperty(JSON_KEY_STATE) final String state,
        @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
            final Long creationTimestamp,
        @JsonProperty(JSON_KEY_EXPIRATION_TIMESTAMP)
            final Long expirationTimestamp,
        @JsonProperty(JSON_KEY_USED_TIMESTAMP) final Long usedTimestamp,
        @JsonProperty(JSON_KEY_RESPONSE)
            final AuthorizationCodeResponse response,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        this(
            code,
                oAuthClientId,
            scopes,
            redirectUri,
            state,
            creationTimestamp,
            expirationTimestamp,
            usedTimestamp,
            response,
            internalVersion,
            null);
    }

    /**
     * Validates and builds the authorization code.
     *
     * @param code
     *        The unique code value for this authorization code.
     *
     * @param oAuthClientId
     *        The unique identifier for the OAuth client that is requesting the
     *        code.
     *
     * @param scopes
     *        The set of scopes being requested by the OAuth client.
     *
     * @param redirectUri
     *        The URI to use to redirect the user after they have completed the
     *        authorization flow.
     *
     * @param state
     *        A string that will be echoed around and ultimately returned to
     *        the OAuth client to help them reinitialize their state.
     *
     * @param creationTimestamp
     *        The number of milliseconds since the Unix epoch when
     *        authorization code was made.
     *
     * @param expirationTimestamp
     *        The number of milliseconds since the Unix epoch when
     *        authorization code expires.
     *
     * @param usedTimestamp
     *        The number of milliseconds since the Unix epoch when
     *        authorization code was used.
     *
     * @param response
     *        The response to this authorization code, or null if no response
     *        has been made.
     *
     * @param internalReadVersion
     *        The internal version of this authorization code when it was read
     *        from the database.
     *
     * @param internalWriteVersion
     *        The new internal version of this authorization code when it will
     *        be written back to the database.
     *
     * @throws InvalidArgumentException
     *         One of the parameters was invalid.
     */
    private AuthorizationCode(
        final String code,
        final String oAuthClientId,
        final Set<Scope> scopes,
        final URI redirectUri,
        final String state,
        final Long creationTimestamp,
        final Long expirationTimestamp,
        final Long usedTimestamp,
        final AuthorizationCodeResponse response,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        // Pass the versioning parameters to the parent.
        super(internalReadVersion, internalWriteVersion);

        if(code == null) {
            throw new IllegalArgumentException("The code is null.");
        }
        if(oAuthClientId == null) {
            throw new InvalidArgumentException("The OAuth client ID is null.");
        }
        if(scopes == null) {
            throw new InvalidArgumentException("The scopes are null.");
        }
        if(redirectUri == null) {
            throw new InvalidArgumentException("The redirect URI is null.");
        }
        if(scopes.size() == 0) {
            throw new InvalidArgumentException("The scopes are empty.");
        }
        if(creationTimestamp == null) {
            throw
                new InvalidArgumentException(
                    "The creation timestamp is null.");
        }
        if(expirationTimestamp == null) {
            throw
                new InvalidArgumentException(
                    "The expiration timestamp is null.");
        }
        if(creationTimestamp > expirationTimestamp) {
            throw
                new InvalidArgumentException(
                    "The timestamp expired before it was created.");
        }

        this.code = code;
        this.oAuthClientId = oAuthClientId;
        this.scopes = Collections.unmodifiableSet(new HashSet<Scope>(scopes));
        this.redirectUri = redirectUri;
        this.state = state;
        this.creationTimestamp = creationTimestamp;
        this.expirationTimestamp = expirationTimestamp;
        this.response = response;
        this.usedTimestamp = usedTimestamp;
    }

    /**
     * Returns the value of this code.
     *
     * @return The value of this code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the OAuth client's unique identifier.
     *
     * @return The OAuth client's unique identifier.
     */
    public String getOAuthClientId() {
        return oAuthClientId;
    }

    /**
     * Returns the set of scopes.
     *
     * @return The set of scopes.
     */
    public Set<Scope> getScopes() {
        return scopes;
    }

    /**
     * Returns the URI to redirect the user to after they have completed the
     * authorization flow.
     *
     * @return The URI to redirect the user to after they have completed the
     *         authorization flow.
     */
    public URI getRedirectUri() {
        return redirectUri;
    }

    /**
     * Returns the state.
     *
     * @return The state.
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the number of milliseconds since the Unix epoch when this code
     * was created.
     *
     * @return The number of milliseconds since the Unix epoch when this code
     *         was created.
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Returns the number of milliseconds since the Unix epoch when this code
     * expires.
     *
     * @return The number of milliseconds since the Unix epoch when this code
     *         expires.
     */
    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    /**
     * Returns the number of milliseconds since the Unix epoch when this code
     * was used, which may be null if it has not been used.
     *
     * @return The number of milliseconds since the Unix epoch when this code
     *         was used, which may be null if it has not been used.
     */
    public Long getUsedTimestamp() {
        return usedTimestamp;
    }

    /**
     * Returns the response to this authorization code.
     *
     * @return The response to this authorization code.
     */
    public AuthorizationCodeResponse getResponse() {
        return response;
    }

    /**
     * Generates a globally unique random code.
     *
     * @return A globally unique random code.
     */
    private static String generateCode() {
        return UUID.randomUUID().toString();
    }
}