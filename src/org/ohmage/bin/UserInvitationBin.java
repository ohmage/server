package org.ohmage.bin;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.user.UserInvitation;

/**
 * <p>
 * The interface to the database-backed user invitation repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class UserInvitationBin {
    /**
     * The singular instance of this class.
     */
    private static UserInvitationBin instance;

    /**
     * Initializes the singleton instance to this.
     */
    protected UserInvitationBin() {
        instance = this;
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static final UserInvitationBin getInstance() {
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
        final UserInvitation userInvitation)
        throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Returns an UserInvitation object based on the ID or null.
     *
     * @param invitationId
     *        The unique identifier for the invitation.
     *
     * @return An UserInvitation object that represents this stream.
     *
     * @throws IllegalArgumentException
     *         The invitation ID is null.
     */
    public abstract UserInvitation getInvitation(
        final String invitationId)
        throws IllegalArgumentException;

    /**
     * Returns a set of the UserInvitation objects that were issued to a given
     * email address.
     *
     * @param email
     *        The email address to get the user invitations for.
     *
     * @return The, possibly empty but never null, set of UserInvitations for a
     *         given email address.
     *
     * @throws IllegalArgumentException
     *         The email address is null.
     */
    public abstract MultiValueResult<? extends UserInvitation> getInvitations(
        final String email)
        throws IllegalArgumentException;

    /**
     * Updates the invitation in the database.
     *
     * @param invitation
     *        The updated UserInvitation object.
     *
     * @throws IllegalArgumentException
     *         The invitation is null.
     */
    public abstract void updateInvitation(
        final UserInvitation invitation)
        throws IllegalArgumentException;
}