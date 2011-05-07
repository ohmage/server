package edu.ucla.cens.awserver.jee.servlet.validator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.MediaUploadAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates a multipart/form-data POST for media upload using the Apache Commons library.
 * 
 * @author selsky
 */
public class MediaUploadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(MediaUploadValidator.class);
	private Set<String> _parameterSet;
	private DiskFileItemFactory _diskFileItemFactory;
	private int _fileSizeMax;
	
	public MediaUploadValidator(DiskFileItemFactory diskFileItemFactory, int fileSizeMax) {
		if(null == diskFileItemFactory) {
			throw new IllegalArgumentException("a DiskFileItemFactory is required");
		}
		_diskFileItemFactory = diskFileItemFactory;
		_fileSizeMax = fileSizeMax;
		
		_parameterSet = new TreeSet<String>();
		_parameterSet.addAll(Arrays.asList(new String[]{"campaign_urn",
				                                        "client",
				                                        "id",
				                                        "password",
				                                        "user",
				                                        "campaign_creation_timestamp"}));
	}
	
	/**
	 * Validates the media upload data and creates a new AwRequest in order to avoid parsing the multipart upload twice. 
	 * The AwRequest is pushed into the HttpServletRequest, which is a crappy hack that should be fixed. TODO
	 */
	@Override
	public boolean validate(HttpServletRequest request) {
		
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(_diskFileItemFactory);
		upload.setHeaderEncoding("UTF-8");
		upload.setFileSizeMax(_fileSizeMax);
		
		List<?> uploadedItems = null;
		
		// Parse the request
		try {
		
			uploadedItems = upload.parseRequest(request);
		
		} catch(FileUploadException fue) { // image too large; the parseRequest will fail fast
			
			_logger.warn("unable to retrieve items from request");
			throw new IllegalStateException(fue);
		}
		
		int numberOfUploadedItems = uploadedItems.size();
		
		if(7 != numberOfUploadedItems) {
			_logger.warn("an incorrect number of parameters was found on media upload. 7 were expected and " + numberOfUploadedItems
				+ " were received");
			return false;
		}
		
		MediaUploadAwRequest awRequest = new MediaUploadAwRequest();
		User user = new UserImpl();
		
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			String tmp = null;
			
			if(fi.isFormField()) {
				String name = fi.getFieldName();
				
				if(! _parameterSet.contains(name)) {
					
					_logger.warn("an unknown parameter was found in a media upload: " + name);
					return false;
				}
				
				tmp = StringUtils.urlDecode(fi.getString());
				
				if("campaign_urn".equals(name)) {
					
					if(greaterThanLength("campaign URN", "campaign_urn", tmp, 250)) {
						return false;
					}
					
					awRequest.setCampaignUrn(tmp);
				}
				
				if("client".equals(name)) {
					
					if(greaterThanLength("client", "client", tmp, 250)) {
						return false;
					}
					
					awRequest.setClient(tmp);
				}
				
				if("id".equals(name)) {
					
					if(greaterThanLength("id", "id", tmp, 36)) {
						return false;
					}
					
					awRequest.setMediaId(tmp);
				}
				
				if("password".equals(name)) {
					
					if(greaterThanLength("password", "password", tmp, 100)) {
						return false;
					}
					
					user.setPassword(tmp);
				}
				
				
				if("user".equals(name)) {
					
					if(greaterThanLength("user name", "user", tmp, 15)) {
						return false;
					}
					
					user.setUserName(tmp);
				}
				
				if("campaign_creation_timestamp".equals(name)) {
					
					if(greaterThanLength("campaign creation timestamp", "campaign_creation_timestamp", tmp, 19)) {
						return false;
					}
					
					awRequest.setCampaignCreationTimestamp(tmp);
				}
				
			} else { // it's an attached file 
				
				// The media data is not checked because its length is so variable and potentially huge.
				// The default setting for Tomcat is to disallow requests that are greater than 2MB, which may have to change in the future
				
				String contentType = fi.getContentType();
				
				if(! "image/jpeg".equals(contentType)) { // only allow jpegs for now
					
					_logger.warn("an unsupported content-type was found for a media upload attachment: " + contentType);
					return false;
				}
				
				byte[] mediaBytes = fi.get(); // converts the stream to a byte array
				if(_logger.isDebugEnabled()) {
					_logger.debug("attempting upload of a file of " + mediaBytes.length + " bytes");
				}
				awRequest.setMedia(mediaBytes); 
			}
		}
		
		awRequest.setUser(user);
		request.setAttribute("awRequest", awRequest);
		return true;
	}
}
