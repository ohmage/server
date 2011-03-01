package edu.ucla.cens.awserver.request;

import java.util.List;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;

/**
 * Represents state for uploads of JSON data.
 * 
 * @author selsky
 */
public class UploadAwRequest extends ResultListAwRequest {
	// Input state //
	private String _client;
	private String _sessionId;
	private String _campaignName;
	
	// Processing state // 
	private long _startTime;              // processing start time for logging
	private int _currentMessageIndex; // used for logging errors based on the current invalid message
	
	private String _jsonDataAsString;
	private JSONArray _jsonDataAsJsonArray; // The input data array converted to an internal JSON representation
	private List<DataPacket> _dataPackets;  // JSON data converted into an internal representation
	private List<Integer> _duplicateIndexList; // store the indexes of duplicate mobility or prompt responses for logging
	
	/**
	 * Default no-arg constructor.	
	 */
	public UploadAwRequest() {
		_currentMessageIndex = -1;
	}
	
	public String getCampaignName() {
		return _campaignName;
	}
	
	public void setCampaignName(String campaignName) {
		_campaignName = campaignName;
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
	
	public String getClient() {
		return _client;
	}
	
	public void setClient(String client) {
		_client = client;
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
	
	public JSONArray getJsonDataAsJsonArray() {
		return _jsonDataAsJsonArray;
	}
	
	public void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray) {
		_jsonDataAsJsonArray = jsonDataAsJsonArray;
	}
	
	public String getJsonDataAsString() {
		return _jsonDataAsString;
	}
	
	public void setJsonDataAsString(String jsonDataAsString) {
		_jsonDataAsString = jsonDataAsString;
	}

	public List<Integer> getDuplicateIndexList() {
		return _duplicateIndexList;
	}
	
	public void setDuplicateIndexList(List<Integer> duplicateIndexList) {
		_duplicateIndexList = duplicateIndexList;
	}

	@Override
	public String toString() {
		return "UploadAwRequest [_campaignName=" + _campaignName + ", _client="
				+ _client + ", _currentMessageIndex=" + _currentMessageIndex
				+ ", _dataPackets=" + _dataPackets + ", _duplicateIndexList="
				+ _duplicateIndexList + ", _jsonDataAsJsonArray="
				+ _jsonDataAsJsonArray + ", _jsonDataAsString="
				+ _jsonDataAsString + ", _sessionId=" + _sessionId
				+ ", _startTime=" + _startTime + "]";
	}
}
