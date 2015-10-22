package org.ohmage.domain;

import java.net.URL;
import java.util.UUID;

import org.ohmage.exception.DomainException;

/**
 * <p>
 * This class represents file data.
 * </p>
 *
 * @author HT
 */
public class OFile extends Media {
	/**
	 * The root MIME type for any media of this type, {@value #MIME_TYPE}.
	 */
	public static final String MIME_TYPE_ROOT = "application";
	
	/**
	 * Creates a new representation of file data.
	 * 
	 * @param id
	 *        The unique identifier for this file data.
	 * 
	 * @param contentType
	 *        The media content-type.
	 * 
	 * @param fileName 
	 * 		  The media file name. 
	 * 
	 * @param content
	 *        The actual media data as a byte array.
	 * 
	 * @throws DomainException
	 *         One of the parameters was invalid.
	 */	
	public OFile(UUID id, String contentType, String fileName,
			byte[] content) throws DomainException {
	
		super(id, contentType, fileName, content);
	}


	/**
	 * Creates an file with an ID from the given URL.
	 * 
	 * @param id
	 *        This file's unique identifier.
	 * 
	 * @param url
	 *        A URL to the file file.
	 * 
	 * @param info 
	 * 		  Metadata about the media object stored in the DB.
	 * 
	 * @throws DomainException
	 *         The URL was invalid or the object it points to does not exist.
	 */
	public OFile(final UUID id, final URL url, final String info) throws DomainException {
		super(id, url, info);
	}
	

	/**
	 * @return Always returns {@value #MIME_TYPE}.
	 *
	 * Not used. 
	protected String getMimeTypeRoot() {
		return MIME_TYPE_ROOT;
	}
		 */
}