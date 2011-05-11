package edu.ucla.cens.awserver.jee.servlet.validator;

import java.security.InvalidParameterException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.CacheMissException;
import edu.ucla.cens.awserver.cache.PreferenceCache;
import edu.ucla.cens.awserver.request.DocumentCreationAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

public class DocumentCreationValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DocumentCreationValidator.class);
	
	private DiskFileItemFactory _diskFileItemFactory;
	
	public DocumentCreationValidator(DiskFileItemFactory diskFileItemFactory) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("The disk file item factory cannot be null.");
		}
		
		_diskFileItemFactory = diskFileItemFactory;
	}

	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(_diskFileItemFactory);
		// TODO: Is this necessary? Should it even be done?
		upload.setHeaderEncoding("UTF-8");
		
		// Set the maximum allowed size of a document.
		try {
			upload.setFileSizeMax(Long.valueOf(PreferenceCache.instance().lookup(PreferenceCache.KEY_MAXIMUM_DOCUMENT_SIZE)));
		}
		catch(CacheMissException e) {
			_logger.error("Cache miss while trying to get maximum allowed document size.", e);
			return false;
		}
		
		// Parse the request
		List<?> uploadedItems = null;
		try {
		
			uploadedItems = upload.parseRequest(httpRequest);
		
		} catch(FileUploadException fue) { 			
			_logger.error("Caught exception while uploading a document.", fue);
			return false;
		}
		
		// Check that the correct number of items were in the request.
		int numberOfUploadedItems = uploadedItems.size();
		if(numberOfUploadedItems < 5) {
			_logger.warn("An incorrect number of parameters were found on a campaign creation attempt. At least 5 were expected and " + numberOfUploadedItems
				+ " were received");
			return false;
		}
		
		// Parse the request for each of the parameters.
		String token = null;
		String urn = null;
		String name = null;
		String document = null;
		String privacyState = null;
		String description = null;
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String fieldName = fi.getFieldName();
				String fieldValue = StringUtils.urlDecode(fi.getString());
				
				if(InputKeys.AUTH_TOKEN.equals(fieldName)) {
					if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, fieldValue, 36)) {
						return false;
					}
					token = fieldValue;
				}
				else if(InputKeys.DOCUMENT_URN.equals(fieldName)) {
					if(greaterThanLength(InputKeys.DOCUMENT_URN, InputKeys.DOCUMENT_URN, fieldValue, 255)) {
						return false;
					}
					urn = fieldValue;
				}
				else if(InputKeys.DOCUMENT_NAME.equals(fieldName)) {
					if(greaterThanLength(InputKeys.DOCUMENT_NAME, InputKeys.DOCUMENT_NAME, fieldValue, 255)) {
						return false;
					}
					name = fieldValue;
				}
				else if(InputKeys.PRIVACY_STATE.equals(fieldName)) {
					if(greaterThanLength(InputKeys.PRIVACY_STATE, InputKeys.PRIVACY_STATE, fieldValue, 50)) {
						return false;
					}
					privacyState = fieldValue;
				}
				if(InputKeys.DESCRIPTION.equals(fieldName)) {
					if(greaterThanLength(InputKeys.DESCRIPTION, InputKeys.DESCRIPTION, fieldValue, 65535)) {
						return false;
					}
					description = fieldValue;
				}
			} else {
				if(InputKeys.DOCUMENT.equals(fi.getFieldName())) {					
					document = new String(fi.get()); // Gets the document.
				}
			}
		}
		
		DocumentCreationAwRequest request;
		try {
			request = new DocumentCreationAwRequest(urn, name, document, privacyState, description);
		}
		catch(InvalidParameterException e) {
			_logger.error("Attempting to create a document with insufficient data after data presence has been checked.");
			return false;
		}
		request.setUserToken(token);
		httpRequest.setAttribute("awRequest", request);
		
		return true;
	}
}