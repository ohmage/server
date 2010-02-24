package edu.ucla.cens.awserver.request;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptType;

/**
 * 
 * @author selsky
 */
public class SensorUploadAwRequest extends AbstractAwRequest {
	private List<Integer> _duplicateIndexList;
	private int _campaignPromptGroupId;
	private List<PromptType> _promptTypeRestrictions;
	private Map<Integer, List<Integer>> _duplicatePromptResponseMap;
	private int _campaignPromptVersionId;
	private long _startTime;
	private String _sessionId;
	private String _requestType; 
	private String _phoneVersion;
	private String _protocolVersion;
	private String _jsonDataAsString;
	private List<DataPacket> _dataPackets;
	private int _currentMessageIndex;
	private JSONArray _jsonDataAsJsonArray;
	private String _groupId;
	private int[] _promptIdArray;
	private String _versionId;
	private int _currentPromptId;
	private List<?> _resultList;
	
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

