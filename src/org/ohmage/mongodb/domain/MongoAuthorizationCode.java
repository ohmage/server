package org.ohmage.mongodb.domain;

import java.net.URI;
import java.util.Set;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.auth.AuthorizationCodeResponse;
import org.ohmage.domain.auth.Scope;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.bin.MongoAuthorizationCodeBin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link AuthorizationCode} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoAuthorizationCodeBin.COLLECTION_NAME)
public class MongoAuthorizationCode
    extends AuthorizationCode
    implements MongoDbObject {

    /**
     * The database ID for this object.
     */
    @ObjectId
    private final String dbId;

    /**
     * Creates an {@link AuthorizationCode} object via Jackson from the data
     * layer.
     *
     * @param dbId
     *        The database ID for this authorization token.
     *
     * @param code
     *        The unique code value for this authorization code.
     *
     * @param oauthClientId
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
     * @param authorizationCodeResponse
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
    protected MongoAuthorizationCode(
        @Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_AUTHORIZATION_CODE) final String code,
        @JsonProperty(JSON_KEY_OAUTH_CLIENT_ID) final String oauthClientId,
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
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) {

        super(
            code,
            oauthClientId,
            scopes,
            redirectUri,
            state,
            creationTimestamp,
            expirationTimestamp,
            usedTimestamp,
            response,
            internalVersion);

        // Store the MongoDB ID.
        if(dbId == null) {
            throw new IllegalArgumentException("The MongoDB ID is missing.");
        }
        else {
            this.dbId = dbId;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.mongodb.domain.MongoDbObject#getDatabaseId()
     */
    @Override
    @ObjectId
    public String getDbId() {
        return dbId;
    }
}