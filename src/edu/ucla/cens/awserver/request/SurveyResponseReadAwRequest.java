package edu.ucla.cens.awserver.request;

import java.util.List;

import edu.ucla.cens.awserver.domain.Configuration;

/**
 * State for "new" data point API queries.
 * 
 * @author selsky
 */
public class SurveyResponseReadAwRequest extends ResultListAwRequest {
	private String _startDate;
	private String _endDate;
	private String _currentUser;
	private String _client;
	private String _campaignUrn;
	
	private String _userListString;
	private List<String> _userList;
	
	private String _promptIdListString;
	private List<String> _promptIdList;
	
	private String _surveyIdListString;
	private List<String> _surveyIdList;
	
	private String _columnListString;
	private List<String> _columnList;
	
	private String _outputFormat;
	
	private String _sortOrderString;
	private List<String> _sortOrderList;
 	
	private boolean _prettyPrint;
	private boolean _suppressMetadata;
	
	private Configuration _configuration;
	
	// private String _authToken; see userToken in parent class

	public Configuration getConfiguration() {
		return _configuration;
	}

	public void setConfiguration(Configuration configuration) {
		_configuration = configuration;
	}
	
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

	public String getCampaignUrn() {
		return _campaignUrn;
	}

	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}
	
	public String getUserListString() {
		return _userListString;
	}

	public void setUserListString(String userListString) {
		_userListString = userListString;
	}

	public List<String> getUserList() {
		return _userList;
	}

	public void setUserList(List<String> userList) {
		_userList = userList;
	}

	public String getPromptIdListString() {
		return _promptIdListString;
	}

	public void setPromptIdListString(String promptIdListString) {
		_promptIdListString = promptIdListString;
	}

	public List<String> getPromptIdList() {
		return _promptIdList;
	}

	public void setPromptIdList(List<String> promptIdList) {
		_promptIdList = promptIdList;
	}

	public String getSurveyIdListString() {
		return _surveyIdListString;
	}

	public void setSurveyIdListString(String surveyIdListString) {
		_surveyIdListString = surveyIdListString;
	}

	public List<String> getSurveyIdList() {
		return _surveyIdList;
	}

	public void setSurveyIdList(List<String> surveyIdList) {
		_surveyIdList = surveyIdList;
	}

	public String getColumnListString() {
		return _columnListString;
	}

	public void setColumnListString(String columnListString) {
		_columnListString = columnListString;
	}

	public List<String> getColumnList() {
		return _columnList;
	}

	public void setColumnList(List<String> columnList) {
		_columnList = columnList;
	}
	
	public String getOutputFormat() {
		return _outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		_outputFormat = outputFormat;
	}

	public String getSortOrderString() {
		return _sortOrderString;
	}

	public void setSortOrderString(String sortOrderString) {
		_sortOrderString = sortOrderString;
	}

	public List<String> getSortOrderList() {
		return _sortOrderList;
	}

	public void setSortOrderList(List<String> sortOrderList) {
		_sortOrderList = sortOrderList;
	}

	public boolean isPrettyPrint() {
		return _prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		_prettyPrint = prettyPrint;
	}
	
	public boolean isSuppressMetadata() {
		return _suppressMetadata;
	}

	public void setSuppressMetadata(boolean suppressMetadata) {
		_suppressMetadata = suppressMetadata;
	}

	@Override
	public String toString() {
		return "NewDataPointQueryAwRequest [_campaignUrn=" + _campaignUrn
				+ ", _client=" + _client + ", _columnList=" + _columnList
				+ ", _columnListString=" + _columnListString
				+ ", _configuration=" + _configuration + ", _currentUser="
				+ _currentUser + ", _endDate=" + _endDate + ", _outputFormat="
				+ _outputFormat + ", _prettyPrint=" + _prettyPrint
				+ ", _promptIdList=" + _promptIdList + ", _promptIdListString="
				+ _promptIdListString + ", _sortOrderList=" + _sortOrderList
				+ ", _sortOrderString=" + _sortOrderString + ", _startDate="
				+ _startDate + ", _suppressMetadata=" + _suppressMetadata
				+ ", _surveyIdList=" + _surveyIdList + ", _surveyIdListString="
				+ _surveyIdListString + ", _userList=" + _userList
				+ ", _userListString=" + _userListString + "]";
	}
}
