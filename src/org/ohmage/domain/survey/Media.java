package org.ohmage.domain.survey;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * The internal representation of a generic media object.
 * </p>
 *
 * @author John Jenkins
 */
public class Media {
    /**
     * The unique identifier for this media file.
     */
    private final String id;
    /**
     * The {@link InputStream} that is connected to the data.
     */
    private final InputStream stream;
    /**
     * The size, in bytes, of the data.
     */
    private final long size;
    /**
     * The content-type of the data.
     */
    private final String contentType;

    /**
     * Creates a new Media object from a {@link MultipartFile} object.
     *
     * @param id
     *        The universally unique identifier to use for this media.
     *
     * @param media
     *        The {@link MultipartFile} to base this Media object off of.
     *
     * @throws IllegalArgumentException
     *         The ID or media file were null or could not be read.
     *
     * @see #generateUuid()
     */
    public Media(final String id, final MultipartFile media)
        throws IllegalArgumentException {

        // Validate the input.
        if(id == null) {
            throw new IllegalArgumentException("The ID is null.");
        }
        if(media == null) {
            throw new IllegalArgumentException("The media is null.");
        }

        // Save the object.
        this.id = id;
        try {
            stream = media.getInputStream();
        }
        catch(IOException e) {
            throw
                new IllegalArgumentException(
                    "Could not connect to the media.",
                    e);
        }
        size = media.getSize();
        contentType = media.getContentType();
    }

    /**
     * Creates a new Media object.
     *
     * @param id
     *        The unique identifier for this Media object.
     *
     * @param stream
     *        The {@link InputStream} connected to the data.
     *
     * @param size
     *        The size, in bytes, of the data.
     *
     * @param contentType
     *        The content-type of the data.
     *
     * @throws IllegalArgumentException
     *         The ID, stream, or content-type were null.
     *
     * @see #generateUuid()
     */
    public Media(
        final String id,
        final InputStream stream,
        final long size,
        final String contentType)
        throws IllegalArgumentException {

        if(id == null) {
            throw new IllegalArgumentException("The ID is null.");
        }
        if(stream == null) {
            throw new IllegalArgumentException("The stream is null.");
        }
        if(contentType == null) {
            throw new IllegalArgumentException("The content-type is null.");
        }

        this.id = id;
        this.stream = stream;
        this.size = size;
        this.contentType = contentType;
    }

    /**
     * Returns the unique identifier for this point.
     *
     * @return The unique identifier for this point.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the input stream.
     *
     * @return The input stream.
     */
    public InputStream getStream() {
        return stream;
    }

    /**
     * Returns the size.
     *
     * @return The size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the content-type.
     *
     * @return The content-type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Generates a new, universally unique identifier to be used for a new
     * Media object.
     *
     * @return A new, universally unique identifier.
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
}