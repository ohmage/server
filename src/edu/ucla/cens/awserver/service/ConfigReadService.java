package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.cache.CacheMissException;
import edu.ucla.cens.awserver.cache.PreferenceCache;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.ConfigReadRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

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
