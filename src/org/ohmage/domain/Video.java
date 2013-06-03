package org.ohmage.domain;

import java.net.URL;
import java.util.UUID;

import org.ohmage.exception.DomainException;

/**
 * <p>
 * This class represents a video file.
 * </p>
 *
 * @author John Jenkins
 */
public class Video extends Media {
	/**
	 * The root MIME type for any media of this type, {@value #MIME_TYPE}.
	 */
	public static final String MIME_TYPE = "video";
	
	/**
	 * Constructs a new video object.
	 * 
	 * @param id
	 *        The video's unique identifier.
	 * 
	 * @param type
	 *        The video's extension.
	 * 
	 * @param content
	 *        The byte array of the video.
	 */
	public Video(
		final UUID id, 
		final String type, 
		final byte[] content)
		throws DomainException {
		
		super(id, type, content);
	}
	
	/**
	 * Creates a video file with an ID from the given URL.
	 * 
	 * @param id
	 *        This video's unique identifier.
	 * 
	 * @param url
	 *        A URL to the video file.
	 * 
	 * @throws DomainException
	 *         The URL was invalid or the object it points to does not exist.
	 */
	public Video(final UUID id, final URL url) throws DomainException {
		super(id, url);
	}
	
	/**
	 * @return Always returns {@value #MIME_TYPE}.
	 */
	protected String getMimeTypeRoot() {
		return MIME_TYPE;
	}
}