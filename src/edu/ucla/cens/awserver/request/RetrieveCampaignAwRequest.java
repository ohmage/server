package edu.ucla.cens.awserver.request;

import java.util.List;

/**
 * TODO/FIXME this class should extend UploadAwRequest
 * 
 * @author selsky
 */
public class RetrieveCampaignAwRequest extends UploadAwRequest {
	
	// Input
	private String _outputFormat;
	private String _campaignUrnListAsString;
	private List<String> _campaignUrnList;
	private String _startDate;
	private String _endDate;
	private String _privacyState;
	private String _runningState;
	private String _userRole;
	private String _classUrnListAsString;
	private List<String> _classUrnList;
	
	public String getOutputFormat() {
		return _outputFormat;
	}
	
	public void setOutputFormat(String outputFormat) {
		_outputFormat = outputFormat;
	}
	
	public String getCampaignUrnListAsString() {
		return _campaignUrnListAsString;
	}
	
	public void setCampaignUrnListAsString(String campaignUrnListAsString) {
		_campaignUrnListAsString = campaignUrnListAsString;
	}
	
	public List<String> getCampaignUrnList() {
		return _campaignUrnList;
	}
	
	public void setCampaignUrnList(List<String> campaignUrnList) {
		_campaignUrnList = campaignUrnList;
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
	
	public String getPrivacyState() {
		return _privacyState;
	}
	
	public void setPrivacyState(String privacyState) {
		_privacyState = privacyState;
	}
	
	public String getRunningState() {
		return _runningState;
	}
	
	public void setRunningState(String runningState) {
		_runningState = runningState;
	}
	
	public String getUserRole() {
		return _userRole;
	}
	
	public void setUserRole(String userRole) {
		_userRole = userRole;
	}
	
	public String getClassUrnListAsString() {
		return _classUrnListAsString;
	}
	
	public void setClassUrnListAsString(String classListAsString) {
		_classUrnListAsString = classListAsString;
	}
	
	public List<String> getClassUrnList() {
		return _classUrnList;
	}
	
	public void setClassUrnList(List<String> classList) {
		_classUrnList = classList;
	}
}
