package edu.ucla.cens.awserver.request;

import java.util.List;


/**
 * State for data point API queries.
 * 
 * @author selsky
 */
public class DataPointQueryAwRequest extends ResultListAwRequest {
	private String _startDate;
	private String _endDate;
	private String _userNameRequestParam;
	private String _client;
	private String _campaignName;
	private String _campaignVersion;
	
	private String[] _dataPointIds;
	// private String _authToken; see userToken in parent class
	private List<String> _metadataPromptIds;
	
	public String getStartDate() {
		return _startDate;
	}
	
	public void setStartDate(String startDate) {
		_startDate = startDate;
	}
	
	public String getEndDate() {
		return _endDate;
	}
	
	public void setEndDate(String endDate) {
		_endDate = endDate;
	}
	
	public String getUserNameRequestParam() {
		return _userNameRequestParam;
	}

	public void setUserNameRequestParam(String userNameRequestParam) {
		_userNameRequestParam = userNameRequestParam;
	}

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}

	public String getCampaignName() {
		return _campaignName;
	}

	public void setCampaignName(String campaignName) {
		_campaignName = campaignName;
	}

	public String getCampaignVersion() {
		return _campaignVersion;
	}

	public void setCampaignVersion(String campaignVersion) {
		_campaignVersion = campaignVersion;
	}
	
	public String[] getDataPointIds() {
		return _dataPointIds;
	}

	public void setDataPointIds(String[] dataPointIds) {
		_dataPointIds = dataPointIds;
	}
	
	public List<String> getMetadataPromptIds() {
		return _metadataPromptIds;
	}

	public void setMetadataPromptIds(List<String> metadataPromptIds) {
		_metadataPromptIds = metadataPromptIds; 
	}
}
