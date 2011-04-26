package edu.ucla.cens.awserver.domain;

/**
 * @author joshua selsky
 */
public class UserRoleCampaignResult {
	private String _campaignUrn;
	private UserRole _userRole;
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}

	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}
	
	public UserRole getUserRole() {
		return _userRole;
	}
	
	public void setUserRole(UserRole userRole) {
		_userRole = userRole;
	}
}
