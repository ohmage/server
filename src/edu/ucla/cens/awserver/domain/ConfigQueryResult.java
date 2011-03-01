package edu.ucla.cens.awserver.domain;

import java.util.List;
import java.util.Map;

/**
 * Wrapper for the various items that form a result to the config query.
 * 
 * @author selsky
 */
public class ConfigQueryResult {
	private String _campaignName;
	private String _userRole;
	private List<String> _userList;
	private Map<String, String> _versionXmlMap;
	
	public String getCampaignName() {
		return _campaignName;
	}
	
	public void setCampaignName(String campaignName) {
		_campaignName = campaignName;
	}
	
	public String getUserRole() {
		return _userRole;
	}
	
	public void setUserRole(String userRole) {
		_userRole = userRole;
	}
	
	public List<String> getUserList() {
		return _userList;
	}
	
	public void setUserList(List<String> userList) {
		_userList = userList;
	}
	
	public Map<String, String> getVersionXmlMap() {
		return _versionXmlMap;
	}
	
	public void setVersionXmlMap(Map<String, String> versionXmlMap) {
		_versionXmlMap = versionXmlMap;
	}	
}
