package edu.ucla.cens.awserver.domain;

/**
 * Simple wrapper for mapping a user role string to a campaign URN for a particular user.
 * 
 * @author selsky
 */
public class LoginIdUserRole {
	private String _loginId;
	private String _role;
	
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
	
	@Override
	public String toString() {
		return "CampaignUrnUserRole [_role=" + _role + ", _urn=" + _loginId + "]";
	}
}
