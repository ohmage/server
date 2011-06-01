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

import java.util.Arrays;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


/**
 * Checks the value of the flag that determines if the list of documents that
 * are associated directly with the user via the document-user table should be
 * returned. If it exists and is set to true, it will get that list of 
 * documents and add it to the list that is going to be returned to the user.
 * 
 * @author John Jenkins
 */
public class DocumentIdAggregationForRequestingUserService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(DocumentIdAggregationForRequestingUserService.class);
	
	private boolean _required;
	
	/**
	 * Builds this Service.
	 * 
	 * @param dao The DataSource that is run to get the list of document IDs.
	 * 
	 * @param required Whether or not this validation is required if the key is
	 * 				   missing from the request.
	 */
	public DocumentIdAggregationForRequestingUserService(Dao dao, boolean required) {
		super(dao);
		
		_required = required;
	}
	
	/**
	 * Checks that the flag that indicates whether or not documents that are
	 * directly associated with the user through the document-user table should
	 * be returned exists and throws an exception if it is required. Then, it
	 * checks the actual value of the flag and if false, quits.
	 * 
	 * It then runs the DAO, gets the list of document IDs back, and places
	 * them in the list of document IDs that will be returned to the user.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the flag that indicates if we should retrieve the list of
		// documents that belong to the requesting user through the document-
		// user table.
		String personalDocuments;
		try {
			personalDocuments = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key : " + InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
			}
			else {
				return;
			}
		}
		
		// Get the result set this far.
		Set<String> result;
		try {
			String resultString = (String) awRequest.getToReturnValue(DocumentIdAggregationService.KEY_DOCUMENT_ID_AGGREGATION_SERVICE_RESULT);
			if("".equals(resultString)) {
				result = new HashSet<String>();
			}
			else {
				result = new HashSet<String>(Arrays.asList(resultString.split(InputKeys.LIST_ITEM_SEPARATOR)));
			}
		}
		catch(IllegalArgumentException e) {
			result = new HashSet<String>();
		}
		
		// If the flag is false, then abort.
		if(personalDocuments.equals("true")) {
			_logger.info("Retrieving the documents to which this user is personally assigned.");
			
			// Run the DAO to populate the list.
			try {
				getDao().execute(awRequest);
			}
			catch(DataAccessException e) {
				throw new ServiceException(e);
			}
	
			// Add any new values to the result set.
			ListIterator<?> daoResultsIter = awRequest.getResultList().listIterator();
			while(daoResultsIter.hasNext()) {
				result.add((String) daoResultsIter.next());
			}
		}
		
		// Place the updated result set back in the request.
		awRequest.addToReturn(DocumentIdAggregationService.KEY_DOCUMENT_ID_AGGREGATION_SERVICE_RESULT, 
							  StringUtils.collectionToDelimitedString(result, InputKeys.LIST_ITEM_SEPARATOR), 
							  true);
	}

}
