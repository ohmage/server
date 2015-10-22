package org.ohmage.domain;

import java.net.URL;
import java.util.UUID;

import org.ohmage.exception.DomainException;

/**
 * <p>
 * This class represents audio media data.
 * </p>
 *
 * @author John Jenkins
 */
public class Audio extends Media {
	/**
	 * The root MIME type for any media of this type, {@value #MIME_TYPE}.
	 */
	public static final String MIME_TYPE_ROOT = "audio";
	
	/**
	 * Creates a new representation of audio data.
	 * 
	 * @param id
	 *        The unique identifier for this audio data.
	 * 
	 * @param contentType
	 *        The Audio content-type.
	 * 
	 * @param fileName 
	 * 		  The Audio file name. 
	 * 
	 * @param content
	 *        The actual media data as a byte array.
	 * 
	 * @throws DomainException
	 *         One of the parameters was invalid.
	 */	
	public Audio(UUID id, String contentType, String fileName,
			byte[] content) throws DomainException {
	
		super(id, contentType, fileName, content);
	}

	
	/**
	 * Creates an audio file with an ID from the given URL.
	 * 
	 * @param id
	 *        This audio file's unique identifier.
	 * 
	 * @param url
	 *        A URL to the audio file.
	 * 
	 * @param info 
	 * 		  Metadata about the media object stored in the DB.
	 * 
	 * @throws DomainException
	 *         The URL was invalid or the object it points to does not exist.
	 */
	public Audio(final UUID id, final URL url, final String info) throws DomainException {
		super(id, url, info);
	}
	
	
	/**
	 * @return Always returns {@value #MIME_TYPE}.
	 * 
	 * Not use.
	protected String getMimeTypeRoot() {
		return MIME_TYPE_ROOT;
	}
	*/
}