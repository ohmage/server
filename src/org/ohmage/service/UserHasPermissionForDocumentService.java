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
package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Checks that the user has sufficient permissions to delete the file.
 * 
 * @author John Jenkins
 */
public class UserHasPermissionForDocumentService extends AbstractAnnotatingDaoService {
	public static Logger _logger = Logger.getLogger(UserHasPermissionForDocumentService.class);
	
	private boolean _required;
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to respond with should the user not have
	 * 					the correct permissions to delete this document.
	 * 
	 * @param dao The DAO to run to execute this service.
	 */
	public UserHasPermissionForDocumentService(AwRequestAnnotator annotator, Dao dao, boolean required) {
		super(dao, annotator);
		
		_required = required;
	}

	/** 
	 * Gets the list of documents that the requesting user is allowed to delete
	 * and checks that the one in the request is among them.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the document ID from the request.
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key '" + InputKeys.DOCUMENT_ID + "'.");
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the requesting user has sufficient permissions to perform this action on the document.");
		
		// Get the list of IDs the user is allowed to delete.
		try {
			getDao().execute(awRequest);
		} 
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// Validate that the user can delete the document.
		if(! awRequest.getResultList().contains(documentId)) {
			getAnnotator().annotate(awRequest, "The requesting user doesn't have sufficient permissions to perform this action on the document.");
			awRequest.setFailedRequest(true);
		}
	}
}
