package org.ohmage.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
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
// HT: change from abstract class to regular class.
public interface IMedia {
	static final Logger LOGGER = Logger.getLogger(IMedia.class);
	/**
	 * <p>
	 * The class that contains request information relevant to the 
	 * media object. This class is immutable.
	 * </p>
	 *
	 * @author Hongsuda T.
	 */
	public static class ContentInfo {
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
								
			// extract info from url instead. This is also for backward compatibility
			if (url == null) 
				return new ContentInfo(null, null);
				
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
	public InputStream getContentStream();
	
	/**
	 * Returns the media's size.
	 * 
	 * @return The media's size.
	 */
	public long getFileSize();
	
	/**
	 * Returns ContentInfo object associated with this media.
	 * 
	 * @return The media's size.
	 */
	public ContentInfo getContentInfo();
	
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
	public String getFileType();
	
	/**
	 * Returns the MIME type for this media object. 
	 * 
	 * @return The MIME type for this object of the form "{super}/{sub}".
	 */
	public String getContentType();
	
	
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
	public void writeFile(final File destination)
		throws DomainException;
	
}
	