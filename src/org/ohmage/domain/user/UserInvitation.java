package org.ohmage.domain.user;

import java.util.UUID;

import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * </p>
 *
 * @author John Jenkins
 */
public class UserInvitation extends OhmageDomainObject {
    /**
     * <p>
     * The builder for {@link UserInvitation} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder
        extends OhmageDomainObject.Builder<UserInvitation> {

        /**
         * A unique identifier for this invitation.
         */
        private final String invitationId;

        /**
         * The email address to which the email address corresponds.
         */
        private final String email;

        /**
         * The unique identifier of some ohmlet invitation ID which, if used,
         * invalidates this invitation.
         */
        private final String ohmletInvitationId;

        /**
         * The number of milliseconds since the Unix epoch when this invitation
         * was created.
         */
        private final Long creationTimestamp;

        /**
         * The number of milliseconds since the Unix epoch when this invitation
         * was used.
         */
        private Long usedTimestamp;

        /**
         * Creates a new builder based on the invitation.
         *
         * @param invitation
         *        The invitation to base this builder off of.
         */
        public Builder(final UserInvitation invitation) {
            super(invitation);

            invitationId = invitation.invitationId;
            email = invitation.email;
            creationTimestamp = invitation.creationTimestamp;
            usedTimestamp = invitation.usedTimestamp;
            ohmletInvitationId = invitation.ohmletInvitationId;
        }

        /**
         * Sets the used time-stamp.
         *
         * @param timestamp
         *        The time-stamp to use as the new used time-stamp or null to
         *        clear the used time-stamp.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setUsedTimestamp(final Long timestamp) {
            usedTimestamp = timestamp;

            return this;
        }

        /**
         * Builds and returns a new {@link UserInvitation} object based on the
         * state of this builder.
         *
         * @return The {@link UserInvitation} object based on the state of this
         *         builder.
         */
        public UserInvitation build() {
            return
                new UserInvitation(
                    invitationId,
                    email,
                    ohmletInvitationId,
                    creationTimestamp,
                    usedTimestamp,
                    internalReadVersion,
                    internalWriteVersion);
        }
    }

    /**
     * The JSON key for the invitation ID.
     */
    public static final String JSON_KEY_INVITATION_ID = "user_invitation_id";

    /**
     * The JSON key for the email address.
     */
    public static final String JSON_KEY_EMAIL = "email";

    /**
     * The JSON key for the creation timestamp.
     */
    public static final String JSON_KEY_CREATION_TIMESTAMP =
        "creation_timestamp";

    /**
     * The JSON key for the timestamp when this invitation was used.
     */
    public static final String JSON_KEY_USED_TIMESTAMP = "used_timestamp";

    /**
     * The JSON key for the ohmlet invitation ID.
     */
    public static final String JSON_KEY_OHMLET_INVITATION_ID =
        "ohmlet_invitation_id";

    /**
     * A unique identifier for this invitation.
     */
    @JsonProperty(JSON_KEY_INVITATION_ID)
    private final String invitationId;

    /**
     * The email address to which the email address corresponds.
     */
    @JsonProperty(JSON_KEY_EMAIL)
    private final String email;

    /**
     * The unique identifier of some ohmlet invitation ID which, if used,
     * invalidates this invitation.
     */
    @JsonProperty(JSON_KEY_OHMLET_INVITATION_ID)
    private final String ohmletInvitationId;

    /**
     * The number of milliseconds since the Unix epoch when this invitation was
     * created.
     */
    @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
    private final Long creationTimestamp;

    /**
     * The number of milliseconds since the Unix epoch when this invitation was
     * used.
     */
    @JsonProperty(JSON_KEY_USED_TIMESTAMP)
    private final Long usedTimestamp;

    /**
     * Creates a new invitation.
     *
     * @param email
     *        The user's email address.
     */
    public UserInvitation(
        final String email,
        final String ohmletInvitationId)
        throws InvalidArgumentException {

        this(
            generateInvitationId(),
            email,
            ohmletInvitationId,
            System.currentTimeMillis(),
            null,
            null);
    }

    /**
     * Recreates an existing ohmlet invitation.
     *
     * @param invitationId
     *        The invitation's unique identifier.
     *
     * @param email
     *        The user's email address.
     *
     * @param ohmletInvitationId
     *        A verification ID to use to automatically verify an email
     *        address.
     *
     * @param creationTimestamp
     *        The number of milliseconds since the Unix epoch when this
     *        invitation was created.
     *
     * @param usedTimestamp
     *        The number of milliseconds since the Unix epoch when this
     *        invitation was used.
     *
     * @param internalVersion
     *        The internal version of this object when it was read from the
     *        database to facilitate updating it.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    protected UserInvitation(
        @JsonProperty(JSON_KEY_INVITATION_ID) final String invitationId,
        @JsonProperty(JSON_KEY_EMAIL) final String email,
        @JsonProperty(JSON_KEY_OHMLET_INVITATION_ID)
            final String ohmletInvitationId,
        @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
            final long creationTimestamp,
        @JsonProperty(JSON_KEY_USED_TIMESTAMP) final Long usedTimestamp,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        this(
            invitationId,
            email,
            ohmletInvitationId,
            creationTimestamp,
            usedTimestamp,
            internalVersion,
            null);
    }

    /**
     * Constructs the ohmlet invitation with all of the validators for the
     * individual fields. All constructors should eventually call into here to
     * prevent duplicate validation.
     *
     * @param invitationId
     *        The invitation's unique identifier.
     *
     * @param email
     *        The user's email address.
     *
     * @param ohmletInvitationId
     *        A verification ID to use to automatically verify an email
     *        address.
     *
     * @param creationTimestamp
     *        The number of milliseconds since the Unix epoch when this
     *        invitation was created.
     *
     * @param usedTimestamp
     *        The number of milliseconds since the Unix epoch when this
     *        invitation was used.
     *
     * @param internalReadVersion
     *        The internal version of the object when it was read from the
     *        database.
     *
     * @param internalWriteVersion
     *        The new internal version of the object to use when it is written
     *        back to the database.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    private UserInvitation(
        final String invitationId,
        final String email,
        final String ohmletInvitationId,
        final Long creationTimestamp,
        final Long usedTimestamp,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        super(internalReadVersion, internalWriteVersion);

        if(invitationId == null) {
            throw new InvalidArgumentException("The invitation ID is null.");
        }
        if(email == null) {
            throw new InvalidArgumentException("The email address is null.");
        }
        if(ohmletInvitationId == null) {
            throw new InvalidArgumentException("The verification ID is null.");
        }
        if(creationTimestamp == null) {
            throw
                new InvalidArgumentException(
                    "The creation timestamp is null.");
        }

        this.invitationId = invitationId;
        this.email = email;
        this.ohmletInvitationId = ohmletInvitationId;
        this.creationTimestamp = creationTimestamp;
        this.usedTimestamp = usedTimestamp;
    }

    /**
     * Returns the invitation's unique identifier.
     *
     * @return The invitation's unique identifier.
     */
    public String getId() {
        return invitationId;
    }

    /**
     * Returns the email address associated with this invitation.
     *
     * @return The email address associated with this invitation.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns whether or not the invitation is still valid. It is not valid if
     * it has been used, for example.
     *
     * @return Whether or not the invitation is still valid.
     */
    public boolean isValid() {
        return usedTimestamp == null;
    }

    /**
     * Generates a new, random, unique invitation identifier.
     *
     * @return A new, random, unique invitation identifier.
     */
    private static String generateInvitationId() {
        return UUID.randomUUID().toString();
    }
}