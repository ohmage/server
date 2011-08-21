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
package org.ohmage.util;

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
public final class JsonUtils {
	private static final Logger LOGGER = Logger.getLogger(JsonUtils.class);
	
	private JsonUtils() { }
	
	/**
	 * @return the String value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static String getStringFromJsonObject(JSONObject jsonObject, String key) {
		String value = null;
		
		try {
			
			value = jsonObject.getString(key); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Integer value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static Integer getIntegerFromJsonObject(JSONObject jsonObject, String key) {
		Integer value = null;
		
		try {
			
			value = jsonObject.getInt(key); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Long value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static Long getLongFromJsonObject(JSONObject jsonObject, String key) {
		Long value = null;
		
		try {
			
			value = jsonObject.getLong(key); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the JSONObject value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static JSONObject getJsonObjectFromJsonObject(JSONObject jsonObject, String key) {
		JSONObject value = null;
		
		try {
			
			value = jsonObject.getJSONObject(key); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Double value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static Double getDoubleFromJsonObject(JSONObject jsonObject, String key) {
		Double value = null;
		
		try {
			
			value = jsonObject.getDouble(key); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
			}
			
		}
		
		return value;
	}
		
	/**
	 * @return the JSONArray value found in the JSONObject using the provided key. If no value is found, null is returned. 
	 */
	public static JSONArray getJsonArrayFromJsonObject(JSONObject jsonObject, String key) {
		JSONArray array = null;
		
		try {
			
			array = jsonObject.getJSONArray(key); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
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
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve index " + index + " from JSON array " + array, jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the Integer value found in the JSONArray using the provided index. If no value is found, null is returned. 
	 */
	public static Integer getIntegerFromJsonArray(JSONArray array, int index) {
		Integer value = null;
		
		try {
			
			value = array.getInt(index); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve index " + index + " from JSON array " + array, jsone);
			}
			
		}
		
		return value;
	}
	
	/**
	 * @return the JSONArray value found in the JSONArray using the provided index. If no value is found, null is returned. 
	 */
	public static JSONArray getJsonArrayFromJsonArray(JSONArray array, int index) {
		JSONArray a = null;
		
		try {
			
			a = array.getJSONArray(index); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve index " + index + " from JSON array " + array, jsone);
			}
			
		}
		
		return a;
	}
	
	/**
	 * @return the JSONObject value found in the JSONArray using the provided index. If no value is found, null is returned. 
	 */
	public static JSONObject getJsonObjectFromJsonArray(JSONArray array, int index) {
		JSONObject value = null;
		
		try {
			
			value = array.getJSONObject(index); 
			
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve index " + index + " from JSON array " + array, jsone);
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
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to convert string \"" + string + "\" to a JSON array", jsone);
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
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve index " + index + " from JSON array " + array, jsone);
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
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to convert string \"" + string + "\" to a JSON object", jsone);
			}
			
		}
		
		return value;
		
	}
	
	/**
	 * @return the Object found within the provided JSONObject using the provided key or null if nothing could be found using the 
	 * key. 
	 */
	public static Object getObjectFromJsonObject(JSONObject jsonObject, String key) {
		Object value = null;
		
		try {
			
			value = jsonObject.get(key); 
		
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve " + key + " from JSON object " + jsonObject, jsone);
			}
			
		}
		
		return value;
		
	}
	
	/**
	 * @return the Object found within the provided JSONArray at the provided index or null if the array is syntactically invalid
	 * or nothing exists at the index
	 */
	public static Object getObjectFromJsonArray(JSONArray jsonArray, int index) {
		Object value = null;
		
		try {
			
			value = jsonArray.get(index); 
		
		} catch (JSONException jsone) {
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("failed attempt to retrieve index " + index + " from JSON array " + jsonArray, jsone);
			}
			
		}
		
		return value;
		
	}

}
