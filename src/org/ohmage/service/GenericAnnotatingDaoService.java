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
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * "Generic" Service that dispatches to a DAO where the DAO controls whether the request has failed. This class exists
 * in order separate the annotation of the request up into the Service layer instead of handling it in the DAO. 
 * 
 * @author Joshua Selsky
 */
public class GenericAnnotatingDaoService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(GenericAnnotatingDaoService.class);
	private String _annotationMessage;
	private String _loggingMessage;
	
	/**
	 * Builds a validation service to check if a survey key (survey_id) exists.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public GenericAnnotatingDaoService(AwRequestAnnotator annotator, Dao dao, String annotationMessage, String loggingMessage) {
		super(dao, annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(annotationMessage)) {
			throw new IllegalArgumentException("The annotation message cannot be null or empty.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(loggingMessage)) {
			throw new IllegalArgumentException("The logging message cannot be null or empty.");
		}
		
		_annotationMessage = annotationMessage;
		_loggingMessage = loggingMessage;
	}
	
	/**
	 * Dispatches to the DAO and annotates the request if the request has failed in the DAO layer.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info(_loggingMessage);
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, _annotationMessage);
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
