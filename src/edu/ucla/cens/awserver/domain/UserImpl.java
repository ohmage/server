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
public class UserImpl implements User {
	private int _id;
	private String  _userName;
    private Map<Integer, List<Integer>> _campaignRoles;
	private boolean _loggedIn;
	private String _password;
	private String _currentCampaignId;
	
	public UserImpl() {
		_id = -1;
	}
	
	/**
	 * Copy constructor.
	 */
	public UserImpl(User user) {
		if(null == user) {
			throw new IllegalArgumentException("a null user is not allowed");
		}
		_id = user.getId();
		_userName = user.getUserName();
		_campaignRoles = new HashMap<Integer, List<Integer>>();
		_campaignRoles.putAll(user.getCampaignRoles()); // shallow copy ok because once a user is created it is read-only in practice 
		_loggedIn = user.isLoggedIn();
		_currentCampaignId = user.getCurrentCampaignId();
	}
	
    public int getId() {
    	return _id;
    }
    
    public void setId(int id) {
    	_id = id;
    }
    
	public Map<Integer, List<Integer>> getCampaignRoles() {
		return _campaignRoles;
	}
	
	public void addCampaignRole(Integer campaignId, Integer roleId) {
		if(null == _campaignRoles) {
			_campaignRoles = new HashMap<Integer, List<Integer>>();
		}
		
		List<Integer> roles = _campaignRoles.get(campaignId);
		if(null == roles) {
			roles = new ArrayList<Integer>();
			_campaignRoles.put(campaignId, roles);
		}
		
		roles.add(roleId);
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
	
	public void setCurrentCampaignId(String id) {
		_currentCampaignId = id;
	}
	
	public String getCurrentCampaignId() {
		return _currentCampaignId;
	}

	public boolean getIsResearcherOrAdmin() {
		boolean isResearcherOrAdmin = false;
		List<Integer> list = getCampaignRoles().get(Integer.valueOf(getCurrentCampaignId()));
		
		for(Integer i : list) {
			// 1 is admin, 3 is researcher (hard coded for now)
			if(i == 1 || i == 3) {
				isResearcherOrAdmin = true;
				break;
			}
		}
		
		return isResearcherOrAdmin;
	}
	
	@Override
	public String toString() { // password is deliberately omitted
		return "UserImpl [_campaignRoles=" + _campaignRoles
				+ ", _currentCampaignId=" + _currentCampaignId + ", _id=" + _id
				+ ", _loggedIn=" + _loggedIn + ", _userName=" + _userName + "]";
	}
}
