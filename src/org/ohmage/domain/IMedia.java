package org.ohmage.domain;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import org.ohmage.exception.DomainException;

/**
 * <p>
 * Media interface. Need to define an interface
 * to integrate Image with other media types. 
 * This is an experiment at the moment!
 * </p>
 *
 * @author Hongsuda T.
 */
public interface IMedia {
	
	/**
	 * Returns the ID.
	 * 
	 * @return The ID.
	 */
	public UUID getId();
	

	/**
	 * Returns an input stream connected to the data.
	 * 
	 * @return An input stream connected to the data.
	 */
	public InputStream getContentStream() throws DomainException;
	
	/**
	 * Returns the media's size.
	 * 
	 * @return The media's size.
	 */
	public long getFileSize() throws DomainException;
	
	/**
	 * Returns ContentInfo object associated with this media.
	 * 
	 * @return The ContentInfo of this object.
	 */
	// public Media.ContentInfo getContentInfo();
	
	/**
	 * Creates a filename for this media based on its ID and type.
	 * 
	 * @return The file's filename.
	 */
	public String getFileName();
	
	/**
	 * Returns the type (e.g. file extension)
	 * 
	 * @return The type.
	 */
	//	public String getFileType();
	
	/**
	 * Returns the MIME type for this media object. 
	 * 
	 * @return The MIME type for this object of the form "{super}/{sub}".
	 */
	public String getContentType();
	
	
	/**
	 * Returns the metadata string to be stored in the database entry
	 * together with the media url.  
	 * 
	 * @return The metadata for this object of the form key:value.
	 * 			Each key:value is separated by ";".
	 */
	public String getMetadata();
	
	/**
	 * Writes the media data to the given directory. 
	 * 
	 * @param directory The directory to write the media content to.
	 * 
	 * @throws DomainException There was an error reading the data
	 * 						   or writing the file.
	 */
	public File writeContent(final File directory) throws DomainException;
	
}
	