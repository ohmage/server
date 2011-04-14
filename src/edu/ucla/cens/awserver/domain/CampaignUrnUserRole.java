package edu.ucla.cens.awserver.domain;

/**
 * Simple bean for mapping a user role string to a campaign URN.
 * 
 * @author selsky
 */
public class CampaignUrnUserRole {
	private String _role;
	private String _urn;
	
	public String getRole() {
		return _role;
	}
	
	public void setRole(String role) {
		_role = role;
	}
	
	public String getUrn() {
		return _urn;
	}
	
	public void setUrn(String urn) {
		_urn = urn;
	}
	
	@Override
	public String toString() {
		return "CampaignUrnUserRole [_role=" + _role + ", _urn=" + _urn + "]";
	}
}
