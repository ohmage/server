package edu.ucla.cens.awserver.request;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptType;

/**
 * Represents state for a sensor data upload.
 * 
 * @author selsky
 */
public class SensorUploadAwRequest extends ResultListAwRequest {
	// Input state // 
	private String _requestType; 
	private String _phoneVersion;
	private String _protocolVersion;
	private String _jsonDataAsString;
	private String _sessionId;
	
	// Processing state // 
	private long _startTime;              // processing start time for logging
	
	private List<?> _resultList; 	      // used for authentication output
	
	private String _groupId;              // prompt group id sent from phone 
	private int _campaignPromptGroupId;   // the internal primary key for group of prompts within a campaign. campaigns may have
	                                      // many groups of prompts
	
	private String _versionId;            // prompt version id sent from phone
	private int _campaignPromptVersionId; // the internal primary key for the campaign-prompt-version (the version id for an 
	                                      // for an entire collection (possibly many groups) of prompts within a campaign
	
	private int[] _promptIdArray;         // prompt ids sent from phone (not the db primary keys)
	private List<PromptType> _promptTypeRestrictions; // the prompt data restrictions for validating prompt uploads
	
	private List<Integer> _duplicateIndexList; // store the indexes of duplicate mobility or prompt responses for logging
	private Map<Integer, List<Integer>> _duplicatePromptResponseMap; // prompts are uploaded in groups so a map stores
	                                                                 // a list of duplicate prompt responses at key by their  
	                                                                 // message index
	
	private int _currentMessageIndex; // used for logging errors based on the current invalid message
	private int _currentPromptId;     // used for logging errors for invalid prompts
	
	private JSONArray _jsonDataAsJsonArray; // The input data array converted to an internal JSON representation
	private List<DataPacket> _dataPackets;  // JSON data converted into an internal representation
	
	/**
	 * Default no-arg constructor.	
	 */
	public SensorUploadAwRequest() {
		_currentMessageIndex = -1;
		_currentPromptId = -1;
	}
	
	public List<Integer> getDuplicateIndexList() {
		return _duplicateIndexList;
	}
	
	public void setDuplicateIndexList(List<Integer> duplicateIndexList) {
		_duplicateIndexList = duplicateIndexList;
	}
	
	public int getCampaignPromptGroupId() {
		return _campaignPromptGroupId;
	}
	
	public void setCampaignPromptGroupId(int campaignPromptGroupId) {
		_campaignPromptGroupId = campaignPromptGroupId;
	}
	
	public List<PromptType> getPromptTypeRestrictions() {
		return _promptTypeRestrictions;
	}
	
	public void setPromptTypeRestrictions(List<PromptType> promptTypeRestrictions) {
		_promptTypeRestrictions = promptTypeRestrictions;
	}
	
	public Map<Integer, List<Integer>> getDuplicatePromptResponseMap() {
		return _duplicatePromptResponseMap;
	}
	
	public void setDuplicatePromptResponseMap(Map<Integer, List<Integer>> duplicatePromptResponseMap) {
		_duplicatePromptResponseMap = duplicatePromptResponseMap;
	}
	
	public int getCampaignPromptVersionId() {
		return _campaignPromptVersionId;
	}
	
	public void setCampaignPromptVersionId(int campaignPromptVersionId) {
		_campaignPromptVersionId = campaignPromptVersionId;
	}
	
	public long getStartTime() {
		return _startTime;
	}
	
	public void setStartTime(long startTime) {
		_startTime = startTime;
	}
	
	public String getSessionId() {
		return _sessionId;
	}
	
	public void setSessionId(String sessionId) {
		_sessionId = sessionId;
	}
	
	public String getRequestType() {
		return _requestType;
	}
	
	public void setRequestType(String requestType) {
		_requestType = requestType;
	}
	
	public String getPhoneVersion() {
		return _phoneVersion;
	}
	
	public void setPhoneVersion(String phoneVersion) {
		_phoneVersion = phoneVersion;
	}
	
	public String getProtocolVersion() {
		return _protocolVersion;
	}
	
	public void setProtocolVersion(String protocolVersion) {
		_protocolVersion = protocolVersion;
	}
	
	public String getJsonDataAsString() {
		return _jsonDataAsString;
	}
	
	public void setJsonDataAsString(String jsonDataAsString) {
		_jsonDataAsString = jsonDataAsString;
	}
	
	public List<DataPacket> getDataPackets() {
		return _dataPackets;
	}
	
	public void setDataPackets(List<DataPacket> dataPackets) {
		_dataPackets = dataPackets;
	}
	
	public int getCurrentMessageIndex() {
		return _currentMessageIndex;
	}
	
	public void setCurrentMessageIndex(int currentMessageIndex) {
		_currentMessageIndex = currentMessageIndex;
	}
	
	public int getCurrentPromptId() {
		return _currentPromptId;
	}
	
	public void setCurrentPromptId(int currentPromptId) {
		_currentPromptId = currentPromptId;
	}
	
	public JSONArray getJsonDataAsJsonArray() {
		return _jsonDataAsJsonArray;
	}
	
	public void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray) {
		_jsonDataAsJsonArray = jsonDataAsJsonArray;
	}
	
	public String getGroupId() {
		return _groupId;
	}
	
	public void setGroupId(String groupId) {
		_groupId = groupId;
	}
	
	public int[] getPromptIdArray() {
		return _promptIdArray;
	}
	
	public void setPromptIdArray(int[] promptIdArray) {
		_promptIdArray = promptIdArray;
	}
	
	public String getVersionId() {
		return _versionId;
	}
	
	public void setVersionId(String versionId) {
		_versionId = versionId;
	}
	
	public List<?> getResultList() {
		return _resultList;
	}
	
	public void setResultList(List<?> resultList) {
		_resultList = resultList;
	}

	@Override
	public String toString() {
		return "SensorUploadAwRequest [_campaignPromptGroupId="
				+ _campaignPromptGroupId + ", _campaignPromptVersionId="
				+ _campaignPromptVersionId + ", _currentMessageIndex="
				+ _currentMessageIndex + ", _currentPromptId="
				+ _currentPromptId + ", _dataPackets=" + _dataPackets
				+ ", _duplicateIndexList=" + _duplicateIndexList
				+ ", _duplicatePromptResponseMap="
				+ _duplicatePromptResponseMap + ", _groupId=" + _groupId
				+ ", _jsonDataAsJsonArray=" + _jsonDataAsJsonArray
				+ ", _jsonDataAsString=" + _jsonDataAsString
				+ ", _phoneVersion=" + _phoneVersion + ", _promptIdArray="
				+ Arrays.toString(_promptIdArray)
				+ ", _promptTypeRestrictions=" + _promptTypeRestrictions
				+ ", _protocolVersion=" + _protocolVersion + ", _requestType="
				+ _requestType + ", _resultList=" + _resultList
				+ ", _sessionId=" + _sessionId + ", _startTime=" + _startTime
				+ ", _versionId=" + _versionId + ", toString()="
				+ super.toString() + "]";
	}
}

