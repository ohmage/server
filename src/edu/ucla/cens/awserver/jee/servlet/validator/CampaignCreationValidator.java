package edu.ucla.cens.awserver.jee.servlet.validator;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validator for HTTP requests to create a new campaign to the database.
 * 
 * @author John Jenkins
 */
public class CampaignCreationValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignCreationValidator.class);
	private List<String> _parameterList;
	
	private DiskFileItemFactory _diskFileItemFactory;
	private int _fileSizeMax;
	
	/**
	 * Basic constructor that sets up the list of required parameters.
	 */
	public CampaignCreationValidator(DiskFileItemFactory diskFileItemFactory, int fileSizeMax) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("a DiskFileItemFactory is required");
		}
		_diskFileItemFactory = diskFileItemFactory;
		_fileSizeMax = fileSizeMax;
		
		_parameterList = new ArrayList<String>(Arrays.asList(new String[] { "auth_token", "running_state", "privacy_state", "xml", "classes" }));
	}

	/**
	 * Ensures that all the required parameters exist and that each parameter
	 * is of a sane length.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
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
			throw new IllegalStateException(fue);
		}
		
		// Check that the correct number of items were in the request.
		int numberOfUploadedItems = uploadedItems.size();
		if(numberOfUploadedItems != 5) {
			_logger.warn("An incorrect number of parameters were found on a campaign creation attempt. 5 were expeced and " + numberOfUploadedItems
				+ " were received");
			return false;
		}
		
		// Parse the request for each of the parameters.
		String runningState = null;
		String privacyState = null;
		String xml = null;
		String classes = null;
		String description = null;
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String name = fi.getFieldName();
				String tmp = StringUtils.urlDecode(fi.getString());
				
				if(! _parameterList.contains(name)) {
					_logger.warn("An unknown parameter was found in a media upload: " + name);
					return false;
				}
				else if("auth_token".equals(name)) {
					if(greaterThanLength("authToken", "auth_token", tmp, 36)) {
						return false;
					}
				}
				else if("running_state".equals(name)) {
					if(greaterThanLength("runningState", "running_state", tmp, 50)) {
						return false;
					}
					runningState = tmp;
				}
				else if("privacy_state".equals(name)) {
					if(greaterThanLength("privacyState", "privacy_state", tmp, 50)) {
						return false;
					}
					privacyState = tmp;
				}
				else if("classes".equals(name)) {
					// Note: This is based on the maximum size of a campaign
					// times 100 plus 100 commas.
					if(greaterThanLength("classes", "classes", tmp, 25600)) {
						return false;
					}
					classes = tmp;
				}
				else if("description".equals(name)) {
					if(greaterThanLength("description", "description", tmp, 65535)) {
						return false;
					}
					description = tmp;
				}
				
			} else { // It's the attached file.
				
				// The XML data is not checked because its length is so variable and potentially huge.
				// The default setting for Tomcat is to disallow requests that are greater than 2MB, which may have to change in the future
				String contentType = fi.getContentType();
				if(! "text/xml".equals(contentType)) {
					_logger.warn("The data type must be text/xml but instead we got: " + contentType);
					return false;
				}
				
				xml = new String(fi.get()); // Gets the XML file.
				if(_logger.isDebugEnabled()) {
					_logger.debug("Attempting upload of an XML file of " + xml.length() + " bytes: " + xml);
				}
			}
		}
		
		CampaignCreationAwRequest request;
		try {
			request = new CampaignCreationAwRequest(runningState, privacyState, xml, classes, description);
		}
		catch(InvalidParameterException e) {
			_logger.error("Attempting to create a campaign with insufficient data after data presence has been checked.");
			return false;
		}
		httpRequest.setAttribute("awRequest", request);
		
		return true;
	}

}
