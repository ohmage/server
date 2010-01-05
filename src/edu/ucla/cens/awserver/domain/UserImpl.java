package edu.ucla.cens.awserver.domain;


/**
 * The default user implementation.
 * 
 * @author selsky
 */
public class UserImpl implements User {
	private String  _userName;
    private int _campaignId;
//	private boolean _loggedIn;
	
	public int getCampaignId() {
		return _campaignId;
	}
	
	public void setCampaignId(int id) {
		_campaignId = id;
	}
	
	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}
	
//	public boolean isLoggedIn() {
//		return _loggedIn;
//	}
//	
//	public void setLoggedIn(boolean loggedIn) {
//		_loggedIn = loggedIn;
//	}
	
}
