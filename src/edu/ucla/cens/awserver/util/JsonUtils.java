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
				_logger.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject);
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Integer value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static Integer getIntegerFromJson(JSONObject jsonObject, String key) {
		Integer value = null;
		
		try {
			
			value = jsonObject.getInt(key); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject);
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
				_logger.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject);
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
				_logger.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject);
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
				_logger.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject);
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
				_logger.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject);
				_logger.debug(jsone);
			}
			
		}
		
		return array;
	}
		
	/**
	 * @return the Double value found in the JSONArray using the provided index. If no value is found, null is returned. 
	 */
	public static Double getDoubleFromJsonArray(JSONArray array, int index) {
		Double value = null;
		
		try {
			
			value = array.getDouble(index); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("failed attempt to retrieve index " + index + " from JSON array " + array);
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the JSONObject value found in the JSONArray using the provided index. If no value is found, null is returned. 
	 */
	public static JSONObject getJsonObjectFromJsonArray(JSONArray array, int index) {
		JSONObject value = null;
		
		try {
			
			value = array.getJSONObject(index); 
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("failed attempt to retrieve index " + index + " from JSON array " + array);
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the JSONArray representation of the provided string or null if the string is unparseable as JSON
	 */
	public static JSONArray getJsonArrayFromString(String string) {
		JSONArray array = null;
		
		try {
			
			array = new JSONArray(string);
			
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("failed attempt to convert string \"" + string + "\" to a JSON array");
				_logger.debug(jsone);
			}
		}
		
		return array;
	}
	
	/**
	 * @return the string found at the provided index or null if no value could be found 
	 */
	public static String getStringFromJsonArray(JSONArray array, int index) {
		String value = null;
		
		try {
			
			value = array.getString(index);
		
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("failed attempt to retrieve index " + index + " from JSON array " + array);
				_logger.debug(jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the JSONObject representation of the provided string or null if the string is not a parseable JSONObject.
	 */
	public static JSONObject getJsonObjectFromString(String string) {
		JSONObject value = null;
		
		try {
			
			value = new JSONObject(string);
		
		} catch (JSONException jsone) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("failed attempt to convert string \"" + string + "\" to a JSON object");
				_logger.debug(jsone);
			}
			
		}
		
		return value;
		
	}
	
}
