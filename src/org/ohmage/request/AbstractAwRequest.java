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
package org.ohmage.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.ohmage.domain.DataPacket;
import org.ohmage.domain.SurveyDataPacket;
import org.ohmage.domain.User;


/**
 * State that is common across features.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public abstract class AbstractAwRequest implements AwRequest {
	// Input state
	private String _requestUrl;
	
	// Processing state
	private User _user;
	
	private boolean _isFailedRequest;
	private String _failedRequestErrorMessage;
	
	private String _campaignUrn;
	
	private Map<String, Object> _toValidate;
	private Map<String, Object> _toProcess;
	private Map<String, Object> _toReturn;
	
	protected AbstractAwRequest() {
		_requestUrl = null;
		
		_user = null;
		
		_isFailedRequest = false;
		_failedRequestErrorMessage = null;
		
		_toValidate = new HashMap<String, Object>();
		_toProcess = new HashMap<String, Object>();
		_toReturn = new HashMap<String, Object>();
	}
	
	public String getRequestUrl() {
		return _requestUrl;
	}
	
	public void setRequestUrl(String requestUrl) {
		_requestUrl = requestUrl;
	}
	
	public boolean isFailedRequest() {
		return _isFailedRequest;
	}
	
	public void setFailedRequest(boolean isFailedRequest) {
		_isFailedRequest = isFailedRequest;
	}
	
	public User getUser() {
		return _user;
	}
	
	public void setUser(User user) {
		_user = user;
	}
	
	public String getFailedRequestErrorMessage() {
		return _failedRequestErrorMessage;
	}
	
	public void setFailedRequestErrorMessage(String failedRequestErrorMessage) {
		_failedRequestErrorMessage = failedRequestErrorMessage;
	}
	
	public Map<String, Object> getToValidate() {
		return _toValidate;
	}
	
	public Map<String, Object> getToProcess() {
		return _toProcess;
	}
	
	public Map<String, Object> getToReturn() {
		return _toReturn;
	}
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}
	
	public void setCampaignUrn(String urn) {
		_campaignUrn = urn;
	}
	
	/**
	 * Returns whether or not a key exists in the toProcess map.
	 * 
	 * @param key The key whose existance is to be checked.
	 * 
	 * @return True iff the key exists in the map; false, otherwise.
	 */
	public boolean existsInToProcess(String key) {
		if(key == null) {
			return false;
		}
		
		return _toProcess.containsKey(key);
	}
	
	/**
	 * Checks if the key exists in the toReturn map.
	 * 
	 * @param key The key to check for in the toReturn map.
	 * 
	 * @return Returns true if the key exists, false otherwise.
	 */
	public boolean existsInToReturn(String key) {
		if(key == null) {
			return false;
		}
		
		return _toReturn.containsKey(key);
	}
	
	/**
	 * Checks if the key exists in the toValidate map.
	 * 
	 * @param key The key to check for in the toReturn map.
	 * 
	 * @return Returns true if the key exists, false otherwise.
	 */
	public boolean existsInToValidate(String key) {
		if(key == null) {
			return false;
		}
		
		return _toValidate.containsKey(key);
	}
	
	/**
	 * Returns the Object associated with the parameterized 'key' from the
	 * toProcess map. If no such value exists, throw an 
	 * IllegalArgumentException.
	 * 
	 * @param key The key to use when looking in the toProcess map.
	 * 
	 * @return The value that the key points to in the toProcess map.
	 * 
	 * @throws IllegalArgumentException Thrown if no such key exists in the 
	 * 									toProcess map or if the key is null.
	 */
	public Object getToProcessValue(String key) {
		if(key == null) {
			throw new IllegalArgumentException("Key is null and null keys are dissallowed.");
		}
		else if(! _toProcess.containsKey(key)) {
			throw new IllegalArgumentException("Key not found in toProcess map.");
		}
		else {
			return _toProcess.get(key);
		}
	}
	
	/**
	 * Gets the value for the parameterized key from the toReturn map. If no
	 * such key exists, it will throw an IllegalArgumentException. 
	 * 
	 * @param key The key to use when looking in the toReturn map.
	 * 
	 * @return The value that that the key points to in the toReturn map.
	 * 
	 * @throws IllegalArgumentException Thrown if the key is null or not found
	 * 									in the toReturn map.
	 */
	public Object getToReturnValue(String key) {
		if(key == null) {
			throw new IllegalArgumentException("Key is null and null keys are dissallowed.");
		}
		else if(! _toReturn.containsKey(key)) {
			throw new IllegalArgumentException("Key not found in the toReturn map.");
		}
		else {
			return _toReturn.get(key);
		}
	}

	/**
	 * Gets the value for the parameterized key from the toValidate map. If no
	 * such key exists, it will throw an IllegalArgumentException. 
	 * 
	 * @param key The key to use when looking in the toReturn map.
	 * 
	 * @return The value that that the key points to in the toReturn map.
	 * 
	 * @throws IllegalArgumentException Thrown if the key is null or not found
	 * 									in the toReturn map.
	 */
	public Object getToValidateValue(String key) {
		if(key == null) {
			throw new IllegalArgumentException("Key is null and null keys are dissallowed.");
		}
		else if(! _toValidate.containsKey(key)) {
			throw new IllegalArgumentException("Key not found in the toValidate map.");
		}
		else {
			return _toValidate.get(key);
		}
	}

	/**
	 * Adds an item to the toProcess map. This is done as opposed to getting
	 * the whole map, making local changes, and overwriting the whole map.
	 * 
	 * This allows us to put in place our own restrictions such as the
	 * dissallowance of null keys or values.
	 * 
	 * @param key The key for the value that is attempting to be inserted.
	 * 
	 * @param value The value that is attempting to be inserted.
	 * 
	 * @param overwrite A flag to denote that if such a value already exists
	 * 					that it should not be overwriten. If such a case were
	 * 					to present itself, an IllegalArgumentException will be
	 * 					thrown.
	 * 
	 * @throws IllegalArgumentException Thrown if key or value are null, or if
	 * 									such a key already exists and
	 * 									'overwrite' is set to false.
	 */
	public void addToProcess(String key, Object value, boolean overwrite) {
		if(key == null) {
			throw new IllegalArgumentException("Null keys are dissallowed.");
		}
		else if(value == null) {
			throw new IllegalArgumentException("Null values are dissallowed.");
		}
		else if((! overwrite) && (_toProcess.containsKey(key))) {
			throw new IllegalArgumentException("The key '" + key + "' already exists but overwriting is disallowed.");
		}
		else {
			_toProcess.put(key, value);
		}
	}
	
	/**
	 * Adds the parameterized value with the parameterized key to the toReturn
	 * map. If 'overwrite' is set to true it will overwrite any existing
	 * values; if it is set to false, and an entry with the same 'key' already
	 * exists, it will throw an IllegalArgumentException indicating such.
	 * 
	 * @param key The key to use when adding this entry in the toReturn map.
	 * 
	 * @param value The value to use when adding this entry in the toReturn
	 * 				map.
	 * 
	 * @param overwrite Whether or not to overwrite any existing entries in
	 * 					the toReturn map that have the same key.
	 * 
	 * @throws IllegalArgumentException Thrown if the key or value are null or
	 * 									if 'overwrite' is set to false and
	 * 									such a key already exists.
	 */
	public void addToReturn(String key, Object value, boolean overwrite) {
		if(key == null) {
			throw new IllegalArgumentException("Null keys are dissallowed.");
		}
		else if(value == null) {
			throw new IllegalArgumentException("Null values are dissallowed.");
		}
		else if((! overwrite) && (_toReturn.containsKey(key))) {
			throw new IllegalArgumentException("The key '" + key + "' already exists but overwriting is disallowed.");
		}
		else {
			_toReturn.put(key, value);
		}
	}

	/**
	 * Adds the parameterized value with the parameterized key to the toValidate
	 * map. If 'overwrite' is set to true it will overwrite any existing
	 * values; if it is set to false, and an entry with the same 'key' already
	 * exists, it will throw an IllegalArgumentException indicating such.
	 * 
	 * @param key The key to use when adding this entry in the toValidate map.
	 * 
	 * @param value The value to use when adding this entry in the toValidate
	 * 				map.
	 * 
	 * @param overwrite Whether or not to overwrite any existing entries in
	 * 					the toValidate map that have the same key.
	 * 
	 * @throws IllegalArgumentException Thrown if the key or value are null or
	 * 									if 'overwrite' is set to false and
	 * 									such a key already exists.
	 */
	public void addToValidate(String key, Object value, boolean overwrite) {
		if(key == null) {
			throw new IllegalArgumentException("Null keys are dissallowed.");
		}
		else if((! overwrite) && (_toValidate.containsKey(key))) {
			throw new IllegalArgumentException("The key '" + key + "' already exists but overwriting is disallowed.");
		}
		else {
			_toValidate.put(key, value);
		}
	}

	/****
	
	Unsupported Operations -- it is up to subclasses to implement the desired functionality otherwise "unimplemented" methods
	will throw an UnsupportedOperationException, which indicates a logical error in the calling code.
	
	****/
	
	public int getCurrentMessageIndex() {
		throw new UnsupportedOperationException("it is illegal to invoke getCurrentMessageIndex() on this instance");
	}

	public int getCurrentPromptId() {
		throw new UnsupportedOperationException("it is illegal to invoke getCurrentPromptId() on this instance");
	}
	
	public List<DataPacket> getDataPackets() {
		throw new UnsupportedOperationException("it is illegal to invoke getDataPackets() on this instance");
	}
	
	public List<Integer> getDuplicateIndexList() {
		throw new UnsupportedOperationException("it is illegal to invoke getDuplicateIndexList() on this instance");
	}
	
	public String getEndDate() {
		throw new UnsupportedOperationException("it is illegal to invoke getEndDate() on this instance");
	}

	public JSONArray getJsonDataAsJsonArray() {
		throw new UnsupportedOperationException("it is illegal to invoke getJsonDataAsJsonArray() on this instance");
	}

	public String getJsonDataAsString() {
		throw new UnsupportedOperationException("it is illegal to invoke getJsonDataAsString() on this instance");
	}

	public String getClient() {
		throw new UnsupportedOperationException("it is illegal to invoke getClient() on this instance");
	}

	public byte[] getMedia() {
		throw new UnsupportedOperationException("it is illegal to invoke getMedia() on this instance");
	}
	
	public String getMediaId() {
		throw new UnsupportedOperationException("it is illegal to invoke getMediaId() on this instance");
	}
	
	public String getMediaType() {
		throw new UnsupportedOperationException("it is illegal to invoke getMediaType() on this instance");
	}
	
	public int[] getPromptIdArray() {
		throw new UnsupportedOperationException("it is illegal to invoke getPromptIdArray() on this instance");
	}

	public List<?> getResultList() {
		throw new UnsupportedOperationException("it is illegal to invoke getResultList() on this instance");
	}

	public String getSessionId() {
		throw new UnsupportedOperationException("it is illegal to invoke getSessionId() on this instance");
	}

	public String getStartDate() {
		throw new UnsupportedOperationException("it is illegal to invoke getStartDate() on this instance");
	}

	public long getStartTime() {
		throw new UnsupportedOperationException("it is illegal to invoke getStartTime() on this instance");
	}
	
	public String getUserNameRequestParam() {
		throw new UnsupportedOperationException("it is illegal to invoke getUserNameRequestParam() on this instance");
	}
	
	public String getUserToken() {
		throw new UnsupportedOperationException("it is illegal to invoke getUserToken() on this instance");
	}

	public void setClient(String client) {
		throw new UnsupportedOperationException("it is illegal to invoke setClient() on this instance");
	}

	public void setCurrentMessageIndex(int currentMessageIndex) {
		throw new UnsupportedOperationException("it is illegal to invoke setCurrentMessageIndex() on this instance");
	}
	
	public void setCurrentPromptId(int currentPromptId) {
		throw new UnsupportedOperationException("it is illegal to invoke setCurrentPromptId() on this instance");
	}

	public void setDataPackets(List<DataPacket> dataPackets) {
		throw new UnsupportedOperationException("it is illegal to invoke setDataPackets() on this instance");		
	}

	public void setDuplicateIndexList(List<Integer> duplicateIndexList) {
		throw new UnsupportedOperationException("it is illegal to invoke setDuplicateIndexList() on this instance");
	}

	public void setEndDate(String endDate) {
		throw new UnsupportedOperationException("it is illegal to invoke setEndDate() on this instance");		
	}

	public void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray) {
		throw new UnsupportedOperationException("it is illegal to invoke setJsonDataAsJsonArray() on this instance");
	}

	public void setJsonDataAsString(String jsonDataAsString) {
		throw new UnsupportedOperationException("it is illegal to invoke setJsonDataAsString() on this instance");
	}
	
	public void setMedia(byte[] media) {
		throw new UnsupportedOperationException("it is illegal to invoke setMedia() on this instance");
	}

	public void setMediaId(String id) {
		throw new UnsupportedOperationException("it is illegal to invoke setMediaId() on this instance");
	}

	public void setMediaType(String id) {
		throw new UnsupportedOperationException("it is illegal to invoke setMediaType() on this instance");
	}

	public void setResultList(List<?> resultList) {
		throw new UnsupportedOperationException("it is illegal to invoke setResultList() on this instance");		
	}

	public void setSessionId(String sessionId) {
		throw new UnsupportedOperationException("it is illegal to invoke setSessionId() on this instance");
	}

	public void setStartDate(String startDate) {
		throw new UnsupportedOperationException("it is illegal to invoke setStartDate() on this instance");
	}

	public void setStartTime(long startTime) {
		throw new UnsupportedOperationException("it is illegal to invoke setStartTime() on this instance");
	}
	
	public void setSurveys(List<SurveyDataPacket> surveys) {
		throw new UnsupportedOperationException("it is illegal to invoke setSurveys() on this instance");		
	}

	public void setUserNameRequestParam(String userNameRequestParam) {
		throw new UnsupportedOperationException("it is illegal to invoke setUserNameRequestParam() on this instance");
	}
	
	public void setUserToken(String userToken) {
		throw new UnsupportedOperationException("it is illegal to invoke setUserToken() on this instance");
	}
}
