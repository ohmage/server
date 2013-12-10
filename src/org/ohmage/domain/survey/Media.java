package org.ohmage.domain.survey;

import java.io.InputStream;

/**
 * <p>
 * The internal representation of a generic media object.
 * </p>
 *
 * @author John Jenkins
 */
public class Media {
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
     * Creates a new Media object.
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
     *         The stream or content-type were null.
     */
    public Media(
        final InputStream stream,
        final long size,
        final String contentType)
        throws IllegalArgumentException {

        if(stream == null) {
            throw new IllegalArgumentException("The stream is null.");
        }
        if(contentType == null) {
            throw new IllegalArgumentException("The content-type is null.");
        }

        this.stream = stream;
        this.size = size;
        this.contentType = contentType;
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
}
