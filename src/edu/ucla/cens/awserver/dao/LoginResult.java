package edu.ucla.cens.awserver.dao;

/**
 * Container used for authentication query results.
 * 
 * @author selsky
 */
public class LoginResult {
//	private int _campaignId;
	private String _campaignUrn;
	private int _userRoleId;
	private int _userId;
	private boolean _enabled;
	private boolean _new;
	
//	public int getCampaignId() {
//		return _campaignId;
//	}
//	public void setCampaignId(int campaignId) {
//		_campaignId = campaignId;
//	}
	public int getUserId() {
		return _userId;
	}
	
	public void setUserId(int userId) {
		_userId = userId;
	}
	
	public boolean isEnabled() {
		return _enabled;
	}
	
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
	public boolean isNew() {
		return _new;
	}
	
	public void setNew(boolean bnew) {
		_new = bnew;
	}
	
	public int getUserRoleId() {
		return _userRoleId;
	}
	
	public void setUserRoleId(int userRoleId) {
		_userRoleId = userRoleId;
	}
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}
	
	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}
}