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
import org.ohmage.request.survey.SurveyUploadRequest;

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
	private static final Logger LOGGER =
			Logger.getLogger(SurveyUploadRequest.class);
	/**
	 * The maximum length for a file extension.
	 */
	public static final int MAX_EXTENSION_LENGTH = 4;

	private final UUID id;
	private final InputStream content; 
	private ContentInfo contentInfo; 
	
	 private String contentType = null;  
	 private String type = null;
	 private String filename = null;
	 
	/**
	 * The size, in bytes, of the media file.
	 */
	public final int size;

	public static class ContentInfo {
		public static String FIELD_SEPARATOR = ";";
		public static String KEY_VALUE_SEPARATOR = ":";
		public static String KEY_CONTENT_TYPE = "ContentType";
		public static String KEY_FILE_NAME = "FileName";
		private String contentType;
		private String fileName; 
		private String fileType;
		
		ContentInfo(String contentType, String fileName) {
			this.contentType = contentType;
			this.fileName = fileName;
			this.fileType = getFileTypeFromFileName(fileName);
			LOGGER.debug(this.toMetadata());
		}
		
		ContentInfo(String contentType, String fileName, String fileType) {
			this.contentType = contentType;
			this.fileName = fileName;
			this.fileType = fileType;
			LOGGER.debug(this.toMetadata());
		}

		public static ContentInfo createContentInfoFromUrl(URL url, String info) 
		throws DomainException {
			String tContentType = null;
			String tFileName = null;
			String tFileType = null;
			
			if ((info == null) || info.isEmpty()) {
				// extract info from url instead. Backward compatibility
				if (url == null) 
					return new ContentInfo(null, null);
				
				String paths[] = url.toString().split("/");
				String simpleName = paths[paths.length-1];
				String elms[] = simpleName.split("__");
				tFileName = elms[0];
				tFileType = getFileTypeFromFileName(tFileName);

				// get the contentType if it exists
				if (elms.length > 1) {
					tContentType = elms[1].replaceFirst("[.]", "/");
				} 
				
					
				// Try contentType first. If doesn't work, use file extension. 
				// If there is no extension or if it is greater than
				// three characters long, the extension will be set to null.
				try {
					if (tContentType == null) 
						tContentType = url.openConnection().getContentType();

						// extract from filename
					if ((tContentType == null) || tContentType.contains("unknown")) {
						// 2 cases: no file extension, or invalid file extension file extension 
						// length is more than 4 characters
						if (tFileType == null) { 
							// check long extend for content-type info
							String parts[] = tFileName.split("\\.", 2); 
							// LOGGER.debug("HT: Checking filename:" + tFileName);
							if (parts.length > 1) {  // contain some content type info but not file ext
								tFileName = parts[0];
								tContentType = "application/" + parts[1];
								// LOGGER.debug("HT: parts[0]=" + parts[0] + " parts[1]=" + parts[1]);
							} else { // no info
								// LOGGER.debug("HT: parts[0]=" + parts[0]);
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
			} // there is info in the db
			else {
				String parts[] = info.split(FIELD_SEPARATOR);
				for (String p : parts){
					p = p.trim();  // trim white space?
					String keyValue[] = p.split(KEY_VALUE_SEPARATOR);
					if (keyValue[0].equals(KEY_CONTENT_TYPE) && (! keyValue[1].isEmpty()))
						tContentType = keyValue[1];
					if (keyValue[0].equals(KEY_FILE_NAME) && (! keyValue[1].isEmpty()))
						tFileName = keyValue[1];
				}
				LOGGER.debug("HT: contentType:" + tContentType + " FileName:" + tFileName);
				return new ContentInfo(tContentType, tFileName);
			}
			
		}
		
		String getContentType() {
			return this.contentType;
		}
		String getFileName() { 
			return this.fileName;
		}
		String getFileType() { 
			return this.fileType;
		}
	
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
			
			// Validate the type. Allow null at the moment. 
			// if(type == null) throw new DomainException("The type is null.");
			if (type != null) {
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
		
	// can be deleted
	/*
	private String getFileType(String filename){
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
	*/
	
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

		String paths[] = url.toString().split("/");
		String simpleName = paths[paths.length-1];
		String elms[] = simpleName.split("__");
		String filename = elms[0];
		String fileType = getFileTypeFromFileName(filename);

		if (elms.length > 1) {
			this.contentType = elms[1].replaceFirst("[.]", "/");
		} 
		
		if (fileType != null) {
			this.filename = filename;
			this.type = fileType;
		} else {	
			
		// Try contentType first. If doesn't work, use file extension. 
		// If there is no extension or if it is greater than
		// three characters long, the extension will be set to null.
			try {
				this.contentType = url.openConnection().getContentType();
				if ((this.type == null) || this.contentType.contains("unknown")) {
					String parts[] = filename.split(".", 2);
					if (parts.length > 1) {
						this.contentType = "Application/" + parts[1];
					} else 
						this.contentType = null;
					this.type = null;
				} else {
					this.type = this.contentType.split("/")[1];
				}
			} catch(IOException e) {
				throw new DomainException(
						ErrorCode.SYSTEM_GENERAL_ERROR,
						"The media file does not exist.",
						e);
			}
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
	
	public ContentInfo getContentInfo(){
		return this.contentInfo;
	}
	
	/**
	 * Creates a filename for this media based on its ID and type.
	 * 
	 * @return The file's filename.
	 */
	public String getFileName() {
		return contentInfo.getFileName();
		/*
		if (filename != null)
			return filename;
		
		if (type == null)
			return id.toString();
		return id.toString() + "." + type;
		*/
	}
	
	/**
	 * Returns the type.
	 * 
	 * @return The type.
	 */
	public String getType() {
		return contentInfo.getFileType();
	}
	
	/**
	 * Returns the MIME type for this media object. The type is set based on
	 * the filename extension.
	 * 
	 * @return The MIME type for this object of the form "{super}/{sub}".
	 */
	public String getContentType() {
		return contentInfo.getContentType();
		/*
		if (contentType != null)
			return contentType;
		
		String ttype = getType();
		if (ttype == null)
			return null;
		return getMimeTypeRoot(ttype) + "/" + ttype;
		*/
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
	