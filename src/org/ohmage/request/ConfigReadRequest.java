package org.ohmage.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;

/**
 * <p>This class is responsible for updating a class.</p>
 * <p>There are no required parameters for this call.</p>
 * 
 * @author John Jenkins
 */
public class ConfigReadRequest extends Request {
	private static final Logger LOGGER = Logger.getLogger(ConfigReadRequest.class);
	
	private final JSONObject result;
	
	/**
	 * Default constructor.
	 */
	public ConfigReadRequest() {
		super();
		
		result = new JSONObject();
	}
	
	/**
	 * Gathers the appropriate information and stores the result in the result
	 * object.
	 */
	@Override
	public void service() {
		LOGGER.info("Gathering information about the system.");
		
		// Get the response values to be returned to the requestor and place
		// them in the response.
		try {
			// Get the application's name.
			result.put("application_name", PreferenceCache.instance().lookup(PreferenceCache.KEY_APPLICATION_NAME));
			
			// Get the application's version.
			result.put("application_version", PreferenceCache.instance().lookup(PreferenceCache.KEY_APPLICATION_VERSION));
			
			// Get the Git build hash.
			result.put("application_build", PreferenceCache.instance().lookup(PreferenceCache.KEY_APPLICATION_BUILD));
			
			// Get the default survey response sharing state.
			result.put("default_survey_response_sharing_state", PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE));
		}
		catch(CacheMissException e) {
			setFailed();
			LOGGER.error("Unknown Value for 'known' key '" + PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE + "'. Is the cache database missing a key-Value pair?", e);
		}
		catch(JSONException e) {
			setFailed();
			LOGGER.error("Error creating response JSONObject.", e);
		}
	}
	
	/**
	 * Writes the response to the client.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing configuration read response.");
		
		respond(httpRequest, httpResponse, result);
	}
}