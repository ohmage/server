package edu.ucla.cens.awserver.request;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.SurveyDataPacket;
import edu.ucla.cens.awserver.domain.User;

/**
 * State that is common across features.
 * 
 * @author selsky
 */
public abstract class AbstractAwRequest implements AwRequest {
	// Input state
	private String _requestUrl;
	
	// Processing state
	private boolean _isFailedRequest;
	private User _user;
	private String _failedRequestErrorMessage;
	private Map<String, Object> _toValidate;
	
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
	
	public void setToValidate(Map<String, Object> toValidate) {
		_toValidate = toValidate;
	}
	
	@Override
	public String toString() {
		return "AbstractAwRequest [_failedRequestErrorMessage="
				+ _failedRequestErrorMessage + ", _isFailedRequest="
				+ _isFailedRequest + ", _requestUrl=" + _requestUrl
				+ ", _user=" + _user + "]";
	}
	
	/****
	
	Unsupported Operations -- it is up to subclasses to implement the desired functionality otherwise "unimplemented" methods
	will throw an UnsupportedOperationException, which indicates a logical error in the calling code.
	
	****/

	public String getCampaignUrn() {
		throw new UnsupportedOperationException("it is illegal to invoke getCampaignUrn() on this instance");	
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

	public void setCampaignUrn(String campaignName) {
		throw new UnsupportedOperationException("it is illegal to invoke setCampaignName() on this instance");
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
