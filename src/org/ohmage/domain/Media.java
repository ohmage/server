package org.ohmage.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;

/**
 * <p>
 * The super class for all media in ohmage.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class Media {
	/**
	 * The maximum length for a file extension.
	 */
	private static final int MAX_EXTENSION_LENGTH = 4;

	public final UUID id;
	public final String type;
	public final InputStream content; 
	/**
	 * The size, in bytes, of the video file.
	 */
	public final int size;

	/**
	 * Creates a Media object with an ID, type, and the literal content.
	 * 
	 * @param id
	 *        The ID of the Media.
	 * 
	 * @param type
	 *        The content type of the media.
	 * 
	 * @param content
	 *        The content of the media.
	 * 
	 * @throws DomainException
	 *         One of the parameters was invalid.
	 */
	public Media(
		final UUID id, 
		final String type, 
		final byte[] content)
		throws DomainException {
		
		// Validate the ID.
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		else {
			this.id = id;
		}
		
		// Validate the type.
		if(type == null) {
			throw new DomainException("The type is null.");
		}
		else {
			String trimmedType = type.trim();
			
			if(trimmedType.length() == 0) {
				throw new DomainException("The type is empty.");
			}
			else {
				this.type = trimmedType;
			}
		}
		
		// Validate the content.
		if(content == null) {
			throw new DomainException("The content is null.");
		}
		else if(content.length == 0) {
			throw new DomainException("The content is empty.");
		}
		else {
			this.content = new ByteArrayInputStream(content);
		}
		
		// Validate the size.
		this.size = content.length;
	}
	
	/**
	 * Creates a Media object with an ID and a URL referencing the data.
	 * 
	 * @param id
	 *        The ID of the Media.
	 * 
	 * @param url
	 *        The URL referencing the data.
	 * 
	 * @throws DomainException
	 *         A parameter was invalid or the data could not be read.
	 */
	public Media(final UUID id, final URL url) throws DomainException {
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		else {
			this.id = id;
		}
		
		// Compute the type. If there is no extension or if it is greater than
		// three characters long, the extension will be set to null.
		String[] parts = url.toString().split("[\\.]");
		if(parts.length == 0) {
			this.type = null;
		}
		else {
			String extension = parts[parts.length - 1];
			if(extension.length() > MAX_EXTENSION_LENGTH) {
				this.type = null;
			}
			else {
				this.type = parts[parts.length - 1];
			}
		}
		
		// Create a connection to the stream.
		try {
			this.content = url.openStream();
		}
		catch(MalformedURLException e) {
			throw new DomainException("The URL is invalid.", e);
		}
		catch(IOException e) {
			throw new DomainException(
				ErrorCode.SYSTEM_GENERAL_ERROR,
				"The video file does not exist.",
				e);
		}
		
		// Get the size of the data.
		try {
			this.size = url.openConnection().getContentLength();
		}
		catch(IOException e) {
			throw new DomainException("Could not connect to the file.", e);
		}
	}
	
	/**
	 * Returns the ID.
	 * 
	 * @return The ID.
	 */
	public UUID getId() {
		return id;
	}
	
	/**
	 * Returns the type.
	 * 
	 * @return The type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns an input stream connected to the data.
	 * 
	 * @return An input stream connected to the data.
	 */
	public InputStream getContentStream() {
		return content;
	}
	
	/**
	 * Returns the video's size.
	 * 
	 * @return The video's size.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Creates a filename for this video based on its ID and type.
	 * 
	 * @return The file's filename.
	 */
	public String getFilename() {
		return id.toString() + "." + type;
	}
	
	/**
	 * Returns the MIME type for this media object.
	 * 
	 * @return The MIME type for this object of the form "{super}/{sub}".
	 */
	public String getMimeType() {
		return getMimeTypeRoot() + "/" + getType();
	}
	
	/**
	 * Returns the root MIME type of this media type. This should really be
	 * static, except that abstract static functions are not allowed in Java.
	 * 
	 * @return The root MIME type of this media type.
	 */
	protected abstract String getMimeTypeRoot();
}