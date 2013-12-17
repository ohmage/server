package org.ohmage.bin;

import org.ohmage.domain.survey.Media;

/**
 * <p>
 * The interface to the database-backed media repository.
 * <p>
 *
 * @author John Jenkins
 */
public abstract class MediaBin {
    /**
     * The singular instance of this class.
     */
    private static MediaBin instance;

    /**
     * Initializes the singleton instance to this.
     */
    protected MediaBin() {
        instance = this;
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static final MediaBin getInstance() {
        return instance;
    }

    /**
     * Adds a {@link Media} object to the in.
     *
     * @param media
     *        The {@link Media} object to save.
     *
     * @throws IllegalArgumentException
     *         The media object was null.
     */
    public abstract void addMedia(
        final Media media)
        throws IllegalArgumentException;

    /**
     * Returns an input stream to the media specified by the media ID.
     *
     * @param mediaId
     *        The media's unique identifier.
     *
     * @return The {@link Media} object representing the media.
     *
     * @throws IllegalArgumentException
     *         The media ID was null.
     */
    public abstract Media getMedia(
        final String mediaId)
        throws IllegalArgumentException;
}