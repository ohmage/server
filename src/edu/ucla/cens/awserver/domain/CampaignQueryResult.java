package edu.ucla.cens.awserver.domain;

import java.util.List;

/**
 * @author selsky
 */
public class CampaignQueryResult {
	private String _urn;
	private String _name;
	private String _description;
	private String _xml;
	private String _runningState;
	private String _privacyState;
	private String _creationTimestamp;
	private List<String> _userRoles;
	
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
	
	public String getXml() {
		return _xml;
	}
	
	public void setXml(String xml) {
		_xml = xml;
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
	
	public String getCreationTimestamp() {
		return _creationTimestamp;
	}
	
	public void setCreationTimestamp(String creationTimestamp) {
		_creationTimestamp = creationTimestamp;
	}
	
	public List<String> getUserRoles() {
		return _userRoles;
	}
	
	public void setUserRoles(List<String> roles) {
		_userRoles = roles;
	}
}
