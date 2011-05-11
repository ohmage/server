package edu.ucla.cens.awserver.domain;

/**
 * Storage of campaign metadata (basically everything except the XML).
 * 
 * @author Joshua Selsky
 */
public class Campaign {
	private String _urn;
	private String _name;
	private String _description;
	private String _runningState;
	private String _privacyState;
	private String _campaignCreationTimestamp;
	
	public String getUrn() {
		return _urn;
	}
	
	public void setUrn(String urn) {
		_urn = urn;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getDescription() {
		return _description;
	}
	
	public void setDescription(String description) {
		_description = description;
	}
	
	public String getRunningState() {
		return _runningState;
	}
	
	public void setRunningState(String runningState) {
		_runningState = runningState;
	}
	
	public String getPrivacyState() {
		return _privacyState;
	}
	
	public void setPrivacyState(String privacyState) {
		_privacyState = privacyState;
	}
	
	public String getCampaignCreationTimestamp() {
		return _campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		_campaignCreationTimestamp = campaignCreationTimestamp;
	}
}
