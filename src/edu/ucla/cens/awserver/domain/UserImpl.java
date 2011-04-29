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
    private Map<String, List<UserRole>> _campaignUserRoleMap; // a user can have many roles in one campaign
	private boolean _loggedIn;
	private String _password;
	
	public UserImpl() {
		_id = -1;
		_campaignUserRoleMap = new HashMap<String, List<UserRole>>();
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
		_campaignUserRoleMap = new HashMap<String, List<UserRole>>();
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
    
	public Map<String, List<UserRole>> getCampaignUserRoleMap() {
		return _campaignUserRoleMap;
	}
	
	public void addCampaignRole(String campaignUrn, UserRole userRole) {
		if(null == _campaignUserRoleMap) {
			_campaignUserRoleMap = new HashMap<String, List<UserRole>>();
		}
		
		List<UserRole> roles = _campaignUserRoleMap.get(campaignUrn);
		if(null == roles) {
			roles = new ArrayList<UserRole>();
			_campaignUserRoleMap.put(campaignUrn, roles);
		}
		
		roles.add(userRole);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((_campaignUserRoleMap == null) ? 0 : _campaignUserRoleMap
						.hashCode());
		result = prime * result + _id;
		result = prime * result + (_loggedIn ? 1231 : 1237);
		result = prime * result
				+ ((_password == null) ? 0 : _password.hashCode());
		result = prime * result
				+ ((_userName == null) ? 0 : _userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserImpl other = (UserImpl) obj;
		if (_campaignUserRoleMap == null) {
			if (other._campaignUserRoleMap != null)
				return false;
		} else if (!_campaignUserRoleMap.equals(other._campaignUserRoleMap))
			return false;
		if (_id != other._id)
			return false;
		if (_loggedIn != other._loggedIn)
			return false;
		if (_password == null) {
			if (other._password != null)
				return false;
		} else if (!_password.equals(other._password))
			return false;
		if (_userName == null) {
			if (other._userName != null)
				return false;
		} else if (!_userName.equals(other._userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserImpl [_id=" + _id + ", _userName=" + _userName
				+ ", _campaignUserRoleMap=" + _campaignUserRoleMap
				+ ", _loggedIn=" + _loggedIn + ", _password=" + "(omitted)" + "]";
	}	
}
