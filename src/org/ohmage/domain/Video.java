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
 * @author Hongsuda T. 
 */
public class Video extends Media {
	/**
	 * The root MIME type for any media of this type, {@value #MIME_TYPE}.
	 */
	public static final String MIME_TYPE_ROOT = "video";
	
	/**
	 * Constructs a new video object.
	 * 
	 * @param id
	 *        The video's unique identifier.
	 * 
	 * @param contentType
	 *        The media content-type.
	 * 
	 * @param fileName 
	 * 		  The media file name. 
	 * 
	 * @param content
	 *        The byte array of the video.
	 */	
	public Video(UUID id, String contentType, String fileName,
			byte[] content) throws DomainException {
		
		super(id, contentType, fileName, content);
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
	 * @param info 
	 * 		  Metadata about the media object stored in the DB.
	 * 
	 * @throws DomainException
	 *         The URL was invalid or the object it points to does not exist.
	 */
	public Video(final UUID id, final URL url, final String info) throws DomainException {
		super(id, url, info);
	}
	

	/**
	 * @return Always returns {@value #MIME_TYPE}.
	 */
	protected String getMimeTypeRoot() {
		return MIME_TYPE_ROOT;
	}
}