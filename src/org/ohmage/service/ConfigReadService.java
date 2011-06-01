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
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ConfigReadRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Gets the information from the system and stores it in the request.
 * 
 * @author John Jenkins
 */
public class ConfigReadService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(ConfigReadService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use to get the information about the 
	 * 					request.
	 */
	public ConfigReadService(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Gathering information about the system.");
		
		// Get the response values to be returned to the requestor and place
		// them in the response.
		try {
			JSONObject response = new JSONObject();
			
			// Get the application's name.
			try {
				response.put("application_name", PreferenceCache.instance().lookup(PreferenceCache.KEY_APPLICATION_NAME));
			}
			catch(CacheMissException e) {
				_logger.error("Unknown value for 'known' key '" + PreferenceCache.KEY_APPLICATION_NAME + "'. Is the cache database missing a key-value pair?", e);
				throw new ServiceException(e);
			}
			
			// Get the application's version.
			try {
				response.put("application_version", PreferenceCache.instance().lookup(PreferenceCache.KEY_APPLICATION_VERSION));
			}
			catch(CacheMissException e) {
				_logger.error("Unknown value for 'known' key '" + PreferenceCache.KEY_APPLICATION_NAME + "'. Is the cache database missing a key-value pair?", e);
				throw new ServiceException(e);
			}
			
			// Get the default survey response sharing state.
			try {
				response.put("default_survey_response_sharing_state", PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE));
			}
			catch(CacheMissException e) {
				_logger.error("Unknown value for 'known' key '" + PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE + "'. Is the cache database missing a key-value pair?", e);
				throw new ServiceException(e);
			}
			
			awRequest.addToReturn(ConfigReadRequest.RESULT, response, true);
		}
		catch(JSONException e) {
			_logger.error("Error creating response JSONObject.", e);
			throw new ServiceException(e);
		}
	}

}
