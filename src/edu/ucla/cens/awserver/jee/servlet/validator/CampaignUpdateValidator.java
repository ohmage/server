package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.CampaignUpdateAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

public class CampaignUpdateValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignUpdateValidator.class);
	
	private DiskFileItemFactory _diskFileItemFactory;
	private int _fileSizeMax;
	
	/**
	 * Basic constructor that sets up the list of required parameters.
	 */
	public CampaignUpdateValidator(DiskFileItemFactory diskFileItemFactory, int fileSizeMax) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("a DiskFileItemFactory is required");
		}
		_diskFileItemFactory = diskFileItemFactory;
		_fileSizeMax = fileSizeMax;
	}

	/**
	 * Ensures that all the required parameters exist and that each parameter
	 * is of a sane length.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		// TODO: Check that the content header type is "multipart/form-data"
		//		 or "multipart/mixed" stream, content header type.
		// 		 For now, this is handled in the first try-catch below, but it
		//		 throws an ugly crash exception in the server which is
		//		 missleading.
		
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
		if((numberOfUploadedItems < 3) || (numberOfUploadedItems > 6)) {
			_logger.warn("An incorrect number of parameters were found on a campaign creation attempt. 3 through 7 were expected and " + numberOfUploadedItems
				+ " were received");
			return false;
		}
		
		// Parse the request for each of the parameters.
		CampaignUpdateAwRequest request = new CampaignUpdateAwRequest();
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String name = fi.getFieldName();
				String tmp = StringUtils.urlDecode(fi.getString());
				
				if(InputKeys.DESCRIPTION.equals(name)) {
					if(greaterThanLength("description", InputKeys.DESCRIPTION, tmp, 65535)) {
						return false;
					}
					request.setDescription(tmp);
				}
				else if(InputKeys.RUNNING_STATE.equals(name)) {
					if(greaterThanLength("runningState", InputKeys.RUNNING_STATE, tmp, 50)) {
						return false;
					}
					request.setRunningState(tmp);
				}
				else if(InputKeys.PRIVACY_STATE.equals(name)) {
					if(greaterThanLength("privacyState", InputKeys.PRIVACY_STATE, tmp, 50)) {
						return false;
					}
					request.setPrivacyState(tmp);
				}
				else if(InputKeys.CLASS_URN_LIST.equals(name)) {
					// Note: This is based on the maximum size of a campaign
					// times 100 plus 100 commas.
					if(greaterThanLength("classes", InputKeys.CLASS_URN_LIST, tmp, 25600)) {
						return false;
					}
					request.setCommaSeparatedClasses(tmp);
				}
				else if(InputKeys.AUTH_TOKEN.equals(name)) {
					if(greaterThanLength("authToken", InputKeys.AUTH_TOKEN, tmp, 36)) {
						return false;
					}
					request.setUserToken(tmp);
				}
				else if(InputKeys.CAMPAIGN_URN.equals(name)) {
					if(greaterThanLength("campaignUrn", InputKeys.CAMPAIGN_URN, tmp, 255)) {
						return false;
					}
					request.setCampaignUrn(tmp);
				}
				else {
					_logger.warn("An unknown parameter was found in a campaign creation request: " + name);
					return false;
				}
				
			} else {
				if(InputKeys.XML.equals(fi.getFieldName()))
				{
					// The XML data is not checked because its length is so variable and potentially huge.
					// The default setting for Tomcat is to disallow requests that are greater than 2MB, which may have to change in the future
					String contentType = fi.getContentType();
					if(! "text/xml".equals(contentType)) {
						_logger.warn("The data type must be text/xml but instead we got: " + contentType);
						return false;
					}
					
					request.setXmlAsByteArray(fi.get()); // Gets the XML file.
				}
				else {
					_logger.warn("An unknown parameter was found in a campaign creation request: " + fi.getFieldName());
					return false;
				}
			}
		}
		
		if(request.getUserToken() == null) {
			_logger.warn("No token in the request.");
			return false;
		}
		else if(request.getCampaignUrn() == null) {
			_logger.warn("No campaign URN in the request.");
			return false;
		}
		httpRequest.setAttribute("awRequest", request);
		
		return true;
	}
}
