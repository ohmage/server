package org.ohmage.bin;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.ohmlet.OhmletInvitation;

/**
 * <p>
 * The interface to the database-backed ohmlet invitation repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class OhmletInvitationBin {
    /**
     * The singular instance of this class.
     */
    private static OhmletInvitationBin instance;

    /**
     * Initializes the singleton instance to this.
     */
    protected OhmletInvitationBin() {
        instance = this;
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static final OhmletInvitationBin getInstance() {
        return instance;
    }

    /**
     * Adds a new invitation to the repository.
     *
     * @param invitation
     *        The invitation to add.
     *
     * @throws IllegalArgumentException
     *         The invitation is null.
     *
     * @throws InvalidArgumentException
     *         An invitation with the same ID already exists.
     */
    public abstract void addInvitation(
        final OhmletInvitation userInvitation)
        throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Returns an OhmletInvitation object based on the ID or null.
     *
     * @param invitationId
     *        The unique identifier for the invitation.
     *
     * @return An OhmletInvitation object that represents this stream.
     *
     * @throws IllegalArgumentException
     *         The invitation ID is null.
     */
    public abstract OhmletInvitation getInvitation(
        final String invitationId)
        throws IllegalArgumentException;

    /**
     * Updates the invitation in the database.
     *
     * @param invitation
     *        The updated OhmletInvitation object.
     *
     * @throws IllegalArgumentException
     *         The invitation is null.
     */
    public abstract void updateInvitation(
        final OhmletInvitation invitation)
        throws IllegalArgumentException;
}