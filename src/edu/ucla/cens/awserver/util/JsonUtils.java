package edu.ucla.cens.awserver.util;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utilities for working with the json.org JSON library. This is a class of static methods that handle retrieving data from
 * various JSON containers while also abstracting the checked exceptions that the JSON lib throws with abandon.
 * 
 * @author selsky
 */
public class JsonUtils {
	private static Logger _logger = Logger.getLogger(JsonUtils.class);
	
	private JsonUtils() { }
	
	/**
	 * @return the String value found in the JSONObject retrieved using the provided key. If no value is found, null is returned. 
	 */
	public static String getStringFromJson(JSONObject jsonObject, String key) {
		String value = null;
		
		try {
			
			value = jsonObject.getString(key); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Long value found in the JSONObject retrieved using the provided key. If no value is found, null is returned. 
	 */
	public static Long getLongFromJson(JSONObject jsonObject, String key) {
		Long value = null;
		
		try {
			
			value = jsonObject.getLong(key); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
}
