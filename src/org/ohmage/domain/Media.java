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

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;

/**
 * <p>
 * This is the super class of all media classes except image
 * </p>
 *
 * @author Hongsuda T.
 */
// HT: change from abstract class to regular class.
public class Media implements IMedia {
	private static final Logger LOGGER = Logger.getLogger(Media.class);
	/**
	 * The maximum length for a file extension.
	 */
	public static final int MAX_EXTENSION_LENGTH = 4;

	private final UUID id;
	private final InputStream content; 
	private Media.ContentInfo contentInfo; 
	// The size, in bytes, of the media file.
	public final long size;
	/*
	private String contentType = null;  
	 private String type = null;
	 private String filename = null;
	 */

	/**
	 * <p>
	 * The class that contains request information relevant to the 
	 * media object. This class is immutable.
	 * </p>
	 *
	 * @author Hongsuda T.
	 */
	static class ContentInfo {
		public static String FIELD_SEPARATOR = ";";
		public static String KEY_VALUE_SEPARATOR = ":";
		public static String KEY_CONTENT_TYPE = "ContentType";
		public static String KEY_FILE_NAME = "FileName";
		private String contentType;
		private String fileName; 
		private String fileType;
	
		
		/**
		 * Creates a ContentInfo object with contentType and fileName.
		 * 
		 * @param contentType the ContentType string
		 * 		
		 * @param fileName The name of the file.
		 * 
		 */
		ContentInfo(String contentType, String fileName) {
			this.contentType = contentType;
			this.fileName = fileName;
			this.fileType = getFileTypeFromFileName(fileName);
			LOGGER.debug(this.toMetadata());
		}
	
		/**
		 * Creates a ContentInfo object with contentType, fileName, fileType.
		 * 
		 * @param contentType the ContentType string.
		 * 		
		 * @param fileName The name of the file.
		 * 
		 * @param fileType File extension.
		 * 
		 */
		ContentInfo(String contentType, String fileName, String fileType) {
			this.contentType = contentType;
			this.fileName = fileName;
			this.fileType = fileType;
			LOGGER.debug(this.toMetadata());
		}

		/**
		 * Creates a ContentInfo object with url and info retrieved from the db. 
		 * 
		 * @param url The url of the media object.
		 * 		
		 * @param info The metadata related to the media object.
		 */
		public static ContentInfo createContentInfoFromUrl(URL url, String info) 
		throws DomainException {
			String tContentType = null;
			String tFileName = null;
			String tFileType = null;
			
			// get information from the info if it is available
			if ((info != null) && (! info.isEmpty())) {
				String parts[] = info.split(FIELD_SEPARATOR);
				for (String p : parts){
					p = p.trim();  // trim white space?
					String keyValue[] = p.split(KEY_VALUE_SEPARATOR);
					if (keyValue[0].equals(KEY_CONTENT_TYPE) && (! keyValue[1].isEmpty()))
						tContentType = keyValue[1];
					if (keyValue[0].equals(KEY_FILE_NAME) && (! keyValue[1].isEmpty()))
						tFileName = keyValue[1];
				}
			}
		
			if (tContentType != null && tFileName != null) {  // we are done!
				LOGGER.debug("HT: contentType:" + tContentType + " FileName:" + tFileName);
				return new ContentInfo(tContentType, tFileName);
			}
								
			if (url == null)
				throw new DomainException("Cannot create ContentInfo. Url is null");
							
			String paths[] = url.toString().split("/");
			String simpleName = paths[paths.length-1];
			String elms[] = simpleName.split("__");
			tFileName = elms[0];
			tFileType = getFileTypeFromFileName(tFileName);

			// If the contentType exist on filename, use it if necessary
			if ((elms.length > 1) && (tContentType != null)) {
				tContentType = elms[1].replaceFirst("[.]", "/");
			} 
									
			// Try contentType first. If doesn't work, use file extension. 
			// If the string after . is greater than 4 characters, use it for 
			// content-type and set file extension to null. 			
			try {
				if (tContentType == null) 
					tContentType = url.openConnection().getContentType();
				
				// extract from filename
				if ((tContentType == null) || tContentType.contains("unknown")) {
					// Only need the following on mtest that encoded request info in the 
					// filename before changing it to use the DB. 
					
					// 2 cases: no file extension, or invalid file extension file extension 
					// length is more than 4 characters
					if (tFileType == null) { 
						// check long extend for content-type info
						String parts[] = tFileName.split("\\.", 2); 
						if (parts.length > 1) {  // contain some content type info but not file ext
							tFileName = parts[0];
							tContentType = "application/" + parts[1];
						} else { // no info. 
							tContentType = null;
						}
					} else {  // construct content-type from fileType
						if (tFileType.equals("txt"))
							tContentType = "text/plain";
						tContentType = "application/" + tFileType;
					}
				} else { 
					// construct filename derived from content type
					if (tFileType == null) {
						tFileType = tContentType.split("/")[1];
						if (tFileType != null)  
							tFileName = tFileName + "." + tFileType;			
					}
				} 
			} catch(IOException e) {
				throw new DomainException(
						ErrorCode.SYSTEM_GENERAL_ERROR,
						"The media file does not exist.",
						e);
			} // end try
					
			return new ContentInfo(tContentType, tFileName, tFileType);
		} 
				
		/**
		 * Return the content-type of this media object. Null can be 
		 * returned if the content-type doesn't exist.  
		 *  		
		 * @return Content-type string.
		 */
		String getContentType() {
			return this.contentType;
		}

		/**
		 * Return the media file name. Return null if this info doesn't exist. 
		 *  		
		 * @return Media file name. 
		 */
		String getFileName() { 
			return this.fileName;
		}
	
		/**
		 * Return the file type (derived from file extension). 
		 * Return null if this info doesn't exist. 
		 *  		
		 * @return Media file type. 
		 */
		String getFileType() { 
			return this.fileType;
		}
	
		/**
		 * Return the string representing the data in the contentInfo object to be 
		 * stored in the url_based_resource table. 
		 *  		
		 * @return Metadata string representing the contentInfo object.  
		 */
		public String toMetadata() {
			StringBuilder builder = new StringBuilder();
			if (contentType != null)
				builder.append(KEY_CONTENT_TYPE + KEY_VALUE_SEPARATOR + contentType + FIELD_SEPARATOR);
			if (fileName != null)
				builder.append(KEY_FILE_NAME + KEY_VALUE_SEPARATOR + fileName + FIELD_SEPARATOR);
			
			if (builder.length() > 0)
				return builder.toString();
			else return null;			
		}
	}
	
	/**
	 * Extract the file extension from a given filename. The file extension has to 
	 * be no more than MAX_EXTENSION_LENGTH.  
	 *  		
	 * @param filename The name of the media file. 
	 * 
	 * @return File extension if it exists. Otherwise, return null.
	 */
	public static String getFileTypeFromFileName(String filename){
		String type = null;
		
		if (filename != null) {
			String[] parts = filename.split("[\\.]");
			if(parts.length != 0) {
				String extension = parts[parts.length - 1];
				if(extension.length() <= Media.MAX_EXTENSION_LENGTH) {
					type = parts[parts.length - 1];
				} 
			}
		}
		
		return type;
	}
	
	/**
	 * Creates a Media object with an ID, type, and the literal content.
	 * This is usually called from survey/upload.
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
		final String contentType,
		final String fileName,
		final byte[] content)
		throws DomainException {
		
		// Validate the ID.
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		else {
			this.id = id;
		}
		
		this.contentInfo = new ContentInfo(contentType, fileName);
		
		// Validate the content.
		if ((content == null) || (content.length == 0)) {
			throw new DomainException(ErrorCode.MEDIA_INVALID_DATA, "The media content is empty.");
		}
		else {
			this.content = new ByteArrayInputStream(content);
		}
		
		// Validate the size.
		this.size = content.length;
	}
	
	/**
	 * Creates a Media object with an ID, type, and the content stream.
	 * This is usually called to create a media object from other 
	 * media object. This is no longer needed after IMedia was created!
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
		final String contentType,
		final String fileName,
		final long fileSize,
		final InputStream contentStream)
		throws DomainException {
		
		// Validate the ID.
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		else {
			this.id = id;
		}
		
		this.contentInfo = new ContentInfo(contentType, fileName);
		
		// Validate the content.
		if ((contentStream == null) || (fileSize <= 0)) {
			throw new DomainException(ErrorCode.MEDIA_INVALID_DATA, "The media content is empty.");
		}
		else {
			this.content = contentStream;
		}
		
		// Validate the size. 
		this.size = fileSize; 
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
	 * @param info
	 * 			The request metadata e.g. content-type, filename. 
	 *  
	 * @throws DomainException
	 *         A parameter was invalid or the data could not be read.
	 */
	public Media(final UUID id, final URL url, final String info) throws DomainException {
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		else {
			this.id = id;
		}
		
		if (url == null)
			throw new DomainException("[MediaID " + id.toString() + "] URL is null.");
		
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
		
		// extract contentInfo from metadata
		contentInfo = ContentInfo.createContentInfoFromUrl(url, info);
		
	}
	
	// ==== Begin IMedia implementation ============================
	/**
	 * Returns the ID.
	 * 
	 * @return The ID.
	 */
	public UUID getId() {
		return id;
	}
	

	/**
	 * Returns an input stream connected to the data.
	 * 
	 * @return An input stream connected to the data.
	 */
	public InputStream getContentStream() throws DomainException {
		return content;
	}
	
	/**
	 * Returns the media's size.
	 * 
	 * @return The media's size.
	 */
	public long getFileSize() {
		return size;
	}
	
	/**
	 * Returns ContentInfo object associated with this media.
	 * 
	 * @return The media's size.
	 */
	//public Media.ContentInfo getContentInfo(){
	//	return this.contentInfo;
	//}
	
	/**
	 * Creates a filename for this media based on its ID and type.
	 * 
	 * @return The file's filename.
	 */
	public String getFileName() {
		return contentInfo.getFileName();
	}
	
	/**
	 * Returns the type (e.g. file extension)
	 * 
	 * @return The type.
	 */
	public String getFileType() {
		return contentInfo.getFileType();
	}
	
	/**
	 * Returns the ContentInfo of this object.
	 * 
	 * @return The ContentInfo of this object.
	 */
	public String getContentType() {
		return contentInfo.getContentType();
	}
	
	/**
	 * Returns the metadata string to be stored in the database entry
	 * together with the media url.  
	 * 
	 * @return The metadata for this object of the form key:value.
	 * 			Each key:value is separated by ";".
	 */
	public String getMetadata() {
		return contentInfo.toMetadata();
	}
	
	/**
	 * Writes the media content to the given file. 
	 *  
	 * @param directory The directory to write the media to.
	 * 
	 * @throws DomainException There was an error reading or writing 
	 * 						   the file.
	 * 
	 */
	public final File writeContent(final File directory) throws DomainException {
		
		if (directory == null)
			throw new DomainException("Directory to write the content file is null");
		
		File mediaFile = new File(directory.getAbsolutePath() + "/" + id.toString());
		writeFile(mediaFile);
		return mediaFile;
	}
	
	
	// ==== End IMedia implementation ======================
	
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
	 * @param destination The file to write the image to.
	 * 
	 * @throws DomainException There was an error reading the data
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
	