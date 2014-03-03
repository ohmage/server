package org.ohmage.mongodb.domain;

import java.net.URI;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.auth.OauthClient;
import org.ohmage.mongodb.bin.MongoOauthClientBin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link OauthClient} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoOauthClientBin.COLLECTION_NAME)
public class MongoOauthClient extends OauthClient implements MongoDbObject {
    /**
     * The database ID for this object.
     */
    @ObjectId
    private final String dbId;

    /**
     * Creates a {@link OauthClient} object via Jackson from the data layer.
     *
     * @param dbId
     *        The database ID for this authentication token.
     *
     * @param oauthClientId
     *        The unique ID for this OAuth client
     *
     * @param sharedSecret
     *        The secret that will be used to authenticate this OAuth client.
     *
     * @param owner
     *        The ID for the user that created this OAuth client.
     *
     * @param name
     *        A user-friendly name for this OAuth client.
     *
     * @param description
     *        A user-friendly explanation of who this OAuth client is.
     *
     * @param redirectUri
     *        The URI to redirect the user back to after they have granted or
     *        rejected this OAuth client's authorization request.
     *
     * @throws OmhException
     *         Any of the parameters is null or empty.
     */
    @JsonCreator
    protected MongoOauthClient(
        @Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_ID) final String oauthClientId,
        @JsonProperty(JSON_KEY_SHARED_SECRET) final String sharedSecret,
        @JsonProperty(JSON_KEY_OWNER) final String owner,
        @JsonProperty(JSON_KEY_NAME) final String name,
        @JsonProperty(JSON_KEY_DESCRIPTION) final String description,
        @JsonProperty(JSON_KEY_REDIRECT_URI) final URI redirectUri,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) {

        super(
            oauthClientId,
            sharedSecret,
            owner,
            name,
            description,
            redirectUri,
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