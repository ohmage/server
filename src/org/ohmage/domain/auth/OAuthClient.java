package org.ohmage.domain.auth;

import java.net.URI;
import java.util.UUID;

import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A OAuth client system that has registered itself to request access for
 * users' data.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(OAuthClient.JACKSON_FILTER_GROUP_ID)
public class OAuthClient extends OhmageDomainObject {
    /**
     * <p>
     * A builder for {@link OAuthClient}
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder
        extends OhmageDomainObject.Builder<OAuthClient> {

        /**
         * The unique identifier for this OAuth client.
         */
        private String id;
        /**
         * The shared secret for this OAuth client that will be used
         */
        private String sharedSecret;
        /**
         * The user that created this OAuth client identity.
         */
        private String owner;
        /**
         * A user-friendly name for this OAuth client.
         */
        private final String name;
        /**
         * A user-friendly description for this OAuth client.
         */
        private final String description;
        /**
         * The URI to use to redirect the user after authorization has been
         * granted or not.
         */
        private final URI redirectUri;

        /**
         * Creates a new builder with the given fields.
         *
         * @param name
         *        A user-friendly name for this OAuth client.
         *
         * @param description
         *        A user-friendly explanation of who this OAuth client is.
         *
         * @param redirectUri
         *        The URI to redirect the user back to after they have granted
         *        or rejected this OAuth client's authorization request.
         */
        public Builder(
            @JsonProperty(JSON_KEY_NAME) final String name,
            @JsonProperty(JSON_KEY_DESCRIPTION) final String description,
            @JsonProperty(JSON_KEY_REDIRECT_URI) final URI redirectUri) {

            super(null);

            this.name = name;
            this.description = description;
            this.redirectUri = redirectUri;
        }

        /**
         * Creates a new builder based on the given OauthClient object.
         *
         * @param original
         *        The OauthClient object on which this builder should be based.
         */
        public Builder(final OAuthClient original) {
            super(original);

            id = original.id;
            sharedSecret = original.sharedSecret;
            owner = original.owner;
            name = original.name;
            description = original.description;
            redirectUri = original.redirectUri;
        }

        /**
         * Returns the currently set redirect URI.
         *
         * @return The currently set redirect URI.
         */
        public URI getRedirectUri() {
            return redirectUri;
        }

        /**
         * Sets the redirect URI.
         *
         * @param redirectUri
         *        The URI to redirect the user back to after completing the
         *        authorization flow.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setRedirectUri(final URI redirectUri) {
            return this;
        }

        /**
         * Sets the owner.
         *
         * @param owner
         *        The unique identifier of the user that owns this entity.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setOwner(final String owner) {
            this.owner = owner;

            return this;
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.OhmageDomainObject.Builder#build()
         */
        @Override
        public OAuthClient build() {
            return
                new OAuthClient(
                    (id == null) ? generateId() : id,
                    (sharedSecret == null) ? generateSharedSecret() : sharedSecret,
                    owner,
                    name,
                    description,
                    redirectUri,
                    internalReadVersion,
                    internalWriteVersion);
        }
    }

    /**
     * The JSON key for the ID.
     */
    public static final String JSON_KEY_ID = "client_id";
    /**
     * The JSON key for the shared secret.
     */
    public static final String JSON_KEY_SHARED_SECRET = "shared_secret";
    /**
     * The JSON key for the identifier of a user that created this OAuth
     * client.
     */
	public static final String JSON_KEY_OWNER = "owner";
	/**
	 * The JSON key for the name.
	 */
	public static final String JSON_KEY_NAME = "name";
	/**
	 * The JSON key for the description.
	 */
	public static final String JSON_KEY_DESCRIPTION = "description";
	/**
	 * The JSON key for the redirect URI.
	 */
	public static final String JSON_KEY_REDIRECT_URI = "redirect_uri";

    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.auth.OauthClient";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(OAuthClient.class);
    }

	/**
	 * The user that created this OAuth client identity.
	 */
	@JsonProperty(JSON_KEY_OWNER)
	private final String owner;
	/**
	 * The unique identifier for this OAuth client.
	 */
	@JsonProperty(JSON_KEY_ID)
	private final String id;
	/**
	 * The shared secret for this OAuth client that will be used
	 */
	@JsonProperty(JSON_KEY_SHARED_SECRET)
	@JsonFilterField
	private final String sharedSecret;
	/**
	 * A user-friendly name for this OAuth client.
	 */
	@JsonProperty(JSON_KEY_NAME)
	private final String name;
	/**
	 * A user-friendly description for this OAuth client.
	 */
	@JsonProperty(JSON_KEY_DESCRIPTION)
	private final String description;
	/**
	 * The URI to use to redirect the user after authorization has been granted
	 * or not.
	 */
	@JsonProperty(JSON_KEY_REDIRECT_URI)
	private final URI redirectUri;

	/**
	 * Creates a new OauthClient object.
	 *
	 * @param owner
	 *        The user creating this OAuth client entity.
	 *
	 * @param name
	 *        A user-friendly name for this OAuth client.
	 *
	 * @param description
	 *        A user-friendly explanation of who this OAuth client is.
	 *
	 * @param redirectUri
	 *        The URI to use to redirect the user to after authorization has or
	 *        has not been granted.
     *
     * @throws InvalidArgumentException
     *         Any of the user-supplied parameters is invalid.
     *
     * @throws InvalidArgumentException
     *         Any of the server-generated parameters is invalid.
	 */
	public OAuthClient(
            final String owner,
            final String name,
            final String description,
            final URI redirectUri)
		throws InvalidArgumentException, IllegalArgumentException {

	    this(
            generateId(),
            generateSharedSecret(),
	        owner,
	        name,
	        description,
	        redirectUri,
	        null);
	}

	/**
	 * Rebuilds an existing OauthClient object.
     *
     * @param id
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
	protected OAuthClient(
            @JsonProperty(JSON_KEY_ID) final String id,
            @JsonProperty(JSON_KEY_SHARED_SECRET) final String sharedSecret,
            @JsonProperty(JSON_KEY_OWNER) final String owner,
            @JsonProperty(JSON_KEY_NAME) final String name,
            @JsonProperty(JSON_KEY_DESCRIPTION) final String description,
            @JsonProperty(JSON_KEY_REDIRECT_URI) final URI redirectUri,
            @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws InvalidArgumentException, IllegalArgumentException {

	    this(
	        id,
	        sharedSecret,
	        owner,
	        name,
	        description,
	        redirectUri,
	        internalVersion,
	        null);
	}

   /**
     * Constructs a OauthClient object.
     *
     * @param id
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
    private OAuthClient(
            final String id,
            final String sharedSecret,
            final String owner,
            final String name,
            final String description,
            final URI redirectUri,
            final Long internalReadVersion,
            final Long internalWriteVersion) {

        super(internalReadVersion, internalWriteVersion);

        // Validate the parameters.
        if(id == null) {
            throw new IllegalArgumentException("The ID is null.");
        }
        if(sharedSecret == null) {
            throw new IllegalArgumentException("The shared secret is null.");
        }
        if(owner == null) {
            throw new IllegalArgumentException("The owner is null.");
        }
        if(name == null) {
            throw new InvalidArgumentException("The name is null.");
        }
        if(description == null) {
            throw new InvalidArgumentException("The description is null.");
        }
        if(redirectUri == null) {
            throw new InvalidArgumentException("The redirect URI is null.");
        }

        this.id = id;
        this.sharedSecret = sharedSecret;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.redirectUri = redirectUri;
    }

	/**
	 * Returns the username of the user that created/owns this OAuth client.
	 *
	 * @return The username of the user that created/owns this OAuth client.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Returns the unique identifier for this OAuth client.
	 *
	 * @return The unique identifier for this OAuth client.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the shared secret.
	 *
	 * @return The shared secret for the OAuth client.
	 */
	public String getSecret() {
		return sharedSecret;
	}

	/**
	 * Returns the name.
	 *
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the redirect URI.
	 *
	 * @return The redirect URI.
	 */
	public URI getRedirectUri() {
		return redirectUri;
	}

	/**
	 * Generates a new, universally unique identifier for this OAuth client.
	 *
	 * @return A new, universally unique identifier for this OAuth client.
	 */
    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a new, random secret for this OAuth client.
     *
     * @return A new, random secret for this OAuth client.
     */
    private static String generateSharedSecret() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "OauthClient{" +
                "owner='" + owner + '\'' +
                ", id='" + id + '\'' +
                ", sharedSecret='" + sharedSecret + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", redirectUri=" + redirectUri +
                '}';
    }
}