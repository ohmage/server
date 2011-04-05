package edu.ucla.cens.awserver.domain;

import java.util.List;

/**
 * Wrapper for the various items that form a result to the config query.
 * 
 * @author selsky
 */
public class ConfigQueryResult {
	private String _urn;
	private String _userRole;
	private List<String> _userList;
	private String _xml;
	
	public ConfigQueryResult(String urn, String userRole, List<String> userList, String xml) {
		_urn = urn;
		_userRole = userRole;
		_userList = userList;
		_xml = xml;
	}
	
	public String getCampaignUrn() {
		return _urn;
	}
	
	public String getUserRole() {
		return _userRole;
	}
	
	public List<String> getUserList() {
		return _userList;
	}
	
	public String getXml() {
		return _xml;
	}	
}
