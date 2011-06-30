package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.ohmage.request.ClassRosterUpdateRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

/**
 * Basic validator for a class roster update.
 * 
 * @author John Jenkins
 */
public class ClassRosterUpdateValidator extends AbstractHttpServletRequestValidator {
	private static final Logger _logger = Logger.getLogger(ClassRosterUpdateValidator.class);
	
	private final DiskFileItemFactory _diskFileItemFactory;
	private final int _fileSizeMax;
	
	/**
	 * Builds this validator.
	 * 
	 * @param diskFileItemFactory The disk file item factory to decide when to
	 * 							  push parameters to disk.
	 * 
	 * @param fileSizeMax The maximum allowed file size of the roster.
	 */
	public ClassRosterUpdateValidator(DiskFileItemFactory diskFileItemFactory, int fileSizeMax) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("The disk file item factory cannot be null.");
		}
		
		_diskFileItemFactory = diskFileItemFactory;
		_fileSizeMax = fileSizeMax;
	}

	/**
	 * Gets the file from the request and performs basic validation for the
	 * required parameters.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(_diskFileItemFactory);
		upload.setHeaderEncoding("UTF-8");
		upload.setFileSizeMax(_fileSizeMax);
		
		// Parse the request
		List<?> uploadedItems = null;
		try {
		
			uploadedItems = upload.parseRequest(httpRequest);
		
		} catch(FileUploadException fue) { 			
			_logger.error("Caught exception while uploading XML to create a new campaign.", fue);
			return false;
		}
		
		// Get the number of items were in the request.
		int numberOfUploadedItems = uploadedItems.size();
		
		// Get the authentication / session token from the header.
		String token = null;
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			// Check if the token is a parameter. This is the temporary fix
			// until we get Set-Cookie to work.
			//throw new MissingAuthTokenException("The required authentication / session token is missing.");
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		else {
			token = tokens.get(0);
		}
		
		// Parse the request for each of the parameters.
		String client = null;
		String roster = null;
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String name = fi.getFieldName();
				String value = StringUtils.urlDecode(fi.getString());
				
				if(InputKeys.AUTH_TOKEN.equals(name)) {
					if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, value, 36)) {
						throw new MissingAuthTokenException("The required authentication / session token is the wrong length.");
					}
					token = value;
				}
				else if(InputKeys.CLIENT.equals(name)) {
					if(greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, value, 255)) {
						_logger.warn("The client parameter is too long.");
						return false;
					}
					client = value;
				}
			} else {
				if(InputKeys.ROSTER.equals(fi.getFieldName())) {
					// Gets the class roster.
					roster = new String(fi.get()); 
					
					// Excel (and most of Windows) saves newlines as carriage
					// returns instead of newlines, so we substitute those here
					// as we only deal with newlines.
					roster = roster.replace('\r', '\n');
				}
			}
		}
		
		if(client == null) {
			_logger.warn("Missing required parameter: " + InputKeys.CLIENT);
			return false;
		}
		
		ClassRosterUpdateRequest request;
		try {
			request = new ClassRosterUpdateRequest(token, roster);
		}
		catch(IllegalArgumentException e) {
			_logger.warn("Missing a required parameter in the request: " + e.getMessage());
			return false;
		}
		httpRequest.setAttribute("awRequest", request);
		
		return true;
	}
}