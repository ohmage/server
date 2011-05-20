package edu.ucla.cens.awserver.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The default user implementation.
 * 
 * @author selsky
 */
public class User {
	private int _id;
	private String  _userName;
	private Map<String, CampaignUserRoles> _campaignUserRoleMap; // a user can have many roles in one campaign 
	private boolean _loggedIn;
	private String _password;
	
	public User() {
		_id = -1;
		_campaignUserRoleMap = new HashMap<String, CampaignUserRoles>();
	}
	
	/**
	 * Copy constructor.
	 */
	public User(User user) {
		if(null == user) {
			throw new IllegalArgumentException("a null user is not allowed");
		}
		_id = user.getId();
		_userName = user.getUserName();
		_campaignUserRoleMap = new HashMap<String, CampaignUserRoles>();
		// Authentication no longer sets the user roles on the user, so the users
		// are added to the bin with no roles. It is the responsibility of application
		// flows that require knowledge of the user's role to obtain them at runtime.
		_campaignUserRoleMap.putAll(user.getCampaignUserRoleMap()); // shallow copy ok because once a user is created it is read-only in practice
		_loggedIn = user.isLoggedIn();
	}
	
    public int getId() {
    	return _id;
    }
    
    public void setId(int id) {
    	_id = id;
    }
    
	public Map<String, CampaignUserRoles> getCampaignUserRoleMap() {
		return _campaignUserRoleMap;
	}
	
	public void addCampaignRole(Campaign campaign, UserRole userRole) {
		if(null == _campaignUserRoleMap) {
			_campaignUserRoleMap = new HashMap<String, CampaignUserRoles>();
		}
		
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaign.getUrn());
		
		if(null == campaignUserRoles) {
			campaignUserRoles = new CampaignUserRoles();
			campaignUserRoles.setCampaign(campaign);
			List<UserRole> userRoles = new ArrayList<UserRole>();
			campaignUserRoles.setUserRoles(userRoles);
			_campaignUserRoleMap.put(campaign.getUrn(), campaignUserRoles);
		}
		
		campaignUserRoles.addUserRole(userRole);
	}
	
	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}
	
	public boolean isLoggedIn() {
		return _loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		_loggedIn = loggedIn;
	}
	
	public void setPassword(String password) {
		_password = password;
	}

	public String getPassword() {
		return _password;
	}
	
	public boolean isSupervisorInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			throw new IllegalArgumentException("user not in campaign");
		}
		// FIXME to use user role constants once John's 2.4 db changes are merged in
		return campaignUserRoles.getUserRoleStrings().contains("supervisor");
	}
	
	public boolean isAuthorInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			throw new IllegalArgumentException("user not in campaign");
		}
		// FIXME to use user role constants once John's 2.4 db changes are merged in
		return campaignUserRoles.getUserRoleStrings().contains("author");
	}
	
	public boolean isAnalystInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			throw new IllegalArgumentException("user not in campaign");
		}
		// FIXME to use user role constants once John's 2.4 db changes are merged in
		return campaignUserRoles.getUserRoleStrings().contains("analyst");
	}
	
	public boolean isParticipantInCampaign(String campaignUrn) {
		CampaignUserRoles campaignUserRoles = _campaignUserRoleMap.get(campaignUrn);
		if(null == campaignUserRoles) {
			throw new IllegalArgumentException("user not in campaign");
		}
		// FIXME to use user role constants once John's 2.4 db changes are merged in
		return campaignUserRoles.getUserRoleStrings().contains("participant");
	}
	
	public boolean isOnlyAnalystOrAuthor(String campaignUrn) {
		return (isAnalystInCampaign(campaignUrn) 
				&& (getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 1))
				
			||  (isAuthorInCampaign(campaignUrn) 
					&& (getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 1))
					
			|| (isAnalystInCampaign(campaignUrn) 
			    	&& isAuthorInCampaign(campaignUrn) 
			    	&& (getCampaignUserRoleMap().get(campaignUrn).getUserRoles().size() == 2));
	}
	
	// NOTE: if you regenerate this toString() automatically in your IDE, please remember to omit the user's password!
	@Override
	public String toString() {
		return "User [_id=" + _id + ", _userName=" + _userName
				+ ", _campaignUserRoleMap=" + _campaignUserRoleMap
				+ ", _loggedIn=" + _loggedIn + ", _password=omitted]";
	}
}
