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

import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.Document;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DocumentReadAwRequest;


public class DocumentReadResponseBuilderService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(DocumentReadResponseBuilderService.class);
	
	public DocumentReadResponseBuilderService(Dao dao) {
		super(dao);
	}

	/**
	 * Runs the DAO to set the result list as the list of Documents and their
	 * information.
	 * 
	 * Gets the result from the result list which should be a list of Document
	 * objects. Then, adds each of them to a JSONObject with the key being 
	 * their unique IDs. Finally, it returns that JSONObject as a String to the
	 * request to be written by the writer.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Creating response for document read request.");
		
		// Run the DAO to set the result list as the Documents and their info.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// Create the JSONObject that will be the response.
		JSONObject result = new JSONObject();
		ListIterator<?> resultIter = awRequest.getResultList().listIterator();
		while(resultIter.hasNext()) {
			Document currDocument = (Document) resultIter.next();
			
			try {
				result.put(currDocument.getDocumentId(), currDocument.toJsonObject());
			}
			catch(JSONException e) {
				_logger.error("Error creating response object for document: " + currDocument.getDocumentId(), e);
			}
		}
		
		// Set the response in the request.
		awRequest.addToReturn(DocumentReadAwRequest.KEY_DOCUMENT_INFORMATION, result, true);
	}
}
