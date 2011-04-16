package edu.ucla.cens.awserver.domain;

/**
 * Simple wrapper for mapping a user role string to a campaign URN for a particular user.
 * 
 * @author selsky
 */
public class CampaignUrnLoginIdUserRole {
	private String _loginId;
	private String _role;
	private String _campaignUrn;
	
	public String getRole() {
		return _role;
	}
	
	public void setRole(String role) {
		_role = role;
	}
	
	public String getLoginId() {
		return _loginId;
	}
	
	public void setLoginId(String loginId) {
		_loginId = loginId;
	}
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}
	
	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}

	@Override
	public String toString() {
		return "CampaignUrnLoginIdUserRole [_campaignUrn=" + _campaignUrn
				+ ", _loginId=" + _loginId + ", _role=" + _role + "]";
	}
}
