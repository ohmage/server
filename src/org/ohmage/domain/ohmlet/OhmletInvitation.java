package org.ohmage.domain.ohmlet;

import java.util.UUID;

import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.user.UserInvitation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * Creates a new invitation for an email address to automatically join an
 * ohmlet. It also has a verification code that will circumvent a user's need
 * to verify their email address because this invitation should only be
 * accessible via that same email account.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmletInvitation extends OhmageDomainObject {
    /**
     * <p>
     * The builder for {@link UserInvitation} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder
        extends OhmageDomainObject.Builder<OhmletInvitation> {

        /**
         * A unique identifier for this invitation.
         */
        private final String invitationId;

        /**
         * The unique identifier for the ohmlet.
         */
        private final String ohmletId;

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
        public Builder(final OhmletInvitation invitation) {
            super(invitation);

            invitationId = invitation.invitationId;
            ohmletId = invitation.ohmletId;
            creationTimestamp = invitation.creationTimestamp;
            usedTimestamp = invitation.usedTimestamp;
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
         * Builds and returns a new {@link OhmletInvitation} object based on
         * the state of this builder.
         *
         * @return The {@link OhmletInvitation} object based on the state of
         *         this builder.
         */
        public OhmletInvitation build() {
            return
                new OhmletInvitation(
                    invitationId,
                    ohmletId,
                    creationTimestamp,
                    usedTimestamp,
                    internalReadVersion,
                    internalWriteVersion);
        }
    }

    /**
     * The JSON key for the invitation ID.
     */
    public static final String JSON_KEY_INVITATION_ID = "ohmlet_invitation_id";

    /**
     * The JSON key for the ohmlet ID.
     */
    public static final String JSON_KEY_OHMLET_ID = "ohmlet_id";

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
     * A unique identifier for this invitation.
     */
    @JsonProperty(JSON_KEY_INVITATION_ID)
    private final String invitationId;

    /**
     * The unique identifier for the ohmlet.
     */
    @JsonProperty(JSON_KEY_OHMLET_ID)
    private final String ohmletId;

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
     * @param ohmletId
     *        The ohmlet's unique identifier.
     */
    public OhmletInvitation(final String ohmletId) {
        this(
            generateInvitationId(),
            ohmletId,
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
     * @param ohmletId
     *        The ohmlet's unique identifier.
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
    protected OhmletInvitation(
        @JsonProperty(JSON_KEY_INVITATION_ID) final String invitationId,
        @JsonProperty(JSON_KEY_OHMLET_ID) final String ohmletId,
        @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
            final Long creationTimestamp,
        @JsonProperty(JSON_KEY_USED_TIMESTAMP) final Long usedTimestamp,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        this(
            invitationId,
            ohmletId,
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
     * @param ohmletId
     *        The ohmlet's unique identifier.
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
    private OhmletInvitation(
        final String invitationId,
        final String ohmletId,
        final Long creationTimestamp,
        final Long usedTimestamp,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        super(internalReadVersion, internalWriteVersion);

        if(invitationId == null) {
            throw new InvalidArgumentException("The invitation ID is null.");
        }
        if(ohmletId == null) {
            throw new InvalidArgumentException("The ohmlet's ID is null.");
        }
        if(creationTimestamp == null) {
            throw
                new InvalidArgumentException(
                    "The creation timestamp is null.");
        }

        this.invitationId = invitationId;
        this.ohmletId = ohmletId;
        this.creationTimestamp = creationTimestamp;
        this.usedTimestamp = usedTimestamp;
    }

    /**
     * Returns this invitation's ID.
     *
     * @return This invitation's ID.
     */
    public String getId() {
        return invitationId;
    }

    /**
     * Returns the unique identifier for the ohmlet to which this invitation
     * belongs.
     *
     * @return The unique identifier for the ohmlet to which this invitation
     *         belongs.
     */
    public String getOhmletId() {
        return ohmletId;
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