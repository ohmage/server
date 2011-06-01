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
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validates with the database that the URN doesn't already exist.
 * 
 * @author John Jenkins
 */
public class DocumentUrnDoesntExistValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(DocumentUrnDoesntExistValidationService.class);
	
	/**
	 * Sets up this service with an annotator to respond with should the
	 * validation fail and a DAO to use to check the database.
	 * 
	 * @param annotator The annotator to use should the validation fail.
	 * 
	 * @param dao The DAO to run that will query the database.
	 */
	public DocumentUrnDoesntExistValidationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Runs the DAO and catches any exceptions it throws.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that this document's URN doesn't already exist.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The URN already exists for another document.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

}
