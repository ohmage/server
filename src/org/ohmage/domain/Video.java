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
 * This class represents a video file.
 *
 * @author John Jenkins
 */
public class Video {
	private static final int MAX_EXTENSION_LENGTH = 3;
	
	public final UUID id;
	public final String type;
	public final int size;
	public final InputStream content; 
	
	/**
	 * Constructs a new video object.
	 * 
	 * @param id The video's unique identifier.
	 * 
	 * @param type The video's extension.
	 * 
	 * @param content The byte array of the video.
	 */
	public Video(final UUID id, final String type, final byte[] content) {
		this.id = id;
		this.type = type;
		this.size = content.length;
		this.content = new ByteArrayInputStream(content);
	}
	
	/**
	 * Creates a video file with an ID from the given URL. 
	 * 
	 * @param id This video's unique identifier.
	 * 
	 * @param url A URL string to the video file.
	 * 
	 * @throws DomainException The URL was invalid or the object it points to
	 * 						   does not exist.
	 */
	public Video(final UUID id, final String url) throws DomainException {
		this.id = id;
		
		// Compute the type. If there is no extension or if it is greater than
		// three characters long, the extension will be set to null.
		String[] parts = url.split("[\\.]");
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
			URL tempUrl = new URL(url);
			this.size = tempUrl.openConnection().getContentLength();
			this.content = tempUrl.openStream();
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
	}
	
	/**
	 * Returns the video's type (extension).
	 * 
	 * @return The video's type (extension) or null if it doesn't have one.
	 */
	public String getType() {
		return type;
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
	 * Returns the content of the video.
	 * 
	 * @return The content of the video.
	 */
	public InputStream getContentStream() {
		return content;
	}
	
	/**
	 * Creates a filename for this video based on its ID and type.
	 * 
	 * @return The file's filename.
	 */
	public String getFilename() {
		return id.toString() + "." + type;
	}
}
