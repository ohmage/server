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
package org.ohmage.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bidirectional hash map for keeping track of a pair of objects and being
 * able to query on either and get the other. This requires that no duplicate
 * keys or values exist in the Map.
 * 
 * This will take time equivalent to that of a standard Java HashMap object,
 * but it will take up twice as much space.
 * 
 * @author John Jenkins
 *
 * @param <K> The type of values to be referenced as keys.
 * 
 * @param <V> The type of values to be referenced as valuse.
 */
public class BidirectionalHashMap<K, V> {
	private final Map<K, V> keyToValueMap;
	private final Map<V, K> valueToKeyMap;
	
	/**
	 * Default constructor.
	 */
	public BidirectionalHashMap() {
		keyToValueMap = new HashMap<K, V>();
		valueToKeyMap = new HashMap<V, K>();
	}

	/**
	 * Clears the map of all keys and values.
	 */
	public void clear() {
		keyToValueMap.clear();
		valueToKeyMap.clear();
	}
	
	/**
	 * Returns true if 'key' exists in the map as a key.
	 * 
	 * @param key The Value to check for as a key in the map.
	 * 
	 * @return Whether or not 'key' exists in the map as a key.
	 */
	public boolean containsKey(K key) {
		return keyToValueMap.containsKey(key);
	}
	
	/**
	 * Returns true if 'Value' exists in the map as a Value.
	 * 
	 * @param Value The Value to check for as a Value in the map.
	 * 
	 * @return Whether or not 'Value' exists in the map as a Value.
	 */
	public boolean containsValue(V value) {
		return valueToKeyMap.containsKey(value);
	}
	
	/**
	 * Gets the Value associated with 'key' if 'key' exists in the map.
	 * 
	 * @param key They key to search for within the map.
	 * 
	 * @return The Value with which 'key' is associated; otherwise, null is
	 * 		   returned.
	 */
	public V getValue(K key) {
		return keyToValueMap.get(key);
	}
	
	/**
	 * Gets the key associated with 'Value' if 'Value' exists in the map.
	 * 
	 * @param Value They Value to search for within the map.
	 * 
	 * @return The key with which 'Value' is associated; otherwise, null is
	 * 		   returned.
	 */
	public K getKey(V value) {
		return valueToKeyMap.get(value);
	}
	
	/**
	 * Creates an association between 'key' and 'Value' in the map. If a Value
	 * is already associated with 'key', then it is returned. If 'Value'
	 * already existed and was associated with another key, that association is
	 * broken and replaced with this new association.
	 * 
	 * Functionally, this is the same as {@link #putValue(Object, Object)},
	 * but it returns only the previous Value should one have already existed 
	 * or null if one didn't exist. To retrieve both the old key and the old 
	 * Value, it is safer to first use both of the containsValue functions to 
	 * determine if such associations already exist and then to use the 
	 * getValue functions to retrieve those values.
	 *  
	 * @param key The key to be associated with 'Value'.
	 * 
	 * @param Value The Value to be associated with 'key'.
	 * 
	 * @return The Value with which 'key' was already associated or null if no
	 * 		   such association already existed.
	 */
	public V putKey(K key, V value) {
		K oldKey = valueToKeyMap.put(value, key);
		
		if(oldKey != null) {
			keyToValueMap.remove(oldKey);
		}
		
		V oldValue = keyToValueMap.put(key, value);
		
		return oldValue;
	}
	
	/**
	 * Creates an association between 'key' and 'Value' in the map. If a key
	 * is already associated with 'Value', then it is returned. If 'key'
	 * already existed and was associated with another Value, that association
	 * is broken and replaced with this new association.
	 * 
	 * Functionally, this is the same as {@link #putKey(Object, Object)},
	 * but it returns only the previous key should one have already existed, 
	 * or null if one didn't exist. To retrieve both the old key and the old 
	 * Value, it is safer to first use both of the containsValue functions to 
	 * determine if such associations already exist and then to use the 
	 * getValue functions to retrieve those values.
	 *  
	 * @param key The key to be associated with 'Value'.
	 * 
	 * @param Value The Value to be associated with 'key'.
	 * 
	 * @return The key with which 'Value' was already associated or null if no
	 * 		   no such association already existed.
	 */
	public K putValue(K key, V value) {
		V oldValue = keyToValueMap.put(key, value);
		
		if(oldValue != null) {
			valueToKeyMap.remove(oldValue);
		}
		
		K oldKey = valueToKeyMap.put(value, key);
		
		return oldKey;
	}
	
	/**
	 * Returns a Set of all the known keys.
	 * 
	 * @return All of the known keys.
	 */
	public Set<K> keySet() {
		return keyToValueMap.keySet();
	}
	
	/**
	 * Returns a Set of all the known values.
	 * 
	 * @return All of the known values.
	 */
	public Set<V> valueSet() {
		return valueToKeyMap.keySet();
	}
}
