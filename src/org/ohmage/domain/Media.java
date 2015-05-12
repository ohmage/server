package org.ohmage.domain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * @author Hongsuda T.
 */
// HT: change from abstract class to regular class.
public class Media {
	/**
	 * The maximum length for a file extension.
	 */
	private static final int MAX_EXTENSION_LENGTH = 4;

	private final UUID id;
	private String contentType = null;  
	private String type = null;
	private final InputStream content; 
	/**
	 * The size, in bytes, of the media file.
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
		
		if (url == null)
			throw new DomainException("[MediaID " + id.toString() + "] URL is null.");
		
		// Compute the contentType
		// Try contentType first. If doesn't work, use file extension. 
		// If there is no extension or if it is greater than
		// three characters long, the extension will be set to null.
		try {
			this.contentType = url.openConnection().getContentType();
			this.type = contentType.split("/")[1];
		
			if (this.type == null) {
				String[] parts = url.toString().split("[\\.]");
				if(parts.length != 0) {
					String extension = parts[parts.length - 1];
					if(extension.length() <= MAX_EXTENSION_LENGTH) {
						this.type = parts[parts.length - 1];
					}	
				}
			}
			
			// Create a connection to the stream.
			this.content = url.openStream();
		}	
		catch(MalformedURLException e) {
			throw new DomainException("The URL is invalid.", e);
		}
		catch(IOException e) {
			throw new DomainException(
				ErrorCode.SYSTEM_GENERAL_ERROR,
				"The media file does not exist.",
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
	 * Returns the media's size.
	 * 
	 * @return The media's size.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Creates a filename for this media based on its ID and type.
	 * 
	 * @return The file's filename.
	 */
	public String getFilename() {
		if (type == null)
			return id.toString();
		return id.toString() + "." + type;
	}
	
	/**
	 * Returns the MIME type for this media object. The type is set based on
	 * the filename extension.
	 * 
	 * @return The MIME type for this object of the form "{super}/{sub}".
	 */
	public String getContentType() {
		if (contentType != null)
			return contentType;
		
		String ttype = getType();
		if (ttype == null)
			return null;
		return getMimeTypeRoot(ttype) + "/" + ttype;
	}
	
	/**
	 * Returns the root MIME type of this media type. 
	 * 
	 * @param type
	 * 			Media type
	 * 
	 * @return The root MIME type of this media type.
	 */
	protected String getMimeTypeRoot(String type) {
	
		if (type == null)
			return null;
		
		switch (type) {
		case "gif":
		case "png":
		case "jpg": 
		case "jpeg": return ("image");
		case "txt": 
		case "csv": return ("text");
		case "wav":
		case "aiff": 
		case "mp3": return ("audio");
		case "mpg":
		case "mpeg": 
		case "mp4": return ("video");
		default: return("application");	
		}	
	}
	
	/**
	 * Writes the media data to the given file. This file *should* end with
	 * the string given by the {@link #getExtension()} function.
	 * 
	 * @param imageData The image data to be written.
	 * 
	 * @param destination The file to write the image to.
	 * 
	 * @throws DomainException There was an error reading the image data
	 * 						   or writing the file.
	 * 
	 * @see {@link #getExtension()}
	 */
	public final void writeFile(final File destination)
		throws DomainException {
		
		// Get the image data.
		InputStream contents = getContentStream();
		
		if(contents == null) {
			throw new DomainException("The contents parameter is null.");
		}
		
		// Connect to the file that should write it.
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(destination);
		}
		catch(SecurityException e) {
			throw
				new DomainException(
					"The file is not allowed to be created.",
					e);
		}
		catch(FileNotFoundException e) {
			throw new DomainException("The file cannot be created.", e);
		}
		
		// Write the image data.
		try {
			int bytesRead;
			byte[] buffer = new byte[4096];
			while((bytesRead = contents.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}
		catch(IOException e) {
			throw
				new DomainException(
					"Error reading or writing the data.",
					e);
		}
		finally {
			try {
				fos.close();
			}
			catch(IOException e) {
				throw new DomainException("Could not close the file.", e);
			}
		}
	}
	
}
	