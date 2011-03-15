package edu.ucla.cens.awserver.request;

/**
 * State for "new" data point API queries.
 * 
 * @author selsky
 */
public class NewDataPointQueryAwRequest extends ResultListAwRequest {
	private String _startDate;
	private String _endDate;
	private String _currentUser;
	private String _userListString;
	private String[] _userListArray;
	private String _promptIdListString;
	private String[] _promptIdListArray;
	private String _surveyIdListString;
	private String[] _surveyIdListArray;
	private String _client;
	private String _campaignName;
	private String _campaignVersion;
	private String _columnListString;
	private String[] _columnListArray;
	
	// private String _authToken; see userToken in parent class
	
	public String getCurrentUser() {
		return _currentUser;
	}
	
	public void setCurrentUser(String currentUser) {
		_currentUser = currentUser;
	}
	
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

	public int getNumberOfUsersInQuery() {
		return (null == _userListArray) ? 0 : _userListArray.length;
	}
	
	public String getUserListString() {
		return _userListString;
	}

	public void setUserListString(String userListString) {
		_userListString = userListString;
	}

	public String[] getUserListArray() {
		return _userListArray;
	}

	public void setUserListArray(String[] userListArray) {
		_userListArray = userListArray;
	}

	public String getPromptIdListString() {
		return _promptIdListString;
	}

	public void setPromptIdListString(String promptIdListString) {
		_promptIdListString = promptIdListString;
	}

	public String[] getPromptIdListArray() {
		return _promptIdListArray;
	}

	public void setPromptIdListArray(String[] promptIdListArray) {
		_promptIdListArray = promptIdListArray;
	}

	public String getSurveyIdListString() {
		return _surveyIdListString;
	}

	public void setSurveyIdListString(String surveyIdListString) {
		_surveyIdListString = surveyIdListString;
	}

	public String[] getSurveyIdListArray() {
		return _surveyIdListArray;
	}

	public void setSurveyIdListArray(String[] surveyIdListArray) {
		_surveyIdListArray = surveyIdListArray;
	}

	public String getColumnListString() {
		return _columnListString;
	}

	public void setColumnListString(String columnListString) {
		_columnListString = columnListString;
	}

	public String[] getColumnListArray() {
		return _columnListArray;
	}

	public void setColumnListArray(String[] columnListArray) {
		_columnListArray = columnListArray;
	}
}
