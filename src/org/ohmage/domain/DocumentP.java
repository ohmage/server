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
public class DocumentP extends Media {
	/**
	 * The root MIME type for any media of this type, {@value #MIME_TYPE}.
	 */
	public static final String MIME_TYPE = "application";
	
	/**
	 * Creates a new representation of file data.
	 * 
	 * @param id
	 *        The unique identifier for this file data.
	 * 
	 * @param type
	 *        The content type of the media data.
	 * 
	 * @param content
	 *        The actual media data as a byte array.
	 * 
	 * @throws DomainException
	 *         One of the parameters was invalid.
	 */
	public DocumentP(
		final UUID id,
		final String type,
		final byte[] content)
		throws DomainException {
		
		super(id, type, content);
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
	 * @throws DomainException
	 *         The URL was invalid or the object it points to does not exist.
	 */
	public DocumentP(final UUID id, final URL url) throws DomainException {
		super(id, url);
	}
	
	/**
	 * @return Always returns {@value #MIME_TYPE}.
	 */
	protected String getMimeTypeRoot() {
		return MIME_TYPE;
	}
}