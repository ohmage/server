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
import org.ohmage.domain.ClassInfo;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassReadAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Reads the information about a class using the provided DAO.
 * 
 * @author John Jenkins
 */
public class ClassReadService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(ClassReadService.class);
	
	/**
	 * Creates the service with an annotator to respond with should there be
	 * an error and a DAO to call to make the request.
	 * 
	 * @param annotator The annotator to respond with should there be an
	 * 					error.
	 * 
	 * @param dao The DAO to use to make the request.
	 */
	public ClassReadService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Runs the DAO and then gets the information that it aggregated and places
	 * it in a JSONObject and adds that to the request to be returned by the
	 * writer.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to read the class information.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "There was an error reading the information about the classes.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
		
		JSONObject result = new JSONObject();
		ListIterator<?> classInfoList = awRequest.getResultList().listIterator();
		while(classInfoList.hasNext()) {
			ClassInfo currClassInfo = (ClassInfo) classInfoList.next();
			
			try {
				result.put(currClassInfo.getUrn(), currClassInfo.getJsonRepresentation(false));
			}
			catch(JSONException e) { 
				_logger.error("Error creating a response for the current class: " + currClassInfo.getUrn());	
			}
		}
		
		awRequest.addToReturn(ClassReadAwRequest.RETURN, result, true);
	}

}
