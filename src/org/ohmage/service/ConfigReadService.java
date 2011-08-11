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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
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
	
	private static final String KEY_APPLICATION_NAME = "application_name";
	private static final String KEY_APPLICATION_VERSION = "application_version";
	private static final String KEY_APPLICATION_BUILD = "application_build";
	
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
	 * Gets the system properties and places them in the JSONObject to be
	 * returned to the user.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Gathering information about the system.");
		
		// Get the response values to be returned to the requestor and place
		// them in the response.
		try {
			JSONObject response = new JSONObject();
			
			Properties properties = new Properties();
			try {
				InputStream in = new FileInputStream(PreferenceCache.instance().lookup(PreferenceCache.KEY_PROPERTIES_FILE));
				properties.load(in);
				in.close();
			}
			catch(CacheMissException e) {
				_logger.error("Unknown value for 'known' key '" + PreferenceCache.KEY_PROPERTIES_FILE + "'. Is the cache database missing a key-value pair?", e);
				throw new ServiceException(e);
			}
			catch(IOException e) {
				_logger.error("Missing the properties file that should have been built with the WAR file.", e);
				throw new ServiceException(e);
			}
			
			// Get the application's name.
			response.put("application_name", properties.get(KEY_APPLICATION_NAME));
			
			// Get the application's version.
			response.put("application_version", properties.get(KEY_APPLICATION_VERSION));
			
			// Get the Git build hash.
			response.put("application_build", properties.get(KEY_APPLICATION_BUILD));
			
			// Get the default survey response sharing state.
			try {
				response.put("default_survey_response_sharing_state", PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE));
			}
			catch(CacheMissException e) {
				_logger.error("Unknown value for 'known' key '" + PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE + "'. Is the cache database missing a key-value pair?", e);
				throw new ServiceException(e);
			}
			
			// Get the actual privacy states.
			response.put("survey_response_privacy_states", new JSONArray(SurveyResponsePrivacyStateCache.instance().getKeys()));

			awRequest.addToReturn(ConfigReadRequest.RESULT, response, true);
		}
		catch(JSONException e) {
			_logger.error("Error creating response JSONObject.", e);
			throw new ServiceException(e);
		}
	}

}
