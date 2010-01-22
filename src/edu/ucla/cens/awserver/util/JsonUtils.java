package edu.ucla.cens.awserver.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
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
	 * @return the String value found in the JSONObject using the provided key. If no value is found, null is returned. 
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
	 * @return the Long value found in the JSONObject using the provided key. If no value is found, null is returned. 
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
	
	/**
	 * @return the JSONObject value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static JSONObject getObjectFromJson(JSONObject jsonObject, String key) {
		JSONObject value = null;
		
		try {
			
			value = jsonObject.getJSONObject(key); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Double value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static Double getDoubleFromJson(JSONObject jsonObject, String key) {
		Double value = null;
		
		try {
			
			value = jsonObject.getDouble(key); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the JSONArray value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static JSONArray getJsonArrayFromJson(JSONObject jsonObject, String key) {
		JSONArray array = null;
		
		try {
			
			array = jsonObject.getJSONArray(key); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(jsone);
			}
			
		}
		
		return array;
	}
		
	/**
	 * @return the JSONArray value found in the JSONArray using the provided index. If no value is found, null is returned. 
	 */
	public static Double getDoubleFromJsonArray(JSONArray array, int index) {
		Double value = null;
		
		try {
			
			value = array.getDouble(index); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
}
