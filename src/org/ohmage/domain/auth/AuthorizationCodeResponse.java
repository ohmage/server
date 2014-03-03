package org.ohmage.domain.auth;

import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A response to an authorization request. This links a user to the request and
 * contains the user's response.
 * </p>
 *
 * @author John Jenkins
 */
public class AuthorizationCodeResponse {
    /**
     * <p>
     * A builder for {@link AuthorizationCodeResponse} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder {
        /**
         * The unique identifier of the user that responded to the authorization
         * request.
         */
        private final String userId;
        /**
         * Whether or not the user granted the request.
         */
        private final boolean granted;
        /**
         * The number of milliseconds since the Unix epoch when this code was
         * created.
         */
        private final long creationTimestamp;
        /**
         * The number of milliseconds since the Unix epoch when this code was
         * invalidated.
         */
        private Long invalidationTimestamp;

        /**
         * Creates a new builder based on the state of the
         * {@link AuthorizationCodeResponse}.
         *
         * @param original
         *        The {@link AuthorizationCodeResponse} on which this builder
         *        should be based.
         */
        public Builder(final AuthorizationCodeResponse original) {
            userId = original.userId;
            granted = original.granted;
            creationTimestamp = original.creationTimestamp;
            invalidationTimestamp = original.invalidationTimestamp;
        }

        /**
         * Sets the time-stamp when this response was invalidated.
         *
         * @param invalidationTimestamp
         *        The time this response was invalidated.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setInvalidationTimestamp(
            final Long invalidationTimestamp) {

            this.invalidationTimestamp = invalidationTimestamp;

            return this;
        }

        /**
         * Builds a new {@link AuthorizationCodeResponse} based on the current
         * state of this builder.
         *
         * @return A new {@link AuthorizationCodeResponse} based on the current
         *         state of this builder.
         */
        public AuthorizationCodeResponse build() {
            return
                new AuthorizationCodeResponse(
                    userId,
                    granted,
                    creationTimestamp,
                    invalidationTimestamp);
        }
    }

    /**
     * The JSON key for the user's unique identifier.
     */
    public static final String JSON_KEY_USER_ID = "user_id";
    /**
     * The JSON key for the authorization code value.
     */
    public static final String JSON_KEY_AUTHORIZATION_CODE = "code";
    /**
     * The JSON key for whether or not the user granted the request.
     */
    public static final String JSON_KEY_GRANTED = "granted";
    /**
     * The JSON key for when this code was created.
     */
    public static final String JSON_KEY_CREATION_TIMESTAMP =
        "creation_timestamp";
    /**
     * The JSON key for when this code was invalidated.
     */
    public static final String JSON_KEY_INVALIDATION_TIMESTAMP =
        "invalidation_timestamp";

    /**
     * The unique identifier of the user that responded to the authorization
     * request.
     */
    @JsonProperty(JSON_KEY_USER_ID)
    private final String userId;
    /**
     * Whether or not the user granted the request.
     */
    @JsonProperty(JSON_KEY_GRANTED)
    private final boolean granted;
    /**
     * The number of milliseconds since the Unix epoch when this code was
     * created.
     */
    @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
    private final long creationTimestamp;
    /**
     * The number of milliseconds since the Unix epoch when this code was
     * invalidated.
     */
    @JsonProperty(JSON_KEY_INVALIDATION_TIMESTAMP)
    private final Long invalidationTimestamp;

    /**
     * Creates a new authorization code response.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param granted
     *        Whether or not the user granted the request.
     */
    public AuthorizationCodeResponse(
        final String userId,
        final boolean granted) {

        this(
            userId,
            granted,
            System.currentTimeMillis(),
            null);
    }

    /**
     * Recreates an existing authorization code response.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param granted
     *        Whether or not the user granted the request.
     *
     * @param creationTimestamp
     *        When the user responded to the request.
     *
     * @param invalidationTimestamp
     *        When the user invalidated their response.
     */
    @JsonCreator
    protected AuthorizationCodeResponse(
        @JsonProperty(JSON_KEY_USER_ID) final String userId,
        @JsonProperty(JSON_KEY_GRANTED) final Boolean granted,
        @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
            final Long creationTimestamp,
        @JsonProperty(JSON_KEY_INVALIDATION_TIMESTAMP)
            final Long invalidationTimestamp) {

        if(userId == null) {
            throw new InvalidArgumentException("The user ID is null.");
        }
        if(granted == null) {
            throw new InvalidArgumentException("The granted value is null.");
        }
        if(creationTimestamp == null) {
            throw
                new InvalidArgumentException(
                    "The creation timestamp is null.");
        }

        this.userId = userId;
        this.granted = granted;
        this.creationTimestamp = creationTimestamp;
        this.invalidationTimestamp = invalidationTimestamp;
    }

    /**
     * Returns the unique identifier of the user that responded to this
     * request.
     *
     * @return The unique identifier of the user that responded to this
     *         request.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns whether or not the user granted the request.
     *
     * @return Whether or not the user granted the request.
     */
    public boolean getGranted() {
        return granted;
    }
}