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

import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Creates the document on the disk and places an entry in the database.
 * 
 * @author John Jenkins
 */
public class DocumentCreationService extends AbstractAnnotatingDaoService {
	/**
	 * Creates this service with an annotator to use if the request fails and
	 * a DAO to call to run the actual creation.
	 * 
	 * @param annotator The annotator to respond with should the request fail.
	 * 
	 * @param dao The DAO to call to create the document.
	 */
	public DocumentCreationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Calls the DAO to create the document and insert it into the database.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "Document creation failed.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}
