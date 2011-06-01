/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.request.DocumentUpdateRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


/**
 * Validates that the incoming document update request is valid.
 * 
 * @author John Jenkins
 */
public class DocumentUpdateValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DocumentUpdateValidator.class);
	
	private DiskFileItemFactory _diskFileItemFactory;
	
	/**
	 * Builds this validator.
	 * 
	 * @param diskFileItemFactory 
	 */
	public DocumentUpdateValidator(DiskFileItemFactory diskFileItemFactory) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("The disk file item factory cannot be null.");
		}
		
		_diskFileItemFactory = diskFileItemFactory;
	}

	/**
	 * Checks which type of request it is, one that contains a new document
	 * stream and or not, and pushes the validation onto the appropriate 
	 * validator.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String requestType = httpRequest.getContentType();
		
		if(requestType.contains("multipart/form-data;") || requestType.contains("multipart/mixed;")) {
			return validateMultipartRequest(httpRequest);
		}
		else {
			return validateRegularRequest(httpRequest);
		}
	}

	/**
	 * Ensures that the required parameters exist and then builds the request
	 * based on the parameters. It then sets the built request in the HTTP
	 * request as required by convention.
	 * 
	 * @param httpRequest The HTTP request from the end user.
	 * 
	 * @return Returns true if it successfully validated and built the request
	 * 		   and placed that request in the HTTP request.
	 */
	private boolean validateRegularRequest(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String documentId = httpRequest.getParameter(InputKeys.DOCUMENT_ID);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if((authToken == null) || (authToken.length() != 36)) {
			return false;
		}
		else if((documentId == null) || greaterThanLength(InputKeys.DOCUMENT_ID, InputKeys.DOCUMENT_ID, documentId, 255)) {
			return false;
		}
		else if(client == null) {
			return false;
		}
		
		String name = httpRequest.getParameter(InputKeys.DOCUMENT_NAME);
		String description = httpRequest.getParameter(InputKeys.DESCRIPTION);
		String privacyState = httpRequest.getParameter(InputKeys.PRIVACY_STATE);
		String campaignRoleListAdd = httpRequest.getParameter(InputKeys.CAMPAIGN_ROLE_LIST_ADD);
		String campaignListRemove = httpRequest.getParameter(InputKeys.CAMPAIGN_LIST_REMOVE);
		String classRoleListAdd = httpRequest.getParameter(InputKeys.CLASS_ROLE_LIST_ADD);
		String classListRemove = httpRequest.getParameter(InputKeys.CLASS_LIST_REMOVE);
		String userRoleListAdd = httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD);
		String userListRemove = httpRequest.getParameter(InputKeys.USER_LIST_REMOVE);
		
		try {
			DocumentUpdateRequest request = new DocumentUpdateRequest(documentId, name, description, privacyState, null,
					campaignRoleListAdd, campaignListRemove, classRoleListAdd, classListRemove, userRoleListAdd, userListRemove);
			request.setUserToken(authToken);
			
			httpRequest.setAttribute("request", request);
			return true;
		}
		catch(IllegalArgumentException e) {
			_logger.warn("There was an error while building the request.", e);
		}
		
		return false;
	}
	
	/**
	 * Ensures that the required parameters exist and then builds the request
	 * based on the parameters. It then sets the built request in the HTTP
	 * request as required by convention.
	 * 
	 * @param httpRequest The HTTP request from the end user.
	 * 
	 * @return Returns true if it successfully validated and built the request
	 * 		   and placed that request in the HTTP request.
	 */
	private boolean validateMultipartRequest(HttpServletRequest httpRequest) {
		ServletFileUpload upload = new ServletFileUpload(_diskFileItemFactory);
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
		
		int numberOfParameters = uploadedItems.size();
		String authToken = null;
		String documentId = null;
		String name = null;
		String description = null;
		String privacyState = null;
		String document = null;
		String campaignRoleListAdd = null;
		String campaignListRemove = null;
		String classRoleListAdd = null;
		String classListRemove = null;
		String userRoleListAdd = null;
		String userListRemove = null;
		for(int i = 0; i < numberOfParameters; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String fieldName = fi.getFieldName();
				String fieldValue = StringUtils.urlDecode(fi.getString());
				
				if(InputKeys.AUTH_TOKEN.equals(fieldName)) {
					if(fieldValue.length() != 36) {
						return false;
					}
					authToken = fieldValue;
				}
				else if(InputKeys.DOCUMENT_ID.equals(fieldName)) {
					if(greaterThanLength(InputKeys.DOCUMENT_ID, InputKeys.DOCUMENT_ID, fieldValue, 255)) {
						return false;
					}
					documentId = fieldValue;
				}
				else if(InputKeys.DOCUMENT_NAME.equals(fieldName)) {
					name = fieldValue;
				}
				else if(InputKeys.DESCRIPTION.equals(fieldName)) {
					description = fieldValue;
				}
				else if(InputKeys.PRIVACY_STATE.equals(fieldName)) {
					privacyState = fieldValue;
				}
				else if(InputKeys.CAMPAIGN_ROLE_LIST_ADD.equals(fieldName)) {
					campaignRoleListAdd = fieldValue;
				}
				else if(InputKeys.CAMPAIGN_LIST_REMOVE.equals(fieldName)) {
					campaignListRemove = fieldValue;
				}
				else if(InputKeys.CLASS_ROLE_LIST_ADD.equals(fieldName)) {
					classRoleListAdd = fieldValue;
				}
				else if(InputKeys.CLASS_LIST_REMOVE.equals(fieldName)) {
					classListRemove = fieldValue;
				}
				else if(InputKeys.USER_ROLE_LIST_ADD.equals(fieldName)) {
					userRoleListAdd = fieldValue;
				}
				else if(InputKeys.USER_LIST_REMOVE.equals(fieldName)) {
					userListRemove = fieldValue;
				}
			}
			else {
				if(InputKeys.DOCUMENT.equals(fi.getFieldName())) {
					document = new String(fi.get());
				}
			}
		}
		
		try {
			DocumentUpdateRequest request = new DocumentUpdateRequest(documentId, name, description, privacyState, document,
					campaignRoleListAdd, campaignListRemove, classRoleListAdd, classListRemove, userRoleListAdd, userListRemove);
			request.setUserToken(authToken);
			
			httpRequest.setAttribute("request", request);
			return true;
		}
		catch(IllegalArgumentException e) {
			_logger.warn("There was an error while building the request.");
		}
		
		return false;
	}
}
