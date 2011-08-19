package org.ohmage.request;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.exception.CacheMissException;

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
		super(null);
		
		result = new JSONObject();
	}
	
	/**
	 * Gathers the appropriate information and stores the result in the result
	 * object.
	 */
	@Override
	public void service() {
		LOGGER.info("Gathering information about the system.");
		
		// Get the response values to be returned to the requester and place
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
			
			result.put("survey_response_privacy_states", SurveyResponsePrivacyStateCache.instance().getKeys());
		}
		catch(CacheMissException e) {
			setFailed();
			LOGGER.error("Unknown value for 'known' key '" + PreferenceCache.KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE + "'. Is the cache database missing a key-value pair?", e);
		}
		catch(JSONException e) {
			setFailed();
			LOGGER.error("Error creating response JSONObject.", e);
		}
	}
	
	/**
	 * Returns an empty map. This is for requests that don't have any specific
	 * information to return.
	 */
	@Override
	public Map<String, String[]> getAuditInformation() {
		return new HashMap<String, String[]>();
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