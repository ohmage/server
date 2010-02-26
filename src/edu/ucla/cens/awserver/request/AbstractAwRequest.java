package edu.ucla.cens.awserver.request;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptType;
import edu.ucla.cens.awserver.domain.User;

/**
 * State that is common across features.
 * 
 * @author selsky
 */
public abstract class AbstractAwRequest implements AwRequest {
	// Input state
	private String _subdomain;
	private String _requestUrl;
	
	// Processing state
	private boolean _isFailedRequest;
	private User _user;
	private String _failedRequestErrorMessage;
	
	public String getSubdomain() {
		return _subdomain;
	}
	
	public void setSubdomain(String subdomain) {
		_subdomain = subdomain;
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
	
	@Override
	public String toString() {
		return "AbstractAwRequest [_failedRequestErrorMessage="
				+ _failedRequestErrorMessage + ", _isFailedRequest="
				+ _isFailedRequest + ", _requestUrl=" + _requestUrl
				+ ", _subdomain=" + _subdomain + ", _user=" + _user + "]";
	}
	
	/****
	
	Unsupported Operations -- it is up to subclasses to implement the desired functionality otherwise "unimplemented" methods
	will throw an UnsupportedOperationException, which indicates a logical error in the calling code.
	
	****/
	
	public int getCampaignPromptGroupId() {
		throw new UnsupportedOperationException("it is illegal to invoke getCampaignPromptGroupId() on this instance");
	}

	public int getCampaignPromptVersionId() {
		 throw new UnsupportedOperationException("it is illegal to invoke getCampaignPromptVersionId() on this instance");	
	}

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

	public Map<Integer, List<Integer>> getDuplicatePromptResponseMap() {
		throw new UnsupportedOperationException("it is illegal to invoke getDuplicatePromptResponseMap() on this instance");
	}

	public String getEndDate() {
		throw new UnsupportedOperationException("it is illegal to invoke getEndDate() on this instance");
	}

	public String getGroupId() {
		throw new UnsupportedOperationException("it is illegal to invoke getGroupId() on this instance");
	}

	public JSONArray getJsonDataAsJsonArray() {
		throw new UnsupportedOperationException("it is illegal to invoke getJsonDataAsJsonArray() on this instance");
	}

	public String getJsonDataAsString() {
		throw new UnsupportedOperationException("it is illegal to invoke getJsonDataAsString() on this instance");
	}

	public String getPhoneVersion() {
		throw new UnsupportedOperationException("it is illegal to invoke getPhoneVersion() on this instance");
	}

	public int[] getPromptIdArray() {
		throw new UnsupportedOperationException("it is illegal to invoke getPromptIdArray() on this instance");
	}

	public List<PromptType> getPromptTypeRestrictions() {
		throw new UnsupportedOperationException("it is illegal to invoke getPromptTypeRestrictions() on this instance");
	}

	public String getProtocolVersion() {
		throw new UnsupportedOperationException("it is illegal to invoke getProtocolVersion() on this instance");
	}

	public String getRequestType() {
		throw new UnsupportedOperationException("it is illegal to invoke getRequestType() on this instance");
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

	public String getVersionId() {
		throw new UnsupportedOperationException("it is illegal to invoke getVersionId() on this instance");
	}

	public void setResultList(List<?> resultList) {
		throw new UnsupportedOperationException("it is illegal to invoke setResultList() on this instance");		
	}

	public void setCampaignPromptGroupId(int campaignPromptGroupId) {
		throw new UnsupportedOperationException("it is illegal to invoke setCampaignPromptGroupId() on this instance");		
	}

	public void setCampaignPromptVersionId(int campaignPromptVersionId) {
		throw new UnsupportedOperationException("it is illegal to invoke setCampaignPromptVersionId() on this instance");
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

	public void setDuplicatePromptResponseMap(Map<Integer, List<Integer>> duplicatePromptResponseMap) {
		throw new UnsupportedOperationException("it is illegal to invoke setDuplicatePromptResponseMap() on this instance");		
	}

	public void setEndDate(String endDate) {
		throw new UnsupportedOperationException("it is illegal to invoke setEndDate() on this instance");		
	}

	public void setGroupId(String groupId) {
		throw new UnsupportedOperationException("it is illegal to invoke setGroupId() on this instance");	
	}

	public void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray) {
		throw new UnsupportedOperationException("it is illegal to invoke setJsonDataAsJsonArray() on this instance");
	}

	public void setJsonDataAsString(String jsonDataAsString) {
		throw new UnsupportedOperationException("it is illegal to invoke setJsonDataAsString() on this instance");
	}

	public void setPhoneVersion(String phoneVersion) {
		throw new UnsupportedOperationException("it is illegal to invoke setPhoneVersion() on this instance");
	}

	public void setPromptIdArray(int[] promptIdArray) {
		throw new UnsupportedOperationException("it is illegal to invoke setPromptIdArray() on this instance");
	}

	public void setPromptTypeRestrictions(List<PromptType> promptTypeRestrictions) {
		throw new UnsupportedOperationException("it is illegal to invoke setPromptTypeRestrictions() on this instance");
	}

	public void setProtocolVersion(String protocolVersion) {
		throw new UnsupportedOperationException("it is illegal to invoke setProtocolVersion() on this instance");
	}

	public void setRequestType(String requestType) {
		throw new UnsupportedOperationException("it is illegal to invoke setRequestType() on this instance");
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

	public void setVersionId(String versionId) {
		throw new UnsupportedOperationException("it is illegal to invoke setVersionId() on this instance");
	}
}
