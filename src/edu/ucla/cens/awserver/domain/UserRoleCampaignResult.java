package edu.ucla.cens.awserver.domain;

/**
 * @author joshua selsky
 */
public class UserRoleCampaignResult {
	private String _campaignUrn;
	private String _campaignName;
	private String _campaignDescription;
	private String _campaignPrivacyState;
	private String _campaignRunningState;
	private String _campaignCreationTimestamp;
	private UserRole _userRole;
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}

	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}
	
	public String getCampaignName() {
		return _campaignName;
	}

	public void setCampaignName(String campaignName) {
		_campaignName = campaignName;
	}

	public String getCampaignDescription() {
		return _campaignDescription;
	}

	public void setCampaignDescription(String campaignDescription) {
		_campaignDescription = campaignDescription;
	}

	public String getCampaignPrivacyState() {
		return _campaignPrivacyState;
	}

	public void setCampaignPrivacyState(String campaignPrivacyState) {
		_campaignPrivacyState = campaignPrivacyState;
	}

	public String getCampaignRunningState() {
		return _campaignRunningState;
	}

	public void setCampaignRunningState(String campaignRunningState) {
		_campaignRunningState = campaignRunningState;
	}
	
	public String getCampaignCreationTimestamp() {
		return _campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		_campaignCreationTimestamp = campaignCreationTimestamp;
	}
	
	public UserRole getUserRole() {
		return _userRole;
	}
	
	public void setUserRole(UserRole userRole) {
		_userRole = userRole;
	}

	@Override
	public String toString() {
		return "UserRoleCampaignResult [_campaignUrn=" + _campaignUrn
				+ ", _campaignName=" + _campaignName
				+ ", _campaignDescription=" + _campaignDescription
				+ ", _campaignPrivacyState=" + _campaignPrivacyState
				+ ", _campaignRunningState=" + _campaignRunningState
				+ ", _campaignCreationTimestamp=" + _campaignCreationTimestamp
				+ ", _userRole=" + _userRole + "]";
	}
}
