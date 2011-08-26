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
package org.ohmage.cache;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.ohmage.exception.CacheMissException;

/**
 * Singleton cache for the indices and String values for Mobility privacy
 * states.
 * 
 * @author John Jenkins
 */
public final class PreferenceCache extends KeyValueCache {
	private static final String SQL_KEY_KEY = "p_key";
	private static final String SQL_VALUE_KEY = "p_value";
	
	private static final String SQL_GET_KEYS_AND_VALUES = "SELECT " + SQL_KEY_KEY + ", " + SQL_VALUE_KEY + " " +
														  "FROM preference";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "preferenceCache";
	
	// Known campaign privacy states.
	public static final String KEY_DEFAULT_CAN_CREATE_PRIVILIEGE = "default_can_create_privilege";
	public static final String KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE = "default_survey_response_sharing_state";
	
	// URL-based resource constants.
	public static final String KEY_MAXIMUM_NUMBER_OF_FILES_PER_DIRECTORY = "max_files_per_dir";
	public static final String KEY_FILE_HIERARCHY_DEPTH = "document_depth";
	
	// Document-specific information.
	public static final String KEY_DOCUMENT_DIRECTORY = "document_directory";
	
	// Image-specific information.
	public static final String KEY_IMAGE_DIRECTORY = "image_directory";
	
	// Properties-specific information.
	public static final String KEY_PROPERTIES_FILE = "properties_file";
	
	// Build-specific information.
	public static final String KEY_APPLICATION_NAME = "application_name";
	public static final String KEY_APPLICATION_VERSION = "application_version";
	public static final String KEY_APPLICATION_BUILD = "application_build";
	public static final String KEY_SSL_ENABLED = "ssl_enabled";
	
	// Visualization-specific information.
	public static final String KEY_VISUALIZATION_SERVER = "visualization_server_address";
	
	// The reference to one's self to return to requesters.
	private static PreferenceCache instance;
	
	private static Properties properties = null;
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	protected PreferenceCache(DataSource dataSource, long updateFrequency) {
		super(dataSource, updateFrequency, SQL_GET_KEYS_AND_VALUES, SQL_KEY_KEY, SQL_VALUE_KEY);
		
		instance = this;
		
		try {
			InputStream in = new FileInputStream(lookup(PreferenceCache.KEY_PROPERTIES_FILE));
			properties = new Properties();
			properties.load(in);
			in.close();
		}
		catch(CacheMissException e) {
			throw new IllegalStateException("Unknown value for 'known' key '" + PreferenceCache.KEY_PROPERTIES_FILE + "'. Is the cache database missing a key-value pair?", e);
		}
		catch(IOException e) {
			throw new IllegalStateException("Missing the properties file that should have been built with the WAR file.", e);
		}
	}
	
	/**
	 * A reference to the one instance of this object.
	 * 
	 * @return The one instance of this object through which call calls to
	 * 		   this cache should be made.
	 */
	public static PreferenceCache instance() {
		return instance;
	}
	
	/**
	 * Returns a human-readable name for this cache.
	 */
	@Override
	public String getName() {
		return CACHE_KEY;
	}
	
	/**
	 * Compares the current timestamp with the last time we did an update plus
	 * the amount of time between updates. If our cache has become stale, we
	 * attempt to update it.
	 * 
	 * Then, we check to see if such a key exists in our cache. If not, we
	 * throw an exception because, if someone is querying for a key that
	 * doesn't exist, we need to bring it to their immediate attention rather
	 * than returning an "error" value. Otherwise, the corresponding integer
	 * value is returned.
	 * 
	 * It is recommended, but not required, to use the constants declared in
	 * the concrete cache class as the parameter.
	 * 
	 * The complexity is O(n) if a refresh is required; otherwise, the 
	 * complexity of a Java Map object to lookup a key and return its value.
	 * 
	 * @param key The key whose corresponding value is being requested.
	 * 
	 * @return The corresponding value.
	 * 
	 * @throws CacheMissException Thrown if no such key exists.
	 */
	public String lookup(String key) throws CacheMissException {		
		// If the lookup table is out-of-date, refresh it.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		if(KEY_APPLICATION_NAME.equals(key) ||
		   KEY_APPLICATION_VERSION.equals(key) ||
		   KEY_APPLICATION_BUILD.equals(key) || 
		   KEY_SSL_ENABLED.equals(key)) {
			return getSystemProperty(key);
		}
		else {
			return super.lookup(key);
		}
	}
	
	/**
	 * Returns the value with the specified key from the system properties. If
	 * no such key exists, it will throw a CacheMissException.
	 * 
	 * @param key The key to use to retrieve the desired value.
	 * 
	 * @return Returns the value indexed by the key in question.
	 * 
	 * @throws CacheMissException Thrown if the key is not known in the system
	 * 							  properties.
	 */
	private String getSystemProperty(String key) throws CacheMissException {
		String value = properties.getProperty(key);
		if(value == null) {
			throw new CacheMissException("The key is not in the system properties file: " + key);
		}
		
		return value;
	}
}